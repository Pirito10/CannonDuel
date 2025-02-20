# Cannon Duel
_Cannon Duel_ is an **Android Turn-Based Game** developed as part of the course "[Programación de Sistemas Inteligentes](https://secretaria.uvigo.gal/docnet-nuevo/guia_docent/?centre=305&ensenyament=V05G301V01&assignatura=V05G301V01403&any_academic=2024_25)" in the Telecommunications Engineering Degree at the Universidad de Vigo (2024 - 2025).

## About The Project
This project implements a turn-based strategy game for Android, where players control tanks on a grid, aiming to destroy the opponent's tank through strategic shooting and movement. The system integrates key concepts of mobile development using [Kotlin](https://kotlinlang.org) and [Jetpack Compose](https://developer.android.com/compose), with AI-driven gameplay using reinforcement learning techniques through [Chaquopy](https://chaquo.com/chaquopy/) to run Python scripts for intelligent behavior.

The project features:
- Turn-based strategy gameplay with grid-based movement.
- Single-player mode against an AI opponent.
- Two AI difficulty levels: a random AI that makes arbitrary moves and a Q-Learning AI that adapts and learns from previous games.
- Integration with Chaquopy to execute Python scripts within the Android app.
- User-friendly interface developed with Jetpack Compose.
- Easy-to-modify game parameters and AI behavior for testing and expansion. 

## How To Run
### Requirements
Make sure you have [Android Studio](https://developer.android.com/studio) and [Python](https://www.python.org/downloads/) installed on your system.

### Usage
Open or clone the repository on Android Studio, and wait for the project to build.
Then, select a target device and run the app by pressing `Shift + F10` or by clicking the `Run 'app'` button.

Alternatively, you can download and install the precompiled version from the [releases page](https://github.com/Pirito10/CannonDuel-PSI-UVigo/releases/tag/1.0).

## About The Code
Refer to [`Wiki.pdf`](docs/Wiki.pdf) and [`Memoria.pdf`](docs/Memoria.pdf) for an in-depth explanation of the project, how the game works, the development process, how the different modes work, and more.
