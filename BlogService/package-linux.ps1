$ErrorActionPreference = "Stop"
$cmd = Join-Path $PSScriptRoot "package-linux.cmd"
cmd /c "`"$cmd`""
exit $LASTEXITCODE
