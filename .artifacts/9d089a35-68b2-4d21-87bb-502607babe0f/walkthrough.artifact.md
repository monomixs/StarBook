# Walkthrough - App Version Update to 1.2 BETA

I have updated the application version to "1.2 BETA" and deployed it to your physical device.

## Changes Made

### Gradle Configuration

#### [MODIFY] [gradle.properties](file:///C:/Users/wedle/StudioProjects/StarBook/gradle.properties)
- Added `com.starbook.versionName=1.2 BETA` to the project properties.

## Verification Results

### Manual Verification
- **Gradle Sync**: Successfully picked up the new version property.
- **Deployment**: The app was successfully built and deployed to your physical device (`adb-R5CX60TGL7J-AZtinh`).
- **Settings Screen**: The version "1.2 BETA" should now be visible in the "App version" row under Preferences.

> [!TIP]
> You can verify the version by navigating to **Preferences** and scrolling to the bottom to see the **App version** item.
