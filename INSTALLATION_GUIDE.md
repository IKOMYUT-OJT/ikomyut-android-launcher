# [Learning Module] Deploying the iKomyut Android Kiosk Launcher

**Subject:** Enterprise Android Deployment & Kiosk Management  
**Instructor:** Antigravity (Assistant)  
**Target Audience:** Students & New Developers  

---

## 👨‍🏫 Overview
Welcome, class! Today, we are going to learn how to transform a standard Android POS device into a professional Kiosk system. By the end of this tutorial, you will be able to install the **iKomyut Launcher** and lock the device so it only runs approved applications. 

Please follow these steps carefully. In software development, missing even one small command can prevent the entire system from working!

---

## 📋 Phase 1: Preparation (The Toolkit)
Before we start, ensure you have these "tools" ready on your workstation:
1. **Android Studio**: The environment where we build the app.
2. **USB Cable**: To connect your POS device to your computer.
3. **Developer Access**: Your POS device must have "USB Debugging" turned ON (found in Settings > Developer Options).

---

## 📂 Phase 2: Opening the Project
1. Launch **Android Studio**.
2. Click on **File > Open**.
3. Navigate to the folder where you cloned the repository (`ikomyut-android-launcher`).
4. Wait for the green progress bar at the bottom to finish. This is called "Gradle Syncing"—it’s the computer's way of organizing the project files.

---

## 🛠️ Phase 3: Building and Installing the App
Now, let's put the app onto the device.
1. Connect your POS device to your computer via USB.
2. Look at the top toolbar in Android Studio. Ensure your device name is visible in the dropdown menu.
3. Click the **Green Play Button** (▶️) or press `Shift + F10`.
4. **Watch the Device**: A prompt might appear asking "Allow USB Debugging?". Click **Always Allow** and **OK**.
5. Once the installation is finished, the iKomyut screen will appear on your POS.

---

## 💻 Phase 4: Using the Terminal (The Master Command)
This is the most important part of our lesson. Simply installing the app isn't enough to "lock" the device. We must give the app "Device Owner" permissions using a terminal command.

1. In Android Studio, look at the very bottom left and click on the **Terminal** tab.
2. Type the following command to make sure your device is recognized:
   ```bash
   adb devices
   ```
   *(You should see a serial number followed by the word "device".)*

3. Now, type this "Master Command" exactly as shown and press **Enter**:
   ```powershell
   adb shell dpm set-device-owner com.ikomyut.launcher/.DeviceAdminReceiver
   ```

> [!NOTE] 
> **Teacher's Tip:** If you see an error saying "Already has accounts," you must go to the POS Settings > Accounts and remove any Google or email accounts first. Then try the command again.

---

## 🏠 Phase 5: Setting the Default Launcher
1. Press the **Home Button** on your POS device.
2. The system will ask which app to use for the Home screen.
3. Select **iKomyut Launcher** and tap **"Always"**.

---

## 🔐 Phase 6: Admin Operations (How to Manage the App)
Now that the app is running, you need to know how to manage it.
1. On the main screen, find the icon labeled **Settings**.
2. Click it. A box will ask for a password.
3. Type the secret password: `ipick`
4. **The Admin Menu will appear!** From here, you can:
   - **Add Application**: Click this to add a new app to the student/user view.
   - **Remove Application**: Click this to hide an app.
   - **Unlock Kiosk**: Use this if you need to go back to the original Android settings.

---

## 🎓 Final Summary
Congratulations! You have successfully deployed a professional Kiosk Launcher. You have learned how to:
- Open and sync a project in Android Studio.
- Deploy an APK to a physical device.
- Use the ADB Terminal to grant administrative privileges.
- Manage a locked environment using an Admin Password.

**Class Dismissed!** If you have any questions, feel free to ask.
