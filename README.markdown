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
- 🔄 Regenerate AI responses based on selected response style

---

## 📸 Screenshots

| Chat List Screen | Chat Screen (Short Response) | Chat Screen (Detailed Response) | Chat Screen (Explanatory Response) | Regenerate (Response Style Options) |
|------------------|-----------------------------|--------------------------------|------------------------------------|------------------------------------|
| <img src="https://github.com/user-attachments/assets/b130caf5-9002-40cb-ae2b-b79f4b589e54" width="100%"/> | <img src="https://github.com/user-attachments/assets/4bf44ac4-49ec-4cbe-a0eb-a658c5182a6c" width="100%"/> | <img src="https://github.com/user-attachments/assets/eaae2dfb-5ef5-4526-bda9-768ecd5496d2" width="100%"/> | <img src="https://github.com/user-attachments/assets/da4a70e8-31e1-4dff-aff8-4f7e9690a4ed" width="100%"/> | <img src="https://github.com/user-attachments/assets/07d199ce-ee97-4d36-adc0-932e3ac04085" width="100%"/> |
| **User Input**: "Show my recent chat sessions." <br> Displays multiple chat sessions in a clean navigation drawer. | **User Input**: "Write a short poem about a futuristic city." <br> Shows a concise, poetic response with Markdown formatting. | **User Input**: "Explain blockchain in simple terms (Detailed)." <br> Demonstrates a comprehensive response with rich details. | **User Input**: "Describe how machine learning works (Explanatory)." <br> Highlights an in-depth, educational response with clear formatting. | **User Input**: "Regenerate the response for 'What is AI?' in Explanatory style." <br> Showcases the ability to regenerate responses with different styles. |

---

## 🚀 Ongoing Improvements

- **SLM Model Deployment**: Implementing Small Language Models (SLM) for offline support, enabling chat functionality without internet connectivity.
- **Model Selection**: Adding support for selecting between multiple Groq models and toggling between offline (SLM) and online (API) modes for flexible user experience.
- **Performance & UI/UX Enhancements**: Optimizing app performance and refining the user interface for a smoother, more intuitive experience.

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
   git clone https://github.com/JayeshBainwad/VersaChat.git
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
