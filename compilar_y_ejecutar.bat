@echo off
echo =========================================
echo   COMPILACION Y EJECUCION - SISTEMA ACID
echo =========================================

REM Crear directorio de clases compiladas
if not exist "bin" mkdir bin

echo.
echo [1/3] Compilando clases Java...
echo.

REM Compilar todas las clases Java incluyendo las dependencias
javac -cp "lib\postgresql-42.7.7.jar;lib\gson-2.10.1.jar" -d bin -sourcepath src src\com\puce\*.java src\com\puce\config\*.java src\com\puce\dao\*.java src\com\puce\model\*.java src\com\puce\service\*.java src\com\puce\web\*.java

if %ERRORLEVEL% neq 0 (
    echo.
    echo ❌ ERROR: Falló la compilación
    echo.
    pause
    exit /b 1
)

echo ✅ Compilación exitosa
echo.

echo [2/3] Verificando dependencias...
if not exist "lib\postgresql-42.7.7.jar" (
    echo ❌ ERROR: No se encuentra postgresql-42.7.7.jar en lib\
    pause
    exit /b 1
)

if not exist "lib\gson-2.10.1.jar" (
    echo ❌ ERROR: No se encuentra gson-2.10.1.jar en lib\
    pause
    exit /b 1
)

echo ✅ Todas las dependencias encontradas
echo.

echo [3/3] Iniciando aplicación...
echo.
echo 🚀 Iniciando Sistema de Gestión ACID...
echo.
echo ⚡ Opciones disponibles:
echo    1. Interfaz de consola (tradicional)
echo    2. Servidor web + interfaz web  
echo    3. Ambos (consola + web)
echo.

REM Ejecutar la aplicación
java -cp "bin;lib\postgresql-42.7.7.jar;lib\gson-2.10.1.jar" com.puce.Main

echo.
echo 👋 Aplicación terminada
pause 