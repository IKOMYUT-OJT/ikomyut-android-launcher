# iKomyut Kiosk Launcher 🚀

A professional-grade Android Kiosk Launcher designed for POS (Point of Sale) systems. This launcher locks the device into a restricted environment, allowing only whitelisted applications to run.

## ✨ Features
- **Total Lockdown**: Disables system navigation and status bar when locked.
- **Custom App Grid**: Easy-to-use interface for launching approved applications.
- **Admin Settings**: Password-protected menu to manage apps and toggle kiosk mode.
- **System Monitors**: Real-time indicators for Battery, WiFi, and Cellular Data.
- **Auto-Boot**: Automatically launches into Kiosk mode on device startup.

---

## 🛠️ Installation Guide

Follow these steps to install the launcher on your POS device:

### 1. Prerequisites
- **Android Studio** installed on your computer.
- **ADB (Android Debug Bridge)** configured in your system path.
- **Developer Options** and **USB Debugging** enabled on your POS device.
- Ensure **no other Google accounts** are signed in on the device (required for Device Owner setup).

### 2. Clone the Repository
```bash
git clone https://github.com/IKOMYUT-OJT/ikomyut-android-launcher.git
cd ikomyut-android-launcher
```

### 3. Build & Install
1. Open the project in **Android Studio**.
2. Connect your POS device via USB.
3. Build the APK and install it to your device (Shift + F10 or use the 'Run' button).

### 4. Set as Device Owner (CRITICAL)
For the Kiosk mode to fully lock the device (disable home button, recent apps, etc.), you must set the app as the **Device Owner** via ADB:

```powershell
adb shell dpm set-device-owner com.ikomyut.launcher/.DeviceAdminReceiver
```

> [!IMPORTANT]
> If you get an error saying "already has accounts", you must remove all accounts (Google, etc.) from the device settings before running this command.

### 5. Set as Default Home
1. Press the **Home** button on your POS.
2. Select **iKomyut Launcher** and choose **"Always"**.

---

## 🔐 Admin Access
To manage applications or exit the Kiosk:
1. Tap the **Settings** card in the app grid.
2. Enter the Admin Password: `ipick`
3. From the Admin Menu, you can:
   - **Add Application**: Choose from installed apps to add to the launcher.
   - **Remove Application**: Remove apps from the grid.
   - **Unlock Kiosk Mode**: Temporarily disable the lock to access system settings.

---

## 🏗️ Technical Details
- **Package Name**: `com.ikomyut.launcher`
- **Admin Receiver**: `.DeviceAdminReceiver`
- **Minimum SDK**: Android 7.1.2 (Tested on Telpo POS)

---

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
