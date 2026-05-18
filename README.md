iKomyut Launcher

The iKomyut Launcher is an Android Kiosk application designed for POS (Point of Sale) Devices. It restricts device access to a specific set of whitelisted applications, ensuring security and system integrity.


Features

Total Lockdown: Disables system navigation and the status bar to prevent unauthorized access.

Custom App Grid: A simplified interface for users to launch approved applications.

Admin Settings: A password-protected menu for managing the app whitelist and toggling kiosk 
mode.

Real-time Monitoring: Built-in indicators for battery life, Wi-Fi status, and cellular data connectivity.


 	Prerequisites

 	•	Before installing, ensure you have the following on your computer and             		device:
 	•	Android Studio (latest version)

 	•	Visual Studio Code (VS Code)

 	•	ADB (Android Debug Bridge) configured in your system path

 	•	USB Cable for device connection

•	Developer Options and USB Debugging enabled on the POS device

 	•	Ensure no Google or email accounts are signed in on the device (required for Device Owner setup)


Detailed Installation Instructions

Follow these steps carefully. Missing a single line of code can prevent the Kiosk from locking correctly.

Step 1: Clone the Project in VS Code

Open Visual Studio Code on your computer.

Open a new terminal by going to Terminal > New Terminal in the top menu.

Copy and paste the following command to download the code from GitHub:

 	```bash
 	git clone https://github.com/IKOMYUT-OJT/ikomyut-android-launcher.git
 	```

Move into the project folder by typing:

 	```bash
 	cd ikomyut-android-launcher
 	```


Step 2: Launch Android Studio from VS Code

In the same VS Code terminal, type the following command to automatically launch the project in Android Studio:

 	```bash
 	studio .
 	```

Note: If the command above is not recognized, simply open Android Studio manually and select "Open" then navigate to the ikomyut-android-launcher folder.


Step 3: Sync and Build the Project

Once Android Studio opens, look at the bottom of the window for a progress bar. This is the "Gradle Sync" process where the computer downloads the necessary building blocks for the app.

Wait for the sync to finish completely.

Connect your POS device to your computer using a USB cable.


Step 4: Install the App to the POS

In Android Studio, look at the top toolbar and ensure your POS device name is visible in the dropdown menu.

Click the Green Play Button (Run) or press Shift + F10.

Watch your POS device screen. If a pop-up appears asking to "Allow USB Debugging," check Always allow and tap OK.

The app will now install and open automatically on the device.


Step 5: Grant Administrative Privileges (Device Owner)

Simply installing the app does not lock the device. You must "promote" the app to a Device Owner using the terminal.

In Android Studio, click the Terminal tab at the bottom of the screen.

Type the following command to ensure your device is recognized:

 	```bash
 	adb devices
 	```

Next, run this command exactly to lock the device into Kiosk mode:

 	```bash
 	adb shell dpm set-device-owner com.ikomyut.launcher/.DeviceAdminReceiver
 	```


Step 6: Set as Default Home Launcher

Press the physical Home button on your POS device.

When the system asks which application to use, select iKomyut Launcher.

Choose Always. The device is now fully locked.
Admin Access & Management

To add apps or exit the kiosk:

 	1. Tap the Settings icon on the main screen.

 	2. Enter the administrator password: ipick

 	3. From here, you can add new applications to the grid or unlock the kiosk for	maintenance.


Troubleshooting

"Already has accounts" error: This means there is a Google or Email account on the device. You must go to Settings > Accounts and remove all accounts before the Step 5 command will work.

"studio" is not recognized: This happens if Android Studio is not in your computer's environment path. If this occurs, just open Android Studio normally and use the "Open" menu.

Device not detected: Try a different USB cable or USB port, and ensure "USB Debugging" is still enabled in Developer Options.