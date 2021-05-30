# group-01 - AlSet

![AlSet](images/alset-whiteOutline.svg)

## What you are going to make?

We are going to build a remotely drivable car with voice recognition capability.

---

## Why will you make it?

We want to simulate the experience of owning a luxury vehicle, with all the bells and whistles of modern-day technology — from the safety of your own home. 

---

## What problem does it solve?

Safe transportation has proven to be very difficult due to the ongoing COVID-19 pandemic. AlSet offers a solution for stressed car owners who are worried about the changing circumstances of the world around them, by allowing them to drive their cars from the safety of their own home: wherever they want, whenever they want.

Driving a car can be a very boring task for some people. But, by combining the possibilities of IoT into a well-designed app, we aim to provide a driving experience that is ultimately safer, more secure, and most of all, enjoyable, to the end user. In times of self-isolation, our product ultimately makes the action of commuting by yourself more attractive than using public transport. 

---

## Setting up your AlSet

### Requirements:

- Android device/emulator (Version: Android 9.0+) 
- Android Studio preinstalled on your computer
- SMCE-gd preinstalled
- MQTT localhost preinstalled and running

### Installation steps: 

1) Clone, or download, the ```master-branch``` onto your computer.
2) Launch Android Studio, and point it to where you have saved your project.
3) Go to ```Build``` and click ```Build APK```.
4) Locate the file, and transfer it to your Android device. 
5) Install the APK.
6) Launch SMCE-gd, and compile and start up the sketch.
7) Ensure MQTT is up and running.
8) Enjoy your AlSet!

### Using voice functionality on an Android Virtual Device
1) Launch your AVD.
2) Go to Extended controls.
3) Select Microphone.
4) Toggle the option "Virtual microphone uses host audio input."
5) Recompile the application without closing the virtual device.
6) You should now hear a sound when you press the microphone button from the drive-screen.
7) Any problems can usually be solved by doing the following steps:
    
    • Go to AVD Manager, and select "Wipe Data" on your AVD.
    
    • Follow the "Running on an Android Virtual Device" steps.

Clarification on how the app is operated can be found on the [Wiki](https://github.com/DIT112-V21/group-01/wiki).

---

## Technical Overview

### How AlSet was created:

We developed an Android app coded in Java, that allows the user to control the car via joystick on the software itself. All necessary information such as user credentials, driving sensitivities, and active sessions, are stored in Google Firebase (which is loaded upon app startup and user log-in).

As for the car itself, the code is written with the Arduino IDE (which is based on C++). The SmartCar shield library contains most of the vehicle's inner functionalities (odometer, driving sensors, vice versa). AlSet and its companion app communicate through MQTT.

### Software: 

Originally, the plan was to implement AlSet with a physical Arduino car. However, because of COVID-19, an Arduino emulator called SMCE (see below) was incorporated for this software development project. The following libraries, technologies, and dependencies were used:

- Java
- C++
- GitHub Actions
- [Espresso 3.3.0](https://developer.android.com/training/testing/espresso)
- [JUnit](https://junit.org/junit5/)
- [Firebase Realtime Database](https://firebase.google.com/docs/database) 
- [Firebase Authentication](https://firebase.google.com/docs/auth)
- [MQTT](https://github.com/eclipse/paho.mqtt.android)
- [Lottie](https://airbnb.design/lottie/)
- [SmartCar API](https://platisd.github.io/smartcar_shield/) 
- [SMCE](https://github.com/ItJustWorksTM/smce-gd)

---

### Development team

Sicily Ann Brannen (gusbrannsi@student.gu.se) 

Numan Korkmaz (guskorknu@student.gu.se)

Mislav Milicevic (gusmilicmi@student.gu.se)

Karl Nilsson (gusnilkaay@student.gu.se)

Gregory Sastrawidjaya (gussasgr@student.gu.se)

Jens Sjödin (gussjodije@student.gu.se)
