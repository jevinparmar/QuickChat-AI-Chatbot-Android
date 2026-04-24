# ⚡ Quick Start Guide - 5 Minutes Setup

Get Quick Chat running in less than 5 minutes!

---

## Prerequisites

- ✅ Android Studio (Jellyfish or later)
- ✅ Android device or emulator (API 24+)
- ✅ Internet connection

---

## Step 1: Clone the Repository (1 min)

```bash
git clone https://github.com/yourusername/quick-chat.git
cd quick-chat
```

Or download the ZIP and extract it.

---

## Step 2: Get Your Groq API Key (1 min)

1. Go to [https://console.groq.com](https://console.groq.com)
2. Sign up or log in
3. Click **API Keys** → **Create API Key**
4. Copy the key (looks like `gsk_xxx...`)
5. Open `app/src/main/java/com/example/quickchataichatbot/ChatApp.java`
6. Find line 86: `private static final String GROQ_API_KEY = "...";`
7. Replace with your key:
   ```java
   private static final String GROQ_API_KEY = "gsk_your_actual_key";
   ```

---

## Step 3: Setup Firebase (2 min)

### Quick Setup (For Development)

1. Go to [https://console.firebase.google.com](https://console.firebase.google.com)
2. Click **Create Project** → Enter name → Click **Create**
3. Click **Authentication** → **Get Started** → **Email/Password** → **Enable**
4. Click **Firestore Database** → **Create Database** → **Test Mode** → **Create**
5. Go to **Project Settings** (⚙️)
6. Download `google-services.json`
7. Place it in the `app/` folder:
   ```
   app/
   └── google-services.json
   ```

---

## Step 4: Open in Android Studio (1 min)

1. Open Android Studio
2. Click **File** → **Open**
3. Select the `quick-chat` folder
4. Wait for Gradle sync (automatic)
5. Trust the project when prompted

---

## Step 5: Run the App (< 1 min)

1. **Connect a device** or **start an emulator**
2. Click the **Run** button (▶️) in Android Studio
3. Select your device
4. Wait for app to install and launch

---

## You're Done! 🎉

### First Time Using the App?

1. **Sign Up** - Create an account with email and password
2. **Chat** - Type a message and send it
3. **Wait** - The AI will respond in 2-3 seconds
4. **Share Images** - Tap the paperclip icon to analyze images

---

## Common Issues & Quick Fixes

### Issue: "API Key Invalid"
**Fix**: Make sure you copied the Groq API key correctly without extra spaces.

### Issue: "Firebase Error"
**Fix**: Make sure `google-services.json` is in the `app/` folder, not `app/src/`.

### Issue: "Gradle Sync Failed"
**Fix**: 
```bash
cd quick-chat
./gradlew clean
./gradlew build
```

### Issue: "App Won't Install"
**Fix**: 
- Make sure emulator is running
- Try: `./gradlew installDebug`
- Check: `adb devices` (device should be listed)

---

## Next Steps

- 📖 Read the full [README.md](README.md)
- 🔐 Learn about [API Setup](API_SETUP_GUIDE.md) in detail
- 🤝 Want to contribute? See [CONTRIBUTING.md](CONTRIBUTING.md)
- 🐛 Found a bug? Open an [issue](https://github.com/yourusername/quick-chat/issues)

---

## Need Help?

- Check [Troubleshooting Section](README.md#-troubleshooting-1) in README
- Read [API_SETUP_GUIDE.md](API_SETUP_GUIDE.md) for detailed setup
- Open a [GitHub Issue](https://github.com/yourusername/quick-chat/issues)

---

**Happy chatting!** 🚀
