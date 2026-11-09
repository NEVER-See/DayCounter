param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$CacheRoot = (Join-Path $PSScriptRoot '.cache'),
    [string[]]$Tasks = @('build'),
    [string]$MinecraftVersion = '26.3',
    [switch]$Offline
)
$ErrorActionPreference = 'Stop'
if (-not $JavaHome) {
    $compiler = Get-Command javac -ErrorAction SilentlyContinue
    if ($compiler) { $JavaHome = Split-Path (Split-Path $compiler.Source -Parent) -Parent }
}
if (-not $JavaHome) { throw 'Set JAVA_HOME to a JDK 25 installation or pass -JavaHome.' }
$java = Join-Path $JavaHome 'bin/java.exe'
if (-not (Test-Path -LiteralPath $java)) { throw 'Java 25 is required. Pass -JavaHome with the JDK path.' }
New-Item -ItemType Directory -Force -Path $CacheRoot | Out-Null
$env:JAVA_HOME = $JavaHome
$env:GRADLE_USER_HOME = Join-Path $CacheRoot 'gradle'
$env:TEMP = $CacheRoot
$env:TMP = $CacheRoot
Push-Location $PSScriptRoot
try {
    $arguments = @("-Djava.io.tmpdir=$CacheRoot", '-jar', './gradle/wrapper/gradle-wrapper.jar') + $Tasks + @('--console=plain', '-Dfabric.loom.ci=true', ('-PminecraftVersion=' + $MinecraftVersion))
    if ($Offline) { $arguments += '--offline' }
    & $java @arguments
    if ($LASTEXITCODE -ne 0) { throw "Gradle failed: $LASTEXITCODE" }
} finally { Pop-Location }
