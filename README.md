# AES67 Player for Android

The AES67 Player is an Android application used to play AES67 streams.

The application is licensed under [GNU GPL](https://www.gnu.org/licenses/gpl-3.0.en.html).

The application can be built and installed on your device by using Android Studio.
You can also download the last released APK from:

The application requires the AES67 Linux Daemon to work and playout streams.

The AES67 Linux Daemon is an open source Linux application and you can find  it at: https://github.com/bondagit/aes67-linux-daemon

# How to setup the Daemon and use the AES67 Player

1. Install and configure the AES67 Linux daemon on a Linux host and attach it to your AES67 LAN.
2. Using the AES67 Linux daemon WebUI, in the Config tab, enable the Streamer feature.
3. Attach one or more AES67 Sources to your LAN.
4. Using the AES67 Linux daemon WebUI, in the Sinks tab, create one of more Sinks specifying the AES67 Sources discovered by the daemon.
5. Open the AES67 Player application and enter the URL of the AES67 Linux daemon WebUI.
6. When the daemon appears in the daemon's list click on the go icon on the right side to connect to it.
7. On the Sinks page start or stop a Sink playback by using the play icon on the right.
8. Expand a Sink info by clicking on the expand icon on the left.
