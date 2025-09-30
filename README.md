# Storage Sign Display

マインクラフトのサーバーにstorage signがインストールされている場合の視認性向上mod

## 概要

このModは、Minecraftサーバーに Storage Sign プラグインがインストールされている環境で、ストレージサインの視認性を向上させるクライアントサイドModです。

## 機能

- ストレージサインの表示を改善
- アイテム名とカウント表示のカスタマイズ
- 設定可能なアイテム識別方法（アイテムタグ または 特定アイテム）
- ModMenuとの統合による設定GUI
- 自動アップデートチェック機能
- ユーザーエクスペリエンスの向上

## 必要環境

- Minecraft 1.21.8
- Fabric Loader 0.17.2以上
- Fabric API 0.132.0以上
- YetAnotherConfigLib 3.8.0以上

## インストール方法

1. [Fabric](https://fabricmc.net/) をインストール（Minecraft 1.21.8対応版）
2. 必要な依存関係をダウンロードしてmodsフォルダに配置：
   - [Fabric API](https://modrinth.com/mod/fabric-api)（0.132.0+1.21.8以上）
   - [YetAnotherConfigLib](https://modrinth.com/mod/yacl)（3.8.0以上）
   - [ModMenu](https://modrinth.com/mod/modmenu)（15.0.0以上）- 設定GUIアクセス用（オプション）
3. このModのjarファイルをmodsフォルダに配置

## 設定

- ModMenuがインストールされている場合、ゲーム内でMod設定画面からアクセス可能
- 設定ファイル：`.minecraft/config/StorageSignDisplay.json`
- ストレージサインの表示方法、アイテム識別方法などをカスタマイズ可能

## 開発環境

### 必要なもの

- Java 17以上
- Git
- Gradle 8.0以上（Gradle Wrapperを使用するため不要）

### セットアップ

```bash
git clone https://github.com/2288-256/storage-sign-display.git
cd storage-sign-display
./gradlew build
```

## 技術仕様

- **プラットフォーム**: Fabric Mod
- **対象環境**: クライアントサイド
- **設定ライブラリ**: Yet Another Config Lib
- **ネットワーク**: OkHttp（アップデートチェック用）
- **JSON処理**: Gson

## 機能詳細

### ストレージサイン表示改善
- サインテクスチャの変更機能
- アイテム名の表示位置カスタマイズ
- アイテム数量の表示/非表示切り替え

### 設定システム
- アイテム識別方法の選択（アイテムタグ/特定アイテム）
- 各アイテムごとの個別設定
- リアルタイム設定反映

## ライセンス

このプロジェクトは [LICENSE.txt](LICENSE.txt) に記載されたライセンスの下で公開されています。

## 貢献

プルリクエストやIssueの報告を歓迎します。

### 開発に参加する場合

1. このリポジトリをフォーク
2. 機能ブランチを作成 (`git checkout -b feature/amazing-feature`)
3. 変更をコミット (`git commit -m 'Add some amazing feature'`)
4. ブランチにプッシュ (`git push origin feature/amazing-feature`)
5. プルリクエストを作成

## 更新履歴

- **1.1-SNAPSHOT**: 設定・更新チェック機能の追加
- **1.0**: 初回リリース

## 作者

- 2288-256

## 関連リンク

- [Modrinth](https://modrinth.com/mod/storage-sign-display)
