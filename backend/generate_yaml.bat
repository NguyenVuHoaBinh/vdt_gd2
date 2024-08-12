@echo off
:: Check if the correct number of arguments is provided
if "%~2"=="" (
    echo Usage: %0 dataPlatform databaseName
    exit /b 1
)

:: Get the data platform and database name from arguments
set DATA_PLATFORM=urn:li:dataPlatform:%1
set DB_NAME=%2

set OUTPUT_DIR=metadata_output

:: Create output directory if it doesn't exist
if not exist %OUTPUT_DIR% (
    mkdir %OUTPUT_DIR%
)

:: Fetch all dataset URNs in the database
:: Replace this with the actual command to get all dataset URNs in the DB.
:: Using DataHub CLI and jq, but adapted for Windows. You might need jq installed on Windows.

datahub search --entity dataset --query "database = %DB_NAME%" --platform %1 --fields "urn" --json > temp_results.json
for /F "delims=" %%A in ('jq -r ".results[] | .urn" temp_results.json') do (
    set URN=%%A
    set FILE_NAME=%OUTPUT_DIR%\%%~nxA.yaml
    echo Generating YAML for %URN% into %FILE_NAME%
    datahub dataset get --urn "%URN%" --to-file "%FILE_NAME%"
)

:: Cleanup temporary files
del temp_results.json
exit