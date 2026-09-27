param(
    [string]$Repository = 'https://github.com/misheckzuze/PointOfSale.git',
    [string]$Branch = 'master',
    [string]$InstallRoot = (Join-Path $env:LOCALAPPDATA 'PointOfSaleGit'),
    [switch]$CheckOnly,
    [switch]$Offline
)
$ErrorActionPreference = 'Stop'
$InstallRoot = [IO.Path]::GetFullPath($InstallRoot)
if ($Branch -notmatch '^[a-zA-Z0-9][a-zA-Z0-9/_.-]*$' -or $Branch.Contains('..')) { throw 'Invalid Git branch.' }
New-Item -ItemType Directory -Force -Path $InstallRoot | Out-Null
$hash = [Security.Cryptography.SHA256]::Create()
try { $id = [BitConverter]::ToString($hash.ComputeHash([Text.Encoding]::UTF8.GetBytes($InstallRoot.ToLowerInvariant()))).Replace('-', '') }
finally { $hash.Dispose() }
$mutex = New-Object Threading.Mutex($false, ('Local\PointOfSaleGit-' + $id))
$locked = $false
$oldPrompt = $env:GIT_TERMINAL_PROMPT

function Run-Checked([string]$Executable, [string[]]$CommandArguments, [string]$Directory) {
    # No shell command construction: pass an argument array to the command.
    Push-Location $Directory
    try {
        $ErrorActionPreference = 'Continue'
        & $Executable @CommandArguments
        $exitCode = $LASTEXITCODE
        $ErrorActionPreference = 'Stop'
        if ($exitCode -ne 0) { throw "$Executable failed with exit code $exitCode" }
    } finally { Pop-Location }
}

