@echo off
setlocal
set VERSION=1.5

call mvn -B clean package || exit /b 1

rmdir /s /q staging staging-content installer 2>nul
mkdir staging staging-content
copy target\PointOfSale-%VERSION%.jar staging\ >nul
copy scripts\Start-POS.ps1 staging-content\ >nul

jpackage ^
 --type exe ^
 --name PointOfSale ^
 --app-version %VERSION% ^
 --input staging ^
 --main-jar PointOfSale-%VERSION%.jar ^
 --main-class com.pointofsale.Launcher ^
 --app-content staging-content ^
 --win-shortcut --win-menu ^
 --win-upgrade-uuid 3f2c8a44-1b7e-4c9d-9a55-6d0e2b7c1a10 ^
 --runtime-image "C:\Users\mzuze\Documents\OtherProjects\PointOfSale\jdk-21.0.7+6-jre" ^
 --dest installer

echo.
echo Done. Installer is in the "installer" folder.
pause