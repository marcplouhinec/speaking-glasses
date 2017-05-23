# Speaking Glasses

## Introduction
This application targets the [Recon Jet smart glasses](https://www.reconinstruments.com/products/jet/) that runs Android OS.
The goal is to help visually impaired people to get a better idea of what is in front of them.

Technically, the application works by taking a photo of the scene in front of the user, send it to [Cloud Sight](https://cloudsight.ai/)
and using Text-to-Speech technology to describe the scene.

Here is a demo video of the application:
[![Demo Video](https://img.youtube.com/vi/bGrgviXpvKw/0.jpg)](https://www.youtube.com/watch?v=bGrgviXpvKw)

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

The application currently works in English and French.

The third step is to subscribe to [Cloud Sight](https://cloudsight.ai/) and create a project with a "Whole Image" response type.
If it is your first project, this website will give you some free credits for starting.
You will then receive an *API Key* that you need to copy in the file `/app/src/main/assets/application.properties`.

The last step is to open this project with [Android Studio](https://developer.android.com/studio/index.html), plug the 
[Recon Jet ](https://www.reconinstruments.com/products/jet/) and run the app!
