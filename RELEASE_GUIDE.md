# Release 發布指南

本專案已設置自動化 GitHub Actions 工作流程，可以手動觸發 release 並自動增加版本號。

## 🚀 如何發布新版本

### 方法一：透過 GitHub 網頁介面

1. 前往 [GitHub Actions 頁面](https://github.com/SamWang32191/ldap-plugin/actions)
2. 點擊左側的「手動發布 Release」工作流程
3. 點擊右側的「Run workflow」按鈕
4. 選擇發布類型：
   - **patch** (預設)：小版號 +1 (例如：1.0.0 → 1.0.1)
   - **minor**：中版號 +1，小版號歸零 (例如：1.0.5 → 1.1.0)  
   - **major**：大版號 +1，中、小版號歸零 (例如：1.2.3 → 2.0.0)
5. 可選填發布說明
6. 點擊「Run workflow」開始執行

### 方法二：透過 GitHub CLI

```bash
# 發布 patch 版本 (預設)
gh workflow run "手動發布 Release" --repo SamWang32191/ldap-plugin

# 發布 minor 版本
gh workflow run "手動發布 Release" --repo SamWang32191/ldap-plugin \
  -f release_type=minor \
  -f release_notes="新增功能：支援 LDAP 群組管理"

# 發布 major 版本
gh workflow run "手動發布 Release" --repo SamWang32191/ldap-plugin \
  -f release_type=major \
  -f release_notes="重大更新：全新的使用者介面"
```

## 📋 工作流程執行步驟

當觸發工作流程後，系統會自動執行以下步驟：

1. **版本計算**：根據選擇的發布類型計算新版本號
2. **檔案更新**：
   - 更新 `gradle.properties` 中的 `pluginVersion`
   - 更新 `CHANGELOG.md` 添加新版本記錄
3. **建構測試**：
   - 執行 `./gradlew clean build`
   - 執行所有測試
   - 建構插件發布包
4. **版本提交**：
   - 提交版本變更到 Git
   - 創建版本標籤 (例如：v1.0.1)
5. **發布 Release**：
   - 在 GitHub 創建新的 Release
   - 上傳建構好的插件 ZIP 檔案
   - 生成發布說明

## 📦 發布產物

每次成功發布後，會在 [Releases 頁面](https://github.com/SamWang32191/ldap-plugin/releases) 產生：

- **LDAP-Manager-{版本號}.zip**：可直接安裝到 IntelliJ IDEA 的插件檔案
- **發布說明**：包含變更內容和安裝指南
- **自動生成的變更日誌**：比較與上一版本的差異

## 🔧 安裝發布的插件

1. 前往 [Releases 頁面](https://github.com/SamWang32191/ldap-plugin/releases)
2. 下載最新版本的 ZIP 檔案
3. 在 IntelliJ IDEA 中：
   - `File` → `Settings` → `Plugins`
   - 點擊齒輪圖示 → `Install Plugin from Disk...`
   - 選擇下載的 ZIP 檔案
   - 重新啟動 IDE

## ⚠️ 注意事項

- 工作流程會自動推送版本變更到 `main` 分支
- 請確保在觸發 release 前，所有變更都已合併到 `main` 分支
- 版本號遵循 [語義化版本](https://semver.org/) 規範
- 建議在發布前先在本地測試建構：`./gradlew clean build test`

## 🐛 故障排除

如果工作流程執行失敗，請檢查：

1. **權限問題**：確保 GitHub Actions 有寫入權限
2. **建構錯誤**：檢查 Java 21 和 Gradle 配置
3. **測試失敗**：修復失敗的測試後重新觸發
4. **版本衝突**：確保沒有重複的版本標籤

需要協助時，請查看 [GitHub Actions 執行日誌](https://github.com/SamWang32191/ldap-plugin/actions) 獲取詳細錯誤資訊。
