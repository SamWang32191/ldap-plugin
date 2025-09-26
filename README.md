# LDAP Manager Plugin for IntelliJ IDEA

一個功能完整的 IntelliJ IDEA 插件，用於連線和管理 LDAP 目錄服務。

## 功能特色

- **多 LDAP 伺服器連線管理**：支援同時管理多個 LDAP 伺服器連線
- **安全連線**：支援 SSL/TLS 加密連線
- **目錄瀏覽**：樹狀結構瀏覽 LDAP 目錄
- **條目管理**：支援 LDAP 條目的新增、修改、刪除操作
- **搜尋功能**：強大的 LDAP 搜尋功能
- **持久化設定**：連線設定自動保存，重啟後仍可使用
- **友好界面**：直觀的用戶界面，易於操作

## 系統需求

- IntelliJ IDEA 2024.2 或更高版本（JBR 21）
- Java 21 或更高版本

## 安裝方式

### 方式一：從原始碼編譯安裝

1. **下載原始碼**
   ```bash
   git clone https://github.com/ldap-plugin/ldap-plugin.git
   cd ldap-plugin
   ```

2. **編譯插件**
   ```bash
   ./gradlew build
   ```

3. **安裝插件**
   - 編譯完成後，在 `build/distributions/` 目錄中會生成 `LDAP Manager-1.0.0.zip` 檔案
   - 在 IntelliJ IDEA 中，開啟 `File` → `Settings` → `Plugins`
   - 點擊齒輪圖示 → `Install Plugin from Disk...`
   - 選擇剛才生成的 zip 檔案進行安裝
   - 重啟 IntelliJ IDEA

### 方式二：開發模式運行

如果您想在開發模式下運行插件：

```bash
./gradlew runIde
```

這將啟動一個包含該插件的 IntelliJ IDEA 實例。

## 使用說明

### 開啟 LDAP Manager

安裝完成後，您可以通過以下方式開啟 LDAP Manager：

1. **工具視窗**：在 IDE 左側邊欄中找到 "LDAP Manager" 工具視窗
2. **選單列**：`Tools` → `LDAP` → `連線到 LDAP`

### 建立 LDAP 連線

1. 點擊工具視窗中的 "新增連線" 按鈕
2. 填寫連線資訊：
   - **連線名稱**：為此連線設定一個識別名稱
   - **伺服器地址**：LDAP 伺服器的 IP 位址或域名
   - **連接埠**：LDAP 伺服器連接埠（預設 389，SSL 為 636）
   - **使用者名稱**：用於認證的使用者 DN
   - **密碼**：認證密碼
   - **Base DN**：搜尋的基礎 DN
   - **使用 SSL**：是否使用 SSL 加密連線
3. 點擊 "測試連線" 確認設定正確
4. 點擊 "確定" 保存連線

### 瀏覽 LDAP 目錄

1. 在連線列表中雙擊已建立的連線
2. 成功連線後，左側會顯示 LDAP 目錄樹
3. 展開樹狀節點瀏覽目錄結構
4. 點擊條目查看詳細屬性

### 管理 LDAP 條目

- **新增條目**：右鍵點擊父節點 → "新增條目"
- **修改條目**：右鍵點擊條目 → "修改條目"
- **刪除條目**：右鍵點擊條目 → "刪除條目"
- **搜尋條目**：使用搜尋框輸入 LDAP 篩選器進行搜尋

## 開發資訊

### 專案結構

```
src/
├── main/
│   ├── java/com/ldapplugin/
│   │   ├── action/          # 動作類別
│   │   ├── model/           # 資料模型
│   │   ├── service/         # 服務層
│   │   ├── settings/        # 設定管理
│   │   └── ui/              # 使用者介面
│   └── resources/
│       └── META-INF/
│           └── plugin.xml   # 插件配置
```

### 建置指令

- `./gradlew clean` - 清理建置檔案
- `./gradlew build` - 編譯和打包插件
- `./gradlew runIde` - 在開發模式下運行 IDE
- `./gradlew publishPlugin` - 發布插件到 JetBrains Plugin Repository

## 授權條款

本專案採用 MIT 授權條款，詳情請參閱 LICENSE 檔案。

## 貢獻指南

歡迎提交 Issue 和 Pull Request 來改進這個插件！

## 支援

如果您遇到問題或有建議，請在 GitHub 上提交 Issue。

