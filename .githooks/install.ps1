$ErrorActionPreference = "Stop"
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$src = Join-Path $root ".githooks\post-commit"
$dstDir = Join-Path $root ".git\hooks"
$dst = Join-Path $dstDir "post-commit"
if (-not (Test-Path $src)) { throw "Missing $src" }
New-Item -ItemType Directory -Force -Path $dstDir | Out-Null
Copy-Item -Force $src $dst
Write-Host "Installed git hook: $dst"
