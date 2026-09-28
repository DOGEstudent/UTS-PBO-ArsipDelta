$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
if ($env:JAVA_HOME) {
    $jdkBin = Join-Path $env:JAVA_HOME 'bin'
} else {
    $jdkBin = Split-Path (Get-Command javac -ErrorAction Stop).Source
}
$testOutput = Join-Path $PSScriptRoot 'target/manual-tests'
New-Item -ItemType Directory -Force $testOutput | Out-Null
$sources = Get-ChildItem src -Recurse -Filter *.java | ForEach-Object FullName
& (Join-Path $jdkBin 'javac.exe') -encoding UTF-8 -d $testOutput $sources
if ($LASTEXITCODE -ne 0) { throw 'Kompilasi gagal.' }
& (Join-Path $jdkBin 'java.exe') -cp $testOutput arsipdelta.RegressionTest
if ($LASTEXITCODE -ne 0) { throw 'Pengujian gagal.' }
