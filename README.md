# group-01 - AlSet

![AlSet](images/alset-whiteOutline.svg)

## What you are going to make?

We are going to build a remotely drivable car, with voice recognition capabilities.

---

## Why will you make it?

We want to simulate the experience of owning a luxury vehicle with all the bells and whistles of modern day technology from the safety of your own home. Driving a car can be a very boring task for some people, but by combining the possibilities of IoT into a well designed app, we aim to provide a driving experience that is ultimately safer, more secure, but most of all, enjoyable to the end user.

---

## What problem does it solve?

COVID-19 has unquestionably affected many people's lives worldwide. Safe transportation has proven to be very difficult. AlSet offers a solution for stressed car owners who are worried about the changing circumstances of the world around them by allowing them to drive a car from the safety of their own home, wherever they want, whenever they want.

Ultimately, in times of self isolation, our product makes the action of commuting by yourself more attractive than using public transport.

---

## How you are going to make it?

- We will use an application layer as the communication interface to control the car.
- An external library with voice recognition capabilities will be used as the foundation for the voice control.
- A network layer will be used for MQTT communication.

---

## What kind of technology will you use?

- C++
- Java
- Github

---

## Setting up your AlSet

### Requirements:

- Android device (Android 9.0+)
- Android Studio preinstalled on your computer

### Installation steps: 

1) Clone or download the ```master-branch``` onto your computer.
2) Launch Android Studio and point it to where you have saved your project
3) Go to ```Build``` and click ```Build APK```.
4) Locate the file and transfer it to your Android device. 
5) Install the APK.
6) Enjoy your AlSet!

Clarification on how the app is operated can be found on the [Wiki](https://github.com/DIT112-V21/group-01/wiki).

---

## Technical Overview

### How AlSet was created:

We developed an Android app coded in Java that allows the user to control the car via joystick on the software itself. All information such as user credentials, driving sensitivities, and active sessions are stored in Google Firebase, which are loaded upon app startup and user log-in. Video streaming is done courtesy of the camera provided in the SMCE emulator. Last but not least, AlSet and its companion app's functionalities communicate through an MQTT server.

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

Sicily Ann Brannen (gussbrannsi@student.gu.se) 

Numan Korkmaz (guskorknu@student.gu.se)

Mislav Milicevic (gusmilicmi@student.gu.se)

Karl Nilsson (gusnilkaay@student.gu.se)

Gregory Sastrawidjaya (gussasgr@student.gu.se)
