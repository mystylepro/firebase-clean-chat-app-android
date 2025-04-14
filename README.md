# 💬 Firebase Chat App – Clean Architecture (MVVM + Hilt + Offline Support)

This is a real-time one-on-one chat application built with **Firebase Auth**, **Firestore**, and **Room DB** to support offline-first messaging.  
It follows **Clean Architecture** and **MVVM** with a focus on modular, testable, and scalable code.

---

## 1️⃣ Approach Taken

The app is structured using Clean Architecture, split across multiple layers:

### 🔹 Layers
- **Presentation Layer** – Fragments/ViewModels, displaying chat and handling user interaction
- **Domain Layer** – Business logic using UseCases and domain models
- **Data Layer** – Firestore (remote) + Room (local) repositories, with proper abstraction
- **DI Layer** – Using Dagger Hilt to provide scoped dependencies cleanly

### 💡 Highlights
- **MVVM with StateFlow & SharedFlow** – Ensures reactive and lifecycle-safe data updates
- **Room for offline caching** – Messages sync from Firestore and are stored locally
- **DiffUtil + ListAdapter** – For optimized RecyclerView updates
- **Firebase Auth** – Email/Password based login system
- **Typing indicator** and **message status** (sent, delivered, read)

---

## 2️⃣ Steps to Run the Project

> 🧩 Prerequisites:
- Android Studio Hedgehog or above
- Firebase account with a project setup
- Minimum SDK: 21

### 🛠 Setup
1. **Clone the repo**
   ```bash
   git clone https://github.com/yourusername/firebase-chat-app-clean-architecture.git
   ```

2. **Open the project in Android Studio**

3. **Firebase Setup**
   - Enable **Email/Password Authentication** in Firebase Console
   - Enable **Cloud Firestore**
   - Download `google-services.json` and place it in:  
     `app/src/main/google-services.json`

4. **Build the project** and hit **Run** on a device/emulator

---

## 3️⃣ Trade-offs & Future Improvements

| Area | Current Status | Future Plan |
|------|----------------|-------------|
| Media Sharing | ❌ Not yet implemented | ✅ Add image/audio sharing |
| Group Chat | ❌ Only 1-on-1 | ✅ Add group chat support |
| Notifications | ❌ Not yet | ✅ Integrate FCM for push |
| Theming | ❌ Light theme only | ✅ Add dark mode toggle |
| UI Polish | Basic | Add animations, transitions |
| Tests | Partially covered | Add full unit & UI tests |

---

## 4️⃣ Clean, Modular Code & Best Practices

- 🧼 **Clean Architecture** with clear separation of concerns
- 🧪 **Testable ViewModels** using `kotlinx-coroutines-test` and `Mockito`
- ♻️ **Modular structure**: `domain`, `data`, `presentation`
- 🔐 Secure Auth flow using Firebase
- ⚙️ Centralized dependency management via `libs.versions.toml`
- 🔄 Offline-first message syncing via Room + Firestore
- 🎯 Following SOLID principles in UseCases and Repositories
- 🚫 Avoids leaking Activity/Context — uses Hilt properly

---

## 🧩 Tech Stack

| Category | Tools |
|----------|-------|
| UI | ViewBinding, ConstraintLayout |
| Arch | MVVM + Clean Architecture |
| DI | Hilt |
| Data | Firebase Firestore, Room |
| Auth | Firebase Authentication |
| Async | Kotlin Coroutines, StateFlow, SharedFlow |
| Storage | Encrypted SharedPreferences (optional) |
| Testing | Mockito, Coroutines Test, JUnit |

---

## 👨‍💻 Author

**Anil Kumar**  
📧 er.anil8357@gmail.com  
🔗 [LinkedIn](https://www.linkedin.com/in/mystylepro/)

---

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
 
