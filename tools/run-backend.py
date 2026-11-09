#!/usr/bin/env python3
"""ビルド出力を起動ごとの専用パスへ固定してからJavaを起動する。"""
import argparse
import hashlib
import os
from pathlib import Path
import shutil
import tempfile
import zipfile

ROOT = Path(__file__).resolve().parents[1]


def prepare_artifact(source, releases):
    """起動中のJARを再ビルドで上書きしないよう、専用コピーを作る。"""
    source = Path(source)
    releases = Path(releases)
    releases.mkdir(parents=True, exist_ok=True)
    release = Path(tempfile.mkdtemp(prefix="release-", dir=releases))
    target = release / "app.jar"
    try:
        # 起動中ファイルとビルド出力は、ハードリンクも共有しない。
        with source.open("rb") as incoming, target.open("xb") as outgoing:
            shutil.copyfileobj(incoming, outgoing)
        # コピー中の再ビルドなどで壊れたJARは、Javaを起動する前に拒否する。
        with zipfile.ZipFile(target) as archive:
            if archive.testzip() is not None or "META-INF/MANIFEST.MF" not in archive.namelist():
                raise ValueError("完成した実行JARを指定してください。ビルド完了後に起動し直してください。")
        digest = hashlib.sha256(target.read_bytes()).hexdigest()
        (release / "sha256.txt").write_text(digest + "  app.jar\n", encoding="utf-8")
        target.chmod(0o444)
        return target
    except BaseException:
        shutil.rmtree(release)
        raise


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--jar", type=Path, default=ROOT / "backend/build/libs/overload-api-0.0.1-SNAPSHOT.jar")
    parser.add_argument("--prepare-only", action="store_true", help="専用コピーだけを作成する")
    args, java_args = parser.parse_known_args()
    artifact = prepare_artifact(args.jar, ROOT / ".run")
    print(f"固定した実行ファイル: {artifact}", flush=True)
    if not args.prepare_only:
        os.execvp("java", ["java", "-jar", str(artifact), *java_args])


if __name__ == "__main__":
    main()
