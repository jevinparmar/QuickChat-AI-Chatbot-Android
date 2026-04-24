# 🔑 API Setup Guide - Quick Chat

This guide will help you set up the required API keys and services for the Quick Chat application.

---

## Table of Contents
1. [Groq API Setup](#groq-api-setup)
2. [Firebase Configuration](#firebase-configuration)
3. [Adding Keys to Your Project](#adding-keys-to-your-project)
4. [Testing the Setup](#testing-the-setup)
5. [Troubleshooting](#troubleshooting)

---

## Groq API Setup

### What is Groq?
Groq provides fast, cost-effective API access to advanced AI models like Llama 3.3. It's free to use with a reasonable rate limit.

### Get Your Groq API Key

1. **Visit Groq Console**
   - Go to [https://console.groq.com](https://console.groq.com)
   - Sign up for a free account (or log in if you already have one)

2. **Navigate to API Keys**
   - In the left sidebar, click **API Keys**
   - Click **Create API Key** button

3. **Copy Your Key**
   - Your key will look like: `gsk_xxxxxxxxxxxxxxxxxxxxxxxx`
   - Copy it and keep it safe
   - ⚠️ **Never share this key publicly!**

4. **Verify Rate Limits**
   - Check your current usage at [https://console.groq.com/usage](https://console.groq.com/usage)
   - Free tier includes generous limits for development

### Groq Models Used in Quick Chat

This app uses two Groq models:

| Model | Purpose | Use Case |
|-------|---------|----------|
| `llama-3.3-70b-versatile` | Text chat completions | Answer questions, have conversations |
| `meta-llama/llama-4-scout-17b-16e-instruct` | Vision/Image analysis | Analyze and describe images |

---

## Firebase Configuration

### Create Firebase Project

1. **Go to Firebase Console**
   - Visit [https://console.firebase.google.com](https://console.firebase.google.com)
   - Click **Create a new project** or select existing one

2. **Name Your Project**
   - Project Name: "Quick Chat" (or your choice)
   - Uncheck "Enable Google Analytics" (optional for development)
   - Click **Create project**

3. **Wait for Setup**
   - Firebase will create your project
   - This takes about 1-2 minutes

### Setup Authentication

1. **Enable Email/Password Auth**
   - In Firebase Console, go to **Authentication** (left sidebar)
   - Click **Get Started**
   - Select **Email/Password**
   - Toggle to **Enable**
   - Click **Save**

2. **Test Users (Optional)**
   - Click **Users** tab
   - Click **Add user** to create test accounts
   - Email: `test@example.com`
   - Password: (any password for testing)

### Setup Firestore Database

1. **Create Firestore Database**
   - In Firebase Console, go to **Firestore Database**
   - Click **Create Database**
   - Select **Start in test mode** (for development)
   - Choose a location close to you
   - Click **Create**

2. **Create Collections** (Optional - Auto-creates on first write)
   - The app will automatically create:
     - `users` - Store user profiles
     - `messages` - Store chat messages
     - `chatHistory` - Store chat conversations

3. **Security Rules** (Important!)
   - Go to **Firestore Database → Rules**
   - For **development only**, use:
     ```javascript
     rules_version = '2';
     service cloud.firestore {
       match /databases/{database}/documents {
         allow read, write: if request.auth != null;
       }
     }
     ```
   - ⚠️ **For production**, use stricter rules!

### Download google-services.json

1. **Get Configuration File**
   - In Firebase Console, go to **Project Settings** (⚙️ icon)
   - Click on the **Android** app
   - Scroll to "google-services.json"
   - Click **Download**

2. **Add to Project**
   - Place the file in: `app/google-services.json`
   - Make sure it's NOT committed to Git
   - It's already in `.gitignore`

---

## Adding Keys to Your Project

### Step 1: Add Groq API Key

**File**: `app/src/main/java/com/example/quickchataichatbot/ChatApp.java`

Find this line (around line 86):
```java
private static final String GROQ_API_KEY = "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";
```

Replace it with your actual key:
```java
private static final String GROQ_API_KEY = "gsk_YOUR_ACTUAL_KEY_HERE";
```

✅ **Example**:
```java
private static final String GROQ_API_KEY = "gsk_aB1cD2eF3gH4iJ5kL6mN7oP8qR9sTuVwX";
```

### Step 2: Add Firebase Configuration

1. **Download** `google-services.json` from Firebase
2. **Place** in `app/` directory:
   ```
   app/
   ├── google-services.json  ← Place here
   ├── build.gradle.kts
   └── src/
   ```

3. **Verify** `build.gradle.kts` (Project level) includes:
   ```kotlin
   plugins {
       id("com.google.gms.google-services") version "4.x.x"
   }
   ```

4. **Sync** Gradle
   - Android Studio will automatically sync

---

## Testing the Setup

### Test Groq Connection

Create a simple test in `ChatApp.java`:

```java
private void testGroqConnection() {
    String testMessage = "Hello, what is your name?";
    JSONObject requestBody = new JSONObject();
    requestBody.put("model", GROQ_TEXT_MODEL);
    JSONArray messages = new JSONArray();
    JSONObject message = new JSONObject();
    message.put("role", "user");
    message.put("content", testMessage);
    messages.put(message);
    requestBody.put("messages", messages);
    requestBody.put("max_tokens", 512);

    RequestBody body = RequestBody.create(requestBody.toString(), JSON);
    Request request = new Request.Builder()
            .url(GROQ_CHAT_URL)
            .post(body)
            .addHeader("Authorization", "Bearer " + GROQ_API_KEY)
            .build();

    client.newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            Log.e("Groq Test", "API Error: " + e.getMessage());
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) {
            Log.d("Groq Test", "Success: " + response.code());
        }
    });
}
```

### Test Firebase Connection

1. **Run the app**
2. **Sign up** with an email
3. **Check Firebase Console**:
   - Go to **Authentication → Users**
   - Your new user should appear
4. **Send a message** and check Firestore:
   - Go to **Firestore Database**
   - You should see a `messages` collection

---

## Troubleshooting

### ❌ "Invalid API Key" Error

**Problem**: `com.example.quickchataichatbot E/ChatApp: Groq API Error: Unauthorized`

**Solutions**:
- ✅ Copy your API key again from [console.groq.com](https://console.groq.com)
- ✅ Make sure there are no extra spaces in the key
- ✅ Check if the key is active (not revoked)
- ✅ Verify the app can reach the internet

### ❌ "Authentication Failed" in Firebase

**Problem**: Login not working, users not being created

**Solutions**:
- ✅ Verify `google-services.json` is in `app/` folder
- ✅ Check Firebase Console → Authentication → Email/Password is **Enabled**
- ✅ Verify package name matches in Firebase Console
- ✅ Sync Gradle: `./gradlew build`

### ❌ Messages Not Appearing in Firestore

**Problem**: App doesn't crash, but messages not in Firestore

**Solutions**:
- ✅ Check Firestore Rules allow write:
  ```javascript
  allow write: if request.auth != null;
  ```
- ✅ Verify user is authenticated (login first)
- ✅ Check Firestore quota (free tier: 10K writes/day)
- ✅ View Logcat for errors: `adb logcat | grep Firebase`

### ❌ "Network Error" When Sending Messages

**Problem**: `java.net.SocketTimeoutException`

**Solutions**:
- ✅ Check internet connection
- ✅ Increase timeout in `ChatApp.java`:
  ```java
  OkHttpClient client = new OkHttpClient.Builder()
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .writeTimeout(30, TimeUnit.SECONDS)
      .build();
  ```
- ✅ Check if Groq API is down: [Status Page](https://status.groq.com)

### ❌ "API Rate Limited"

**Problem**: Too many requests, getting 429 error

**Solutions**:
- ✅ Check free tier limits at [console.groq.com/usage](https://console.groq.com/usage)
- ✅ Wait before sending more messages
- ✅ Upgrade to paid plan if needed

---

## Security Best Practices

### ⚠️ API Key Security

**Never do this**:
```java
// ❌ BAD - Key visible in code
private static final String GROQ_API_KEY = "gsk_xxxxx";
```

**Better approach for production**:
1. Store key in `local.properties` (not in Git)
2. Read at runtime in build.gradle:
   ```gradle
   buildConfigField "String", "GROQ_API_KEY", 
       "\"${project.property('groq.api.key')}\""
   ```
3. Access in code:
   ```java
   private static final String GROQ_API_KEY = BuildConfig.GROQ_API_KEY;
   ```

### Firebase Security Rules

**Development** (Permissive):
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    allow read, write: if request.auth != null;
  }
}
```

**Production** (Restrictive):
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth.uid == userId;
    }
    match /messages/{messageId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
                     request.resource.data.userId == request.auth.uid;
    }
  }
}
```

---

## API Pricing & Limits

### Groq Pricing
- **Free Tier**: Generous rate limits (check console)
- **Pay-as-you-go**: Starts after free tier exceeded
- **Pricing**: ~$0.05 per 1M input tokens, $0.15 per 1M output tokens

### Firebase Pricing (Free Tier)
- **Authentication**: Free up to 50K users
- **Firestore**: Free up to 25K reads/day, 10K writes/day, 1GB storage
- **Upgrade**: Use Blaze plan for production (pay as you go)

---

## Next Steps

1. ✅ Get Groq API key
2. ✅ Set up Firebase project
3. ✅ Download `google-services.json`
4. ✅ Add both configurations to the app
5. ✅ Test the app
6. ✅ Push to GitHub

---

## Need Help?

- **Groq Docs**: [https://console.groq.com/docs](https://console.groq.com/docs)
- **Firebase Docs**: [https://firebase.google.com/docs](https://firebase.google.com/docs)
- **Android Docs**: [https://developer.android.com/docs](https://developer.android.com/docs)

---

**Last Updated**: April 2026
