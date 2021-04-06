#include <Smartcar.h>

const unsigned long PRINT_INTERVAL = 100;
unsigned long previousPrintout     = 0;

ArduinoRuntime arduinoRuntime;
BrushedMotor leftMotor(arduinoRuntime, smartcarlib::pins::v2::leftMotorPins);
BrushedMotor rightMotor(arduinoRuntime, smartcarlib::pins::v2::rightMotorPins);
DifferentialControl control(leftMotor, rightMotor);

GY50 gyroscope(arduinoRuntime, 37);

const auto pulsesPerMeter = 600;

DirectionlessOdometer leftOdometer{
    arduinoRuntime,
    smartcarlib::pins::v2::leftOdometerPin,
    []() { leftOdometer.update(); },
    pulsesPerMeter};
DirectionlessOdometer rightOdometer{
    arduinoRuntime,
    smartcarlib::pins::v2::rightOdometerPin,
    []() { rightOdometer.update(); },
    pulsesPerMeter};



SmartCar car(arduinoRuntime, control, gyroscope, leftOdometer, rightOdometer);

const auto triggerPin = 6;
const auto echoPin = 7;
const auto maxDistance = 400;
const unsigned short IR_PIN = 0;

//measures distances in short distances
GP2D120 infraredSensor(arduinoRuntime, IR_PIN);

//measures distances in longer distances
SR04 frontUSSensor(arduinoRuntime, triggerPin, echoPin, maxDistance);

void setup()
{
    Serial.begin(9600);

    car.enableCruiseControl();
    car.setSpeed(1.5); // Maintain a speed of 1.5 m/sec
}

void loop()
{
    // Maintain the speed and update the heading
    car.update();
    unsigned long currentTime = millis();
    if (currentTime >= previousPrintout + PRINT_INTERVAL)
    {
        previousPrintout = currentTime;
        Serial.println(infraredSensor.getDistance());
        //Serial.println(frontUSSensor.getDistance());
    }
    
}