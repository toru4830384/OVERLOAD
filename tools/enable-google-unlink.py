#!/usr/bin/env python3
"""既存V4のGoogle連携表だけに解除権限を追加する。データ移行や削除は実行しない。"""
import argparse
import datetime
import getpass
import importlib.util
import json
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parent.parent
spec = importlib.util.spec_from_file_location('cloud_setup', ROOT / 'tools/prepare-cloud-db.py')
setup = importlib.util.module_from_spec(spec)
spec.loader.exec_module(setup)


def enable(owner, app, schema, app_user):
    """接続本人と既存のV4を確認し、1表のDELETEだけを付与する。"""
    if not re.fullmatch(r'[a-z][a-z0-9_]{0,63}', schema):
        raise setup.PreparationError('DB名が不正です。')
    if not re.fullmatch(r'[A-Za-z0-9_.]{1,32}', app_user) or app_user == owner.user:
        raise setup.PreparationError('既存の実行専用ユーザーを指定してください。')
    if app.sql('SELECT CURRENT_USER()') != app_user + '@%':
        raise setup.PreparationError('実行ユーザーの接続先が一致しません。')
    prefix = '`' + schema + '`.'
    if owner.sql('SELECT version FROM ' + prefix + 'flyway_schema_history WHERE success=1 AND version IS NOT NULL ORDER BY installed_rank DESC LIMIT 1') != '4':
        raise setup.PreparationError('既存DBがV4ではないため停止しました。')
    if owner.sql("SELECT COUNT(*) FROM mysql.user WHERE User='" + app_user + "' AND Host='%'") != '1':
        raise setup.PreparationError('既存ユーザーを確認できません。')
    # 行データは読まず、対象の表と既存のSELECT権限だけを確認する。
    app.sql('SELECT user_id FROM ' + prefix + 'google_accounts WHERE 1=0')
    owner.sql('GRANT DELETE ON ' + prefix + "google_accounts TO '" + app_user + "'@'%'")
    # 0行条件で実行権限を確認し、既存の連携は解除しない。
    app.sql('DELETE FROM ' + prefix + 'google_accounts WHERE 1=0')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--host', required=True)
    parser.add_argument('--owner-user', required=True)
    parser.add_argument('--app-user', required=True)
    parser.add_argument('--schema', default='overload_app')
    parser.add_argument('--output-dir', type=Path, required=True)
    args = parser.parse_args()
    if not sys.stdin.isatty():
        parser.error('本人がターミナルで非表示入力してください。')
    print('対象DB：' + args.schema)
    print('Google連携表だけにDELETE権限を追加します。既存の連携・記録・パスワードは削除しません。')
    if input('承認済みの権限追加を行う場合は「追加」と入力：').strip() != '追加':
        return 0
    owner_password = getpass.getpass('TiDB管理者のDB接続パスワード：')
    app_password = getpass.getpass('既存の実行専用ユーザーのDB接続パスワード：')
    if not owner_password or not app_password:
        print('未入力のため停止しました。'); return 2
    try:
        owner = setup.MySqlClient(args.host,4000,args.owner_user,owner_password)
        app = setup.MySqlClient(args.host,4000,args.app_user,app_password)
        enable(owner,app,args.schema,args.app_user)
        args.output_dir.mkdir(parents=True,exist_ok=True)
        (args.output_dir/'google-unlink-permission.json').write_text(json.dumps(dict(status='verified',schema=args.schema,permission='DELETE on google_accounts only',completedAt=datetime.datetime.now(datetime.timezone.utc).isoformat(),secretsStored=False),indent=2))
        print('Google連携表の解除権限を確認しました。公開更新は別の作業です。')
        return 0
    except setup.PreparationError as error:
        print(str(error)); print('停止しました。公開更新は原因確認後に行います。'); return 1
    finally:
        owner_password = app_password = None

if __name__ == '__main__':
    raise SystemExit(main())
