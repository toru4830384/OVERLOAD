#!/usr/bin/env python3
"""本人の非表示入力で、新しい公開用DBと権限を限定した実行用ユーザーを準備する。"""
import argparse
import datetime
import functools
import getpass
import importlib.util
import json
import os
from pathlib import Path
import re
import shutil
import struct
import subprocess
import sys
import tempfile
import zipfile

ROOT = Path(__file__).resolve().parent.parent
PRIVILEGES = {
    'users': 'SELECT,INSERT,UPDATE',
    'user_credentials': 'SELECT,INSERT,UPDATE',
    'google_accounts': 'SELECT,INSERT',
    'workout_sessions': 'SELECT,INSERT,UPDATE',
    'workout_sets': 'SELECT,INSERT',
    'SPRING_SESSION': 'SELECT,INSERT,UPDATE,DELETE',
    'SPRING_SESSION_ATTRIBUTES': 'SELECT,INSERT,UPDATE,DELETE',
    'exercises': 'SELECT', 'muscle': 'SELECT', 'exercise_muscles': 'SELECT',
}


class PreparationError(Exception):
    """秘密やSQL本文を含めずに、停止した段階を所有者へ通知する。"""


@functools.lru_cache(maxsize=1)
def prepare_sql_client():
    """検証済みアプリのJDBCドライバーと専用クラスを、上書きしない実行先へ固定する。"""
    java, javac = shutil.which('java'), shutil.which('javac')
    if not java or not javac:
        raise PreparationError('Java 17の実行・コンパイル環境が必要です。')
    jar = ROOT / 'backend/build/libs/overload-api-0.0.1-SNAPSHOT.jar'
    try:
        with zipfile.ZipFile(jar) as archive:
            drivers = [name for name in archive.namelist()
                       if name.startswith('BOOT-INF/lib/mysql-connector-j-') and name.endswith('.jar')]
            if len(drivers) != 1:
                raise PreparationError('アプリに同梱されたJDBCドライバーを特定できません。')
            driver = archive.read(drivers[0])
    except (OSError, zipfile.BadZipFile):
        raise PreparationError('先に公開用JARのビルドを完了してください。') from None
    releases = ROOT / '.run'
    releases.mkdir(parents=True, exist_ok=True)
    runtime = Path(tempfile.mkdtemp(prefix='db-client-', dir=releases))
    (runtime / 'mysql-connector.jar').write_bytes(driver)
    shutil.copyfile(ROOT / 'tools/CloudDbSql.java', runtime / 'CloudDbSql.java')
    result = subprocess.run([javac, '-encoding', 'UTF-8', '--release', '17', '-d', str(runtime),
                             str(runtime / 'CloudDbSql.java')], capture_output=True, timeout=45)
    if result.returncode:
        raise PreparationError('DB準備用のJavaクラスを作成できませんでした。')
    for path in runtime.iterdir():
        path.chmod(0o444)
    return (java, '--class-path', str(runtime) + os.pathsep + str(runtime / 'mysql-connector.jar'), 'CloudDbSql')


class MySqlClient:
    """秘密を引数や設定ファイルに置かず、アプリと同じJDBCで検証済みTLS接続する。"""
    def __init__(self, host, port, user, password, verify_identity=True):
        if not re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9.-]{0,252}', host) or not 1 <= port <= 65535:
            raise PreparationError('接続先が不正です。')
        if not verify_identity and host not in ('localhost', '127.0.0.1'):
            raise PreparationError('遠隔接続のTLS検証は無効にできません。')
        self.host, self.port, self.user, self.password = host, port, user, password
        self.verify_identity = verify_identity
        self.command = prepare_sql_client()

    def sql(self, *statements):
        """SQLは標準入力、秘密は子プロセスの環境だけへ渡す。認証設定を保存しない。"""
        url = ('jdbc:mysql://' + self.host + ':' + str(self.port) + '/?sslMode='
               + ('VERIFY_IDENTITY' if self.verify_identity else 'REQUIRED')
               + '&connectTimeout=10000&socketTimeout=15000')
        env = dict(os.environ, OVERLOAD_SQL_URL=url, OVERLOAD_SQL_USER=self.user,
                   OVERLOAD_SQL_PASSWORD=self.password)
        payload = struct.pack('>I', len(statements))
        for statement in statements:
            encoded = statement.encode('utf-8')
            payload += struct.pack('>I', len(encoded)) + encoded
        result = subprocess.run(self.command, input=payload, stdout=subprocess.PIPE,
                                stderr=subprocess.PIPE, env=env, timeout=60)
        if result.returncode:
            # 構文エラー等にSQLやパスワードが含まれても、エラー番号以外は表示しない。
            error = re.search(rb'DB_ERROR:(\d+)', result.stderr)
            raise PreparationError('DB操作を完了できませんでした（エラー番号：'
                                   + (error.group(1).decode() if error else '取得不能') + '）。')
        return result.stdout.decode('utf-8').strip()


