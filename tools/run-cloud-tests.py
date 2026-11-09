#!/usr/bin/env python3
"""所有者本人が入力した秘密を保存せず、隔離DBでクラウド互換性を検証する。"""
import argparse
import datetime
import getpass
import json
import os
from pathlib import Path
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--host', required=True)
    parser.add_argument('--port', type=int, default=4000)
    parser.add_argument('--user', required=True)
    parser.add_argument('--output-dir', type=Path, required=True)
    args = parser.parse_args()
    if not re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9.-]{0,252}', args.host) or not 1 <= args.port <= 65535:
        parser.error('有効な接続ホストとポートを指定してください。')
    if not sys.stdin.isatty():
        parser.error('パスワードを非表示で入力するため、ターミナルから起動してください。')
    repo = Path(__file__).resolve().parent.parent
    print('OVERLOAD：TiDBの専用テストDBによる検証')
    print('接続先：' + args.host + ':' + str(args.port))
    print('この検証はUUID付きの新しい使い捨てスキーマを作成し、終了時にそのスキーマだけを削除します。')
    print('既存のスキーマはテスト対象にしません。料金設定・DBユーザー・パスワードの変更は行いません。')
    print('パスワードはこの処理とテスト用の子プロセスのメモリーだけで使い、ファイルへ保存しません。')
    if input('この使い捨てDBによる検証を始める場合は「開始」と入力してください：').strip() != '開始':
        print('開始せず終了しました。')
        return 0
    password = getpass.getpass('保管済みのTiDB接続パスワード（入力は表示されません）：')
    if not password:
        print('パスワードが未入力のため終了しました。')
        return 2
    env = os.environ.copy()
    # コマンドラインやURLに秘密を含めず、Gradleの永続デーモンも使用しない。
    env.update(OVERLOAD_TEST_MYSQL_SERVER='jdbc:mysql://' + args.host + ':' + str(args.port)
               + '/?sslMode=VERIFY_IDENTITY&connectTimeout=10000&socketTimeout=15000',
               OVERLOAD_TEST_MYSQL_USER=args.user, OVERLOAD_TEST_MYSQL_PASSWORD=password)
    env['GRADLE_USER_HOME'] = str(repo.parent / 'gradle-home')
    output = args.output_dir.resolve()
    output.mkdir(parents=True, exist_ok=True)
    stamp = datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%SZ')
    log_file = output / ('tidb-tests-' + stamp + '.log')
    command = ['./gradlew', '--no-daemon', '--console=plain', '--rerun-tasks', 'test']
    if (repo / 'dist/index.html').is_file():
        command.append('-PfrontendDist=' + str(repo / 'dist'))
    started = time.time()
    with log_file.open('w', encoding='utf-8') as log:
        child = subprocess.Popen(command, cwd=repo / 'backend', env=env,
                                 stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                                 text=True, encoding='utf-8', errors='replace')
        del env['OVERLOAD_TEST_MYSQL_PASSWORD']
        try:
            for line in child.stdout:
                # ドライバー等の例外に万一含まれた場合も、保存・表示前に除去する。
                safe = line.replace(password, '[非表示]')
                log.write(safe)
                log.flush()
                print(safe, end='', flush=True)
            code = child.wait()
        except KeyboardInterrupt:
            child.terminate()
            child.wait()
            print('\n中断しました。検証用スキーマが残った場合は、名前を確認してから片付けます。')
            return 130
        finally:
            password = None
    suites = [p for p in (repo / 'backend/build/test-results/test').glob('TEST-*.xml')
              if p.stat().st_mtime >= started]
    counts = dict(tests=0, failures=0, errors=0, skipped=0)
    for suite in suites:
        data = ET.parse(suite).getroot()
        for key in counts:
            counts[key] += int(data.attrib.get(key, 0))
    report = dict(completedAt=datetime.datetime.now(datetime.timezone.utc).isoformat(),
                  target='TiDB Cloud', tlsMode='VERIFY_IDENTITY', exitCode=code,
                  counts=counts, freshResultFiles=len(suites), logFile=log_file.name)
    report_file = output / ('tidb-tests-' + stamp + '.json')
    report_file.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print('\n検証結果：' + ('成功' if code == 0 else '失敗・要調査'))
    print('確認用の結果ファイル：' + str(report_file))
    return code


if __name__ == '__main__':
    raise SystemExit(main())
