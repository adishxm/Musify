@echo off
setlocal enabledelayedexpansion
title Musify - Launch Android Emulator
color 0B

echo ========================================================
echo          Musify Android Emulator Launcher
echo          Target: Medium_Phone (API 36)
echo ========================================================
echo.

set "SDK_DIR=C:\Users\adity\AppData\Local\Android\Sdk"
set "EMULATOR_DIR=%SDK_DIR%\emulator"
set "EMULATOR_EXE=%EMULATOR_DIR%\emulator.exe"
set "ADB_EXE=%SDK_DIR%\platform-tools\adb.exe"
set "AVD_NAME=Medium_Phone_API_36.1"

set "ANDROID_HOME=%SDK_DIR%"
set "ANDROID_SDK_ROOT=%SDK_DIR%"
set "PATH=%SDK_DIR%\emulator;%SDK_DIR%\platform-tools;%PATH%"

if not exist "%EMULATOR_EXE%" (
    echo [ERROR] Android emulator executable not found at:
    echo %EMULATOR_EXE%
    echo Please verify Android SDK installation.
    echo.
    pause
    exit /b 1
)

:: Check if an existing emulator or QEMU process is running
tasklist /fi "imagename eq qemu-system-x86_64.exe" 2>nul | findstr /i "qemu" >nul
set "IS_QEMU_RUNNING=%errorlevel%"

"%ADB_EXE%" devices 2>nul | findstr /i "emulator-" >nul
set "IS_ADB_RUNNING=%errorlevel%"

if %IS_QEMU_RUNNING% equ 0 (
    echo [NOTICE] An emulator process is already detected running on your system.
    echo If the emulator window is NOT visible on your screen, it may be stuck in the background.
    echo.
    set /p "CHOICE=Do you want to kill and restart it fresh? [Y/N, default Y]: "
    if "!CHOICE!"=="" set "CHOICE=Y"
    if /i "!CHOICE!"=="Y" (
        echo.
        echo Terminating existing emulator process...
        "%ADB_EXE%" emu kill >nul 2>&1
        taskkill /f /im qemu-system-x86_64.exe >nul 2>&1
        taskkill /f /im emulator.exe >nul 2>&1
        timeout /t 2 >nul
    ) else (
        echo Keeping existing emulator running.
        echo Opening ADB device check...
        "%ADB_EXE%" devices
        echo.
        pause
        exit /b 0
    )
)

:: Auto-clean stale AVD lock files if process is not running
del /f /q "%USERPROFILE%\.android\avd\Medium_Phone.avd\*.lock" >nul 2>&1
rd /s /q "%USERPROFILE%\.android\avd\Medium_Phone.avd\hardware-qemu.ini.lock" >nul 2>&1

echo.
echo Starting Android Emulator '%AVD_NAME%'...
pushd "%EMULATOR_DIR%"

:: Start emulator in separate interactive window
start "Medium Phone - Musify Emulator" emulator.exe -avd "%AVD_NAME%"

popd

echo Waiting for emulator process to initialize...
timeout /t 4 >nul

:: Verify emulator process started
tasklist /fi "imagename eq qemu-system-x86_64.exe" 2>nul | findstr /i "qemu" >nul
if %errorlevel% neq 0 (
    echo.
    echo [WARNING] Emulator process did not stay running.
    echo Running diagnostic test to check for errors...
    echo ----------------------------------------------------
    pushd "%EMULATOR_DIR%"
    emulator.exe -avd "%AVD_NAME%"
    popd
    echo ----------------------------------------------------
    echo.
    echo If you see disk space errors, free up space on C: drive.
    echo If AVD is locked, stale lock files may need removal.
    pause
    exit /b 1
)

echo.
echo [1/2] Waiting for ADB device connection...
"%ADB_EXE%" wait-for-device

echo [2/2] Waiting for Android system boot completion...
:wait_boot
for /f "tokens=*" %%a in ('"%ADB_EXE%" shell getprop sys.boot_completed 2^>nul') do set "BOOT_STATUS=%%a"
if not "!BOOT_STATUS!"=="1" (
    timeout /t 2 >nul
    goto wait_boot
)

echo.
echo ========================================================
echo  [SUCCESS] Android Emulator is booted and ready!
echo  You can now view/interact with the emulator window,
echo  or run 'test-on-emulator.bat' to test Musify.
echo ========================================================
echo.
pause
