# カード登録なしの公開：Render FreeとTiDB Cloud Starter

この構成は小規模な公開向け。Renderの無料サーバーは無通信15分で休止する。TiDBは無料Starter・MySQL互換・月額支出上限0ドルを使う。有料プランへの変更やカード登録は前提としない。

## 公開用ビルド

ReactをSpring BootのJARへ同梱し、画面とAPIを同じHTTPSオリジンから配信する。CORSの追加許可やクロスサイトCookieへの切替は不要。

```sh
npm ci
npm test
npm run lint
npm run build
cd backend
./gradlew clean test bootJar -PfrontendDist="$PWD/../dist"
```

`frontendDist`を指定しないAPI単体ビルドも従来どおり可能。公開時は指定を必須とし、JAR内の `BOOT-INF/classes/static/index.html` とそのアセットが同じビルドに入っていることを確認する。実行中JARの上書きを避けるため、ローカルでは `tools/run-backend.py` を使う。

DockerfileはNodeのビルド、Javaのビルド、Java実行環境を分け、実行時は管理者以外のユーザーで起動する。実行するJARはイメージ内で固定し、ビルド先をマウントしない。Renderの本番イメージビルドとLinux上の512MBメモリー制限内での検証は別途必要。手元の検証だけでその合格を主張しない。

## DBの準備と互換性確認

1. 新しい無料インスタンスを使い、既存のMySQLデータは変更しない。
2. パスワードの生成と保管は所有者本人が行う。パスワードをチャットやGitへ貼り付けない。
3. SQL Editorなどで、バージョン、`tidb_enable_check_constraint`、`tidb_enable_foreign_key`、`foreign_key_checks`、`sql_mode`を確認する。CHECKは既定で無効の場合があり、制約が無視される構成を採用しない。
4. 新しい専用スキーマと、移行用ユーザー・実行用ユーザーを分ける。実行用にDDL権限や管理者資格情報を渡さない。
5. 下記の移行コマンドはWebサーバーを起動せず、FlywayのV1〜V3だけを適用する。環境変数は本人が安全に設定する。既存スキーマに対する自動baselineやcleanは行わない。

```sh
java -jar backend/build/libs/overload-api-0.0.1-SNAPSHOT.jar --migrate-only
```

必要な変数は `OVERLOAD_DB_URL`、`OVERLOAD_MIGRATION_USER`、`OVERLOAD_MIGRATION_PASSWORD`。URLにはユーザー・パスワードを埋め込まず、`sslMode=VERIFY_IDENTITY`を必須とする。例えば接続オプションは `?sslMode=VERIFY_IDENTITY&connectTimeout=10000&socketTimeout=15000`。ホスト名・DB名は接続先に合わせる。

現行のV3には、重複マスターを拒否する一時テーブルとCHECKがある。TiDBの実インスタンスで、移行失敗時の原因、制約の有効性、同時要求時の結果を確認する。MySQLでの成功をTiDBでの成功として扱わない。適用済みのSQLをその場で編集してchecksum検証を避けることもしない。

FlywayにはMySQLモジュールに加え、TiDB用のCommunityモジュールを同梱する。MySQL用だけでは、既存DBの明示的baselineでTiDB未対応の`CREATE TABLE ... AS SELECT`を発行する。TiDBモジュール10.26.2はFlyway本体11系を参照する別バージョン体系で、本体はSpring Bootの管理する11.14.1を維持する。TiDBのバージョン文字列から正しいDB種別を選ぶ回帰テストと、実DBで既存記録を保持する試験を行う。

ロールバック試験は、TiDB未対応のトリガーへ依存しない。検証スキーマだけに一時的なCHECK制約を加え、1セット目が実DBへINSERTされたことを同じトランザクションで確認してから、2セット目の制約違反で全登録が取り消されることを確認する。試験後は追加制約を外す。

所有者が保管した接続パスワードで実DBを検証する場合は、`tools/run-cloud-tests.py` をターミナルから実行できる。`--host`、`--user`、`--output-dir`を指定する。パスワードは非表示の対話入力とし、引数・URL・保存ファイルへ含めない。開始前に、この実行で新規作成するUUID付きの使い捨てスキーマだけを終了時に削除することを明示し、所有者の入力を待つ。遠隔接続ではTLSホスト名検証を必須とし、CHECK・外部キーが無効ならスキーマ作成前に停止する。