def provision(owner, schema, app_user, app_password, migrate):
    """存在しないDB・ユーザーだけを新規作成し、業務表単位で必要な権限を与える。"""
    if not re.fullmatch(r'[a-z][a-z0-9_]{0,63}', schema):
        raise PreparationError('DB名が不正です。')
    if not re.fullmatch(r'[A-Za-z0-9_.]{1,32}', app_user) or app_user == owner.user:
        raise PreparationError('専用の実行ユーザー名を指定してください。')
    if len(app_password) < 20 or any(ord(c) < 32 or ord(c) > 126 for c in app_password):
        raise PreparationError('実行用パスワードは20文字以上の半角英数字・記号で設定してください。')
    if owner.sql("SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name='" + schema + "'") != '0':
        raise PreparationError('同名のDBが存在するため停止しました。既存DBは変更していません。')
    if owner.sql("SELECT COUNT(*) FROM mysql.user WHERE User='" + app_user + "'") != '0':
        raise PreparationError('同名ユーザーが存在するため停止しました。既存の認証情報は変更していません。')
    owner.sql('CREATE DATABASE `' + schema + '` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci')
    print('新しい公開用DBを作成しました。移行を実行します。', flush=True)
    migrate()
    # NO_BACKSLASH_ESCAPESをこの接続だけへ適用し、引用符はSQL標準どおり二重化する。
    literal = app_password.replace("'", "''")
    owner.sql("SET SESSION sql_mode=CONCAT(@@SESSION.sql_mode,',NO_BACKSLASH_ESCAPES')",
              "CREATE USER '" + app_user + "'@'%' IDENTIFIED BY '" + literal + "'")
    for table, permissions in PRIVILEGES.items():
        owner.sql('GRANT ' + permissions + ' ON `' + schema + '`.`' + table
                  + "` TO '" + app_user + "'@'%'")


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
        parser.error('所有者本人がターミナルで操作してください。')
    try:
        prepare_sql_client()
    except (PreparationError, OSError, subprocess.SubprocessError) as error:
        print(str(error) if isinstance(error, PreparationError) else '接続ツールの準備に失敗しました。')
        return 1
    print('公開用DB：' + args.schema + '\n実行用ユーザー：' + args.app_user)
    print('新しいDBだけを移行し、新しい実行ユーザーへ10表の必要な読み書き権限だけを与えます。')
    print('既存DB・ユーザー・パスワードを上書きせず、有料化や削除も行いません。')
    print('途中で失敗した場合は、新規DBを消さずに停止して原因を確認します。')
    if input('この準備を実行する場合は「準備」と入力してください：').strip() != '準備':
        return 0
    owner_password = getpass.getpass('保管済みのTiDB管理者の接続パスワード：')
    app_password = getpass.getpass('実行用の新しいパスワード（20文字以上。ご自身で保管してください）：')
    confirmation = getpass.getpass('確認のため、実行用の新しいパスワードをもう一度：')
    if not owner_password or app_password != confirmation:
        print('パスワードが未入力、または確認入力が一致しないため終了しました。')
        return 2
    if app_password == owner_password:
        print('実行用には、管理者用と異なる新しいパスワードを設定してください。')
        return 2
    stage = '接続・制約確認'
    try:
        owner = MySqlClient(args.host, args.port, args.owner_user, owner_password)
        flags = owner.sql('SELECT @@GLOBAL.tidb_enable_check_constraint, @@GLOBAL.tidb_enable_foreign_key, @@SESSION.foreign_key_checks')
        if flags != '1\t1\t1':
            raise PreparationError('CHECKと外部キーが有効ではないため停止しました。')
        module_spec = importlib.util.spec_from_file_location('overload_launcher', ROOT / 'tools/run-backend.py')
        launcher = importlib.util.module_from_spec(module_spec)
        module_spec.loader.exec_module(launcher)
        artifact = launcher.prepare_artifact(ROOT / 'backend/build/libs/overload-api-0.0.1-SNAPSHOT.jar', ROOT / '.run')
        url = ('jdbc:mysql://' + args.host + ':' + str(args.port) + '/' + args.schema
               + '?sslMode=VERIFY_IDENTITY&connectTimeout=10000&socketTimeout=15000')

        def migrate():
            env = dict(os.environ, OVERLOAD_DB_URL=url, OVERLOAD_MIGRATION_USER=args.owner_user,
                       OVERLOAD_MIGRATION_PASSWORD=owner_password)
            # 秘密が表示されないよう、移行出力は終了後に除去処理してから表示する。
            result = subprocess.run(['java', '-jar', str(artifact), '--migrate-only'], env=env,
                                    text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=600)
            safe = result.stdout.replace(owner_password, '[非表示]').replace(app_password, '[非表示]')
            print(safe, end='', flush=True)
            if result.returncode:
                raise PreparationError('DB移行が失敗しました。新規DBを保持して停止します。')

        stage = '新規DB・移行・実行ユーザーの作成'
        provision(owner, args.schema, args.app_user, app_password, migrate)
        stage = '実行ユーザーの接続・権限確認'
        app = MySqlClient(args.host, args.port, args.app_user, app_password)
        for table in PRIVILEGES:
            app.sql('SELECT COUNT(*) FROM `' + args.schema + '`.`' + table + '` WHERE 1=0')
        grants = app.sql('SHOW GRANTS')
        if any(word in grants.upper() for word in ('GRANT ALL', 'GRANT OPTION', 'CREATE', 'DROP', 'ALTER')):
            raise PreparationError('想定外の権限があるため、公開へ進まず停止しました。')
        report = dict(completedAt=datetime.datetime.now(datetime.timezone.utc).isoformat(),
                      status='prepared', databaseUrl=url, databaseUser=args.app_user, schema=args.schema,
                      privileges=PRIVILEGES, administratorPasswordStored=False, deployed=False)
        args.output_dir.mkdir(parents=True, exist_ok=True)
        (args.output_dir / 'cloud-database-preparation.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n')
        print('公開用DBと実行ユーザーの準備が完了しました。パスワードは保存していません。')
        print('実行用パスワードを保管し、管理者用とは区別してください。')
        return 0
    except (PreparationError, OSError, subprocess.SubprocessError) as error:
        print('停止した段階：' + stage)
        print(str(error) if isinstance(error, PreparationError) else '処理を完了できませんでした。秘密を含む詳細は表示しません。')
        return 1
    finally:
        owner_password = app_password = confirmation = None


if __name__ == '__main__':
    raise SystemExit(main())
