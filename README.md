# Avalia+ Android App

## 📝 Table of Contents

- [About](#about)
- [Features](#features)
- [Screenshots](#screenshots)
- [Technical Details](#technical-details)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Dependencies](#dependencies)

## 🧐 About

Avalia+ is an Android application designed to help students prepare for the ENEM (Exame Nacional do Ensino Médio), a standardized national exam in Brazil. The app provides a gamified learning experience with missions, practice tests, a ranking system, and an AI-powered chatbot to assist with studies.

## ✨ Features

- **User Authentication:** Secure user registration and login functionality.
- **Missions:** Users can complete missions in different knowledge areas to earn points.
- **Practice Tests:** Take simulated tests with multiple-choice questions from different ENEM subject areas.
- **Personalized Results:** After completing a test, users receive a detailed results screen with the number of correct answers and the percentage of correct answers.
- **Ranking System:** A global ranking shows the top users based on their total points.
- **AI Chatbot:** An intelligent chatbot, powered by the Gemini API, to answer student questions and provide study assistance.
- **User Profile:** Users can set a profile picture and track their progress and total points.

## 📸 Screenshots

As I am a language model, I am unable to generate images. However, based on the layout files, here's a description of the app's main screens:

* **Login & Register:** Standard screens for user authentication.
* **Home:** The main dashboard displaying a welcome message, user's profile picture, motivational phrases, and navigation to other features.
* **Missions:** A screen listing available missions for a specific knowledge area, with checkboxes to mark them as complete.
* **Test Selection:** A list of available practice tests for the user to choose from.
* **Test Screen:** The interface for taking a test, showing the question, multiple-choice options, and a timer.
* **Test Results:** A summary of the user's performance on a completed test.
* **Ranking:** A list of users with their scores, displaying the top performers.
* **Chatbot:** A chat interface for interacting with the AI assistant.

## 🛠️ Technical Details

- **Language:** Java
- **Architecture:** The project follows a structured approach, with the code organized into packages by feature (e.g., `bancodedados`, `chatbot`, `missoes`, `prova`, `ranking`, `usuario`).
- **Database:** SQLite is used for local data storage, managed by the `BancoDeDados` and `DatabaseContract` classes.
- **AI Integration:** The Gemini API is integrated to power the chatbot feature.
- **User Interface:** The UI is built using XML layouts, with `RecyclerView` for displaying lists of missions, tests, and ranking.

## 📂 Project Structure

The project is organized into the following main packages:

* `avalia_mais-5531f01fdf107f1f71913acd26a5109122112588`
    * `app/src/main/`
        * `java/com/example/avalia/`
            * `bancodedados/`: Contains the classes for database management (`BancoDeDados.java`, `DatabaseContract.java`).
            * `chatbot/`: Includes the chatbot's `Activity`, `Adapter`, and message model.
            * `missoes/`: Holds the `Activity`, `Adapter`, `Controller`, and data model for the missions feature.
            * `principal/`: Contains the main home screen `Activity` of the application.
            * `prova/`: Encompasses all classes related to the practice tests, including `Activities` for test selection, execution, and results, as well as the `Controller` and data models.
            * `ranking/`: Includes the `Activity` and `Adapter` for the ranking feature.
            * `usuario/`: Manages user-related functionalities, including `Activities` for login, registration, and password recovery, as well as the `Controller`, data model, and session manager.
        * `res/`
            * `layout/`: XML files defining the UI for each `Activity`.
            * `drawable/`: Vector assets and other image resources.
            * `values/`: String, color, and theme definitions.

## 🚀 Getting Started

To run this project, you will need to:

1.  Clone the repository.
2.  Open the project in Android Studio.
3.  Create a `local.properties` file in the root of the project.
4.  Add your Gemini API key to the `local.properties` file with the following key: `GEMINI_API_KEY="YOUR_API_KEY"`.
5.  Build and run the application on an Android emulator or a physical device.

## 📦 Dependencies

The project utilizes several AndroidX and Google libraries, including:

-   **AndroidX Libraries:**
    -   AppCompat
    -   ConstraintLayout
    -   CardView
    -   RecyclerView
    -   Activity
-   **Google Libraries:**
    -   Material Components for Android
    -   Generative AI (Gemini)
-   **Third-Party Libraries:**
    -   Markwon: A markdown library for Android.
