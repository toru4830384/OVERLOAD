# Backend tests

Use Java 17 and MySQL 8.0.16 or later (CHECK constraints must be enforced).
The schema was captured from the local MySQL 9.6 database on 2026-09-28.

```sh
cd backend
./gradlew clean test
```

## Isolated MySQL tests

`MySqlIntegrationTest` supplies the connection settings for the application context
and database tests. Each test JVM creates its own randomly named
`overload_review_<uuid>` database and loads `src/test/resources/schema-test.sql`.
The tests never clear `overload_db` or `overload_test_db`. The database is removed
when the test JVM exits normally. A forcibly killed JVM may leave its database
behind; remove only that explicitly identified temporary database.

The MySQL server must already be running. The following environment variables
can override the defaults:

| Variable | Default |
| --- | --- |
| `OVERLOAD_TEST_MYSQL_SERVER` | `jdbc:mysql://127.0.0.1:3306/` |
| `OVERLOAD_TEST_MYSQL_USER` | `root` |
| `OVERLOAD_TEST_MYSQL_PASSWORD` | empty |

The server URL must end in `/` and contain no database name or query parameters.
Use a dedicated test server/account with permission to create/drop the temporary
database and create tables/triggers. Do not use a production server.
Tests share the isolated schema within a JVM; keep JUnit parallel execution disabled.
The schema file is a test baseline, not an automatic production migration.
It preserves the inspected database constraints, including the existing allowance
for zero DB weight; the API and Service require at least 0.01kg.

The rollback test installs a trigger only in this temporary database. The trigger
rejects the second set only after confirming that the first set was inserted in
the same session. The test checks the unique failure marker and verifies that both
session and set rows are absent after the Service transaction rolls back. A normal
commit test and an A→B→A registration/history round trip provide positive controls.
The test itself must not be annotated `@Transactional`, which could hide a missing
Service transaction.

Generated IDs in fixtures come from the INSERT's generated keys, not a later
`LAST_INSERT_ID()` call on a potentially different pooled connection.

## Workout timestamp and API mapping contract

The existing MySQL DATETIME values are interpreted as Asia/Tokyo local time.
`WorkoutTime` defines this explicitly, independently of the JVM default timezone.
POST `/api/workouts` requires an offset-bearing `startedAt` from the start action.
The service preserves its instant and stores it in Japan time. The request also
contains the frozen completion time (`finishedAt`), start-time `bodyWeightKg`, and
a workout UUID (`clientRequestId`). Retry requests reuse those same values. History and session APIs return offset-bearing
ISO 8601 timestamps; an unfinished session keeps `finishedAt: null`.
Changing the database time convention requires migrating existing values first.

API DTOs hold data only. Stateless `controller.mapper.*ApiMapper` classes perform
request and response conversions. SQL/MyBatis mappers remain in `mapper`.
`WorkoutTimeTest` checks multiple JVM default timezones and
`WorkoutTimestampContractTest` checks the HTTP timestamp format on both endpoints.

## Workout persistence migration

Apply `db/migrations/20261010_workout_snapshot.sql` once before starting the updated
backend against an existing database. It adds nullable calorie/weight snapshots,
a request UUID, a SHA-256 input fingerprint, and a per-user unique request key.
The local `overload_db` has been migrated; the 56 existing sessions were preserved
without inventing historical calories. Other environments require the same migration.
The test baseline already includes these columns and the unique key.

The Service computes and stores METs calories, including rest time, rounded to
two decimals. History returns this recorded value, even after profile changes.
Legacy records with no snapshot return null; the UI marks them as unrecorded.
INSERT duplicate-key handling and locking reads serialize concurrent retries.
Identical retries return existing sets; conflicting input returns HTTP 409.
Integration tests cover concurrent requests, retry, fingerprint conflicts,
snapshot restoration, and rollback followed by a successful retry.

Frontend checks: `npm test`, `npm run lint`, and `npm run build` from the repository
root. Profile saves run in a serial queue, collapse pending edits, and expose
loading/saving/error states with retry actions. No default 65kg is substituted.

## In-progress workout recovery and integer input

`src/features/workouts/draft.js` persists an in-progress workout in browser
localStorage under a versioned per-user key. Initialization only reads the draft;
updates acquire a browser Web Lock and compare the last read value before writing,
including notes and unfinished inputs. Pending writes warn before page exit. A completion
attempt persists its UUID, finish time and payload before POST; persistence failure
prevents sending. A stale tab cannot overwrite or delete a newer draft. The draft remains
until POST and canonical history retrieval succeed, so a reload after an unknown
network outcome can reuse the same idempotency key. Storage failures are shown
with retry controls; corrupt data is retained until explicitly discarded.
The browser origin and user must stay the same to recover a draft. Web Locks must
be available (HTTPS or localhost); unsupported browsers show a storage error.
Confirmed POST rejection (400/404/422) permits correction; network/server errors
and failure to read history after a successful POST keep the frozen payload.
409 preserves the draft and offers history inspection or explicit discard.

`tests/workout-draft.test.js` exercises reload, failed storage/cleanup, frozen
retry input and corrupt records. `tests/repetitions.test.js` verifies integer
bounds and scientific notation. Jackson float-to-integer coercion is disabled;
`WorkoutIntegerInputContractTest` checks rejection before the Service call.
`httpHistoryKeepsRestCaloriesAndStartWeightAfterReload` checks the review example
(80kg, 60 minutes, 10 reps = 170.10kcal) against isolated MySQL and HTTP history,
including a later profile change. Daily and monthly totals share the same
`summarizeCalories` function and distinguish unrecorded calories from zero.
