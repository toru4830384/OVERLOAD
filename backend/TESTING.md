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