function Read-Current {
    $pointer = Join-Path $InstallRoot 'current.json'
    if (!(Test-Path -LiteralPath $pointer)) { return $null }
    $current = Get-Content -LiteralPath $pointer -Raw | ConvertFrom-Json
    if ($current.commit -notmatch '^[0-9a-f]{40}$') { throw 'Invalid installed version pointer.' }
    # Old installation pointers did not record a filename and always used 1.0.
    $jarName = if ($current.jar) { [string]$current.jar } else { 'PointOfSale-1.0.jar' }
    if ($jarName -notmatch '^[A-Za-z0-9][A-Za-z0-9._-]*\.jar$') { throw 'Invalid installed JAR filename.' }
    $jar = Join-Path $InstallRoot ('versions\' + $current.commit + '\target\' + $jarName)
    if (!(Test-Path -LiteralPath $jar) -or (Get-FileHash -LiteralPath $jar -Algorithm SHA256).Hash -ne $current.sha256) {
        throw 'Installed application checksum does not match.'
    }
    return @{ Commit = $current.commit; Jar = $jar }
}

try {
    try { $locked = $mutex.WaitOne(0) } catch [Threading.AbandonedMutexException] { $locked = $true }
    if (!$locked) { throw 'Point of Sale is already running or updating from this installation.' }
    $env:GIT_TERMINAL_PROMPT = '0'
    $current = Read-Current
    if (!$Offline) {
        try {
            $git = (Get-Command git -ErrorAction Stop).Source
            $maven = (Get-Command mvn -ErrorAction Stop).Source
            $cache = Join-Path $InstallRoot 'repository.git'
            if (!(Test-Path -LiteralPath $cache)) {
                Run-Checked $git @('clone', '--bare', '--filter=blob:none', '--single-branch', '--branch', $Branch, '--', $Repository, $cache) $InstallRoot
            }
            $origin = (& $git -C $cache remote get-url origin).Trim()
            if ($LASTEXITCODE -ne 0 -or $origin -ne $Repository) { throw 'Cached Git remote differs from configured repository.' }
            Run-Checked $git @('-C', $cache, '-c', 'http.lowSpeedLimit=1024', '-c', 'http.lowSpeedTime=30', 'fetch', '--no-tags', 'origin', ("refs/heads/{0}:refs/heads/{0}" -f $Branch)) $InstallRoot
            $commit = (& $git -C $cache rev-parse ("refs/heads/{0}" -f $Branch)).Trim()
            if ($LASTEXITCODE -ne 0 -or $commit -notmatch '^[0-9a-f]{40}$') { throw 'Unable to resolve release commit.' }
            if (!$current -or $current.Commit -ne $commit) {
                $version = Join-Path $InstallRoot ('versions\' + $commit)
                New-Item -ItemType Directory -Force -Path $version | Out-Null
                $archive = Join-Path $InstallRoot ($commit + '.zip')
                # Never deploy tracked shop databases, old target binaries, or installer files.
                Run-Checked $git @('-C', $cache, 'archive', '--format=zip', "--output=$archive", $commit, 'src', 'pom.xml') $InstallRoot
                Expand-Archive -LiteralPath $archive -DestinationPath $version -Force
                Remove-Item -LiteralPath $archive
                $project = [xml](Get-Content -LiteralPath (Join-Path $version 'pom.xml') -Raw)
                if ($project.project.properties.'pos.update.protocol' -ne '1') {
                    throw 'This Git commit predates the safe updater protocol. Publish the updater-compatible release first.'
                }
                Run-Checked $maven @('-B', 'clean', 'verify') $version
                # clean verify leaves one application JAR plus shade's original-* backup.
                $jars = @(Get-ChildItem -LiteralPath (Join-Path $version 'target') -Filter '*.jar' -File |
                    Where-Object { $_.Name -notmatch '^original-|-(sources|javadoc|tests|shaded)\.jar$' })
                if ($jars.Count -ne 1) { throw 'Expected exactly one packaged application JAR.' }
                $jar = $jars[0].FullName
                Run-Checked (Get-Command java -ErrorAction Stop).Source @('-jar', $jar, '--verify-runtime') $version
                $checksum = (Get-FileHash -LiteralPath $jar -Algorithm SHA256).Hash
                $pointer = Join-Path $InstallRoot 'current.json'
                if (Test-Path -LiteralPath $pointer) { Copy-Item -LiteralPath $pointer -Destination (Join-Path $InstallRoot 'previous.json') -Force }
                $temporary = Join-Path $InstallRoot 'current.next.json'
                @{ commit=$commit; sha256=$checksum; jar=[IO.Path]::GetFileName($jar) } | ConvertTo-Json | Set-Content -LiteralPath $temporary -Encoding UTF8
                Move-Item -LiteralPath $temporary -Destination $pointer -Force
                $current = Read-Current
            }
        } catch {
            if (!$current) { throw }
            Write-Warning "Update unavailable; using installed version $($current.Commit). $($_.Exception.Message)"
        }
    }
    if (!$current) { throw 'No verified application is installed yet. Connect to Git and run this launcher once.' }
    if ($CheckOnly) { Write-Output "Verified application: $($current.Commit) ($([IO.Path]::GetFileName($current.Jar)))"; return }
    $java = (Get-Command java -ErrorAction Stop).Source
    $arguments = @()
    $settings = Join-Path $InstallRoot 'runtime.properties'
    if (Test-Path -LiteralPath $settings) {
        foreach ($line in Get-Content -LiteralPath $settings) {
            if ($line -match '^\s*(pos\.[A-Za-z0-9.]+)\s*=(.*)$') { $arguments += ('-D' + $matches[1] + '=' + $matches[2].Trim()) }
        }
    }
    # The lock stays held until the cashier closes the app. Never replace a running application.
    & $java @arguments '-jar' $current.Jar
    if ($LASTEXITCODE -ne 0) { throw "Point of Sale exited with code $LASTEXITCODE. Previous version is recorded in previous.json." }
} finally {
    $env:GIT_TERMINAL_PROMPT = $oldPrompt
    if ($locked) { $mutex.ReleaseMutex() }
    $mutex.Dispose()
}
