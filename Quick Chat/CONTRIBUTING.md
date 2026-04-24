# Contributing to Quick Chat

Thank you for your interest in contributing to Quick Chat! This document provides guidelines and instructions for contributing.

## Table of Contents
- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Making Changes](#making-changes)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)
- [Coding Standards](#coding-standards)
- [Testing](#testing)
- [Reporting Issues](#reporting-issues)

---

## Code of Conduct

We are committed to providing a welcoming and inclusive environment. Please:
- Be respectful and inclusive
- Provide constructive feedback
- Focus on the code, not the person
- Help others learn and grow

---

## Getting Started

1. **Fork the repository**
   ```bash
   git clone https://github.com/yourusername/quick-chat.git
   cd quick-chat
   ```

2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   # or for bug fixes
   git checkout -b fix/bug-description
   ```

3. **Keep your fork updated**
   ```bash
   git remote add upstream https://github.com/originalrepo/quick-chat.git
   git fetch upstream
   git rebase upstream/main
   ```

---

## Development Setup

### Prerequisites
- Android Studio Jellyfish or later
- JDK 11 or higher
- Android SDK API 24+
- Git

### Setup Steps

1. **Clone and open project**
   ```bash
   git clone https://github.com/yourusername/quick-chat.git
   cd quick-chat
   open -a "Android Studio" .
   ```

2. **Set up API keys** (see [API_SETUP_GUIDE.md](API_SETUP_GUIDE.md))
   - Add your Groq API key
   - Place `google-services.json` in `app/` directory

3. **Sync Gradle**
   - Android Studio will prompt you
   - Or run: `./gradlew build`

4. **Run the app**
   - Connect a device or start an emulator
   - Click the Run button in Android Studio

---

## Making Changes

### Project Structure
```
app/src/main/java/com/example/quickchataichatbot/
├── ChatApp.java              # Main chat activity
├── LoginActivity.java        # Login screen
├── SignupActivity.java       # Registration
├── ProfileActivity.java      # User profile
├── ChatHistory.java          # Data model
├── ChatHistoryActivity.java  # History view
├── ChatAdapter.java          # RecyclerView adapter
├── Message.java              # Message model
├── UserModel.java            # User model
├── FirestoreChatHelper.java  # Firebase helper
└── Splash.java               # Splash screen
```

### Common Tasks

#### Add a New Feature
1. Create new Activity/Fragment if needed
2. Update `AndroidManifest.xml` to register it
3. Add navigation/UI elements
4. Write tests for the feature
5. Update README if it's user-facing

#### Bug Fix
1. Create a test that reproduces the bug
2. Fix the bug
3. Verify the test passes
4. Add regression test if needed

#### Improve UI
1. Update relevant XML layout files
2. Test on multiple Android versions
3. Include screenshots in PR description

---

## Commit Guidelines

### Commit Message Format
```
<type>: <subject>

<body>

<footer>
```

### Type
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, missing semicolons, etc.)
- `refactor`: Code refactoring without feature changes
- `perf`: Performance improvements
- `test`: Adding or updating tests
- `chore`: Build, dependencies, or tooling changes

### Subject
- Use present tense ("add feature" not "added feature")
- Don't capitalize first letter
- No period at the end
- Max 50 characters

### Body
- Explain what and why, not how
- Wrap at 72 characters
- Separate from subject with blank line
- Use bullet points for multiple changes

### Example
```
feat: add image analysis with vision API

- Integrate Llama Vision model for image analysis
- Add image preview before sending
- Support multiple image formats (JPEG, PNG, WebP)

Closes #42
```

### Commit Examples
```bash
git commit -m "feat: add dark mode support"
git commit -m "fix: resolve crash on empty message send"
git commit -m "docs: update API setup instructions"
git commit -m "refactor: simplify FirestoreHelper methods"
```

---

## Pull Request Process

### Before Opening PR
1. **Update your branch**
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Test thoroughly**
   ```bash
   ./gradlew test                 # Unit tests
   ./gradlew connectedAndroidTest # Instrumented tests
   ```

3. **Check code quality**
   - No hardcoded strings (use resources)
   - No sensitive data (API keys, passwords)
   - Proper error handling
   - Null safety checks

### Opening PR

1. **Create descriptive title**
   ```
   Add image analysis feature
   Fix crash when sending empty messages
   Update Firebase security rules
   ```

2. **Fill PR template**
   ```markdown
   ## Description
   Brief explanation of changes

   ## Type of Change
   - [ ] New feature
   - [ ] Bug fix
   - [ ] Breaking change
   - [ ] Documentation

   ## Testing
   How to test these changes

   ## Screenshots (if applicable)
   Before/After UI changes

   ## Checklist
   - [ ] Code follows style guidelines
   - [ ] Tests added/updated
   - [ ] Documentation updated
   - [ ] No breaking changes
   - [ ] No API keys in code
   ```

3. **Link related issues**
   ```
   Closes #123
   Fixes #456
   Related to #789
   ```

### PR Review
- Address all feedback
- Push new commits (don't force push)
- Ask for clarification if needed
- Be patient and respectful

### Merging
- Squash commits if needed
- Ensure CI passes
- Get at least 1 approval
- Merge using "Create a merge commit"

---

## Coding Standards

### Java/Android Style Guide

#### Naming Conventions
```java
// Classes - PascalCase
public class ChatActivity extends AppCompatActivity { }

// Variables/Methods - camelCase
private String userName;
private void sendMessage() { }

// Constants - UPPER_SNAKE_CASE
private static final String API_KEY = "...";
private static final int TIMEOUT_SECONDS = 30;

// Resources - lower_snake_case
// Files: activity_chat.xml, ic_send.xml
// IDs: @+id/btn_send, @+id/et_message
```

#### Code Organization
```java
public class ChatApp extends AppCompatActivity {
    // Constants first
    private static final String TAG = "ChatApp";
    
    // UI components
    private RecyclerView recyclerView;
    
    // Data
    private ArrayList<Message> messages;
    
    // Lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) { }
    
    // Public methods
    public void sendMessage(String text) { }
    
    // Private methods
    private void updateUI() { }
    
    // Inner classes
    private class MyAdapter extends RecyclerView.Adapter { }
}
```

#### Comments
```java
// Use comments for WHY, not WHAT
// Good: Retry mechanism for transient network errors
if (retryCount < MAX_RETRIES && isTransientError(e)) { }

// Bad: Check if retry count is less than max
if (retryCount < MAX_RETRIES) { }
```

#### Error Handling
```java
// Always handle exceptions
try {
    // Do something
} catch (IOException e) {
    Log.e(TAG, "Network error: " + e.getMessage(), e);
    showErrorToUser("Failed to send message");
}

// Use specific exception types
if (response.isSuccessful()) {
    // Handle success
} else {
    // Handle error
}
```

### Null Safety
```java
// Use null checks
if (user != null && user.getName() != null) {
    displayName(user.getName());
}

// Or use Objects
Objects.requireNonNull(user, "User cannot be null");
```

### Layout XML
```xml
<!-- Use meaningful IDs -->
<Button
    android:id="@+id/btn_send_message"
    android:text="@string/send"
    />

<!-- Use attributes from resources -->
android:textColor="@color/text_primary"
android:textSize="@dimen/text_size_body"
android:paddingStart="@dimen/padding_standard"
```

---

## Testing

### Unit Tests
```java
public class ChatAdapterTest {
    @Before
    public void setUp() {
        adapter = new ChatAdapter();
    }
    
    @Test
    public void testAddMessage() {
        adapter.addMessage(new Message("Hello"));
        assertEquals(1, adapter.getItemCount());
    }
}
```

### Running Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# With coverage
./gradlew testDebugUnitTestCoverage
```

### Test Checklist
- [ ] Test happy path
- [ ] Test error cases
- [ ] Test edge cases (null, empty, large values)
- [ ] Test concurrency if applicable
- [ ] Update tests when fixing bugs

---

## Reporting Issues

### Bug Report Template
```markdown
## Description
Brief description of the bug

## Steps to Reproduce
1. Step 1
2. Step 2
3. Step 3

## Expected Behavior
What should happen

## Actual Behavior
What actually happens

## Environment
- Android Version: 12
- Device: Pixel 6
- App Version: 1.0.0

## Screenshots/Logs
Include relevant screenshots or logcat output

## Additional Context
Any other relevant information
```

### Feature Request Template
```markdown
## Description
What feature would you like?

## Use Case
Why do you need this?

## Proposed Solution
How should it work?

## Alternatives
Any alternative approaches?
```

---

## Code Review Checklist

### For Reviewers
- [ ] Code follows style guidelines
- [ ] Tests are included/updated
- [ ] Documentation is clear
- [ ] No security vulnerabilities
- [ ] No hardcoded values
- [ ] Error handling is appropriate
- [ ] Performance is acceptable
- [ ] No unnecessary dependencies

### For Authors
- [ ] I've tested on multiple devices/versions
- [ ] I've updated documentation
- [ ] I've added tests for new code
- [ ] I've removed debug code
- [ ] I've checked for memory leaks
- [ ] I've verified no API keys are exposed

---

## Questions?

- 📚 Check existing [documentation](README.md)
- 💬 Open a [GitHub Discussion](https://github.com/yourusername/quick-chat/discussions)
- 📧 Contact the maintainers

---

## Recognition

Contributors will be:
- Added to [CONTRIBUTORS.md](CONTRIBUTORS.md) file
- Mentioned in release notes
- Credited in the app (if applicable)

---

Thank you for contributing to Quick Chat! 🎉
