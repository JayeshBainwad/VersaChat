# VersaChat - AI Chat Application

A modern Android chat application powered by the **Groq API** for intelligent conversations.  
Built with **Jetpack Compose**, **Material Design 3**, and the latest Android best practices.

---

## 🛠 Tech Stack

| Category | Version / Details |
| --- | --- |
| **IDE** | Android Studio Narwhal Feature Drop (2025.1.2) |
| **Language** | Kotlin 2.0.21 |
| **Architecture** | MVVM + Clean Architecture |
| **UI** | Jetpack Compose (BOM 2025.05.01) |
| **Dependency Injection** | Hilt 2.52 |
| **Networking** | Retrofit 2.9.0 + OkHttp 4.12.0 |
| **Database** | Room 2.6.1 |
| **Min SDK** | 26 (Android 8.0) |
| **Target SDK** | 34 (Android 14) |

---

### 📦 Core Dependencies

| Dependency | Version |
| --- | --- |
| Android Gradle Plugin (AGP) | 8.11.1 |
| Kotlin | 2.0.21 |
| KSP (Kotlin Symbol Processing) | 2.0.21-1.0.27 |
| Hilt (DI) | 2.52 |
| Hilt Navigation Compose | 1.1.0 |
| Room (Database) | 2.6.1 |
| Jetpack Compose BOM | 2025.05.01 |
| Lifecycle Runtime KTX | 2.9.0 |
| Activity Compose | 1.10.1 |
| Core KTX | 1.16.0 |
| JUnit (Unit Testing) | 4.13.2 |
| JUnit (AndroidX Extension) | 1.1.5 |
| Espresso Core (UI Testing) | 3.6.1 |

---

## ✨ Features

- 🤖 AI-powered chat using **Groq API**
- 💬 Support for **multiple chat sessions**
- 📝 Responses with **Markdown formatting**
- 🎯 Option to choose response style: **Short / Detailed / Explanatory**
- 🔄 Real-time chat updates

---

## 🏛 Architecture & File Structure

```
com.jsb.versachat/
├── data/           # Data layer
│   ├── api/        # Groq API integration
│   ├── local/      # Room DB implementation
│   ├── model/      # Data models
│   └── repository/ # Repository implementations
├── domain/         # Business logic
│   ├── model/      # Domain models
│   ├── repository/ # Repository interfaces
│   └── usecase/    # Business use cases
└── presentation/   # UI layer
    ├── ui/         # Compose screens
    └── viewmodel/  # ViewModels
```

---

## 📦 Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/<your-username>/VersaChat.git
   ```

2. Add your **Groq API key** in `local.properties`:

   ```
   GROQ_API_KEY=your_api_key_here
   ```

3. Build and run using **Android Studio Narwhal (or later)**.

---

## ⚠️ API Usage Notice

This project uses the **Groq API** with the **`openai/gpt-oss-120b`** model for AI text generation. Please:

* Obtain your own API key from [x.ai/api](https://x.ai/api)
* Follow Groq’s terms of service
* Be aware of **rate limits & pricing**
* Store API keys securely

---

## 📄 License

This project is licensed under the **MIT License** – see the [LICENSE](./LICENSE) file for details.

---

## 📩 Contact

Looking to collaborate or hire?

| Type     | Link                                                                                             |
| -------- | ------------------------------------------------------------------------------------------------ |
| LinkedIn | [linkedin.com/in/jayesh-bainwad-a09b93250](https://www.linkedin.com/in/jayesh-bainwad-a09b93250) |
| Email    | [jbainwad@gmail.com](mailto:jbainwad@gmail.com)                                                  |

---

<div align="center">
<strong>⭐ Star this repo if you found it helpful!</strong>
</div>
