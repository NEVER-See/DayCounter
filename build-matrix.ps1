param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$CacheRoot = (Join-Path $PSScriptRoot '.cache'),
    [string[]]$MinecraftVersions,
    [switch]$Offline
)
$ErrorActionPreference = 'Stop'
$targets = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'gradle/targets.json') -Raw | ConvertFrom-Json
$known = @($targets.PSObject.Properties.Name)
if (-not $MinecraftVersions) { $MinecraftVersions = $known }
foreach ($game in $MinecraftVersions) {
    if ($game -notin $known) { throw "Unsupported Minecraft version: $game" }
}
foreach ($game in $MinecraftVersions) {
    & (Join-Path $PSScriptRoot 'build.ps1') -JavaHome $JavaHome -CacheRoot $CacheRoot -MinecraftVersion $game -Tasks build -Offline:$Offline
    $name = "daycounter-1.2.0+$game.jar"
    $source = Join-Path $PSScriptRoot "build/$game/libs/$name"
    if (-not (Test-Path -LiteralPath $source)) { throw "Missing mod JAR: $source" }
    $destination = Join-Path $PSScriptRoot "dist/$game"
    New-Item -ItemType Directory -Force -Path $destination | Out-Null
    Copy-Item -LiteralPath $source -Destination (Join-Path $destination $name)
    $hash = (Get-FileHash -LiteralPath $source -Algorithm SHA256).Hash.ToLowerInvariant()
    [IO.File]::WriteAllText((Join-Path $destination 'SHA256SUMS.txt'), "$hash  $name`n", [Text.UTF8Encoding]::new($false))
    Write-Output "Ready: dist/$game/$name"
}
