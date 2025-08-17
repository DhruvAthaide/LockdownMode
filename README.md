# Lockdown Mode

Lockdown Mode is an Android application designed to enhance device security by temporarily restricting access to sensitive features and apps. It provides a customizable lockdown experience, allowing users to disable the camera, microphone, GPS, clear recent apps, kill background processes, switch to a minimal launcher, set a delayed activation, and notify an emergency contact via SMS when lockdown is activated. The app requires a PIN to exit lockdown mode, ensuring secure control.

## Features

- **Disable Camera**: Prevents access to all cameras on the device until lockdown is lifted.
- **Disable Microphone**: Suspends apps with `RECORD_AUDIO` permission, effectively muting microphones.
- **Disable GPS**: Disables GPS and location services, with runtime permission handling.
- **Clear Recent Apps**: Clears the recent apps list upon lockdown activation.
- **Kill Background Processes**: Terminates background processes to enhance security.
- **Minimal Launcher**: Switches to a basic launcher (`LockdownLauncherActivity`), preventing access to the home screen or other apps until the correct PIN is entered.
- **Delayed Activation**: Configurable delay (0, 3, 5, or 10 seconds) before lockdown activates.
- **Emergency Contact**: Sends an SMS to a selected contact when lockdown is activated, with runtime permission handling.
- **PIN Protection**: Requires a user-defined PIN (minimum 4 digits) to exit lockdown mode, securely stored using encrypted SharedPreferences.
- **Intuitive UI**: Provides a clean interface with dynamic status indicators on the home screen and toggle switches in settings for feature configuration.

## Screenshots


## Prerequisites

- **Android Device**: Android 6.0 (API 23) or higher.
- **Permissions**:
    - `BIND_DEVICE_ADMIN`: For camera and microphone control.
    - `BIND_ACCESSIBILITY_SERVICE`: For clearing recent apps and enforcing minimal launcher.
    - `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION`: For GPS control.
    - `SEND_SMS`: For emergency contact notifications.
    - `READ_CONTACTS`: For selecting emergency contacts.
    - `KILL_BACKGROUND_PROCESSES`: For terminating background processes.
    - `QUERY_ALL_PACKAGES`: For suspending apps with microphone permissions.
- **Dependencies**:
    - AndroidX libraries
    - Material Components
    - Security Crypto for encrypted SharedPreferences

## Setup

### 1. Clone the Repository

```bash
git clone https://github.com/DhruvAthaide/LockdownMode.git
cd lockdown-mode
```

### 2. Open in Android Studio

- Open Android Studio.
- Select **Open an existing project** and choose the cloned `lockdown-mode` directory.
- Sync the project with Gradle.

### 3. Configure Dependencies

Ensure the `app/build.gradle` file includes the following dependencies:

```gradle
dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.core:core:1.10.1'
    implementation 'com.google.android.material:material:1.9.0'
    implementation 'androidx.security:security-crypto:1.0.0'
}
```

### 4. Add Resources

Ensure the following drawable resources are in `app/src/main/res/drawable`:

- `ic_house.xml`
- `ic_shield.xml`
- `ic_gear.xml`
- `ic_arrow_left.xml`
- `ic_caret_right.xml`
- `ic_x.xml`
- `ic_app_window.xml`
- `ic_map_pin.xml`
- `ic_microphone.xml`
- `ic_camera.xml`

Ensure the following XML resources are in `app/src/main/res/xml`:

- `device_admin_receiver.xml`
- `accessibility_service_config.xml`

Ensure `app/src/main/res/values/styles.xml` defines `Theme.LockdownMode`.

### 5. Build and Run

- Connect an Android device or start an emulator (API 23 or higher).
- Build and run the app from Android Studio.

### 6. Enable Permissions

- **Device Administrator**: Grant device admin permissions when prompted to enable camera and microphone restrictions.
- **Accessibility Service**: Enable the `LockdownAccessibilityService` in Android settings to support clearing recent apps and enforcing the minimal launcher.
- **Runtime Permissions**: Grant `SEND_SMS`, `READ_CONTACTS`, `ACCESS_FINE_LOCATION`, and `ACCESS_COARSE_LOCATION` when prompted.

## Usage

### Launch the App

- Open the app to access the home screen (`MainActivity`).
- If no PIN is set, you'll be prompted to create a 4-digit PIN.

### Configure Settings

- Navigate to the settings screen (`LockdownSettingsActivity`) via the gear icon or shield icon in the bottom navigation.
- Toggle features (camera, microphone, GPS, recent apps, background processes, minimal launcher).
- Set a delay for lockdown activation (0, 3, 5, or 10 seconds).
- Select an emergency contact from your contacts list.

### Activate Lockdown

- On the home screen, tap "Activate Lockdown".
- The app will delay activation based on the configured delay, then apply the selected restrictions.
- If an emergency contact is set, an SMS will be sent (requires `SEND_SMS` permission).

### Exit Lockdown

- In lockdown mode, you'll be directed to `LockdownLauncherActivity`.
- Tap the exit button and enter your PIN to deactivate lockdown.
- The minimal launcher prevents exiting via back, home, or recents buttons unless the correct PIN is entered.

### Navigation

- **Home Icon**: Navigates to the home screen (`MainActivity`).
- **Shield Icon**: Navigates to the settings screen (`LockdownSettingsActivity`).
- Icons are highlighted (white) when their respective page is active.

## Project Structure

### Java Files

- `MainActivity.java`: Home screen with lockdown activation and status display.
- `LockdownSettingsActivity.java`: Settings screen for configuring lockdown features.
- `LockdownLauncherActivity.java`: Minimal launcher during lockdown mode.
- `LockdownRepository.java`: Handles lockdown activation and deactivation logic.
- `LockdownAccessibilityService.java`: Manages clearing recent apps and enforcing minimal launcher.
- `PermissionUtils.java`: Handles location service permissions.
- `SecurePrefs.java`: Manages encrypted storage of settings and PIN.
- `LockdownDeviceAdminReceiver.java`: Device admin receiver for camera and microphone control.
- `LockdownViewModel.java`: ViewModel for managing lockdown state.
- `Injector.java`: Dependency injection utility.
- `SecurityUtils.java`: PIN hashing and salt generation.
- `App.java`: Application class for initialization.

### XML Layouts

- `activity_main.xml`: Home screen layout with status indicators and navigation.
- `activity_lockdownsettings.xml`: Settings screen with toggles and navigation.
- `activity_lockdown_active.xml`: Lockdown mode screen with exit button.

### Resources

- `res/drawable/`: Icons for UI elements.
- `res/xml/`: Device admin and accessibility service configurations.
- `res/values/styles.xml`: App theme.

## Contributing

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/your-feature`).
3. Commit your changes (`git commit -m "Add your feature"`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Open a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact

For issues or feature requests, please open an issue on the GitHub repository.

---

**Built with security and privacy in mind. Stay safe with Lockdown Mode!**