# Small Circle - Local Marketplace Android App

Small Circle is a native Android marketplace application that connects local buyers and sellers. Users can discover items based on location, chat with sellers, manage listings, and more.

## Screenshots
<div align="center">
<p float="left">
  <img width="250" alt="Cover" src="https://github.com/user-attachments/assets/9f00da30-b64f-47c0-a8da-e72f9373bf6b" />
  <img width="250" alt="Slide 2" src="https://github.com/user-attachments/assets/340db961-8b5f-4274-8ce4-261fbce006e6" />
  <img width="250" alt="Slide FlowChart" src="https://github.com/user-attachments/assets/b03603d8-5993-4cda-82b8-e64eb8febaca" />
</p>
<p float="left">
  <img width="250" alt="Slide 3" src="https://github.com/user-attachments/assets/7b27c5de-5c19-43db-bbc0-45d1e6add3ad" />
  <img width="250" alt="Slide 4" src="https://github.com/user-attachments/assets/b1c2dd0e-571a-4e07-8766-0cbe62724ba6" />
  <img width="250" alt="Slide 5" src="https://github.com/user-attachments/assets/c41903a6-c2ed-461b-966c-06935e8b423f" />
</p>
<p float="left">
  <img width="250" alt="Slide 6" src="https://github.com/user-attachments/assets/4002bfc8-461e-4dc7-9401-7fa85d0f7f03" />
  <img width="250" alt="Slide 7" src="https://github.com/user-attachments/assets/f7887d7a-e6e8-4ab8-981f-0a3a414b10e8" />
  <img width="250" alt="Slide 8" src="https://github.com/user-attachments/assets/4fd2efd1-53fe-4488-a139-ede93c9533e8" />
</p>
<p float="left">
  <img width="250" alt="Slide 9" src="https://github.com/user-attachments/assets/10c47374-9138-43f6-a9e8-365e8571192f" />
  <img width="250" alt="Slide 10" src="https://github.com/user-attachments/assets/0a69185e-3b12-4431-a57c-ff53908704fa" />
  <img width="250" alt="Slide 11" src="https://github.com/user-attachments/assets/7cd00800-3a0c-4efc-bb37-aab84f1d0058" />
</p>
<p float="left">
  <img width="250" alt="Ending" src="https://github.com/user-attachments/assets/5f40905b-1b5e-4076-a111-467472e2bb35" />
</p>
</div>

## Features

- **Authentication**
  - Multiple sign-in options (Google, Email, Phone OTP)
  - Account verification
  - Profile management

- **Marketplace Features**
  - Location-based item discovery using Google Maps
  - Multiple image upload for listings
  - Real-time chat with image sharing
  - Seller profiles
  - Product management (edit, delete, mark as sold)
  - Account verification system

## Prerequisites

Before running the project, make sure you have:

- Android Studio (latest version recommended)
- JDK 8 or higher
- Android device or emulator running Android 6.0 (API 23) or higher
- Google Cloud account
- Firebase account

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/small-circle.git
cd small-circle
```

### 2. Firebase Setup

1. Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Enable the following services:
   - Authentication (Enable Google, Email/Password, and Phone providers)
   - Realtime Database
   - Storage
   - Cloud Messaging
   - Crashlytics

3. Add your Android app to Firebase:
   - Package name: `com.org.smallcircle`
   - Download `google-services.json`
   - Place `google-services.json` in the app/ directory

4. Firebase Authentication Setup:
   - Go to Authentication → Sign-in methods
   - Enable Google Sign-in
   - Enable Email/Password
   - Enable Phone Number
   - Add your SHA-1 and SHA-256 fingerprints (required for Google Sign-in)

5. Firebase Database Rules:
   ```json
   {
     "rules": {
       ".read": "auth != null",
       ".write": "auth != null"
     }
   }
   ```

6. Firebase Storage Rules:
   ```
   rules_version = '2';
   service firebase.storage {
     match /b/{bucket}/o {
       match /{allPaths=**} {
         allow read, write: if request.auth != null;
       }
     }
   }
   ```

### 3. Google Maps Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select your Firebase project
3. Enable the following APIs:
   - Maps SDK for Android
   - Places API
4. Create credentials (API key)
5. Add restrictions to your API key:
   - Application restrictions: Android apps
   - API restrictions: Maps SDK for Android and Places API

### 4. Configure Local Properties

1. Create or open `local.properties` in the project root directory
2. Add your Google Maps API key:
   ```
   MAPS_API_KEY=your_maps_api_key_here
   ```

### 5. Building the Project

1. Open the project in Android Studio
2. Sync project with Gradle files
3. Build the project
4. Run on your device or emulator

## Common Issues & Troubleshooting

1. Google Sign-in not working:
   - Verify SHA-1 and SHA-256 fingerprints in Firebase Console
   - Ensure `google-services.json` is up to date

2. Maps not loading:
   - Check if API key is correctly added in local.properties
   - Verify API restrictions in Google Cloud Console

3. Firebase Connection Issues:
   - Ensure `google-services.json` is in the correct location
   - Check internet connectivity
   - Verify Firebase project settings

## Libraries Used

- Firebase (Analytics, Auth, Database, Storage, Crashlytics)
- Google Play Services (Maps, Places, Auth)
- Country Code Picker (CCP)
- Glide for image loading
- Rounded ImageView
- Material Design components
- Lottie for animations
- Shimmer Effect
- AndroidX Browser
- Play Integrity

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## Let's Connect!

<div align="left">
  <a href="https://www.linkedin.com/in/auriorajaa/">
    <img src="https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white" alt="LinkedIn Badge"/>
  </a>
</div>

Created with ❤️ by Aurio Rajaa
