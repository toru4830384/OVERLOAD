"""公開DBの更新手順が既存データを保ち、必要な権限だけを加えることを隔離MySQLで確認する。"""
import importlib.util
from pathlib import Path
import unittest
import uuid

spec = importlib.util.spec_from_file_location('security_upgrade', Path(__file__).with_name('upgrade-account-security.py'))
upgrade = importlib.util.module_from_spec(spec)
spec.loader.exec_module(upgrade)

class AccountSecurityUpgradeTest(unittest.TestCase):
    def setUp(self):
        token = uuid.uuid4().hex
        self.schema = 'overload_upgrade_' + token
        self.user = 'upgrade_test_' + token[:16]
        self.owner = upgrade.setup.MySqlClient('127.0.0.1', 3306, 'root', '', verify_identity=False)
        self.owner.sql('CREATE DATABASE `' + self.schema + '`')
        self.prefix = '`' + self.schema + '`.'
        self.owner.sql('CREATE TABLE ' + self.prefix + 'flyway_schema_history(installed_rank INT,version VARCHAR(50),success BOOL)',
                       'INSERT INTO ' + self.prefix + "flyway_schema_history VALUES(3,'3',1)",
                       'CREATE TABLE ' + self.prefix + 'users(id BIGINT PRIMARY KEY)',
                       'INSERT INTO ' + self.prefix + 'users VALUES(1)',
                       'CREATE TABLE ' + self.prefix + 'user_credentials(user_id BIGINT PRIMARY KEY,password_hash VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,draft_namespace VARCHAR(50))',
                       'INSERT INTO ' + self.prefix + "user_credentials VALUES(1,'unchanged-hash','unchanged-namespace')",
                       'CREATE TABLE ' + self.prefix + 'workout_sessions(id BIGINT PRIMARY KEY)',
                       'INSERT INTO ' + self.prefix + 'workout_sessions VALUES(1)',
                       'CREATE TABLE ' + self.prefix + 'workout_sets(id BIGINT PRIMARY KEY)',
                       'INSERT INTO ' + self.prefix + 'workout_sets VALUES(1)',
                       "CREATE USER '" + self.user + "'@'%' IDENTIFIED BY 'isolated-upgrade-only-4830'",
                       'GRANT SELECT,INSERT ON ' + self.prefix + "user_credentials TO '" + self.user + "'@'%'")

    def tearDown(self):
        self.owner.sql('DROP DATABASE IF EXISTS `' + self.schema + '`', "DROP USER IF EXISTS '" + self.user + "'@'%'")

    def migrate(self):
        script = (upgrade.ROOT / 'backend/src/main/resources/db/migration/V4__google_accounts_and_auth_version.sql').read_text()
        statements = [statement.strip() for statement in script.split(';') if statement.strip()]
        self.owner.sql('USE `' + self.schema + '`', *statements,
                       'INSERT INTO ' + self.prefix + "flyway_schema_history VALUES(4,'4',1)")

    def test_existing_credentials_records_and_limited_permissions_survive_upgrade(self):
        counts = upgrade.upgrade(self.owner, self.schema, self.user, self.migrate)
        self.assertEqual({'1'}, set(counts.values()))
        app = upgrade.setup.MySqlClient('127.0.0.1',3306,self.user,'isolated-upgrade-only-4830',verify_identity=False)
        self.assertEqual('unchanged-hash\tunchanged-namespace\t0', app.sql('SELECT password_hash,draft_namespace,auth_version FROM ' + self.prefix + 'user_credentials'))
        app.sql('UPDATE ' + self.prefix + 'user_credentials SET auth_version=1 WHERE user_id=1',
                'INSERT INTO ' + self.prefix + "google_accounts(user_id,google_subject) VALUES(1,'test-subject')")
        self.assertEqual('test-subject', app.sql('SELECT google_subject FROM ' + self.prefix + 'google_accounts'))
        for statement in ('DELETE FROM ' + self.prefix + 'google_accounts',
                          'DELETE FROM ' + self.prefix + 'user_credentials',
                          'ALTER TABLE ' + self.prefix + 'users ADD unexpected INT'):
            with self.subTest(statement=statement), self.assertRaises(upgrade.setup.PreparationError):
                app.sql(statement)

    def test_unexpected_version_stops_before_migration_or_grants(self):
        self.owner.sql('UPDATE ' + self.prefix + "flyway_schema_history SET version='2'")
        called=[]
        with self.assertRaises(upgrade.setup.PreparationError):
            upgrade.upgrade(self.owner,self.schema,self.user,lambda:called.append(True))
        self.assertEqual([],called)
        self.assertEqual('unchanged-hash',self.owner.sql('SELECT password_hash FROM ' + self.prefix + 'user_credentials'))

    def test_migration_failure_keeps_existing_data_and_stops_before_grants(self):
        def fail(): raise upgrade.setup.PreparationError('isolated migration failure')
        with self.assertRaises(upgrade.setup.PreparationError):
            upgrade.upgrade(self.owner,self.schema,self.user,fail)
        self.assertEqual('unchanged-hash',self.owner.sql('SELECT password_hash FROM ' + self.prefix + 'user_credentials'))
        app=upgrade.setup.MySqlClient('127.0.0.1',3306,self.user,'isolated-upgrade-only-4830',verify_identity=False)
        with self.assertRaises(upgrade.setup.PreparationError):
            app.sql('UPDATE ' + self.prefix + 'user_credentials SET password_hash=password_hash WHERE 1=0')
