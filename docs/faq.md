### Why doesn't StarBook support the xyz Media Format?

StarBook relies on the media formats that are natively supported by the Android platform.

You can review the currently supported file extensions [here](https://developer.android.com/media/media3/exoplayer/supported-formats).
If a file that should be supported is not displayed, it is most likely either corrupted or incompatible with your Android version.

### Why isn’t feature xyz available in the app?

I adhere to a core design principle of minimalism. As such, the app will only include settings and UI components that are absolutely
essential.

### Which StarBook version should I use on older Android?

!!! tip

    To check your API level, go to **Settings » About » Android version** on your device.

If you’re running an Android release that’s not supported by the latest StarBook build, pick the version below that matches your OS/API level:

| Android Version | API Level (SDK) | StarBook Version                                                           |
|-----------------|-----------------|-------------------------------------------------------------------------|
| Android 9+      | 28+             | Supported in the latest version 🎉                                      |
| Android 8.1     | 27              | [8.2.4‑2](https://github.com/starbook-org/starbook/releases/tag/8.2.4-2) |
| Android 8       | 26              | [8.2.4‑2](https://github.com/starbook-org/starbook/releases/tag/8.2.4-2) |
| Android 7.1     | 25              | [8.2.4‑2](https://github.com/starbook-org/starbook/releases/tag/8.2.4-2) |
| Android 7.0     | 24              | [6.0.10](https://github.com/starbook-org/starbook/releases/tag/6.0.10)   |

### How do I resume playback after the sleep timer stops?

Once the sleep timer elapses, StarBook pauses playback (after a brief fade-out). To keep listening, you have two options:

- **Shake to resume**: Shake your device within 30 seconds of pause to restart playback.
- **Open to resume**: Open the App and simply press on play again

!!! warning

    On some devices (e.g. Samsung S20fe) shake-to-resume may not work reliably.
