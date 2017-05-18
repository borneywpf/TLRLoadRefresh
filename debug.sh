#!/usr/bin/env bash
./gradlew app:assembleDebug &&
adb install -r -d app/build/outputs/apk/app-debug.apk  &&
adb shell am start com.think.uiloader
