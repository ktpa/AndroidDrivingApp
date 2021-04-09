#include <Smartcar.h>

//only for printing current readings of sensor to serial terminal
const unsigned long PRINT_INTERVAL = 100;
unsigned long previousPrintout     = 0;

ArduinoRuntime arduinoRuntime;
BrushedMotor leftMotor(arduinoRuntime, smartcarlib::pins::v2::leftMotorPins);
BrushedMotor rightMotor(arduinoRuntime, smartcarlib::pins::v2::rightMotorPins);
DifferentialControl control(leftMotor, rightMotor);

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

const auto DEFAULT_DRIVING_SPEED = 1.5;

void setup()
{
    Serial.begin(9600);

    car.enableCruiseControl();
    car.setSpeed(DEFAULT_DRIVING_SPEED); // Maintain a speed of 1.5 m/sec
}

void loop()
{
    // Maintain the speed and update the heading
    car.update();
    avoidObstacle();
    
    unsigned long currentTime = millis();
    if (currentTime >= previousPrintout + PRINT_INTERVAL)
    {
        previousPrintout = currentTime;
        //Serial.println(frontIRSensor.getDistance());
        Serial.println(backIRSensor.getDistance());
        //Serial.println(frontUSSensor.getDistance());
    }
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
