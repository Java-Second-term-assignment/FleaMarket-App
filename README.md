# FleaMarket-App

**FleaMarket-App**は、商品の出品・購入・取引機能を統合したフリマアプリケーションです。
ユーザー認証、商品カタログ、取引管理、チャット機能、レビューシステムなどを提供し、
安全で快適なフリマ体験を実現することを目的としたアプリケーションです。
Spring BootをベースとしたRESTful APIを提供し、AWSサービスとの連携により
AIモデレーションや不正検出などの高度な機能を実装しています。

### プロジェクト体制
- **開発メンバー**: 3人
  - **Hirotaka-Tambo**: 要件定義・実装・デバック・レビュー
  - **Kamon-Tahara-504**: 要件定義・実装・デバック・レビュー
  - **soramame174**: 要件定義・デザイン・HTML実装

※大まかな貢献度で振り分けています。

### プロジェクト工程
- **開発開始日**: 2026/ 1/13
- **完成予定**: （2月上旬）

## 主要機能

### アプリ機能
- **商品カタログ**: 商品の検索・閲覧・カテゴリ管理
- **出品機能**: 商品の出品・下書き保存・配送ポリシー設定
- **取引管理**: 注文処理・取引履歴管理
- **チャット機能**: 売買双方のメッセージング
- **レビューシステム**: 取引後の評価・レビュー投稿
- **お気に入り機能**: 商品のお気に入り登録・管理
- **掲示板機能**: コミュニティ掲示板
- **通知機能**: 取引・メッセージなどの通知
- **ユーザー認証**: JWT認証・リフレッシュトークン対応
- **アカウント管理**: プロフィール編集・ユーザーランクシステム

### 管理者機能
- **監査ログ**: システム操作の記録・監査
- **モデレーション**: AIを活用したコンテンツモデレーション
- **不正検出**: AWS Fraud Detectorを活用した不正取引検出
- **CSVエクスポート**: データのエクスポート機能
- **ユーザー管理**: 管理者によるユーザー管理

### 技術スタック
- **バックエンド**: Spring Boot 4.0.1
- **データベース**: PostgreSQL（Flywayマイグレーション対応）
- **認証**: JWT（JSON Web Token）
- **セキュリティ**: Spring Security、Role Hierarchy
- **AWS連携**: 
  - Amazon Comprehend（テキスト分析）
  - Amazon Rekognition（画像分析）
  - AWS Fraud Detector（不正検出）
- **国際化**: 日本語ロケール対応
- **API設計**: RESTful API
- **ビルドツール**: Maven

### データ管理
- **データベース**: PostgreSQL
- **マイグレーション**: Flyway（段階的なスキーマ管理）
- **セキュリティ**: Row Level Security (RLS) 実装済み
- **データ整合性**: トランザクション管理
- **フルテキスト検索**: 商品検索機能

## ディレクトリ構成