## 公開用DBと実行ユーザーの新規準備

`tools/prepare-cloud-db.py`は、同名のDBとユーザーがまだ存在しないことを確認してから準備する。MySQL公式クライアントと完成した公開用JARを使い、所有者の非表示入力で接続する。ホスト・ポート・所有者ユーザー・実行ユーザー・DB名・秘密を含まない結果の出力先を指定する。操作対象と権限は事前に所有者へ提示して承認を得る。

- 公開用スキーマを新規作成し、固定コピーのJARでFlywayを実行する。既存DBへの自動baselineやcleanは行わない。
- 所有者が新しい実行用パスワードを二度入力する。20文字以上の半角英数字・記号とし、管理者用と同じ値は受け付けない。所有者本人が保管する。
- 新規の実行ユーザーへ、ユーザー・認証情報・筋トレ・セッションの必要な読み書き権限と、3つのマスター表の読取権限だけを付与する。DDL・管理者権限・GRANT OPTIONは与えない。
- 作成後、その実行ユーザーで9表へ接続し、権限を確認する。Renderへ設定するURLとユーザー名だけを結果ファイルへ出力し、パスワードは出力・保存しない。
- 途中失敗時は新規DBを保持して停止する。既存の同名DBやユーザーを上書きして再実行しない。残った状態を調査してから対応する。

この準備には`mysql`コマンドが必要。遠隔接続は`VERIFY_IDENTITY`とOSのCA証明書を使う。手元の権限境界試験もTLSで接続し、証明書検証の省略はループバック専用のテストでのみ許す。

## Renderに設定するもの

`render.yaml`は `plan: free`、`region: singapore`、`autoDeployTrigger: "off"` を明記している。Freeでは事前実行ジョブやSSHを使えないため、移行は前項の専用実行で済ませる。

秘密の環境変数には、実行用ユーザーの `OVERLOAD_DB_URL`、`OVERLOAD_DB_USER`、`OVERLOAD_DB_PASSWORD` を設定する。`SPRING_FLYWAY_ENABLED=false`を維持し、移行用のパスワードはRenderへ置かない。`local`プロファイルを有効にしない。

Render用プロファイルはSecure CookieとDBのTLSホスト名検証を必須にし、`PORT`で待ち受ける。接続プールと処理スレッド、Javaヒープの上限を無料枠向けに制限している。HTTPの `/api/health` は必要な表への接続を確認し、失敗時は秘密を含めず503を返す。

GitHubへ反映する場合は、対象を `codex/publication-auth` の新規ブランチに限定し、mainを上書きしない。接続アプリの権限追加・アカウント操作・公開操作は対象と料金を確認してから行う。

## 公開前の合格条件

- TiDB上で、移行・新規登録・認証・初回保存・再取得・同時保存・同UUIDの重複防止・他ユーザーの非表示・ログアウト失効を確認する。
- HTTPSの実URLでSecure/HttpOnly/SameSite Cookie、CSRF、CORSを確認する。
- 無料Starterの自動バックアップは日次・保持1日。長期保管用エクスポートと別の検証DBへの復元・照合方法を決める。
- 5xx、DB接続失敗、転送量・RU・保存容量、再起動を監視する。ログへ本文、Cookie、トークン、メモ、パスワードを記録しない。
- パスワード回復、退会・削除、問い合わせ窓口等の長期公開に必要な運用を決める。
- 別ネットワークの実ブラウザから新規記録の初回保存と再取得まで確認してから、公開完了と報告する。

## 公式資料

- https://render.com/docs/free
- https://render.com/docs/docker
- https://render.com/docs/blueprint-spec
- https://docs.pingcap.com/tidbcloud/select-cluster-tier/
- https://docs.pingcap.com/tidbcloud/mysql-compatibility/
- https://docs.pingcap.com/tidbcloud/backup-and-restore-serverless/
- https://documentation.red-gate.com/fd/tidb-titanium-db-277579334.html
