# flutter_acs_card_reader

A Flutter plugin to read cards on Advanced Card Systems readers through BLE.

## Android setup

You need to add the Bluetooth permission for Android. Here's how you can do it:

Navigate to the Android directory of your Flutter project.

Open the AndroidManifest.xml file located in the android/app/src/main directory.

Add the following line inside the <manifest> tag, just before the <application> tag:

<uses-permission android:name="android.permission.BLUETOOTH" />

This line declares the BLUETOOTH permission, allowing your app to access Bluetooth functionality on Android.

Optionally, if you requires additional Bluetooth permissions, you can include them as well. For example:

<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

These permissions allow your app to perform administrative Bluetooth operations and access fine location information, which can be useful for some Bluetooth functionalities.

Save the AndroidManifest.xml file.
