# Thymeleaf から一度も呼ばれていないバックエンド一覧

Thymeleaf テンプレート（`th:href`・`th:action`・フォーム送信先）から参照されていない、ページ用コントローラのエンドポイントをまとめた一覧です。REST API は対象外です。

---

## 1. GET /password/change（AuthPageController）

| 項目 | 内容 |
|------|------|
| **実装** | パスワード変更画面（`auth/password_change`）を表示する |
| **現状** | どのテンプレートにも `th:href="@{/password/change}"` がない |
| **補足** | `password_change.html` 内のフォームは **POST /password-change** を呼んでいる。画面への「入口」だけリンクされておらず、メールリンクやブックマーク想定なら問題ない |

---

## 2. GET /password-reset-request（AuthPageController）

| 項目 | 内容 |
|------|------|
| **実装** | `/password/forgot` へリダイレクトするだけ |
| **現状** | テンプレートはすべて **/password/forgot** を参照（ログイン画面の「パスワードがわからない場合はこちら」など）。**/password-reset-request** へのリンクはない |
| **補足** | 同じ画面に 2 パスある状態で、フロントは片方のみ使用している |

---

## 3. GET /items/add（ListingPageController）

| 項目 | 内容 |
|------|------|
| **実装** | 出品ページ（`item/product_add`）を表示。`@GetMapping({ "/product/add", "/items/add" })` で同一処理 |
| **現状** | 出品ボタンは **/product/add** のみ使用（`fragments.html` の `location.href='@{/product/add}'`）。**/items/add** へのリンクはない |
| **補足** | バックエンドだけ別名 URL を用意している形で、テンプレートからは未使用 |

---

## 一覧まとめ

| エンドポイント | コントローラ | 状況 |
|----------------|-------------|------|
| GET /password/change | AuthPageController | どのテンプレートからもリンクされていない |
| GET /password-reset-request | AuthPageController | リダイレクト用。テンプレートは /password/forgot のみ使用 |
| GET /items/add | ListingPageController | テンプレートは /product/add のみ使用 |

---

## 参考：逆パターン（Thymeleaf は参照しているがバックエンドにないもの）

| 送信先 | テンプレート | 備考 |
|--------|-------------|------|
| POST /cart/add | product_detail.html（カートに追加フォーム） | コントローラ未実装 |
| POST /product/{id}/review | product_detail.html（レビュー送信フォーム） | コントローラ未実装（注文単位の POST /user/orders/{id}/review はあり） |

---

*Thymeleaf テンプレート・ページ用コントローラを照合して作成*
