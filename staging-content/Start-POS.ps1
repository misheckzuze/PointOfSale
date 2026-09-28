param(
    [string]$Repo = 'misheckzuze/PointOfSale',
    [string]$InstallRoot = (Join-Path $env:LOCALAPPDATA 'PointOfSale'),
    [string]$JavaExe = '',
    [string]$BundledJar = '',
    [switch]$CheckOnly,
    [switch]$Offline
)
$ErrorActionPreference = 'Stop'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
New-Item -ItemType Directory -Force -Path $InstallRoot | Out-Null
$log = Join-Path $InstallRoot 'update.log'
function Log($m) {
    Add-Content $log "$(Get-Date -Format s) $m"
    if ((Get-Item $log).Length -gt 512KB) { Move-Item $log "$log.old" -Force }
}

$mutex = New-Object Threading.Mutex($false, 'Local\PointOfSale-Launcher')
$locked = $false
$exitCode = 0
try {
    try { $locked = $mutex.WaitOne(0) } catch [Threading.AbandonedMutexException] { $locked = $true }
    if (!$locked) { Log 'Already running.'; $exitCode = 2; return }

    $java = if ($JavaExe) { $JavaExe } else { (Get-Command java -ErrorAction Stop).Source }

    # --- Find the currently installed (downloaded) version ---
    $pointer = Join-Path $InstallRoot 'current.json'
    $current = $null
    if (Test-Path $pointer) {
        try {
            $c = Get-Content $pointer -Raw | ConvertFrom-Json
            $jar = Join-Path $InstallRoot "versions\$($c.tag)\PointOfSale.jar"
            if ((Test-Path $jar) -and (Get-FileHash $jar -Algorithm SHA256).Hash.ToLower() -eq $c.sha256) {
                $current = @{ Tag = $c.tag; Jar = $jar }
            } else { Log 'Installed jar missing or checksum mismatch.' }
        } catch { Log "Bad current.json: $($_.Exception.Message)" }
    }

    # --- Check for and install an update ---
    if (!$Offline) {
        try {
            $rel = Invoke-RestMethod "https://api.github.com/repos/$Repo/releases/latest" `
                   -Headers @{ 'User-Agent' = 'POS-Updater' } -TimeoutSec 15
            $tag = [string]$rel.tag_name
            if ($tag -notmatch '^v[0-9][0-9A-Za-z._-]*$') { throw "Bad tag $tag" }
            if (!$current -or $current.Tag -ne $tag) {
                Log "Updating to $tag"
                $jarAsset = $rel.assets | Where-Object name -eq 'PointOfSale.jar' | Select-Object -First 1
                $shaAsset = $rel.assets | Where-Object name -eq 'PointOfSale.jar.sha256' | Select-Object -First 1
                if (!$jarAsset -or !$shaAsset) { throw 'Release is missing assets.' }

                $tmp = Join-Path $InstallRoot 'download'
                Remove-Item $tmp -Recurse -Force -ErrorAction SilentlyContinue
                New-Item -ItemType Directory -Path $tmp | Out-Null
                $tmpJar = Join-Path $tmp 'PointOfSale.jar'
                Invoke-WebRequest $jarAsset.browser_download_url -OutFile $tmpJar -UseBasicParsing -TimeoutSec 300
                $expected = (Invoke-RestMethod $shaAsset.browser_download_url -UseBasicParsing).ToString().Trim().ToLower()
                $actual = (Get-FileHash $tmpJar -Algorithm SHA256).Hash.ToLower()
                if ($actual -ne $expected) { throw 'Checksum mismatch on download.' }

                & $java -jar $tmpJar --verify-runtime
                if ($LASTEXITCODE -ne 0) { throw 'Runtime verification failed.' }

                $dest = Join-Path $InstallRoot "versions\$tag"
                New-Item -ItemType Directory -Force -Path $dest | Out-Null
                Move-Item $tmpJar (Join-Path $dest 'PointOfSale.jar') -Force
                if (Test-Path $pointer) { Copy-Item $pointer (Join-Path $InstallRoot 'previous.json') -Force }
                @{ tag = $tag; sha256 = $actual } | ConvertTo-Json | Set-Content $pointer -Encoding UTF8
                $current = @{ Tag = $tag; Jar = Join-Path $dest 'PointOfSale.jar' }
                Log "Updated to $tag"
            }
        } catch {
            Log "Update failed: $($_.Exception.Message)"
        }
    }

    # --- Pick what to run: downloaded version, else the bundled one ---
    $runJar = $null
    if ($current) { $runJar = $current.Jar; $label = $current.Tag }
    elseif ($BundledJar -and (Test-Path $BundledJar)) { $runJar = $BundledJar; $label = 'bundled' }
    if (!$runJar) { Log 'Nothing to run.'; $exitCode = 3; return }
    if ($CheckOnly) { Write-Output "Would run: $label"; return }

    $jvmArgs = @()
    $settings = Join-Path $InstallRoot 'runtime.properties'
    if (Test-Path $settings) {
        foreach ($line in Get-Content $settings) {
            if ($line -match '^\s*(pos\.[A-Za-z0-9.]+)\s*=(.*)$') { $jvmArgs += ('-D' + $matches[1] + '=' + $matches[2].Trim()) }
        }
    }
    Log "Starting $label"
    & $java @jvmArgs -jar $runJar
    $exitCode = $LASTEXITCODE
    Log "App exited with code $exitCode"
} catch {
    Log "Fatal: $($_.Exception.Message)"
    $exitCode = 3
} finally {
    if ($locked) { $mutex.ReleaseMutex() }
    $mutex.Dispose()
}
exit $exitCode