@echo off
setlocal enabledelayedexpansion
title Musify - Emulator Launcher ^& Test Runner
color 0A

echo ========================================================
echo        Musify - Automated Emulator Test Runner
echo        AVD: Medium_Phone (API 36)
echo ========================================================
echo.

set "SDK_DIR=C:\Users\adity\AppData\Local\Android\Sdk"
set "EMULATOR_EXE=%SDK_DIR%\emulator\emulator.exe"
set "ADB_EXE=%SDK_DIR%\platform-tools\adb.exe"
set "AVD_NAME=Medium_Phone_API_36.1"
set "APK_PATH=%~dp0app\build\outputs\apk\debug\app-debug.apk"
if not exist "%APK_PATH%" (
    set "APK_PATH=%~dp0Musify.apk"
)

if not exist "%EMULATOR_EXE%" (
    echo [ERROR] Android Emulator not found at: %EMULATOR_EXE%
    pause
    exit /b 1
)

if not exist "%ADB_EXE%" (
    echo [ERROR] ADB not found at: %ADB_EXE%
    pause
    exit /b 1
)

set "ANDROID_HOME=%SDK_DIR%"
set "ANDROID_SDK_ROOT=%SDK_DIR%"
set "PATH=%SDK_DIR%\emulator;%SDK_DIR%\platform-tools;%PATH%"

echo [1/5] Checking if emulator is already running...
"%ADB_EXE%" get-state >nul 2>&1
if %errorlevel% neq 0 (
    echo Starting emulator '%AVD_NAME%'...
    del /f /q "%USERPROFILE%\.android\avd\Medium_Phone.avd\*.lock" >nul 2>&1
    rd /s /q "%USERPROFILE%\.android\avd\Medium_Phone.avd\hardware-qemu.ini.lock" >nul 2>&1
    pushd "%SDK_DIR%\emulator"
    start "Medium Phone - Musify Emulator" emulator.exe -avd "%AVD_NAME%"
    popd
    echo Waiting for emulator device to register...
    "%ADB_EXE%" wait-for-device
) else (
    echo Emulator is already running and connected.
)

echo.
echo [2/5] Waiting for Android system boot to complete...
:wait_boot
for /f "tokens=*" %%a in ('"%ADB_EXE%" shell getprop sys.boot_completed 2^>nul') do set "BOOT_STATUS=%%a"
if not "!BOOT_STATUS!"=="1" (
    echo Boot in progress... waiting 3 seconds...
    timeout /t 3 >nul
    goto wait_boot
)
echo Android system booted successfully!

echo.
echo [3/5] Checking APK...
if not exist "%APK_PATH%" (
    echo APK not found at %APK_PATH%.
    echo Building debug APK using Gradle...
    call "%~dp0gradlew.bat" assembleDebug
    set "APK_PATH=%~dp0app\build\outputs\apk\debug\app-debug.apk"
)
echo Using APK: %APK_PATH%

echo.
echo [4/5] Installing Musify on emulator...
"%ADB_EXE%" install -r "%APK_PATH%"
if %errorlevel% neq 0 (
    echo [WARNING] Install had an issue, trying with -d (downgrade) flag...
    "%ADB_EXE%" install -r -d "%APK_PATH%"
)

echo [5/5] Launching Musify (MainActivity)...
"%ADB_EXE%" shell am start -n com.musify.browser.debug/com.musify.browser.MainActivity >nul 2>&1
if %errorlevel% neq 0 (
    "%ADB_EXE%" shell am start -n com.musify.browser/com.musify.browser.MainActivity
)

echo.
echo ========================================================
echo  Musify is now running on your emulator!
echo  - Open 'open.spotify.com'
echo  - Switch to Desktop Mode via toolbar (Desktop / Mobile)
echo  - View diagnostics via 'Media & Playback Recovery'
echo ========================================================
echo.
pause
