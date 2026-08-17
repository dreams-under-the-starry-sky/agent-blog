$ErrorActionPreference = "Stop"
$env:JAVA_HOME = "E:\environment\Java\JDK\jdk21"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
$mvn = "E:\java\maven\apache-maven-3.6.3\bin\mvn.cmd"
$settings = "E:\java\maven\apache-maven-3.6.3\conf\settings.xml"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root
Write-Host "JAVA_HOME=$env:JAVA_HOME"
& $mvn -s $settings spring-boot:run
