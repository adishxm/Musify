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

set "SCRIPT_DIR=%~dp0"
if defined LOCALAPPDATA (
    set "SDK_DIR=%LOCALAPPDATA%\Android\Sdk"
) else (
    set "SDK_DIR=C:\Users\adity\AppData\Local\Android\Sdk"
)
if not exist "%SDK_DIR%" set "SDK_DIR=C:\Users\adity\AppData\Local\Android\Sdk"
set "EMULATOR_EXE=%SDK_DIR%\emulator\emulator.exe"
set "ADB_EXE=%SDK_DIR%\platform-tools\adb.exe"

set "AVD_NAME=Medium_Phone_API_36.1"
if exist "%USERPROFILE%\.android\avd\Medium_Phone_API_36.1.ini" (
    set "AVD_NAME=Medium_Phone_API_36.1"
) else if exist "%USERPROFILE%\.android\avd\Medium_Phone.ini" (
    set "AVD_NAME=Medium_Phone"
)

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
echo   [1] Musify.apk (Universal Release Build - 10.25 MB, Default)
echo   [2] Musify-ARM64.apk (Full GeckoView Engine Build - 89.90 MB)
echo   [3] Musify-Lite.apk (Featherweight System WebView Build - 10.25 MB)
echo   [4] app-debug.apk (Debug Build with Logcat Instrumentation)
echo   [5] Wipe AVD Userdata ^& Fresh Cold Boot (Fix disk or snapshot issues)
echo.
set /p "APK_CHOICE=Enter choice [1-5, default 1]: "
if "!APK_CHOICE!"=="" set "APK_CHOICE=1"
for /f "tokens=1" %%x in ("!APK_CHOICE!") do set "APK_CHOICE=%%x"
if not "!APK_CHOICE!"=="2" if not "!APK_CHOICE!"=="3" if not "!APK_CHOICE!"=="4" if not "!APK_CHOICE!"=="5" set "APK_CHOICE=1"

if "!APK_CHOICE!"=="5" (
    echo.
    echo [ACTION] Wiping emulator userdata and restarting clean...
    "%ADB_EXE%" emu kill >nul 2>&1
    ping -n 3 127.0.0.1 >nul
    del /f /q /a "%USERPROFILE%\.android\avd\Medium_Phone.avd\*.lock" >nul 2>&1
    rd /s /q "%USERPROFILE%\.android\avd\Medium_Phone.avd\hardware-qemu.ini.lock" >nul 2>&1
    del /f /q /a "%USERPROFILE%\.android\avd\Medium_Phone.avd\multiinstance.lock" >nul 2>&1
    rd /s /q "%USERPROFILE%\.android\avd\Medium_Phone.avd\snapshots\default_boot" >nul 2>&1
    del /f /q /a "%USERPROFILE%\.android\avd\Medium_Phone_API_36.1.avd\*.lock" >nul 2>&1
    rd /s /q "%USERPROFILE%\.android\avd\Medium_Phone_API_36.1.avd\hardware-qemu.ini.lock" >nul 2>&1
    del /f /q /a "%USERPROFILE%\.android\avd\Medium_Phone_API_36.1.avd\multiinstance.lock" >nul 2>&1
    rd /s /q "%USERPROFILE%\.android\avd\Medium_Phone_API_36.1.avd\snapshots\default_boot" >nul 2>&1
    pushd "%SDK_DIR%\emulator"
    start "Medium Phone - Musify Emulator - Wiped" /d "%SDK_DIR%\emulator" "%EMULATOR_EXE%" -avd "%AVD_NAME%" -wipe-data -no-snapshot-load
    popd
    echo Emulator launched with wiped data. Please re-run test runner once booted.
    pause
    exit /b 0
)

set "PACKAGE_NAME=com.musify.browser"
if "!APK_CHOICE!"=="1" set "APK_PATH=%SCRIPT_DIR%Musify.apk"
if "!APK_CHOICE!"=="2" set "APK_PATH=%SCRIPT_DIR%Musify-ARM64.apk"
if "!APK_CHOICE!"=="3" set "APK_PATH=%SCRIPT_DIR%Musify-Lite.apk"
if "!APK_CHOICE!"=="4" (
    set "PACKAGE_NAME=com.musify.browser.debug"
    set "APK_PATH=%SCRIPT_DIR%app\build\outputs\apk\debug\app-debug.apk"
    if not exist "!APK_PATH!" (
        echo Building debug APK...
        call "%SCRIPT_DIR%gradlew.bat" assembleDebug
    )
)

