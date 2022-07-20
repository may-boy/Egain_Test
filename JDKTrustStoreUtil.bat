@echo off

TITLE JDKTruststoreUtil

SETLOCAL

@REM Get utility parent directory
for %%a in ("%~dp0.") do set "p_dir=%%~dpa"
for %%a in (%p_dir:~0,-1%) do set "egainPath=%%~dpa"

IF %egainPath:~-1%==\ SET egainPath=%egainPath:~0,-1%

SET LOCAL_INSTALL_DIR=%egainPath%
echo LOCAL_INSTALL_DIR:%LOCAL_INSTALL_DIR%

SET JAVA_HOME="%LOCAL_INSTALL_DIR%\jdk"
SET CLASSPATH=%LOCAL_INSTALL_DIR%\eService\lib\egpl_classpath_mf.jar
SET CLASSPATH=.\classes;%CLASSPATH%

SET cloud.variables.file=%LOCAL_INSTALL_DIR%\eService\bin\platform\windows\cloudvariables.properties
IF EXIST %cloud.variables.file% (
    FOR /F "eol=# tokens=1,* delims==" %%G IN ('type %cloud.variables.file%') DO (IF NOT "%%G"=="" IF NOT "%%H"=="" SET %%G=%%H)
)

SETLOCAL EnableDelayedExpansion
IF /I "%IS_CLOUD_DEPLOYMENT%"=="true" (
    SET JDK_TS_OPTIONS=-Djavax.net.ssl.trustStoreType=PKCS12 -Djavax.net.ssl.trustStorePassword=
    IF /I "%JDK_SECURITY_PROVIDER%"=="bcfips" (SET JDK_TS_OPTIONS=-Djavax.net.ssl.trustStoreProvider=BCFIPS !JDK_TS_OPTIONS!)
    IF /I "%JDK_SECURITY_PROVIDER%"=="sun" (IF NOT "%JDK_TLS_CIPHER_SUITES%"=="" (SET JDK_TLS_OPTS=-Djdk.tls.server.cipherSuites=%JDK_TLS_CIPHER_SUITES% -Djdk.tls.client.cipherSuites=%JDK_TLS_CIPHER_SUITES%))
)

SETLOCAL DisableDelayedExpansion
echo %CLASSPATH%

"%JAVA_HOME%/bin/java" -classpath %CLASSPATH% -Djdk.tls.trustNameService=true %JDK_TS_OPTIONS% %JDK_TLS_OPTS% -DINSTALL_DIR_LOCAL="%LOCAL_INSTALL_DIR%\eService" -Degain.processname=JDKTrustStoreUtil com.egain.platform.util.JDKTrustStoreUtil List "entrustevca [jdk]"
goto NOMAL_EXIT

:NOMAL_EXIT
echo -------------------------------------------------------------------------------------------------------------------------------
echo Utility has finished execution. Please refer %LOCAL_INSTALL_DIR%\eService\logs\eg_log_[hostname]_JDKTruststoreUtil.log for details.
echo -------------------------------------------------------------------------------------------------------------------------------
exit /b 0