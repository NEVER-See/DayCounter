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
$properties = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'gradle.properties') -Raw
if ($properties -notmatch '(?m)^modVersion=([0-9]+\.[0-9]+\.[0-9]+(?:-[A-Za-z0-9.-]+)?)\s*$') {
    throw 'Missing or invalid modVersion in gradle.properties'
}
$modVersion = $Matches[1]
foreach ($game in $MinecraftVersions) {
    & (Join-Path $PSScriptRoot 'build.ps1') -JavaHome $JavaHome -CacheRoot $CacheRoot -MinecraftVersion $game -Tasks build -Offline:$Offline
    $name = "daycounter-$modVersion+$game.jar"
    $source = Join-Path $PSScriptRoot "build/$game/libs/$name"
    if (-not (Test-Path -LiteralPath $source -PathType Leaf)) { throw "Missing mod JAR: $source" }
    $destination = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot "dist/$game"))
    New-Item -ItemType Directory -Force -Path $destination | Out-Null
    $oldFiles = @(Get-ChildItem -LiteralPath $destination -File -Filter '*.jar' | Where-Object {
        $_.Name -ne $name -and $_.Name -match ('^daycounter-([0-9]+\.[0-9]+\.[0-9]+(?:-[A-Za-z0-9.-]+)?)\+' + [regex]::Escape($game) + '\.jar$')
    })
    # Preserve old releases before replacing the currently installable JAR.
    foreach ($old in $oldFiles) {
        [void]($old.Name -match '^daycounter-(.+)\+[^+]+\.jar$')
        $archive = [IO.Path]::GetFullPath((Join-Path $destination ('archive/' + $Matches[1])))
        if (-not $archive.StartsWith($destination + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) {
            throw 'Archive path is outside the Minecraft version folder'
        }
        New-Item -ItemType Directory -Force -Path $archive | Out-Null
        $archivedJar = Join-Path $archive $old.Name
        $oldHash = (Get-FileHash -LiteralPath $old.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
        if (Test-Path -LiteralPath $archivedJar) {
            if ((Get-FileHash -LiteralPath $archivedJar -Algorithm SHA256).Hash.ToLowerInvariant() -ne $oldHash) {
                throw "Archive already contains a different file: $archivedJar"
            }
        } else { Copy-Item -LiteralPath $old.FullName -Destination $archivedJar }
        if ((Get-FileHash -LiteralPath $archivedJar -Algorithm SHA256).Hash.ToLowerInvariant() -ne $oldHash) {
            throw "Archive verification failed: $archivedJar"
        }
        [IO.File]::WriteAllText((Join-Path $archive 'SHA256SUMS.txt'), "$oldHash  $($old.Name)`n", [Text.UTF8Encoding]::new($false))
    }
    $ready = Join-Path $destination $name
    Copy-Item -LiteralPath $source -Destination $ready
    $hash = (Get-FileHash -LiteralPath $source -Algorithm SHA256).Hash.ToLowerInvariant()
    if ((Get-FileHash -LiteralPath $ready -Algorithm SHA256).Hash.ToLowerInvariant() -ne $hash) {
        throw "Copy verification failed: $ready"
    }
    foreach ($old in $oldFiles) {
        if ([IO.Path]::GetFullPath($old.DirectoryName) -ne $destination) { throw 'Unexpected old JAR location' }
        Remove-Item -LiteralPath $old.FullName
    }
    [IO.File]::WriteAllText((Join-Path $destination 'SHA256SUMS.txt'), "$hash  $name`n", [Text.UTF8Encoding]::new($false))
    Write-Output "Ready: dist/$game/$name"
}
