# React + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and [`typescript-eslint`](https://typescript-eslint.io) in your project.

## 認証導入・公開準備の作業版

認証、本人ごとのデータ分離、DB移行と安全な起動方法は [公開準備と起動手順](docs/公開準備と起動手順.md) を参照してください。起動中のJARを再ビルドで上書きしないよう、`tools/run-backend.py` を使用します。クラウドへの公開は別工程です。

カード登録なしの構成は [RenderとTiDBの公開手順](docs/RenderとTiDBの公開手順.md) を参照してください。`render.yaml`は無料枠を明示しています。

パスワード変更・Googleログインの作業版と設定手順は [認証機能の追加手順](docs/パスワード変更とGoogleログイン.md) を参照してください。Google設定と本番DB移行の完了前に、この作業版を公開しないでください。
