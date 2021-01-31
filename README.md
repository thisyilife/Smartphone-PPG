# Smartphone_ppg repository

## Description of repository

This repository gathers all resources and pieces of code used to create an Android application capable of detecting vital signs of a human being using a regular smartphone camera. This project is part of the "Industrial Project" course displayed for fifth-year students of INSA rennes in the department "Electronics and Computer Engineering".

Please read the project report of this project for further information.

## Repository structure

This repository is divided into four main directories :
- `Camera2/` : a legacy directory containing the root of an obsolete android application used to do preliminary tests with android.
- `Documents/` : the directory containing the scientific papers and documentation on which the project is based.
- `SensoriaCompanion/` : the root directory of the created application, meant to be opened using **Android Studio**.
- `Signal Processing/` : a directory containing java functions to perform tests about signal processing and/or additional libraries.

The root of the directory also contains a [plantuml](https://www.planttext.com/) file `class_diagram.puml` exposing the classes of the application under `SensoriaCompanion/`.

--------------------------------------------------------------------------------

## The project as of february 2020

Unfortunately our team was not able to create a fully functional application in the allocated time. Indeed the SensoriaCompanion application is not yet capable of correctly detecting heartbeats on the extracted PPG signal.

Yet the application still has a functional structure and model that provides for a solid basis to be improved in the future. As of february 2020, the main issue is that the extracted PPG signal is not very clean, and one should look for filtering the camera stream and/or the extracted 1D PPG signal in order to improve performances. Indeed the method that detects heartbeat pulses in the class `BloodAnalysisSection` could not be properly tested due to this improper PPG signal and should very probably be improved in the future in order to give accurate results.
