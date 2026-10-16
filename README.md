# CarrotCraft Sorting Patch

CarrotCraft のエンダーチェスト GUI で、インベントリソート Mod のソートやアイテム移動から操作用スロットを保護する、クライアント専用 Mod です。Fabric と NeoForge に対応しています。

> [!NOTE]
> AIによって生成されたコード及びドキュメントが含まれています。

## 目次

- [機能](#機能)
- [動作環境](#動作環境)
- [対応するソート Mod](#対応するソート-mod)
- [インストール](#インストール)
  - [保護対象となる画面](#保護対象となる画面)
- [開発者向け](#開発者向け)
  - [ビルドとテスト](#ビルドとテスト)
  - [プロジェクト構成](#プロジェクト構成)

### 機能

対象サーバーのエンダーチェスト GUI を検出すると、コンテナ内スロット 45〜53 をソートやアイテム移動の対象から保護します。

### 動作環境

- Minecraft: 26.1 以降
- Fabric Loader: 0.19.0 以降（Fabric 版）
- NeoForge: Minecraft 26.1 以降（NeoForge 版）
- Java: 25 以降net`

### 対応するソート Mod

以下の Mod のソート処理に対応しています。これらの Mod は本 Mod に含まれないため、別途インストールしてください。

- [Inventory Profiles Next](https://modrinth.com/mod/inventory-profiles-next) (`inventoryprofilesnext`)
- [ClientSort](https://github.com/TerminalMC/ClientSort) (`clientsort`)

### インストール

1. 上記の動作環境を満たす Fabric または NeoForge クライアントを用意します。
2. 使用するローダー用の本 Mod jar と、同じローダー向けの対応ソート Mod を Minecraft の `mods` フォルダーに配置します。
3. Minecraft を起動し、CarrotCraft のエンダーチェスト GUI を開きます。

### 保護対象となる画面

次の条件をすべて満たす画面だけを保護対象として検出します。

- 接続先が `play.carrotcraft.net`
- プレイヤーインベントリを除くコンテナ領域が 54 スロット
- コンテナ内スロット 45 に「プラン」と表示された時計がある
- コンテナ内スロット 53 に「使い方」と表示された本がある
- 上記のアイテムに `carrotgui:item = 1b` のデータがある

それ以外のサーバーや画面には保護処理を適用しません。

---

## 開発者向け

### ビルドとテスト

Java 25 を用意し、プロジェクトのルートで実行してください。

```bash
./gradlew build
```

テスト:

```bash
./gradlew test
```

ビルド成果物は各ローダーの `build/libs/` に出力されます。

- Fabric: `fabric/build/libs/`
- NeoForge: `neoforge/build/libs/`（Minecraft 26.2 を基準にビルドした、26.1 以降向けの単一jar）

### プロジェクト構成

- `common/` — Fabric と NeoForge で共有する判定・保護処理・Mixin・テスト
- `fabric/` — Fabric の初期化処理、Mixin プラグイン、メタデータ
- `neoforge/` — NeoForge の初期化処理、Mixin プラグイン、メタデータ
