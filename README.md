# LDAP Manager Plugin for IntelliJ IDEA

一個功能完整的 IntelliJ IDEA 插件，用於連線和管理 LDAP 目錄服務。此插件提供了直觀的圖形界面來管理 LDAP 伺服器連線、瀏覽目錄結構、執行搜尋操作以及管理 LDAP 條目。

## 功能特色

### 🔗 連線管理
- **多 LDAP 伺服器支援**：同時管理多個 LDAP 伺服器連線
- **安全連線**：支援 SSL/TLS 加密連線（預設 LDAP 389 埠，SSL 636 埠）
- **連線測試**：建立連線前可測試連線有效性
- **持久化設定**：連線設定自動保存至 `ldap-plugin-settings.xml`，重啟後仍可使用
- **連線狀態顯示**：即時顯示連線狀態（已連線/未連線）

### 🌳 目錄瀏覽
- **樹狀結構瀏覽**：直觀的樹狀視圖瀏覽 LDAP 目錄
- **動態載入**：樹節點展開時動態載入子條目，提升效能
- **條目詳細資訊**：點選條目查看完整的屬性資訊
- **自訂樹狀渲染器**：美化的 LDAP 條目顯示

### 📝 條目管理
- **新增條目**：透過右鍵選單新增 LDAP 條目
- **修改條目**：編輯現有條目的屬性值
- **刪除條目**：安全刪除 LDAP 條目
- **條目編輯對話框**：友善的表單界面進行條目編輯

### 🔍 搜尋功能
- **LDAP 篩選器搜尋**：支援標準 LDAP 搜尋篩選器
- **範圍搜尋**：支援 Base DN 範圍內的搜尋
- **屬性選擇**：可指定要返回的屬性

### 🎨 使用者界面
- **工具視窗整合**：整合至 IntelliJ IDEA 左側工具視窗
- **分割面板**：左側樹狀瀏覽，右側詳細資訊
- **工具列**：完整的連線管理和操作按鈕
- **選單整合**：在 Tools 選單中提供 LDAP 相關動作

## 系統需求

- **IntelliJ IDEA**: 2024.2 或更高版本（支援至 2025.3.*）
- **Java 版本**: Java 21 或更高版本
- **平台支援**: IntelliJ IDEA Community Edition 及以上版本

## 安裝方式

### 方式一：從原始碼編譯安裝

1. **下載原始碼**
   ```bash
   git clone https://github.com/SamWang32191/ldap-plugin.git
   cd ldap-plugin
   ```

2. **編譯插件**
   ```bash
   ./gradlew build
   ```

3. **安裝插件**
   - 編譯完成後，在 `build/distributions/` 目錄中會生成 `LDAP Manager-1.0.1.zip` 檔案
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

## 技術架構

### 核心依賴
- **UnboundID LDAP SDK**: 6.0.9 - 提供 LDAP 客戶端功能
- **IntelliJ Platform**: 2024.2 - 插件開發平台
- **Java**: 21 - 開發語言版本

### 專案結構

```
src/main/java/com/ldapplugin/
├── action/                    # IntelliJ 動作類別
│   ├── ConnectToLdapAction    # 連線到 LDAP 動作
│   └── DisconnectLdapAction   # 斷開 LDAP 連線動作
├── model/                     # 資料模型
│   └── LdapConnection         # LDAP 連線模型類
├── service/                   # 服務層
│   ├── LdapConnectionService  # LDAP 連線服務介面
│   └── impl/
│       └── LdapConnectionServiceImpl  # 服務實現類
├── settings/                  # 設定管理
│   └── LdapSettingsState      # 持久化設定狀態管理
└── ui/                        # 使用者介面
    ├── LdapToolWindowFactory  # 工具視窗工廠
    ├── LdapToolWindowPanel    # 主要 UI 面板
    ├── dialog/                # 對話框
    │   ├── ConnectionConfigDialog  # 連線配置對話框
    │   └── EntryEditDialog    # 條目編輯對話框
    └── tree/                  # 樹狀元件
        ├── LdapTreeModel      # 樹狀模型
        └── LdapTreeNode       # 樹節點

src/main/resources/META-INF/
└── plugin.xml                # 插件配置檔案
```

### 主要類別說明

#### 服務層 (Service Layer)
- **`LdapConnectionService`**: 核心服務介面，提供所有 LDAP 操作功能
  - 連線管理（新增、移除、更新、測試連線）
  - LDAP 操作（搜尋、新增、修改、刪除條目）
  - 目錄瀏覽（取得子條目）

#### 模型層 (Model Layer)
- **`LdapConnection`**: 封裝 LDAP 連線資訊和狀態
  - 連線參數（主機、埠、認證資訊）
  - 連線狀態管理
  - SSL/TLS 支援

#### UI 層 (User Interface Layer)
- **`LdapToolWindowPanel`**: 主要使用者介面
  - 連線下拉選單和工具列
  - 分割面板（樹狀瀏覽 + 詳細資訊）
  - 滑鼠和鍵盤事件處理

#### 設定層 (Settings Layer)
- **`LdapSettingsState`**: 實現 IntelliJ 的持久化狀態組件
  - XML 序列化儲存連線設定
  - 應用程式重啟後自動載入設定

### 建置指令

- `./gradlew clean` - 清理建置檔案
- `./gradlew build` - 編譯和打包插件
- `./gradlew runIde` - 在開發模式下運行 IDE
- `./gradlew test` - 執行單元測試
- `./gradlew koverHtmlReport` - 產生程式碼覆蓋率報告
- `./gradlew runPluginVerifier` - 執行插件驗證
- `./gradlew publishPlugin` - 發布插件到 JetBrains Plugin Repository

### 開發工具
- **程式碼品質**: Qodana 靜態程式碼分析
- **測試覆蓋率**: Kover 測試覆蓋率報告
- **變更日誌**: 自動從 CHANGELOG.md 產生發布說明
- **插件驗證**: IntelliJ Plugin Verifier 相容性檢查

## 授權條款

本專案採用 MIT 授權條款，詳情請參閱 LICENSE 檔案。
