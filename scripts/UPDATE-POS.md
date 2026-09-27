# Updating a POS terminal

## Prepare the release

1. Set the application version in the root `pom.xml` (currently `1.1`). Future version changes do not require editing the launcher.
2. Commit and push the source, `pom.xml`, scripts and workflow to `master`. Do not commit shop databases or generated `target` files.
3. Check that the GitHub **Verify Point of Sale** workflow passes for that commit.

## Switch one terminal first

1. Finish the current sale and close POS. Back up its existing database.
2. Ensure Git, Maven and JDK 21 or newer are installed and available through `git`, `mvn` and `java` in PowerShell. The terminal needs repository access and connectivity for the initial build.
3. Copy the updated `scripts/Start-POS.ps1` to a permanent location, for example `C:\POS\Start-POS.ps1`. The original launcher does not update itself, so this replacement is needed once on every terminal using it.
4. Keep the terminal's settings in `%LOCALAPPDATA%\PointOfSaleGit\runtime.properties`. If the old installation uses a custom database location, set `pos.database.path` to that existing database's absolute path before opening POS. Use the same Windows account as the existing POS installation. Do not copy the development database into the shop.
5. Run this check in PowerShell; it downloads, builds and verifies the application without opening the POS or its database:

   ```powershell
   powershell -NoProfile -ExecutionPolicy Bypass -File "C:\POS\Start-POS.ps1" -CheckOnly
   ```

   Confirm that the final `Verified application` line names `PointOfSale-1.1.jar`. An `Update unavailable` warning means it retained the previous version; resolve the preceding error before proceeding.

6. Start the application:

   ```powershell
   powershell -NoProfile -ExecutionPolicy Bypass -File "C:\POS\Start-POS.ps1"
   ```

7. Confirm the terminal configuration, products and existing pending invoices are present. Set the terminal shortcut to this launcher instead of the old executable. Repeat on other terminals after checking the first one.

Updates are checked at launch, not while a cashier is selling. A failed update preserves the verified cached application. The launcher records its filename and checksum in `current.json` and retains the preceding pointer in `previous.json`; old 1.0 pointers remain supported. Product synchronization is separate from application updates.
