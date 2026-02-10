# Stripe 擬似決済の導入手順

「擬似決済」＝**Stripe テストモード**で、本番と同じフローをテストカードで試すことを指します。実際の請求は発生しません。

---

## 1. 現状の整理

- **決済まわり**: `StripePaymentClient`（PaymentIntent 作成）、`StripeWebhookController`（Webhook 受信）、`StripeWebhookHandler`（`payment_intent.succeeded` で `recordOrderPaid`）は実装済み。
- **注文フロー**: 「注文を確定する」→ `OrderPageController.orderConfirmPost` → `OrderService.createOrderFromProduct` で **即座に status=PAID** で注文作成。Stripe は未使用。
- **DB**: `orders.status` は `PAID, AWAITING_SHIPMENT, SHIPPED, COMPLETED, CANCELLED` のみ（`PENDING` なし）。

Stripe を挟む場合は **「注文作成（PENDING）→ PaymentIntent 作成 → フロントでカード入力・確定 → Webhook で PAID」** に変える必要があります。

---

## 2. やること一覧

| 項目 | 内容 |
|------|------|
| **2.1 Stripe 設定** | ダッシュボードでテストキー・Webhook を取得し、`application.properties` に設定 |
| **2.2 設定の条件付き有効化** | Stripe 未設定時は従来どおり「即 PAID」、設定時のみ Stripe フローを使う（任意） |
| **2.3 注文ステータス PENDING** | マイグレーションで `orders.status` に `PENDING` を追加、`OrderStatus` に追加 |
| **2.4 注文フロー変更** | 注文確定時に「PENDING で注文作成 → PaymentIntent 作成 → client_secret を返す」 |
| **2.5 フロント** | 注文確認画面 or 専用ページで Stripe.js を読み込み、Card Element で支払い → 成功時に注文詳細へ |
| **2.6 Webhook で PAID へ** | `payment_intent.succeeded` で注文を PENDING → PAID に更新し、`recordOrderPaid` を実行 |

---

## 3. 手順

### 3.1 Stripe ダッシュボードで準備

1. [Stripe](https://stripe.com) にログイン（アカウントなしなら作成）。
2. **開発者モード**をオンにし、**テスト用の API キー**を取得:
   - **シークレットキー**: `sk_test_...`
3. **Webhook** を追加:
   - エンドポイント: `https://あなたのドメイン/webhooks/stripe`（ローカルなら `ngrok` 等で HTTPS を公開）
   - イベント: `payment_intent.succeeded` を選択
   - 署名シークレット: `whsec_...` を控える

ローカルで Webhook を受けたい場合:
- [Stripe CLI](https://stripe.com/docs/stripe-cli) で `stripe listen --forward-to localhost:8080/webhooks/stripe` を実行すると、一時的な `whsec_...` が表示されます。

### 3.2 application.properties に設定

```properties
# Stripe（擬似決済＝テストモード）
app.stripe.api-key=sk_test_xxxxxxxxxxxxxxxx
app.stripe.webhook-secret=whsec_xxxxxxxxxxxxxxxx
app.stripe.currency=JPY
```

- 本番では環境変数で上書き推奨: `APP_STRIPE_API_KEY`, `APP_STRIPE_WEBHOOK_SECRET` など。

### 3.3 Stripe 未設定時は従来フローのまま動かす（推奨）

- `app.stripe.api-key` が空 or 未設定のときは、これまでどおり「注文確定 → 即 PAID」にし、Stripe 関連 Bean は作らない（または No-op の PaymentClient を使う）ようにすると、Stripe 未導入環境でもそのまま起動できます。
- 実装例: `StripeConfig` を `@ConditionalOnProperty("app.stripe.api-key")` で条件付きにし、`StripePaymentClient` も同条件 or 別プロファイルで有効化。

### 3.4 注文フロー（Stripe ありの場合）

1. **注文確認画面**で「注文を確定する」を押す。
2. バックエンド:
   - 注文を **status=PENDING** で作成（既存の `createOrderFromProduct` を拡張 or 新メソッド）。
   - `PaymentClient.charge(PaymentRequest)` で PaymentIntent を作成（金額・通貨・metadata に `orderId`）。
   - 注文に `stripe_payment_intent_id` を保存。
   - レスポンスで **client_secret** をフロントに返す（新 API 例: `POST /api/orders/{orderId}/create-payment-intent` や、注文確定用エンドポイントの戻りに含める）。
3. フロント:
   - [Stripe.js](https://stripe.com/docs/js) を読み込み、[Payment Element](https://stripe.com/docs/payments/payment-element) または Card Element を表示。
   - ユーザーがカード情報を入力し、`stripe.confirmPayment({ client_secret })` で確定。
   - 成功時: `/user/orders/{orderId}` へリダイレクト。
4. Stripe が **payment_intent.succeeded** を送信 → `StripeWebhookHandler` で:
   - 該当注文を **PENDING → PAID** に更新するメソッド（例: `markOrderPaidByStripe(orderId)`）を呼ぶ。
   - 続けて既存の `recordOrderPaid(orderId)` でメール・通知。

### 3.5 テストカード（Stripe テストモード）

| 番号 | 結果 |
|------|------|
| `4242 4242 4242 4242` | 成功 |
| `4000 0000 0000 0002` | 拒否 |
| `4000 0025 0000 3155` | 3D Secure 必要 |

有効期限・CVC は任意の未来の日付・3桁で可。詳細は [Stripe テストカード](https://stripe.com/docs/testing#cards) を参照。

---

## 4. 実装の優先順位（段階的にやる場合）

1. **まず動かす**: 上記 3.1・3.2 だけ行い、既存の「即 PAID」フローはそのまま。Stripe は Webhook や別 API から手動で試す。
2. **PENDING 追加**: マイグレーション + `OrderStatus.PENDING` + 注文作成を PENDING にする分岐。
3. **PaymentIntent 連携**: 注文確定時に PaymentIntent 作成と client_secret 返却 API を追加。
4. **フロント**: 注文確認後に Stripe.js で支払い画面を表示し、成功時に注文詳細へ。
5. **Webhook で PAID**: `payment_intent.succeeded` で PENDING → PAID 更新 + `recordOrderPaid`。

---

## 5. 参考リンク

- [Stripe テストモード](https://stripe.com/docs/test-mode)
- [Payment Intents API](https://stripe.com/docs/payments/payment-intents)
- [Stripe.js と Payment Element](https://stripe.com/docs/payments/accept-a-payment)
- [Webhook 署名検証](https://stripe.com/docs/webhooks/signatures)（既存 `StripeWebhookVerifier` で実施済み）

---

この手順に沿って、設定の有効化や「PENDING 追加＋注文フロー変更」の具体的なパッチが必要であれば、どこまで実装するか（設定だけ／フローまで／フロントまで）を指定してもらえれば、その範囲でコード案を出します。
