"""再ビルドと並行する起動の分離を検証する。"""
import importlib.util
from pathlib import Path
import tempfile
import unittest
import zipfile

spec = importlib.util.spec_from_file_location("launcher", Path(__file__).with_name("run-backend.py"))
launcher = importlib.util.module_from_spec(spec)
spec.loader.exec_module(launcher)


def write_jar(path, content):
    with zipfile.ZipFile(path, "w") as archive:
        archive.writestr("META-INF/MANIFEST.MF", "Manifest-Version: 1.0\n")
        archive.writestr("payload", content)


class ArtifactIsolationTest(unittest.TestCase):
    def test_rebuild_does_not_change_running_artifact_or_reuse_its_path(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "build.jar"
            write_jar(source, "first-build")
            first = launcher.prepare_artifact(source, root / "releases")
            write_jar(source, "second-build-with-different-size")
            second = launcher.prepare_artifact(source, root / "releases")
            with zipfile.ZipFile(first) as archive:
                self.assertEqual(archive.read("payload"), b"first-build")
            self.assertEqual(second.read_bytes(), source.read_bytes())
            self.assertNotEqual(first, second)
            self.assertNotEqual(source.stat().st_ino, first.stat().st_ino)
            self.assertEqual(first.stat().st_mode & 0o222, 0)


    def test_incomplete_jar_is_not_launched_or_retained(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "build.jar"
            source.write_bytes(b"incomplete-build")
            with self.assertRaises(zipfile.BadZipFile):
                launcher.prepare_artifact(source, root / "releases")
            self.assertEqual(list((root / "releases").iterdir()), [])


if __name__ == "__main__":
    unittest.main()
