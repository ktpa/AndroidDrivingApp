#if defined(__has_include) && __has_include("secrets.hpp")
#include "secrets.hpp"
#else
#include "secrets-default.hpp"
#endif

#include <vector>
#include <Smartcar.h>
#include <MQTT.h>
#include <WiFi.h>
#include <map>

#ifdef __SMCE__
#include <OV767X.h>
#endif

#ifndef __SMCE__
WiFiClient net;
#endif

namespace mqtt_topic 
{
    const auto CONTROL_GLOBAL = "/smartcar/control/#";
    const auto CONTROL_SPEED = "/smartcar/control/speed";
    const auto CONTORL_STEERING = "/smartcar/control/steering";
    const auto CONTROL_CAMERA = "/smartcar/camera";
}

//only for printing current readings of sensor to serial terminal
const unsigned long PRINT_INTERVAL = 100;
unsigned long previousPrintout     = 0;

ArduinoRuntime arduinoRuntime;
BrushedMotor leftMotor(arduinoRuntime, smartcarlib::pins::v2::leftMotorPins);
BrushedMotor rightMotor(arduinoRuntime, smartcarlib::pins::v2::rightMotorPins);
DifferentialControl control(leftMotor, rightMotor);

MQTTClient mqtt;
std::map<String, std::function<void(String)>> mqttMessageHandlers;

GY50 gyroscope(arduinoRuntime, 37);

const auto PULSES_PER_METER = 600;

DirectionlessOdometer leftOdometer{
    arduinoRuntime,
    smartcarlib::pins::v2::leftOdometerPin,
    []() { leftOdometer.update(); },
    PULSES_PER_METER};
DirectionlessOdometer rightOdometer{
    arduinoRuntime,
    smartcarlib::pins::v2::rightOdometerPin,
    []() { rightOdometer.update(); },
    PULSES_PER_METER};

SmartCar car(arduinoRuntime, control, gyroscope, leftOdometer, rightOdometer);

const auto TRIGGER_PIN = 6;
const auto ECHO_PIN = 7;
const auto MAX_DISTANCE = 400;
const unsigned short FRONT_IR_PIN = 0;
const unsigned short BACK_IR_PIN = 3;

//measures distances in short distances
GP2D120 frontIRSensor(arduinoRuntime, FRONT_IR_PIN);
GP2D120 backIRSensor(arduinoRuntime, BACK_IR_PIN);

//measures distances in longer distances
SR04 frontUSSensor(arduinoRuntime, TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE);

std::vector<char> frameBuffer; // CameraCode

const auto DEFAULT_DRIVING_SPEED = 1.5;

void setup()
{
    Serial.begin(9600);
#ifndef __SMCE__
    mqtt.begin(net);
#else
    Camera.begin(QVGA, RGB888, 15);
    frameBuffer.resize(Camera.width() * Camera.height() * Camera.bytesPerPixel());
    mqtt.begin(MQTT_HOST, MQTT_PORT, WiFi);
#endif
    if (!mqtt.connect("arduino"))
    {
        Serial.print("Failed to connect to MQTT broker at ");
        Serial.println(MQTT_HOST);
        return;
    }

    registerMqttMessageHandlers();

    mqtt.subscribe(mqtt_topic::CONTROL_GLOBAL, 1);
    mqtt.onMessage(handleMqttMessage);
  
    car.enableCruiseControl();
    car.setSpeed(0);
}

void loop(){
    if (mqtt.connected())
    {
        mqtt.loop();
#ifdef __SMCE__
        publishCameraFrame();
#endif
    }
    // Maintain the speed and update the heading
    car.update();
    // avoidObstacle();
}

void registerMqttMessageHandlers()
{
    mqttMessageHandlers[mqtt_topic::CONTROL_SPEED] = handleSpeedChangeRequest;
    mqttMessageHandlers[mqtt_topic::CONTORL_STEERING] = handleSteeringChangeRequest;
}

void handleMqttMessage(String topic, String payload)
{
    if (!mqttMessageHandlers.count(topic))
    {
        Serial.println("No handler found for topic " + topic);
        return;
    }

    mqttMessageHandlers[topic](payload);
}

const auto STOPPING_DISTANCE = 100;

void avoidObstacle() 
{
    unsigned int forwardDistance = frontUSSensor.getDistance();
    unsigned int reverseDistance = backIRSensor.getDistance();

    bool frontStop = forwardDistance != 0 && forwardDistance < STOPPING_DISTANCE;
    bool backStop = reverseDistance != 0 && reverseDistance < STOPPING_DISTANCE;

    car.setSpeed((frontStop || backStop) ? 0 : DEFAULT_DRIVING_SPEED);
}

void handleSpeedChangeRequest(String payload)
{
    if (!isNumber(payload))
    {
        return;
    }

    car.setSpeed(payload.toInt());
}

void handleSteeringChangeRequest(String payload)
{
    if (!isNumber(payload))
    {
        return;
    }

    car.setAngle(payload.toInt());
}

void publishCameraFrame()
{
    const auto currentTime = millis();
    static auto previousFrame = 0UL;
    if (currentTime - previousFrame >= 65) 
    {
        previousFrame = currentTime;
        Camera.readFrame(frameBuffer.data());
        mqtt.publish(mqtt_topic::CONTROL_CAMERA, frameBuffer.data(), frameBuffer.size(), false, 0);
    }
}

bool isNumber(String string)
{
    for (std::size_t i = 0; i < string.length(); i++)
    {
        if (!isDigit(string[i]))
        {
            return false;
        }
        
    }
    
    return true;
}
