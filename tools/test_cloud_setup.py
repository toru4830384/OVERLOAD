"""公開DB準備の権限境界と既存データ保護を、実際の隔離MySQLで確認する。"""
import importlib.util
from pathlib import Path
import unittest
import uuid

spec = importlib.util.spec_from_file_location('cloud_setup', Path(__file__).with_name('prepare-cloud-db.py'))
setup = importlib.util.module_from_spec(spec)
spec.loader.exec_module(setup)


class CloudSetupIntegrationTest(unittest.TestCase):
    def setUp(self):
        token = uuid.uuid4().hex
        self.schema = 'overload_setup_' + token
        self.user = 'overload_test_' + token[:16]
        # 引用符・バックスラッシュを含めても、保存した認証情報が壊れないことを確認する。
        self.password = "isolated-Pass'\\test-4830-long"
        self.owner = setup.MySqlClient('127.0.0.1', 3306, 'root', '', verify_identity=False)

    def tearDown(self):
        # この試験がUUIDから生成した名前だけを片付け、既存の名前は指定できない。
        self.owner.sql('DROP DATABASE IF EXISTS `' + self.schema + '`')
        self.owner.sql("DROP USER IF EXISTS '" + self.user + "'@'%'")

    def fixture_migration(self):
        for table in setup.PRIVILEGES:
            self.owner.sql('CREATE TABLE `' + self.schema + '`.`' + table
                           + '` (id INT PRIMARY KEY, name VARCHAR(50) NOT NULL)')

    def test_runtime_user_can_only_perform_needed_data_operations(self):
        setup.provision(self.owner, self.schema, self.user, self.password, self.fixture_migration)
        app = setup.MySqlClient('127.0.0.1', 3306, self.user, self.password, verify_identity=False)
        app.sql('INSERT INTO `' + self.schema + "`.users VALUES(1,'本人')")
        app.sql('UPDATE `' + self.schema + "`.users SET name='更新' WHERE id=1")
        self.assertEqual('更新', app.sql('SELECT name FROM `' + self.schema + '`.users WHERE id=1'))
        for statement in [
            'CREATE TABLE `' + self.schema + '`.unexpected(id INT)',
            'SELECT User FROM mysql.user',
            'DELETE FROM `' + self.schema + '`.users',
            'INSERT INTO `' + self.schema + "`.exercises VALUES(1,'改変')",
            'UPDATE `' + self.schema + "`.user_credentials SET name='改変'",
        ]:
            with self.subTest(statement=statement), self.assertRaises(setup.PreparationError):
                app.sql(statement)
        self.assertEqual('1', self.owner.sql('SELECT COUNT(*) FROM `' + self.schema + '`.users'))

    def test_existing_database_is_rejected_without_replacing_its_data(self):
        self.owner.sql('CREATE DATABASE `' + self.schema + '`')
        self.owner.sql('CREATE TABLE `' + self.schema + '`.sentinel(value INT)')
        self.owner.sql('INSERT INTO `' + self.schema + '`.sentinel VALUES(4830)')
        with self.assertRaises(setup.PreparationError):
            setup.provision(self.owner, self.schema, self.user, self.password, self.fixture_migration)
        self.assertEqual('4830', self.owner.sql('SELECT value FROM `' + self.schema + '`.sentinel'))
        self.assertEqual('0', self.owner.sql("SELECT COUNT(*) FROM mysql.user WHERE User='" + self.user + "'"))

    def test_migration_failure_keeps_new_database_and_does_not_create_account(self):
        def fail_migration():
            self.owner.sql('CREATE TABLE `' + self.schema + '`.diagnosis(value INT)')
            raise setup.PreparationError('移行の障害を注入')
        with self.assertRaises(setup.PreparationError):
            setup.provision(self.owner, self.schema, self.user, self.password, fail_migration)
        self.assertEqual('1', self.owner.sql("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='"
                                           + self.schema + "' AND table_name='diagnosis'"))
        self.assertEqual('0', self.owner.sql("SELECT COUNT(*) FROM mysql.user WHERE User='" + self.user + "'"))

    def test_remote_certificate_validation_cannot_be_disabled(self):
        with self.assertRaises(setup.PreparationError):
            setup.MySqlClient('db.example.com', 4000, 'user', 'secret', verify_identity=False)


if __name__ == '__main__':
    unittest.main()
