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


## Workout calorie snapshots and retry persistence

Apply `db/migrations/20261010_workout_snapshot.sql` once before starting this
backend against an existing database. It adds nullable start-time weight and
calorie snapshots, a request UUID, an input fingerprint, and a per-user unique
request key. Existing sessions keep null snapshots; historical values are not
inferred from the current profile. The test schema includes these additions.

POST `/api/workouts` requires the start action's `startedAt`, the completion
operation's `finishedAt`, the start-time `bodyWeightKg`, and `clientRequestId`.
Retries must preserve this UUID, completion time, weight and all set input.
The Service stores METs calories including rest, rounded to two decimals; history
returns this stored result after reload and profile changes. Concurrent identical
requests return the same sets, while differing input for the same UUID returns
409. A calorie total outside DECIMAL(12,2) returns 422 before DB writes.

`WorkoutServiceIntegrationTest` verifies HTTP registration/history, rollback,
concurrent retry and profile changes against isolated MySQL. Its review regression
uses 80kg, 60 minutes and 10 reps: the saved and restored total is 170.10kcal.

## In-progress workout recovery

Browser localStorage keeps the start time, start weight, entries, notes and unfinished
inputs under a versioned per-user key. Each mutation acquires a Web Lock, compares
the last observed value, then writes or removes it; stale tabs do not overwrite a
newer draft. Web Locks require a supported browser on HTTPS or localhost.
Initialization is read-only and storage failures remain visible with retry actions.

Completion waits for queued writes before deciding whether sets exist, and persists
the exact UUID, finish time and payload before POST. A storage failure prevents POST.
Unknown network/server outcomes and history failures after POST keep that payload.
Only confirmed POST rejection (400/404/422) resumes editing. HTTP 409 preserves the
draft and offers history inspection or explicit discard. Draft cleanup occurs only
after POST plus canonical history retrieval, or an explicit user discard.

`tests/workout-draft.test.js` covers reload, same-tab ordering, tab conflicts, storage
failures, frozen retries and rejection recovery. `tests/api-errors.test.js` verifies
that HTTP status remains distinguishable from an unknown network failure.

## Repetitions input

Convert the complete frontend value with Number, then require an integer from 1
through 2147483647 before adding a set. Decimal input is rejected; 1e2 becomes 100.
Jackson float-to-integer coercion is disabled so direct JSON clients cannot bypass
the rule by sending decimals for reps, IDs or set numbers. `tests/repetitions.test.js`
and `WorkoutIntegerInputContractTest` cover those boundaries and unchanged values.
