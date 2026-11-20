@echo off
chcp 65001 >nul
REM Sube este proyecto al repositorio https://github.com/JuanSG12/Tramas con TU nombre como autor.
REM Uso: descomprime el proyecto en una ruta sin tildes (por ejemplo C:\Tramas), abre esta carpeta y haz doble clic en este archivo.
setlocal
cd /d "%~dp0.."
where git >nul 2>nul || (echo Git no esta instalado. Instalalo desde https://git-scm.com/download/win y vuelve a ejecutar este archivo. & pause & exit /b 1)

set /p NOMBRE=Tu nombre completo (aparecera como autor de los commits): 
set /p CORREO=Tu correo de GitHub: 
git config --global user.name "%NOMBRE%"
git config --global user.email "%CORREO%"

if not exist .git git init
git add .
git commit -m "Tienda virtual Tramas: proyecto final"
git branch -M main
git remote remove origin >nul 2>nul
git remote add origin https://github.com/JuanSG12/Tramas.git
echo.
echo Subiendo a GitHub (si pide iniciar sesion, se abrira el navegador)...
git push -u origin main
if errorlevel 1 (
  echo.
  echo El push fue rechazado, probablemente porque el repositorio ya tiene contenido.
  set /p FORZAR=Escribe SI para reemplazar el contenido del repositorio con este proyecto: 
  if /i "%FORZAR%"=="SI" git push -u origin main --force
)
echo.
echo Listo. Abre https://github.com/JuanSG12/Tramas en una ventana de incognito para comprobarlo.
pause
