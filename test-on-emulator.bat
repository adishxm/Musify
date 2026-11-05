@echo off
setlocal enabledelayedexpansion
title Musify - Emulator Launcher ^& Test Runner
color 0A

echo ========================================================
echo        Musify - Automated Emulator Test Runner
echo        AVD: Medium_Phone (API 36)
echo        Privacy Shields, Ad Blocking ^& Media Validation
echo ========================================================
echo.

set "SDK_DIR=C:\Users\adity\AppData\Local\Android\Sdk"
set "EMULATOR_EXE=%SDK_DIR%\emulator\emulator.exe"
set "ADB_EXE=%SDK_DIR%\platform-tools\adb.exe"
set "AVD_NAME=Medium_Phone_API_36.1"

set "ANDROID_HOME=%SDK_DIR%"
set "ANDROID_SDK_ROOT=%SDK_DIR%"
set "PATH=%SDK_DIR%\emulator;%SDK_DIR%\platform-tools;%PATH%"

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

:: Select APK to test
echo Select APK variant to install:
echo   [1] Musify.apk (Universal Release Build - 9.60 MB, Default)
echo   [2] Musify-ARM64.apk (Full GeckoView Engine Build - 85.56 MB)
echo   [3] Musify-Lite.apk (Featherweight System WebView Build - 9.60 MB)
echo   [4] app-debug.apk (Debug Build with Logcat Instrumentation)
echo.
set /p "APK_CHOICE=Enter choice [1-4, default 1]: "
if "%APK_CHOICE%"=="" set "APK_CHOICE=1"

if "%APK_CHOICE%"=="1" set "APK_PATH=%~dp0Musify.apk"
if "%APK_CHOICE%"=="2" set "APK_PATH=%~dp0Musify-ARM64.apk"
if "%APK_CHOICE%"=="3" set "APK_PATH=%~dp0Musify-Lite.apk"
if "%APK_CHOICE%"=="4" (
    set "APK_PATH=%~dp0app\build\outputs\apk\debug\app-debug.apk"
    if not exist "!APK_PATH!" (
        echo Building debug APK...
        call "%~dp0gradlew.bat" assembleDebug
    )
)

if not exist "%APK_PATH%" (
    echo [ERROR] Selected APK not found at: %APK_PATH%
    pause
    exit /b 1
)

echo.
echo [1/5] Checking Android Emulator status...
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
echo [2/5] Waiting for Android system boot completion...
:wait_boot
for /f "tokens=*" %%a in ('"%ADB_EXE%" shell getprop sys.boot_completed 2^>nul') do set "BOOT_STATUS=%%a"
if not "!BOOT_STATUS!"=="1" (
    echo Boot in progress... waiting 3 seconds...
    timeout /t 3 >nul
    goto wait_boot
)
echo Android system booted successfully!

echo.
echo [3/5] Installing selected APK: %APK_PATH%...
"%ADB_EXE%" install -r -d "%APK_PATH%"
if %errorlevel% neq 0 (
    echo [ERROR] APK installation failed.
    pause
    exit /b 1
)

echo.
echo [4/5] Launching Musify (MainActivity)...
"%ADB_EXE%" shell am start -n com.musify.browser/com.musify.browser.MainActivity >nul 2>&1
if %errorlevel% neq 0 (
    "%ADB_EXE%" shell am start -n com.musify.browser.debug/com.musify.browser.MainActivity
)

echo.
echo [5/5] Musify launched successfully!
echo ========================================================
echo  Ready to verify Brave-Aligned Shields:
echo.
echo  Quick Test Shortcuts:
echo    [S] Open Spotify Web Player (open.spotify.com)
echo    [Y] Open YouTube (m.youtube.com)
echo    [M] Open YouTube Music (music.youtube.com)
echo    [B] Open d3ward Adblock Benchmark (d3ward.github.io/toolz/adblock.html)
echo    [C] Open CanYouBlockIt Test (canyoublockit.com/testing/)
echo    [D] Open Bounce Tracking Redirect Test (dpbolvw.net)
echo    [Q] Quit runner
echo ========================================================
echo.

:menu_loop
set /p "TEST_CMD=Choose website test [S/Y/M/B/C/D/Q]: "
if /i "!TEST_CMD!"=="S" (
    echo Opening Spotify Web Player...
    "%ADB_EXE%" shell am start -a android.intent.action.VIEW -d "https://open.spotify.com" com.musify.browser
    goto menu_loop
)
if /i "!TEST_CMD!"=="Y" (
    echo Opening YouTube...
    "%ADB_EXE%" shell am start -a android.intent.action.VIEW -d "https://m.youtube.com" com.musify.browser
    goto menu_loop
)
if /i "!TEST_CMD!"=="M" (
    echo Opening YouTube Music...
    "%ADB_EXE%" shell am start -a android.intent.action.VIEW -d "https://music.youtube.com" com.musify.browser
    goto menu_loop
)
if /i "!TEST_CMD!"=="B" (
    echo Opening d3ward Adblock Benchmark...
    "%ADB_EXE%" shell am start -a android.intent.action.VIEW -d "https://d3ward.github.io/toolz/adblock.html" com.musify.browser
    goto menu_loop
)
if /i "!TEST_CMD!"=="C" (
    echo Opening CanYouBlockIt Test...
    "%ADB_EXE%" shell am start -a android.intent.action.VIEW -d "https://canyoublockit.com/testing/" com.musify.browser
    goto menu_loop
)
if /i "!TEST_CMD!"=="D" (
    echo Opening Bounce Tracking Redirect Test...
    "%ADB_EXE%" shell am start -a android.intent.action.VIEW -d "https://dpbolvw.net/click-12345?url=https%%3A%%2F%%2Fmusify.app%%2Fdownload%%3Ffbclid%%3Dtracking_token_123" com.musify.browser
    goto menu_loop
)
if /i "!TEST_CMD!"=="Q" (
    echo Exiting test runner.
    exit /b 0
)
goto menu_loop
