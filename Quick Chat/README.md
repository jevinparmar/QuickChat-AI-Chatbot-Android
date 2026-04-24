# 🤖 Quick Chat - AI-Powered Android Chatbot

> A modern, feature-rich Android messaging application integrated with the **Groq AI API** (Llama 3.3) for intelligent chat responses. Built with Firebase for authentication and real-time messaging.

![Android](https://img.shields.io/badge/Android-14%2B-green)
![Gradle](https://img.shields.io/badge/Gradle-8.0%2B-blue)
![Java](https://img.shields.io/badge/Java-11%2B-orange)
![License](https://img.shields.io/badge/License-MIT-yellow)

---

## ✨ Features

- 🤖 **AI-Powered Chat** - Integrated with Groq API using Llama 3.3 70B model
- 🖼️ **Image Analysis** - Vision capabilities with Llama 4 Scout 17B model
- 🔐 **User Authentication** - Firebase Authentication (Email/Password)
- 💾 **Real-Time Database** - Firebase Firestore for message storage
- 📱 **Rich UI** - Modern, responsive Material Design interface
- 📝 **Chat History** - Browse and manage previous conversations
- 👤 **User Profiles** - Manage user profile and settings
- 🔄 **Message Sync** - Real-time message synchronization across devices
- 📎 **File Attachments** - Send images with AI analysis
- 💬 **Typing Indicators** - Visual feedback for AI processing

---

## 📋 Prerequisites

- **Android Studio**: Jellyfish or later
- **Android SDK**: API Level 24 (Android 7.0) or higher
- **Java**: JDK 11 or later
- **Gradle**: 8.0 or higher
- **Internet Connection**: Required for API calls

---

## 🚀 Quick Setup Guide

### Step 1: Clone the Repository

```bash
git clone https://github.com/yourusername/quick-chat.git
cd quick-chat
```

### Step 2: Get Your Groq API Key

1. Visit [Groq Console](https://console.groq.com) and sign up for a free account
2. Navigate to **API Keys** section
3. Click **Create API Key** and copy it
4. Keep this key safe - you'll need it in the next step

### Step 3: Add Groq API Key to Your Project

Open `app/src/main/java/com/example/quickchataichatbot/ChatApp.java` and replace this line:

```java
// Line 86
private static final String GROQ_API_KEY = "YOUR_GROQ_API_KEY_HERE";
```

With your actual Groq API key:

```java
private static final String GROQ_API_KEY = "gsk_xxxxxxxxxxxxxxxxxxxxxxxx";
```

> ⚠️ **Security Warning**: Never commit your API key to GitHub! The `.gitignore` file already excludes sensitive files, but make sure to:
> - Never share your API key publicly
> - Use environment variables in production
> - Rotate your key if accidentally exposed

### Step 4: Firebase Configuration

1. Create a Firebase Project:
   - Go to [Firebase Console](https://console.firebase.google.com)
   - Click **Create a new project**
   - Follow the setup wizard

2. Enable Authentication:
   - Navigate to **Authentication** → **Sign-in method**
   - Enable **Email/Password** authentication

3. Create Firestore Database:
   - Go to **Firestore Database**
   - Click **Create database**
   - Start in **test mode** (for development)

4. Download `google-services.json`:
   - In Firebase Console, go to **Project Settings**
   - Click **Download** for the Android app
   - Place the file in `app/` directory

5. (Optional) Update Security Rules for Production:
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /messages/{document=**} {
         allow read, write: if request.auth != null;
       }
       match /users/{uid} {
         allow read, write: if request.auth.uid == uid;
       }
     }
   }
   ```

### Step 5: Open in Android Studio

```bash
# Open the project
open -a "Android Studio" .
```

Or manually:
- Open Android Studio
- Click **File → Open**
- Select the project folder
- Wait for Gradle sync to complete

### Step 6: Build and Run

```bash
# Build the project
./gradlew build

# Install and run on connected device/emulator
./gradlew installDebug
```

Or use Android Studio:
- Connect an Android device or start an emulator
- Click the **Run** button (▶️)

---

## 📁 Project Structure

```
quick-chat/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/quickchataichatbot/
│   │   │   │   ├── ChatApp.java              # Main chat activity with Groq API
│   │   │   │   ├── LoginActivity.java        # Firebase login
│   │   │   │   ├── SignupActivity.java       # User registration
│   │   │   │   ├── ProfileActivity.java      # User profile management
│   │   │   │   ├── ChatHistory.java          # Chat history model
│   │   │   │   ├── ChatHistoryActivity.java  # View chat history
│   │   │   │   ├── ChatAdapter.java          # RecyclerView adapter for messages
│   │   │   │   ├── Message.java              # Message model
│   │   │   │   ├── UserModel.java            # User model
│   │   │   │   ├── FirestoreChatHelper.java  # Firebase operations
│   │   │   │   └── Splash.java               # Splash screen
│   │   │   ├── res/
│   │   │   │   ├── layout/                   # XML layouts
│   │   │   │   ├── drawable/                 # Icons and images
│   │   │   │   ├── values/                   # Colors, strings, styles
│   │   │   │   └── anim/                     # Animations
│   │   │   └── AndroidManifest.xml
│   │   ├── androidTest/                      # Android instrumentation tests
│   │   └── test/                             # Unit tests
│   ├── build.gradle.kts                      # App-level build configuration
│   ├── google-services.json                  # Firebase config (add locally)
│   └── proguard-rules.pro                    # ProGuard obfuscation rules
├── gradle/
│   ├── wrapper/                              # Gradle wrapper files
│   └── libs.versions.toml                    # Dependency versions
├── build.gradle.kts                          # Project-level build config
├── settings.gradle.kts                       # Gradle settings
├── gradlew                                   # Gradle wrapper (Unix)
├── gradlew.bat                               # Gradle wrapper (Windows)
├── gradle.properties                         # Gradle properties
├── .gitignore                                # Git ignore rules
└── README.md
```

---

## 🔧 Technologies & Dependencies

### Core Technologies
- **Language**: Java
- **Build System**: Gradle (Kotlin DSL)
- **Target SDK**: Android 14 (API 34)
- **Min SDK**: Android 7.0 (API 24)

### Key Libraries
- **Firebase**:
  - `firebase-auth` - User authentication
  - `firebase-firestore` - Real-time database
- **Groq API** - AI chat completions (via HTTP)
- **OkHttp3** - HTTP client for API calls
- **RecyclerView** - List of messages
- **Material Design** - UI components
- **AndroidX** - Modern Android support libraries

### API Models Used
- **Text Chat**: `llama-3.3-70b-versatile` - Fast, accurate text responses
- **Image Analysis**: `meta-llama/llama-4-scout-17b-16e-instruct` - Vision capabilities

---

## 🏗️ Building & Deployment

### Debug Build
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release Build
```bash
./gradlew assembleRelease
# Requires keystore configuration
# Output: app/build/outputs/apk/release/app-release.apk
```

### Running Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests (on device/emulator)
./gradlew connectedAndroidTest
```

### View Build Info
```bash
./gradlew build --info
```

---

## 📱 Usage Guide

### First Time Setup
1. **Launch the app** and tap **Sign Up**
2. **Enter your email and password** to create an account
3. **Verify email** (if required by Firebase)
4. **Log in** with your credentials

### Chatting with AI
1. **Type your message** in the input field
2. **Tap the Send button** (✈️) or press Enter
3. **Wait for AI response** - Watch the typing indicator
4. **Share your chat** - View previous conversations in Chat History

### Sending Images
1. **Tap the Attachment button** (📎)
2. **Select an image** from your gallery
3. **Preview displays** before sending
4. **AI analyzes** the image and responds

### View Profile
- Tap your **profile icon** to manage user settings
- Update your **name** and other preferences

### Chat History
- Tap the **Menu button** to access Chat History
- View all previous conversations
- Select a chat to review messages

---

## 🔐 Security & Best Practices

### API Key Management
```java
// ❌ NEVER DO THIS
private static final String GROQ_API_KEY = "gsk_xxxxx"; // Exposed!

// ✅ DO THIS (for production)
// Add to local.properties (NOT committed to Git)
// groq.api.key=gsk_xxxxx

// Read from BuildConfig at runtime
private static final String GROQ_API_KEY = BuildConfig.GROQ_API_KEY;
```

### Firebase Rules
- Users can only see/modify their own data
- Messages are validated on the server
- Use Cloud Functions for sensitive operations

### Network Security
- Enable SSL pinning for API calls
- Use HTTPS only
- Validate certificates

---

## 🐛 Troubleshooting

### Issue: "API Key Invalid" Error
**Solution**: 
- Check your Groq API key in `ChatApp.java` (line 86)
- Verify the key is not expired or revoked
- Get a new key from [Groq Console](https://console.groq.com)

### Issue: Firebase Authentication Fails
**Solution**:
- Ensure `google-services.json` is in `app/` directory
- Check Firebase project settings match your app package name
- Verify email/password authentication is enabled in Firebase

### Issue: Messages Not Appearing
**Solution**:
- Check Firestore database rules allow read/write
- Verify user is authenticated
- Check Logcat for error messages: `adb logcat | grep ChatApp`

### Issue: Gradle Sync Fails
**Solution**:
```bash
./gradlew clean
./gradlew build
```

### Issue: App Crashes on Message Send
**Solution**:
- Check internet connection
- View crash logs: `adb logcat`
- Verify Groq API key is valid
- Check request body format in ChatApp.java

---

## 📊 Performance Tips

- **Limit Chat History**: Paginate old messages to improve UI performance
- **Image Compression**: Compress images before sending to API
- **Connection Timeout**: Increase timeout for slow networks
  ```java
  client = new OkHttpClient.Builder()
      .connectTimeout(30, TimeUnit.SECONDS)
      .writeTimeout(30, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .build();
  ```

---

## 🤝 Contributing

We welcome contributions! Here's how:

1. **Fork the repository**
   ```bash
   gh repo fork yourusername/quick-chat
   ```

2. **Create a feature branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```

3. **Commit your changes**
   ```bash
   git commit -m "Add amazing feature"
   ```

4. **Push to your fork**
   ```bash
   git push origin feature/amazing-feature
   ```

5. **Open a Pull Request** on GitHub

### Development Guidelines
- Follow Java conventions (camelCase, proper naming)
- Write comments for complex logic
- Test on multiple Android versions
- Update README if adding new features

---

## 📝 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

You're free to use this project for personal, educational, and commercial purposes.

---

## 👥 Authors

This project was developed as a **group academic project** by:

1. **Jevin Parmar**
2. **Savan Detroja**
3. **Aryan Kapdiya**

---

## 📞 Support & Contact

- **Issues**: [Open an issue on GitHub](https://github.com/yourusername/quick-chat/issues)
- **Email**: yourname@example.com
- **College**: [Your Institution Name]

---

## 🔗 Useful Resources

- [Groq API Documentation](https://console.groq.com/docs)
- [Firebase Documentation](https://firebase.google.com/docs)
- [Android Developer Guide](https://developer.android.com/guide)
- [Material Design](https://material.io/design)
- [OkHttp Documentation](https://square.github.io/okhttp/)

---

## 📈 Roadmap

- [ ] Add voice message support
- [ ] Implement message search functionality
- [ ] Add dark mode support
- [ ] Multi-language support
- [ ] Improved image compression
- [ ] Push notifications
- [ ] User settings/preferences UI
- [ ] Export chat as PDF

---

## ⚠️ Important Notes

1. **Keep your Groq API key private** - Never commit it to Git
2. **Groq API is free** but has rate limits (check [pricing](https://groq.com))
3. **Firebase free tier includes**:
   - 25K reads/day
   - 10K writes/day
   - Upgrade to Blaze plan for production
4. **For production deployment**:
   - Use environment variables for API keys
   - Implement backend server for API calls
   - Add proper error handling and logging
   - Set up CI/CD pipeline

---

**Happy Coding! 🚀**

*Last Updated: April 2026*
