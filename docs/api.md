# HTTP 接口（当前已实现）

统一前缀 `/api`，响应：

```json
{ "code": 0, "message": "ok", "data": {} }
```

`code != 0` 表示失败。业务错误一般 HTTP 200 + 业务码；未登录 HTTP 401。

## 认证

### POST `/api/auth/register`

无需登录。

```json
{
  "phone": "13800000000",
  "password": "123456",
  "nickname": "可选",
  "smsCode": "预留，不校验"
}
```

返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "expiresIn": 7200,
    "tokenType": "Bearer"
  }
}
```

手机号需为大陆 11 位。重复注册返回 `1001`。

### POST `/api/auth/login`

字段同登录：`phone`、`password`、可选 `smsCode`。账号或密码错误 `1003`，禁用 `1004`。

### POST `/api/auth/refresh`

```json
{ "refreshToken": "..." }
```

刷新后旧 refresh jti 被覆盖，需改用新的一对令牌。

### POST `/api/auth/logout`

需要登录。Header：`Authorization: Bearer <accessToken>`。accessToken 进入 Redis 黑名单，refresh 失效。

## 用户

Header 均需 Bearer Token。

### GET `/api/user/me`

返回 `id`、`phone`、`nickname`、`avatar`、`status`。

### PUT `/api/user/profile`

```json
{ "nickname": "新昵称", "avatar": "https://oss.example/avatar.png" }
```

头像目前只存 URL，OSS 上传由客户端后续对接。

## 未开放

任务匹配、计时、积分兑换、知识点、社交接口尚未提供 Controller，请勿调用。
