# Point of Sale

Java/JavaFX desktop POS. Existing B2C offline-first checkout and B2B online-first validation are retained.

## Automatic product synchronization

Every terminal starts its own daemon product worker at application startup. It waits 5–15 seconds initially, then polls its authenticated TIN/site roughly every 60–69 seconds after a successful pass. Failures use exponential backoff, capped at 15 minutes plus jitter. A missing activation/token leaves it waiting; it retries after activation without requiring a restart. Manual **Fetch Products** joins the same in-flight request instead of starting an overlapping download.

The existing server endpoint returns a full catalogue, with no pagination, cursor, revision token or push notification. The client therefore downloads to a temporary file, validates the response envelope, streams products in bounded batches (200 by default), and commits each batch in a short SQLite transaction. No network wait holds a database transaction. A failed batch rolls back; completed batches remain usable and the next pass idempotently retries the catalogue. Temporary downloads are deleted. The Products screen reads in the background and adds display rows in batches when synchronization completes; checkout still reads the local database.

Server values win for names, descriptions, prices, units, tax assignments, expiry and stock thresholds. Local `Discount` is preserved. Quantity uses server stock minus this terminal's pending product quantities, provided the local sales revision has not changed since the download began. Invoice/line triggers detect sales, voids and sync status changes during a download; affected passes preserve local quantities and retry on the next pass. Existing cart lines and historical invoices are not repriced.

This is eventual consistency, not a cross-terminal stock reservation system. An ambiguous server acceptance can temporarily cause pending quantities to be subtracted twice until the existing-invoice response reconciles state and the next catalogue arrives. No products are deleted merely because a response omits them: the current API provides no deletion/tombstone contract. A server-side paged change feed would be required for efficient incremental network synchronization and explicit deletions.

Optional Java system properties:

| Property | Default | Purpose |
|---|---|---|
| `pos.products.pollSeconds` | `60` | Successful poll interval; minimum 15 seconds |
| `pos.products.batchSize` | `200` | SQLite batch size; bounded to 10–1000 |
| `pos.api.baseUrl` | Existing development URL | Explicit terminal API URL, including `/api/v1` |
| `pos.receipts.baseUrl` | Existing receipt-validation URL | Offline receipt URL |
| `pos.database.path` | Existing debugger/AppData selection | Explicit DB location; tests use a unique temporary database |

## Offline invoice compatibility

Startup adds the nullable `Invoices.Payload` column if absent; it never backfills or removes invoice data. `Helper.saveTransaction` saves the generated JSON with the invoice, line items, taxes and levies in the same SQLite transaction. Offline checkout now generates its existing receipt signature before committing, preventing a background worker from transmitting a half-prepared invoice.

All three resend paths prefer stored JSON. They preserve header/configuration versions, buyer/VAT5 details, monetary values, discounts and unknown JSON fields. Only `invoiceSummary.offlineSignature` is added/refreshed from `OfflineTransactionSignature` before sending. The original stored JSON is not rewritten during sync.

If `Payload` is NULL or blank, that path's existing legacy builder still runs. Its existing calculations are intentionally retained; this release does not reinterpret old shop data. Invalid or mismatched snapshots remain pending and are logged rather than silently replaced with reconstructed values. A new snapshot waiting for its signature remains pending.

Manual and automatic submissions share a bounded worker pool and an invoice-number guard. They recheck transmitted state before HTTP, retain pending state on failure, and reconcile the existing API's `Invoice Number already exists` response on HTTP 200 as well as other status codes. The same invoice number is always reused. Server invoice-number uniqueness remains necessary after ambiguous responses, crashes, or separate processes. Manual batch workers wait for actual submission completion instead of launching unlimited HTTP threads.

## Git-based client updates

Product data is supplied by the server API, **not Git commits**. Repository pushes deliver application code; each running terminal's product poll independently picks up central catalogue changes.

`.github/workflows/verify.yml` builds/tests pushes and pull requests to `master` on Windows/JDK 21 and publishes a downloadable build artifact. Configure branch protection to require this check before merging. The repository and workflows must be pushed to GitHub before they can run; this local implementation does not itself publish or change shop installations.

For direct Git-based updates, provision Git, JDK 21+, Maven 3.9+, repository read credentials (if private), and `scripts/Start-POS.ps1` once on each terminal. Use the launcher instead of the old installed executable:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Start-POS.ps1
```

The launcher checks `https://github.com/misheckzuze/PointOfSale.git`, branch `master`, at startup. It archives only `src` and `pom.xml` into a commit-specific directory under `%LOCALAPPDATA%\PointOfSaleGit`, runs `mvn clean verify`, verifies the executable JAR, then switches the current pointer. Tracked databases, `target`, and old installers are never copied from Git. A lock prevents concurrent launches/updates from the same installation. Running sales are never interrupted; updates take effect at the next launch. Failed fetches/builds use the previous verified application. First use requires network access and a successful build. Commits without the `pos.update.protocol=1` marker are rejected before running the application, so older releases cannot accidentally start checkout during verification. Later offline use is available with `-Offline`; `-CheckOnly` installs/verifies without opening the POS.

Per-terminal settings belong in `%LOCALAPPDATA%\PointOfSaleGit\runtime.properties`, outside version directories:

```properties
pos.api.baseUrl=https://your-api.example/api/v1
pos.receipts.baseUrl=https://your-portal.example/ReceiptValidation/Validate
pos.products.pollSeconds=60
pos.products.batchSize=200
```

The installed database remains in its existing AppData location. No database is moved or replaced. The launcher retains `previous.json` and previous version directories; to roll back while the POS is closed, replace `current.json` with `previous.json` and start with `-Offline`. The JAR checksum is checked before launch. Protect repository write access because clients run code from the configured branch. The launcher itself is a one-time bootstrap and is not overwritten by application updates.

## Verification

```powershell
mvn -B clean verify
java -jar target/PointOfSale-1.0.jar --verify-runtime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Test-GitUpdater.ps1
```

Java tests use a disposable SQLite database and loopback HTTP server, never the shop database or live API. They cover additive migration, snapshot fidelity, legacy fallback, corrupt/mismatched JSON, atomic saves, 1,205-product batches, replay, partial failures, stock races, local discounts, site filtering and concurrent/duplicate submissions. The updater tests use a disposable local Git repository to exercise first install, new commits, failed builds, offline fallback and retained settings. The runtime diagnostic exits before database initialization or JavaFX startup. Physical printing, real server credentials and rollout to shops require a deployment smoke test.

The repository already tracks generated binaries and a local database. `.gitignore` prevents new additions but deliberately does not untrack or delete existing files. Review the source-only diff before committing; do not stage shop database or generated binary changes.
