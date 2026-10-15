# CarrotCraft Sorting Patch

CarrotCraft の保管庫 GUI で、インベントリソート Mod のソートやアイテム移動から操作用スロットを保護する、クライアント専用 Fabric Mod です。

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

対象サーバーの保管庫 GUI を検出すると、コンテナ内スロット 45〜53 をソートやアイテム移動の対象から保護します。「プラン」「使い方」などの操作アイテムが並べ替えや移動で崩れるのを防ぎます。

本 Mod はクライアント専用です。サーバー側への導入は不要です。

### 動作環境

- Minecraft: 26.1 以降（このプロジェクトのビルド対象は 26.2）
- Fabric Loader: 0.19.0 以降
- Java: 25 以降
- 対象サーバー: `play.carrotcraft.net`

### 対応するソート Mod

以下の Mod のソート処理に対応しています。これらの Mod は本 Mod に含まれないため、別途インストールしてください。

- [Inventory Profiles Next](https://modrinth.com/mod/inventory-profiles-next) (`inventoryprofilesnext`)
- [ClientSort](https://github.com/TerminalMC/ClientSort) (`clientsort`)
- [Nemo's Inventory Sorting](https://modrinth.com/mod/nemos-inventory-sorting) (`nemos_inventory_sorting`)

### インストール

1. 上記の動作環境を満たす Fabric クライアントを用意します。
2. 対応するソート Mod と本 Mod の jar を Minecraft の `mods` フォルダーに配置します。
3. Minecraft を起動し、`play.carrotcraft.net` の保管庫 GUI を開きます。

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

ビルド成果物は `build/libs/` に出力されます。

### プロジェクト構成

- `src/main/java/` — Mod の Java ソース
- `src/main/resources/` — Fabric Mod メタデータと Mixin 設定
- `src/test/java/` — ユニットテスト
