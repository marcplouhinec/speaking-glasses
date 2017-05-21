# Speaking Glasses

## Introduction
This application targets the [Recon Jet smart glasses](https://www.reconinstruments.com/products/jet/), running Android OS.
The goal is to help a visually impaired user to get a better idea of what is in front of him.

Technically, the application works by taking a photo of the scene in front of the user, send it to [Cloud Sight](https://cloudsight.ai/)
and using Text-to-Speech technologies to describe the scene.

## Preparation and build
The first step is to download the SDK from the [Intel Developer Zone](https://software.intel.com/en-us/recon).
Please follow this documentation in order to setup [adb](https://developer.android.com/studio/command-line/adb.html).

The second step is to setup with adb a Text-to-Speech engine, because you may have none installed by default.
To check if you have a TTS installed, plug the glasses on the computer and open a terminal:

    adb shell
    pm list packages -f

You don't need to setup a TTS if you already have packages like:
* package:/data/app/com.google.android.tts-1.apk=com.google.android.tts
* package:/system/app/PicoTts.apk=com.svox.pico

If not you may need to find an APK on a website like [http://www.apkmirror.com](http://www.apkmirror.com) and setup it on your device with the following command:

    adb install your.apk

The application currently works in english and french.

The third step is to subscribe to [Cloud Sight](https://cloudsight.ai/) and create a project with a "Whole Image" response type.
If it is your first project, this website will give you some free credits for starting.
You will then receive an *API Key* that you need to copy in the file `/app/src/main/assets/application.properties`.
