@echo off

@REM Chua test
for /d %%d in (*) do (
    if "%1"=="start" (
        echo [Starting Docker] %%~nd ====================================
        cd %%d
        docker-compose up -d
        cd ..
    ) else if "%1"=="stop" (
        echo [Stopping Docker] %%~nd ====================================
        cd %%d
        docker-compose down
        cd ..
    ) else if "%1"=="restart" (
        echo [Restarting Docker] %%~nd ====================================
        cd %%d
        docker-compose down
        docker-compose up -d
        cd ..
    ) else (
        echo Usage: docker.bat [start|stop|restart]
        goto :EOF
    )
)
