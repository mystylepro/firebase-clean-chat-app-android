# Firebase Clean Chat App (Android)

A real-time one-on-one chat app built with **Firebase**, following **MVVM + Clean Architecture**, using **Hilt for DI**, **Room for local storage**, and **modular design principles**. Built with production-level best practices.

---

## 🔧 Features

- Firebase Authentication
- Real-time chat with Firestore
- Sent/Delivered/Read message status
- Typing indicator
- Offline-first message syncing with Room
- Modular architecture (Clean Architecture)
- ViewBinding & Dependency Injection with Hilt
- DiffUtil with ListAdapter for UI efficiency

---

## 🧠 Approach

This app follows **Clean Architecture**:

- **Presentation Layer:** Handles UI logic and observes ViewModel state.
- **Domain Layer:** Contains pure Kotlin use cases and business logic.
- **Data Layer:** Responsible for Firebase/Room interactions, mapping between models and entities.

Additional Concepts:
- `ListAdapter` with `DiffUtil` for real-time UI updates.
- Firebase snapshot listener + Room syncing using `callbackFlow`.
- Modular shared components using Kotlin DSL and `libs.versions.toml`.

---

## 🚀 How to Run

1. **Clone the repository**
   ```bash
   git clone https://github.com/mystylepro/firebase-clean-chat-app-android.git
