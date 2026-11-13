#!/usr/bin/env python3
"""バックアップ確認後、既存DBを保持して認証機能のV4移行と必要な追加権限だけを適用する。"""
import argparse
import datetime
import getpass
import importlib.util
import json
import os
from pathlib import Path
import re
import subprocess
import sys

ROOT = Path(__file__).resolve().parent.parent

def module(name, path):
    spec = importlib.util.spec_from_file_location(name, path)
    result = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(result)
    return result

setup = module('overload_cloud_setup', ROOT / 'tools/prepare-cloud-db.py')


def upgrade(owner, schema, app_user, migrate):
    """V3/V4以外では停止し、DDLの失敗後も既存DBを保持する。"""
    if not re.fullmatch(r'[a-z][a-z0-9_]{0,63}', schema):
        raise setup.PreparationError('DB名が不正です。')
    if not re.fullmatch(r'[A-Za-z0-9_.]{1,32}', app_user) or app_user == owner.user:
        raise setup.PreparationError('実行専用ユーザーを指定してください。')
    prefix = '`' + schema + '`.'
    version = owner.sql('SELECT version FROM ' + prefix
                        + '`flyway_schema_history` WHERE success=1 AND version IS NOT NULL ORDER BY installed_rank DESC LIMIT 1')
    if version not in ('3', '4'):
        raise setup.PreparationError('移行履歴が想定したV3/V4ではないため停止しました。')
    if owner.sql("SELECT COUNT(*) FROM mysql.user WHERE User='" + app_user + "' AND Host='%'") != '1':
        raise setup.PreparationError('既存の実行専用ユーザーを特定できません。ユーザーは作成しません。')
    counts = {table: owner.sql('SELECT COUNT(*) FROM ' + prefix + '`' + table + '`')
              for table in ('users', 'user_credentials', 'workout_sessions', 'workout_sets')}
    # baseline/cleanは呼ばず、Flywayのチェックサム確認を含む通常のmigrateだけを実行する。
    migrate()
    if owner.sql('SELECT version FROM ' + prefix
                 + '`flyway_schema_history` WHERE success=1 AND version IS NOT NULL ORDER BY installed_rank DESC LIMIT 1') != '4':
        raise setup.PreparationError('V4の移行完了を確認できません。権限追加前に停止しました。')
    owner.sql('GRANT UPDATE ON ' + prefix + "`user_credentials` TO '" + app_user + "'@'%'",
              'GRANT SELECT,INSERT ON ' + prefix + "`google_accounts` TO '" + app_user + "'@'%'")
    return counts


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--host', required=True)
    parser.add_argument('--port', type=int, default=4000)
    parser.add_argument('--owner-user', required=True)
    parser.add_argument('--app-user', required=True)
    parser.add_argument('--schema', default='overload_app')
    parser.add_argument('--output-dir', type=Path, required=True)
    args = parser.parse_args()
    if not sys.stdin.isatty():
        parser.error('所有者本人がターミナルで非表示入力してください。')
    print('対象DB：' + args.schema)
    print('V4移行：認証世代列の追加・パスワード未設定の許可・Google連携表の追加。')
    print('権限追加：user_credentialsのUPDATE、google_accountsのSELECT/INSERTのみ。')
    print('DB・既存データ・ユーザー・パスワードは削除、上書きしません。')
    print('失敗時は途中の状態を保持して停止します。移行後のDBを自動で戻しません。')
    if input('バックアップの日時と復旧手段を確認済みの場合だけ「更新」と入力：').strip() != '更新':
        return 0
    owner_password = getpass.getpass('TiDB管理者の接続パスワード：')
    app_password = getpass.getpass('既存の実行専用ユーザーの接続パスワード：')
    if not owner_password or not app_password:
        print('未入力のため、接続前に停止しました。')
        return 2
    stage = '接続・制約確認'
    try:
        owner = setup.MySqlClient(args.host, args.port, args.owner_user, owner_password)
        if owner.sql('SELECT @@GLOBAL.tidb_enable_check_constraint, @@GLOBAL.tidb_enable_foreign_key, @@SESSION.foreign_key_checks') != '1\t1\t1':
            raise setup.PreparationError('CHECK・外部キーが有効ではありません。')
        app = setup.MySqlClient(args.host, args.port, args.app_user, app_password)
        app.sql('SELECT COUNT(*) FROM `' + args.schema + '`.user_credentials WHERE 1=0')
        launcher = module('overload_migration_launcher', ROOT / 'tools/run-backend.py')
        artifact = launcher.prepare_artifact(ROOT / 'backend/build/libs/overload-api-0.0.1-SNAPSHOT.jar', ROOT / '.run')
        url = ('jdbc:mysql://' + args.host + ':' + str(args.port) + '/' + args.schema
               + '?sslMode=VERIFY_IDENTITY&connectTimeout=10000&socketTimeout=15000')
        def migrate():
            env = dict(os.environ, OVERLOAD_DB_URL=url, OVERLOAD_MIGRATION_USER=args.owner_user,
                       OVERLOAD_MIGRATION_PASSWORD=owner_password)
            result = subprocess.run(['java', '-jar', str(artifact), '--migrate-only'], env=env,
                                    stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=600)
            if result.returncode:
                # 接続情報やSQL本文を含む可能性のある生ログは表示・保存しない。
                raise setup.PreparationError('移行に失敗しました。DBは保持しています。原因調査まで公開更新を停止します。')
        stage = '移行・必要権限の追加'
        before = upgrade(owner, args.schema, args.app_user, migrate)
        stage = '実行ユーザーの確認'
        app.sql('SELECT auth_version FROM `' + args.schema + '`.user_credentials WHERE 1=0',
                'SELECT user_id FROM `' + args.schema + '`.google_accounts WHERE 1=0',
                'UPDATE `' + args.schema + '`.user_credentials SET auth_version=auth_version WHERE 1=0',
                'INSERT INTO `' + args.schema + '`.google_accounts(user_id,google_subject) SELECT user_id,google_subject FROM `'
                + args.schema + '`.google_accounts WHERE 1=0')
        report = dict(status='migrated-not-deployed', version='4',
                      completedAt=datetime.datetime.now(datetime.timezone.utc).isoformat(),
                      schema=args.schema, beforeCounts=before, secretsStored=False)
        args.output_dir.mkdir(parents=True, exist_ok=True)
        (args.output_dir / 'account-security-db-upgrade.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n')
        print('V4移行と必要権限の確認が完了しました。アプリの公開更新は別の作業です。')
        return 0
    except (setup.PreparationError, OSError, subprocess.SubprocessError) as error:
        print('停止した段階：' + stage)
        print(str(error) if isinstance(error, setup.PreparationError) else '処理に失敗しました。秘密を含む詳細は表示しません。')
        return 1
    finally:
        owner_password = app_password = None

if __name__ == '__main__':
    raise SystemExit(main())
