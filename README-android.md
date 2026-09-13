# Android Studio 本地打包

官方离线 SDK 不能放进 git。按下面做一次就能用 Android Studio 出 debug APK。

## 1. 编译 uni-app 的 App 资源

在仓库根目录：

```bash
npm install
npm run build:app-android
npm run sync:android
```

资源会拷到 `android/app/src/main/assets/apps/__UNI__WEIJI01/www/`。

## 2. 放入 DCloud Android 离线 SDK

1. 打开 [Android 离线 SDK 下载](https://nativesupport.dcloud.net.cn/AppDocs/download/android)。
2. 解压官方工程（名称类似 `SDK` / `HBuilder-Integrate-AS`）。
3. 把其中的 `aar`、`jar`、以及 `jniLibs`/`libs` 下的 `.so` 复制到本仓库的 [`android/app/libs`](android/app/libs)。
4. 若官方示例还有 `res`、`AndroidManifest` 合并项（如 `DCloudApplication` 需要的 meta-data），按 [Android 离线打包](https://nativesupport.dcloud.net.cn/AppDocs/usesdk/android) 补进 `android/app`。本仓库已预置：
   - `appid` `__UNI__WEIJI01`（与 [`src/manifest.json`](src/manifest.json) 一致）
   - 启动 Activity `io.dcloud.PandoraEntry`
   - 明文 HTTP（局域网调试）

未放入 SDK 时 Gradle 会因找不到 `io.dcloud.*` 而失败，这是预期。

## 3. Android Studio

1. 安装 [Android Studio](https://developer.android.com/studio)，带 Android SDK 33。
2. Open `android/` 目录（不要打开仓库根）。
3. 同步 Gradle；`local.properties` 由 IDE 生成，不要提交。
4. 连真机或模拟器，Run，或 `Build > Build Bundle(s) / APK(s) > Build APK(s)`。
5. 命令行（需已生成 `gradlew`，可在 Android Studio 执行一次 Sync）：

```bash
cd android
gradlew.bat assembleDebug
```

APK 一般在 `android/app/build/outputs/apk/debug/`。

## 4. 真机连本机后端

1. 电脑和手机同一 Wi‑Fi，后端监听 `0.0.0.0:8080`（不要只绑 `127.0.0.1`）。
2. 打开 App，登录页填服务器：`http://电脑局域网IP:8080`（不要填 localhost）。
3. 也可在「我」里改地址。