if not exist "!APK_PATH!" (
    echo [ERROR] Selected APK not found at: !APK_PATH!
    pause
    exit /b 1
)

echo.
echo [1/5] Checking Android Emulator status...
set "EMULATOR_ONLINE=0"
for /f "skip=1 tokens=1,2" %%i in ('"%ADB_EXE%" devices 2^>nul') do (
    if "%%j"=="device" set "EMULATOR_ONLINE=1"
)

if "!EMULATOR_ONLINE!"=="0" (
    echo Starting emulator '%AVD_NAME%' - clean cold boot...
    del /f /q /a "%USERPROFILE%\.android\avd\Medium_Phone.avd\*.lock" >nul 2>&1
    rd /s /q "%USERPROFILE%\.android\avd\Medium_Phone.avd\hardware-qemu.ini.lock" >nul 2>&1
    del /f /q /a "%USERPROFILE%\.android\avd\Medium_Phone.avd\multiinstance.lock" >nul 2>&1
    del /f /q /a "%USERPROFILE%\.android\avd\Medium_Phone_API_36.1.avd\*.lock" >nul 2>&1
    rd /s /q "%USERPROFILE%\.android\avd\Medium_Phone_API_36.1.avd\hardware-qemu.ini.lock" >nul 2>&1
    del /f /q /a "%USERPROFILE%\.android\avd\Medium_Phone_API_36.1.avd\multiinstance.lock" >nul 2>&1
    if exist "%USERPROFILE%\.android\avd\Medium_Phone.avd\snapshots\default_boot\ram.img.dirty" (
        echo Purging corrupted snapshot...
        rd /s /q "%USERPROFILE%\.android\avd\Medium_Phone.avd\snapshots\default_boot" >nul 2>&1
    )
    if exist "%USERPROFILE%\.android\avd\Medium_Phone_API_36.1.avd\snapshots\default_boot\ram.img.dirty" (
        echo Purging corrupted snapshot...
        rd /s /q "%USERPROFILE%\.android\avd\Medium_Phone_API_36.1.avd\snapshots\default_boot" >nul 2>&1
    )
    pushd "%SDK_DIR%\emulator"
    start "Medium Phone - Musify Emulator" /d "%SDK_DIR%\emulator" "%EMULATOR_EXE%" -avd "%AVD_NAME%" -no-snapshot-load
    popd
    echo Waiting for emulator device to register with ADB...
    "%ADB_EXE%" wait-for-device
) else (
    echo Emulator is already running and connected.
)

echo.
echo [2/5] Waiting for Android system and Package Manager readiness...
set "BOOT_ATTEMPTS=0"
:wait_boot
set /a BOOT_ATTEMPTS+=1
"%ADB_EXE%" shell getprop sys.boot_completed 2>nul | findstr "1" >nul
if !errorlevel! neq 0 (
    if !BOOT_ATTEMPTS! geq 60 (
        echo [WARNING] Boot check timed out. Attempting to proceed...
        goto wait_pm
    )
    echo Boot in progress - attempt !BOOT_ATTEMPTS! of 60 - waiting 3 seconds...
    ping -n 4 127.0.0.1 >nul
    goto wait_boot
)
echo Android system boot is complete.

set "PM_ATTEMPTS=0"
:wait_pm
set /a PM_ATTEMPTS+=1
"%ADB_EXE%" shell pm path android 2>nul | findstr /i "package:" >nul
if !errorlevel! neq 0 (
    if !PM_ATTEMPTS! geq 30 (
        echo [WARNING] Package Manager check timed out. Attempting install...
        goto do_install
    )
    echo Package Manager initializing - attempt !PM_ATTEMPTS! of 30 - waiting 3 seconds...
    ping -n 4 127.0.0.1 >nul
    goto wait_pm
)
echo Package Manager is fully ready.

:do_install
echo.
echo [3/5] Installing selected APK: !APK_PATH!...
"%ADB_EXE%" install -r -d -g "!APK_PATH!"
if !errorlevel! neq 0 (
    echo [NOTICE] Standard install failed - signature mismatch or cache conflict.
    echo [NOTICE] Performing clean reinstall: uninstalling existing versions...
    "%ADB_EXE%" uninstall com.musify.browser >nul 2>&1
    "%ADB_EXE%" uninstall com.musify.browser.debug >nul 2>&1
    echo [NOTICE] Retrying installation...
    "%ADB_EXE%" install -r -g "!APK_PATH!"
    if !errorlevel! neq 0 (
        echo [ERROR] Clean APK installation failed.
        echo Possible cause: Insufficient storage on host or AVD virtual partition.
        echo Try selecting option 5 from runner menu to wipe userdata.
        pause
        exit /b 1
    )
)
echo APK installed successfully!

