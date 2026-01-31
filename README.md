**MediCap — Offline AI Medicine Assistant and Expiry Management System**

MediCap is an offline-first Android application designed to help users safely manage medicines by combining on-device artificial intelligence with practical healthcare workflows. The application enables medicine scanning, expiry tracking, intelligent search, and AI-based assistance without relying on cloud APIs.

The primary goal of MediCap is to reduce risks associated with expired medicines, unsafe self-medication, and lack of reliable medicine information, especially in low-connectivity or emergency scenarios.

_Problem Statement_

Millions of people store medicines at home without tracking expiry dates or fully understanding their usage and safety guidelines. Existing solutions are either cloud-dependent, privacy-invasive, or limited to basic reminders.

MediCap addresses this gap by providing a fully on-device, privacy-preserving system that works even in offline or low-internet environments.

_Solution Overview_

MediCap acts as a personal medicine companion that allows users to scan medicines, store expiry details locally, search medicines manually, and interact with an AI assistant for basic informational guidance. All intelligence runs locally on the device using the RunAnywhere SDK.

Key Features
Medicine Scanning

Camera-based scanning of medicine strips or boxes

Automatic extraction of medicine name and expiry date

Direct storage of scanned data into a local expiry database

Expiry Vault

Local Room database for persistent storage

Tracks medicine name, expiry date, and user notes

Visual status indicators for safe, expiring, and expired medicines

Designed to prevent accidental consumption of expired drugs

AI Medicine Assistant

Chat-based interface for medicine-related queries

Provides basic information such as usage, precautions, and safety notes

Runs entirely on device using local large language models

No cloud calls, ensuring privacy and reliability

Note: The assistant is informational and does not replace professional medical advice.

Smart Medicine Search

Manual search by medicine name

Useful when scanning is not possible or packaging is unavailable

Emergency Delivery Flow (Prototype)

Conceptual medicine delivery interface for emergency use cases

Demonstrates real-world applicability and system design thinking

Focused on UX and user flow rather than logistics implementation

PDF Export

Export stored medicine data into a shareable PDF

Useful for doctor consultations, family sharing, or record keeping

Demonstration

A complete working demo of the application is available at the following link:

https://drive.google.com/file/d/1lXa6bxx6TOJBdSECkhHcjLpaFm0du04Z/view

Technology Stack
Android

Kotlin

Jetpack Compose

Room Database

CameraX

AI and On-Device Inference

RunAnywhere SDK

Llama.cpp inference module

On-device large language models

Streaming text generation

System Architecture

The application follows a clean, modular architecture:

Application Initialization
        |
        v
ViewModels (State and Business Logic)
        |
        v
Jetpack Compose UI Layer
        |
        v
Room Database (Expiry Vault)

Build and Run Instructions
Requirements

Android 7.0 (API 24) or higher

Minimum 200 MB free storage for AI models

Internet connection required only for initial model download

Build Steps
./gradlew assembleDebug


Alternatively, open the project in Android Studio and run it on a physical Android device.

Supported AI Models
Model Name	Size	Use Case
SmolLM2 360M	~119 MB	Fast, lightweight inference
Qwen 2.5 0.5B Instruct	~374 MB	Improved reasoning and responses
Limitations and Notes

On-device inference is slower compared to cloud-based systems

Performance depends on device CPU and available memory

The application prioritizes privacy and offline usability over speed

Future Enhancements

Expiry reminder notifications

Multi-language support

Barcode scanning integration

Voice-based medicine interaction

Family and multi-profile support

Pharmacy system integration

Resources

RunAnywhere SDK Repository: https://github.com/RunanywhereAI/runanywhere-sdks

RunAnywhere Documentation: https://github.com/RunanywhereAI/runanywhere-sdks/blob/main/CLAUDE.md

Author

Priyanka Khasa
B.Tech Electronics and Communication Engineering

App follows the license of the RunAnywhere SDK.