```
FleaMarket-App/
├── .mvn/                          # Maven Wrapper設定
│   └── wrapper/
│       └── maven-wrapper.properties
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/flea_market_app/
│   │   │       ├── admin/        # 管理者機能
│   │   │       │   ├── audit/
│   │   │       │   │   └── AuditLogService.java
│   │   │       │   ├── controller/
│   │   │       │   │   └── AdminController.java
│   │   │       │   ├── export/   # CSVエクスポート
│   │   │       │   │   └── CsvExportService.java
│   │   │       │   ├── moderation/ # モデレーション・不正検出
│   │   │       │   │   ├── AiModerationService.java
│   │   │       │   │   └── FraudDetectionService.java
│   │   │       │   └── service/
│   │   │       │       ├── AdminService.java
│   │   │       │       └── AdminUserService.java
│   │   │       │
│   │   │       ├── auth/         # 認証・認可
│   │   │       │   ├── controller/
│   │   │       │   │   └── AuthController.java
│   │   │       │   ├── domain/
│   │   │       │   │   ├── AuthUser.java
│   │   │       │   │   └── RefreshToken.java
│   │   │       │   ├── repository/
│   │   │       │   │   └── RefreshTokenRepository.java
│   │   │       │   └── service/
│   │   │       │       ├── AuthService.java
│   │   │       │       └── TokenService.java
│   │   │       │
│   │   │       ├── catalog/      # 商品カタログ
│   │   │       │   ├── controller/
│   │   │       │   │   └── ItemCatalogController.java
│   │   │       │   ├── domain/
│   │   │       │   │   ├── Category.java
│   │   │       │   │   ├── Item.java
│   │   │       │   │   └── ItemCondition.java
│   │   │       │   ├── repository/
│   │   │       │   │   └── ItemRepository.java
│   │   │       │   └── service/
│   │   │       │       └── ItemSearchService.java
│   │   │       │
│   │   │       ├── common/       # 共通機能
│   │   │       │   ├── error/
│   │   │       │   │   └── ErrorCode.java
│   │   │       │   ├── event/
│   │   │       │   │   ├── DomainEvent.java
│   │   │       │   │   └── EventPublisher.java
│   │   │       │   ├── exception/
│   │   │       │   │   ├── AccessDeniedBusinessException.java
│   │   │       │   │   ├── BusinessException.java
│   │   │       │   │   ├── NotFoundBusinessException.java
│   │   │       │   │   └── ValidationBusinessException.java
│   │   │       │   ├── response/
│   │   │       │   │   ├── ApiResponse.java
│   │   │       │   │   └── ErrorResponse.java
│   │   │       │   ├── util/
│   │   │       │   │   └── DateTimeUtil.java
│   │   │       │   └── validation/
│   │   │       │       ├── EmailValidator.java
│   │   │       │       └── PasswordPolicy.java
│   │   │       │
│   │   │       ├── config/       # 設定
│   │   │       │   ├── aws/
│   │   │       │   │   └── AwsClientConfig.java
│   │   │       │   ├── cors/
│   │   │       │   │   └── CorsConfig.java
│   │   │       │   ├── i18n/
│   │   │       │   │   ├── MessageConfig.java
│   │   │       │   │   └── messages_ja.properties
│   │   │       │   ├── jackson/
│   │   │       │   │   └── JacksonConfig.java
│   │   │       │   ├── security/
│   │   │       │   │   ├── AuthorizationConfig.java
│   │   │       │   │   ├── JwtAuthenticationFilter.java
│   │   │       │   │   ├── RoleHierarchyConfig.java
│   │   │       │   │   └── SecurityConfig.java
│   │   │       │   └── web/
│   │   │       │       └── WebMvcConfig.java
│   │   │       │
│   │   │       ├── engagement/   # エンゲージメント機能
│   │   │       │   ├── board/    # 掲示板
│   │   │       │   │   ├── controller/
│   │   │       │   │   │   └── BoardController.java
│   │   │       │   │   ├── domain/
│   │   │       │   │   │   └── BoardPost.java
│   │   │       │   │   └── service/
│   │   │       │   │       └── BoardService.java
│   │   │       │   ├── favorite/ # お気に入り
│   │   │       │   │   ├── controller/
│   │   │       │   │   │   └── FavoriteController.java
│   │   │       │   │   ├── domain/
│   │   │       │   │   │   └── Favorite.java
│   │   │       │   │   └── service/
│   │   │       │   │       └── FavoriteService.java
│   │   │       │   └── notification/ # 通知
│   │   │       │       ├── domain/
│   │   │       │       │   └── Notification.java
│   │   │       │       └── service/
│   │   │       │           └── NotificationService.java
│   │   │       │
│   │   │       ├── integration/  # 外部連携
│   │   │       │   ├── aws/      # AWSサービス連携
│   │   │       │   │   ├── ComprehendClient.java
│   │   │       │   │   ├── FraudDetectorClient.java
│   │   │       │   │   └── RekognitionClient.java
│   │   │       │   └── payment/  # 決済連携
│   │   │       │       └── PaymentClient.java
│   │   │       │
│   │   │       ├── listing/      # 出品機能
│   │   │       │   ├── controller/
│   │   │       │   │   └── ListingController.java
│   │   │       │   ├── domain/
│   │   │       │   │   ├── DraftListing.java
│   │   │       │   │   ├── Listing.java
│   │   │       │   │   └── ShippingPolicy.java
│   │   │       │   ├── repository/
│   │   │       │   │   └── ListingRepository.java
│   │   │       │   └── service/
│   │   │       │       ├── DraftListingService.java
│   │   │       │       ├── ListingService.java
│   │   │       │       └── ShippingService.java
│   │   │       │
│   │   │       ├── transaction/  # 取引機能
│   │   │       │   ├── controller/
│   │   │       │   │   ├── ChatController.java
│   │   │       │   │   └── OrderController.java
│   │   │       │   ├── domain/
│   │   │       │   │   ├── ChatMessage.java
│   │   │       │   │   ├── Order.java
│   │   │       │   │   └── Review.java
│   │   │       │   ├── repository/
│   │   │       │   │   └── OrderRepository.java
│   │   │       │   └── service/
│   │   │       │       ├── ChatService.java
│   │   │       │       ├── OrderService.java
│   │   │       │       └── ReviewService.java
│   │   │       │
│   │   │       ├── user/         # ユーザー機能
│   │   │       │   ├── controller/
│   │   │       │   │   └── UserController.java
│   │   │       │   ├── domain/
│   │   │       │   │   ├── User.java
│   │   │       │   │   └── UserRank.java
│   │   │       │   ├── repository/
│   │   │       │   │   └── UserRepository.java
│   │   │       │   └── service/
│   │   │       │       ├── UserRankService.java
│   │   │       │       └── UserService.java
│   │   │       └── FleaMarketAppApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-legacy.properties
│   │       ├── data.sql
│   │       ├── schema.sql
│   │       └── db/
│   │           └── migration/    # Flywayマイグレーション
│   │               ├── V1__init.sql
│   │               ├── V2_rank.sql
│   │               ├── V3_categories.sql
│   │               ├── V4_category_master_columns.sql
│   │               ├── V5_prohibited_rules.sql
│   │               ├── V6_items_fulltext_search.sql
│   │               └── V7_dev_seed_users.sql
│   └── test/                      # テストコード
│       └── java/
│           └── com/example/flea_market_app/
│               └── FleaMarketAppApplicationTests.java
├── pom.xml                        # Maven設定
├── mvnw                           # Maven Wrapper (Unix)
└── mvnw.cmd                       # Maven Wrapper (Windows)
```

### 主要モジュール

- **admin**: 管理者向け機能（監査ログ、モデレーション、不正検出など）
- **auth**: 認証・認可機能（JWT、リフレッシュトークンなど）
- **catalog**: 商品カタログ機能（商品検索、カテゴリ管理など）
- **common**: 共通機能（エラーハンドリング、バリデーション、ユーティリティなど）
- **config**: アプリケーション設定（セキュリティ、CORS、AWS、国際化など）
- **engagement**: エンゲージメント機能（掲示板、お気に入り、通知など）
- **integration**: 外部サービス連携（AWS、決済など）
- **listing**: 出品機能（出品、下書き、配送ポリシーなど）
- **transaction**: 取引機能（注文、チャット、レビューなど）
- **user**: ユーザー機能（ユーザー管理、ランクシステムなど）



