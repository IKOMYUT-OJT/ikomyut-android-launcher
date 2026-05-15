Project Name: iKomyut Launcher
The iKomyut Launcher is a professional-grade Android Kiosk application designed for POS (Point of Sale) systems. It restricts device access to a specific set of whitelisted applications, ensuring security and system integrity.

Features
Total Lockdown: Disables system navigation and the status bar to prevent unauthorized access.

Custom App Grid: A simplified interface for users to launch approved applications.

Admin Settings: A password-protected menu for managing the app whitelist and toggling kiosk mode.

Real-time Monitoring: Built-in indicators for battery life, WiFi status, and cellular data connectivity.

Prerequisites
Before installing, ensure you have the following on your computer and device:

Android Studio (latest version recommended)

ADB (Android Debug Bridge) configured in your system path

USB Cable for device connection

Developer Options and USB Debugging enabled on the POS device

Ensure no Google or email accounts are signed in on the device (required for Device Owner setup)

Installation Instructions
Please follow these steps exactly to deploy the launcher on your machine.

Step 1: Open the Project
Launch Android Studio on your computer.

Click on File > Open and navigate to the project folder (ikomyut-android-launcher).

Wait for the "Gradle Sync" to complete. This ensures all project components are correctly loaded.

Step 2: Install the App
Connect your POS device to your computer via USB.

In Android Studio, ensure your device name appears in the top device selection dropdown.

Click the Green Play Button (Run) at the top or press Shift + F10.

On the POS device, if prompted to "Allow USB Debugging," select Always allow and click OK.

Step 3: Grant Administrative Privileges
Once the app is installed, you must give it "Device Owner" permissions to enable the lockdown features.

In Android Studio, click on the Terminal tab located at the bottom of the screen.

First, check if your device is connected by typing:
adb devices

Next, run the following command to set the app as the Device Owner:
adb shell dpm set-device-owner com.ikomyut.launcher/.DeviceAdminReceiver

Step 4: Set as Default Home Launcher
Press the Home button on your POS device.

A prompt will appear asking which application to use for the Home screen.

Select iKomyut Launcher and choose Always.

Admin Access
To manage the launcher or add/remove apps:

1. Tap the Settings icon on the main app grid.
2. Enter the administrator password: ipick
3. Use the menu to add applications, remove them, or unlock the kiosk mode.

Troubleshooting
Already has accounts error: If the terminal command fails with this error, you must go to Settings > Accounts on the device and remove all existing accounts before running the command again.

Device not found: Ensure the USB cable is secure and that USB Debugging is still enabled in the device settings.

Gradle Sync Failed: Ensure you have a stable internet connection for Android Studio to download the necessary dependencies.
