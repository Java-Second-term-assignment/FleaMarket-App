# catalog 実装の確認レポート

## 現状のファイル構造

```
catalog/
├── controller/
│   └── ItemCatalogController.java   … GET /api/items/{id}/images, /api/items/{id}/thumbnail
├── domain/
│   ├── ItemEntity.java              … 使用中（listing / favorite / board から参照）
│   └── ItemImageEntity.java         … 使用中（ItemImageRepository, ItemImageServiceImpl）
├── repository/
│   ├── ItemImageRepository.java     … 使用中
│   └── ItemRepository.java          … 使用中（listing, favorite, board）
└── service/
    ├── ItemImageService.java        … 使用中
    └── impl/
        └── ItemImageServiceImpl.java … 使用中
```

## 責務の分離がうまくできていない点

### 1. listing が catalog の永続化詳細に直接依存している

| 対象 | 内容 |
|------|------|
| **listing/domain/Listing.java** | `ItemEntity` を import し、`toEntity()` / `fromEntity(ItemEntity)` で変換している。出品ドメインが「商品」の JPA エンティティ（永続化の詳細）を知っている。 |
| **listing/service/impl/ListingServiceImpl.java** | `ItemRepository` を直接使用し、`Listing.toEntity()` で得た `ItemEntity` を保存している。「商品の作成」という責務が catalog にまとまっておらず、listing が商品の永続化方法を知っている。 |

**あるとよい姿**: catalog が「商品」集約のオーナーとなり、`ItemService`（または `CatalogService`）で `createDraftItem(...)` のような API を提供する。listing はそのサービスだけを呼び、`ItemEntity` / `ItemRepository` には触れない。

---

### 2. engagement が catalog の Entity / Repository に直接依存している

| 対象 | 内容 |
|------|------|
| **FavoriteServiceImpl** | `ItemRepository.findById(itemId)` と `ItemEntity.getStatus()` で「商品の存在・公開状態」を参照している。 |
| **BoardServiceImpl** | 同様に `ItemRepository` と `ItemEntity.getStatus()` で商品の存在・公開チェックをしている。 |

**あるとよい姿**: catalog に「商品の参照」用のサービス（例: `ItemQueryService` や `getItemOrThrow(itemId)`）を用意し、「存在するか」「公開中か」などを DTO や狭いインターフェースで返す。engagement はそのサービスだけに依存し、`ItemEntity` を見ない。

---

### 3. 責務が分離できていると判断できる部分

| 対象 | 理由 |
|------|------|
| **ItemCatalogController** | 画像・サムネイル取得のみで ItemImageService に委譲しており、薄い。 |
| **ItemImageServiceImpl** | 「商品画像」という一つのまとまりで、バリデーション・S3・DB を担当。過度な混在とは言いにくい。必要になったら「バリデーション」「ストレージ」「DB」に分離できる。 |

---

## 改善の方向性（責務の分離）

1. **catalog に「商品」のアプリケーションサービスを用意する**
   - **商品の作成**: `ItemService.createDraftItem(sellerId, categoryId, name, ...)` のような API を catalog に追加し、ListingServiceImpl は `ItemService` のみに依存する。Listing ドメインは `ItemEntity` に依存せず、catalog が提供する DTO や値オブジェクトとだけやりとりする形に寄せる（または Listing の toEntity を catalog 内に閉じる）。
   - **商品の参照**: `ItemQueryService.getItemOrThrow(itemId)` や `existsAndPublished(itemId)` のような API を catalog に追加し、FavoriteServiceImpl / BoardServiceImpl はそのサービスだけを使い、`ItemRepository` / `ItemEntity` を直接参照しない。

2. **listing ドメインから ItemEntity 依存を外す**
   - Listing の `toEntity()` / `fromEntity(ItemEntity)` をやめ、catalog 側で「Listing 用 DTO → ItemEntity」の変換を行うか、catalog が「下書き商品作成」の入力を受け取る形にし、listing は ItemEntity の型を import しないようにする。

---

## 参考：使用状況（参照元）

- **ItemEntity / ItemRepository**:  
  `Listing`（fromEntity/toEntity）, `ListingServiceImpl`, `FavoriteServiceImpl`, `BoardServiceImpl`
- **ItemImageService**:  
  `ItemCatalogController`, `ListingServiceImpl`, `FavoriteServiceImpl`
- **ItemCatalogController**:  
  `/api/items/{itemId}/images`, `/api/items/{itemId}/thumbnail` のみ。商品一覧・検索・1件取得 API は未実装。