echo.
echo [4/5] Launching Musify (!PACKAGE_NAME!)...
"%ADB_EXE%" shell am start -n !PACKAGE_NAME!/com.musify.browser.MainActivity

echo.
echo [5/5] Musify launched successfully!
echo ========================================================
echo  Target Package: !PACKAGE_NAME!
echo  Ready to verify Brave-Aligned Shields:
echo.
echo  Quick Test Shortcuts:
echo    [H] Launch Safari Start Page (musify://home)
echo    [S] Open Spotify Web Player (open.spotify.com)
echo    [Y] Open YouTube (m.youtube.com)
echo    [M] Open YouTube Music (music.youtube.com)
echo    [P] Test Background Audio & System Media Notification
echo    [B] Open d3ward Adblock Benchmark (d3ward.github.io/toolz/adblock.html)
echo    [C] Open CanYouBlockIt Test (canyoublockit.com/testing/)
echo    [D] Open Bounce Tracking Redirect Test (dpbolvw.net)
echo    [Q] Quit runner
echo ========================================================
echo.

:menu_loop
set "TEST_CMD="
set /p "TEST_CMD=Choose website test [H/S/Y/M/P/B/C/D/Q]: "
if not defined TEST_CMD goto menu_loop
for /f "tokens=1" %%t in ("!TEST_CMD!") do set "TEST_CMD=%%t"
if /i "!TEST_CMD!"=="H" (
    echo Opening Safari Start Page...
    "%ADB_EXE%" shell am start -n !PACKAGE_NAME!/com.musify.browser.MainActivity
    goto menu_loop
)
if /i "!TEST_CMD!"=="S" (
    echo Opening Spotify Web Player...
    "%ADB_EXE%" shell am start -a android.intent.action.VIEW -d "https://open.spotify.com" -n !PACKAGE_NAME!/.MainActivity
    goto menu_loop
)
if /i "!TEST_CMD!"=="Y" (
    echo Opening YouTube...
    "%ADB_EXE%" shell am start -a android.intent.action.VIEW -d "https://m.youtube.com" -n !PACKAGE_NAME!/.MainActivity
    goto menu_loop
)
if /i "!TEST_CMD!"=="M" (
    echo Opening YouTube Music...
    "%ADB_EXE%" shell am start -a android.intent.action.VIEW -d "https://music.youtube.com" -n !PACKAGE_NAME!/.MainActivity
    goto menu_loop
)
if /i "!TEST_CMD!"=="P" (
    echo Starting Background Media Service & Notification...
    "%ADB_EXE%" shell am start-foreground-service -a com.musify.browser.media.START -e extra_track_title "Starboy - The Weeknd" -e extra_artist_or_site "Spotify • Designed by adishxm" -n !PACKAGE_NAME!/.media.MusifyMediaPlaybackService
    goto menu_loop
)
if /i "!TEST_CMD!"=="B" (
    echo Opening d3ward Adblock Benchmark...
    "%ADB_EXE%" shell am start -a android.intent.action.VIEW -d "https://d3ward.github.io/toolz/adblock.html" -n !PACKAGE_NAME!/.MainActivity
    goto menu_loop
)
if /i "!TEST_CMD!"=="C" (
    echo Opening CanYouBlockIt Test...
    "%ADB_EXE%" shell am start -a android.intent.action.VIEW -d "https://canyoublockit.com/testing/" -n !PACKAGE_NAME!/.MainActivity
    goto menu_loop
)
if /i "!TEST_CMD!"=="D" (
    echo Opening Bounce Tracking Redirect Test...
    "%ADB_EXE%" shell am start -a android.intent.action.VIEW -d "https://dpbolvw.net/click-12345?url=https%%3A%%2F%%2Fmusify.app%%2Fdownload%%3Ffbclid%%3Dtracking_token_123" -n !PACKAGE_NAME!/.MainActivity
    goto menu_loop
)
if /i "!TEST_CMD!"=="Q" (
    echo Exiting test runner.
    exit /b 0
)
goto menu_loop
