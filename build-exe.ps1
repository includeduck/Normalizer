param(
    [switch]$RunTests
)

$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$targetDir = Join-Path $projectRoot "target"
$stagingDir = Join-Path $targetDir "jpackage-input"
$runtimeDir = Join-Path $targetDir "jpackage-runtime"
$distDir = Join-Path $targetDir "dist"
$appName = "Normalizer"
$appImageDir = Join-Path $distDir $appName
$exePath = Join-Path $appImageDir "$appName.exe"
$artifactName = "dbms-normalizer-1.0.0-SNAPSHOT"
$thinJar = Join-Path $targetDir "$artifactName.jar.original"
$stagedJar = Join-Path $stagingDir "Normalizer.jar"

function Assert-CommandExists {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name
    )

    $command = Get-Command $Name -ErrorAction SilentlyContinue
    if (-not $command) {
        throw "Required command '$Name' was not found on PATH. Use JDK 21+ and Maven before running this script."
    }

    return $command.Source
}

function Remove-ProjectBuildPath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    $fullTarget = [System.IO.Path]::GetFullPath($targetDir)
    $fullPath = [System.IO.Path]::GetFullPath($Path)
    if (-not $fullPath.StartsWith($fullTarget, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to remove path outside target directory: $fullPath"
    }

    if (Test-Path -LiteralPath $fullPath) {
        Remove-Item -LiteralPath $fullPath -Recurse -Force
    }
}

$mvn = Assert-CommandExists "mvn"
$jpackage = Assert-CommandExists "jpackage"
$jlink = Assert-CommandExists "jlink"

Set-Location $projectRoot

Write-Host "Building Maven project..."
$mavenArgs = @()
if (-not $RunTests) {
    $mavenArgs += "-DskipTests"
}
$mavenArgs += @("package")
& $mvn @mavenArgs
if ($LASTEXITCODE -ne 0) {
    throw "Maven package failed."
}

if (-not (Test-Path -LiteralPath $thinJar)) {
    throw "Expected thin application jar was not found: $thinJar"
}

Write-Host "Preparing jpackage input..."
Remove-ProjectBuildPath $stagingDir
New-Item -ItemType Directory -Path $stagingDir | Out-Null

& $mvn "-DincludeScope=runtime" "-DoutputDirectory=$stagingDir" "dependency:copy-dependencies"
if ($LASTEXITCODE -ne 0) {
    throw "Dependency staging failed."
}

Copy-Item -LiteralPath $thinJar -Destination $stagedJar -Force

Write-Host "Creating runtime image..."
Remove-ProjectBuildPath $runtimeDir
& $jlink `
    "--add-modules" "ALL-MODULE-PATH" `
    "--strip-debug" `
    "--no-header-files" `
    "--no-man-pages" `
    "--output" $runtimeDir
if ($LASTEXITCODE -ne 0) {
    throw "Runtime image creation failed."
}

Write-Host "Creating Normalizer.exe app image..."
Remove-ProjectBuildPath $appImageDir
if (-not (Test-Path -LiteralPath $distDir)) {
    New-Item -ItemType Directory -Path $distDir | Out-Null
}

& $jpackage `
    "--type" "app-image" `
    "--name" $appName `
    "--input" $stagingDir `
    "--main-jar" "Normalizer.jar" `
    "--main-class" "com.dbms.analyzer.Launcher" `
    "--runtime-image" $runtimeDir `
    "--dest" $distDir `
    "--vendor" "DBMS Normalizer" `
    "--app-version" "1.0.0" `
    "--java-options" "-Dfile.encoding=UTF-8"
if ($LASTEXITCODE -ne 0) {
    throw "jpackage failed."
}

if (-not (Test-Path -LiteralPath $exePath)) {
    throw "Build finished, but Normalizer.exe was not found at: $exePath"
}

Write-Host ""
Write-Host "Created executable:"
Write-Host $exePath
Write-Host ""
Write-Host "Keep Normalizer.exe inside this generated folder so it can use the bundled app and runtime files."
