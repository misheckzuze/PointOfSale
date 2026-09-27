param([string]$Launcher = (Join-Path $PSScriptRoot 'Start-POS.ps1'))
$ErrorActionPreference = 'Stop'
$testRoot = Join-Path $env:TEMP ('pos-updater-test-' + [Guid]::NewGuid().ToString('N'))
$repository = Join-Path $testRoot 'remote'
$installation = Join-Path $testRoot 'installation'
$launcherPath = [IO.Path]::GetFullPath($Launcher)
function Check($condition, $message) { if (!$condition) { throw $message }; Write-Output "PASS $message" }
function Invoke-FixtureGit([string[]]$GitArguments) {
    & git -C $repository @GitArguments | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'Fixture Git command failed' }
}
function Launch([string[]]$Extra = @()) {
    $ErrorActionPreference = 'Continue'
    & powershell -NoProfile -ExecutionPolicy Bypass -File $launcherPath -Repository $repository -InstallRoot $installation -CheckOnly @Extra *> (Join-Path $testRoot 'launcher.log')
    $exitCode = $LASTEXITCODE
    $ErrorActionPreference = 'Stop'
    if ($exitCode -ne 0) { Get-Content (Join-Path $testRoot 'launcher.log') -Tail 20; throw 'Updater failed' }
}
try {
    New-Item -ItemType Directory -Force -Path (Join-Path $repository 'src/main/java'), (Join-Path $repository 'MQPointOfSale') | Out-Null
    @'
<project xmlns="http://maven.apache.org/POM/4.0.0"><modelVersion>4.0.0</modelVersion><groupId>test</groupId><artifactId>PointOfSale</artifactId><version>1.0</version>
<properties><pos.update.protocol>1</pos.update.protocol></properties><build><plugins><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-compiler-plugin</artifactId><version>3.8.0</version><configuration><source>15</source><target>15</target></configuration></plugin>
<plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-jar-plugin</artifactId><version>3.4.2</version><configuration><archive><manifest><mainClass>FixtureApp</mainClass></manifest></archive></configuration></plugin></plugins></build></project>
'@ | Set-Content -LiteralPath (Join-Path $repository 'pom.xml')
    'public class FixtureApp { public static void main(String[] args) { System.out.println("verified fixture"); } }' | Set-Content -LiteralPath (Join-Path $repository 'src/main/java/FixtureApp.java')
    'shop database must never deploy' | Set-Content -LiteralPath (Join-Path $repository 'MQPointOfSale/EISPointOfSaleDb.db')
    & git init --initial-branch=master $repository | Out-Null
    Invoke-FixtureGit @('config', 'user.email', 'fixture@example.invalid'); Invoke-FixtureGit @('config', 'user.name', 'Updater Test')
    Invoke-FixtureGit @('add', '.'); Invoke-FixtureGit @('commit', '-m', 'Initial fixture')
    Launch
    $first = Get-Content -LiteralPath (Join-Path $installation 'current.json') -Raw | ConvertFrom-Json
    Check ($first.commit -match '^[0-9a-f]{40}$') 'first Git version builds, verifies and installs'
    Check (!(Test-Path -LiteralPath (Join-Path $installation ('versions/' + $first.commit + '/MQPointOfSale')))) 'tracked shop databases are excluded from deployment'
    'pos.api.baseUrl=https://custom.example.invalid/api/v1' | Set-Content -LiteralPath (Join-Path $installation 'runtime.properties')
    '// version two' | Add-Content -LiteralPath (Join-Path $repository 'src/main/java/FixtureApp.java')
    Invoke-FixtureGit @('add', '.'); Invoke-FixtureGit @('commit', '-m', 'Next fixture')
    Launch
    $second = Get-Content -LiteralPath (Join-Path $installation 'current.json') -Raw | ConvertFrom-Json
    Check ($second.commit -ne $first.commit) 'next Git commit updates automatically at launch'
    Check ((Get-Content -LiteralPath (Join-Path $installation 'previous.json') -Raw | ConvertFrom-Json).commit -eq $first.commit) 'previous verified version remains available'
    Check ((Get-Content -LiteralPath (Join-Path $installation 'runtime.properties') -Raw).Contains('custom.example.invalid')) 'terminal runtime settings survive code updates'
    'this is invalid Java' | Add-Content -LiteralPath (Join-Path $repository 'src/main/java/FixtureApp.java')
    Invoke-FixtureGit @('add', '.'); Invoke-FixtureGit @('commit', '-m', 'Broken fixture')
    Launch
    Check ((Get-Content -LiteralPath (Join-Path $installation 'current.json') -Raw | ConvertFrom-Json).commit -eq $second.commit) 'failed build keeps the last verified version'
    Launch @('-Offline')
    Check ((Get-Content -LiteralPath (Join-Path $installation 'current.json') -Raw | ConvertFrom-Json).commit -eq $second.commit) 'offline launcher uses cached verified application'
    $pomPath = Join-Path $repository 'pom.xml'
    $oldPom = [IO.File]::ReadAllText($pomPath).Replace('<pos.update.protocol>1</pos.update.protocol>', '')
    [IO.File]::WriteAllText($pomPath, $oldPom)
    Invoke-FixtureGit @('add', '.'); Invoke-FixtureGit @('commit', '-m', 'Pre-updater fixture')
    Launch
    Check ((Get-Content -LiteralPath (Join-Path $installation 'current.json') -Raw | ConvertFrom-Json).commit -eq $second.commit) 'pre-updater versions are rejected before any application execution'
    Write-Output '8 Git updater integration checks passed.'
} finally {
    $resolved = [IO.Path]::GetFullPath($testRoot)
    $temp = [IO.Path]::GetFullPath($env:TEMP).TrimEnd('\') + '\'
    if (!$resolved.StartsWith($temp, [StringComparison]::OrdinalIgnoreCase) -or (Split-Path $resolved -Leaf) -notlike 'pos-updater-test-*') { throw 'Unexpected test cleanup path' }
    if (Test-Path -LiteralPath $resolved) { Remove-Item -LiteralPath $resolved -Recurse -Force }
}
