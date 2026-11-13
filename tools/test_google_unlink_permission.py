"""使い捨てMySQLの既存ユーザーへ1表の権限だけを加えることを検証する。"""
import importlib.util
from pathlib import Path
import unittest
import test_account_security_upgrade as fixtures
upgrade=fixtures.upgrade
spec=importlib.util.spec_from_file_location('unlink_permission',Path(__file__).with_name('enable-google-unlink.py'))
permission=importlib.util.module_from_spec(spec)
spec.loader.exec_module(permission)

class GoogleUnlinkPermissionTest(unittest.TestCase):
    setUp=fixtures.AccountSecurityUpgradeTest.setUp
    tearDown=fixtures.AccountSecurityUpgradeTest.tearDown
    migrate=fixtures.AccountSecurityUpgradeTest.migrate
    def test_only_link_delete_is_added_without_deleting_rows(self):
        upgrade.upgrade(self.owner,self.schema,self.user,self.migrate)
        app=upgrade.setup.MySqlClient('127.0.0.1',3306,self.user,'isolated-upgrade-only-4830',verify_identity=False)
        app.sql('INSERT INTO '+self.prefix+"google_accounts(user_id,google_subject) VALUES(1,'unchanged-subject')")
        permission.enable(self.owner,app,self.schema,self.user)
        self.assertEqual('1',app.sql('SELECT COUNT(*) FROM '+self.prefix+'google_accounts'))
        app.sql('DELETE FROM '+self.prefix+'google_accounts WHERE user_id=1')
        self.assertEqual('0',app.sql('SELECT COUNT(*) FROM '+self.prefix+'google_accounts'))
        for table in ('users','user_credentials','workout_sessions','workout_sets'):
            self.assertEqual('1',self.owner.sql('SELECT COUNT(*) FROM '+self.prefix+table))
            with self.assertRaises(upgrade.setup.PreparationError):
                app.sql('DELETE FROM '+self.prefix+table+' WHERE 1=0')
    def test_wrong_version_does_not_grant_delete(self):
        upgrade.upgrade(self.owner,self.schema,self.user,self.migrate)
        app=upgrade.setup.MySqlClient('127.0.0.1',3306,self.user,'isolated-upgrade-only-4830',verify_identity=False)
        self.owner.sql('UPDATE '+self.prefix+"flyway_schema_history SET version='5' WHERE version='4'")
        with self.assertRaises(permission.setup.PreparationError):
            permission.enable(self.owner,app,self.schema,self.user)
        with self.assertRaises(upgrade.setup.PreparationError):
            app.sql('DELETE FROM '+self.prefix+'google_accounts WHERE 1=0')
