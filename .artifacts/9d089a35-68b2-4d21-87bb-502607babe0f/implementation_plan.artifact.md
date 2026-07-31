# Implementation Plan - Update App Version to 1.2 BETA

The user wants to change the app version to "1.2 BETA" and ensure it is displayed correctly in the app settings.

## Proposed Changes

### Gradle Configuration

#### [MODIFY] [gradle.properties](file:///C:/Users/wedle/StudioProjects/StarBook/gradle.properties)
- Add `com.starbook.versionName=1.2 BETA` to set the application version name.

### Verification Plan

#### Automated Tests
- No new automated tests are required as the versioning logic is standard AGP/Gradle behavior.
- I will check if `app/build.gradle.kts` correctly picks up this property.

#### Manual Verification
- After applying the change, the `BuildConfig.VERSION_NAME` will be generated with "1.2 BETA".
- The `Settings` screen, which uses `AppInfoProvider.versionName` (mapped to `BuildConfig.VERSION_NAME`), will automatically display "1.2 BETA" in its `AppVersion` row.
