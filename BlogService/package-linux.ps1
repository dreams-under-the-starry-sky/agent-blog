$ErrorActionPreference = "Stop"
$env:JAVA_HOME = "E:\environment\Java\JDK\jdk21"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
$mvn = "E:\java\maven\apache-maven-3.6.3\bin\mvn.cmd"
$settings = "E:\java\maven\apache-maven-3.6.3\conf\settings.xml"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

$tz = [TimeZoneInfo]::FindSystemTimeZoneById("China Standard Time")
$stamp = [TimeZoneInfo]::ConvertTime((Get-Date), $tz).ToString("yyyyMMdd-HHmmss")

Write-Host "JAVA_HOME=$env:JAVA_HOME"
Write-Host "Packaging Linux x86_64 jar (natives-linux, skip tests) stamp=$stamp"

# Windows 会自动激活 natives-windows，必须关掉，否则 FFmpeg 在 Linux 上起不来。
# 必须 Start-Process -Wait：PowerShell 的 & / LASTEXITCODE 在钩子里不可靠。
$line = "/c `"$mvn`" -s `"$settings`" -B -DskipTests package -P-natives-windows,natives-linux"
Write-Host $line
$p = Start-Process -FilePath 'cmd.exe' -ArgumentList $line -Wait -NoNewWindow -PassThru
if ($null -eq $p -or $p.ExitCode -ne 0) {
    $code = if ($null -eq $p) { 1 } else { $p.ExitCode }
    Write-Error "Maven package failed (exit $code)"
    exit $code
}

$built = Join-Path $root "target\blog-service.jar"
if (-not (Test-Path $built)) {
    throw "Maven succeeded but $built is missing"
}

$release = Join-Path $root "release"
New-Item -ItemType Directory -Force -Path $release | Out-Null
$latest = Join-Path $release "blog-service-linux.jar"
$stamped = Join-Path $release "blog-service-linux-$stamp.jar"
Copy-Item -Force $built $latest
Copy-Item -Force $built $stamped
Write-Host "Linux jar ready:"
Write-Host "  $latest"
Write-Host "  $stamped"
