# Google Sign-In Setup Guide

This guide explains how to set up Google Sign-In for your Kotlin Multiplatform app.

## ✅ What's Already Done

1. **API Integration** - Backend API call to `/login-with-google` is configured
2. **UI Components** - Google Sign-In button added to LoginScreen
3. **Android Implementation** - GoogleSignInHelper created for Android
4. **Dependencies** - Google Play Services Auth added to build.gradle.kts

## 📱 Android Configuration

### Client ID
The app is configured with your Google Client ID:
```
107630732978-95o7huv64pimhu2ptf57ngk439qqdb29.apps.googleusercontent.com
```

### How to Use

The Google Sign-In button is already integrated in the LoginScreen. When a user taps it:

1. The Android Google Sign-In dialog appears
2. User selects their Google account
3. Upon success, the ID token is sent to your backend API
4. Backend returns user data and authentication tokens
5. User is logged in automatically

### Testing

1. Build and run the app on an Android device or emulator
2. Navigate to the Login screen
3. Tap "Sign in with Google" button
4. Select a Google account
5. App will authenticate with your backend

## 🍎 iOS Configuration (Future)

For iOS, you'll need to create a similar implementation in the `iosMain` source set using the Google Sign-In iOS SDK.

## 🔧 Troubleshooting

### Google Sign-In Button Disabled
- Make sure you're running on Android (iOS not yet implemented)
- Check that Google Play Services is installed on the device

### Sign-In Fails
- Verify the Client ID matches your Google Cloud Console configuration
- Ensure SHA-1/SHA-256 fingerprints are registered in Google Cloud Console
- Check logcat for detailed error messages

### Backend Errors
- Verify the API endpoint `http://52.20.167.4:5000/login-with-google` is accessible
- Check that the `auth: aaa` header is correct
- Ensure the backend can validate Google ID tokens

## 📝 Notes

- The implementation currently supports Android only
- For production, consider adding error handling UI feedback
- You may want to add a Google icon instead of the "G" text placeholder


