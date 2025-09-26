import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.20"
    id("org.jetbrains.intellij") version "1.17.3"
    id("org.jetbrains.changelog") version "2.2.0"
    id("org.jetbrains.qodana") version "0.1.13"
    id("org.jetbrains.kotlinx.kover") version "0.7.4"
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

// 使用 JDK 21 工具鏈，但為了與目標 IntelliJ 平台相容，將位元組碼目標設為 17
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// 配置 Kotlin 編譯選項
kotlin {
    jvmToolchain(21)
}

// 讓 Kotlin 以 JVM 17 目標編譯，以符合 2025.1 平台需求
tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
}

// 配置專案儲存庫
repositories {
    mavenCentral()
}

// 依賴項
dependencies {
    // LDAP 客戶端依賴
    implementation("com.unboundid:unboundid-ldapsdk:6.0.9")
    
    // 測試依賴
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.6.0")
}

// 配置 Gradle IntelliJ Plugin
// 詳細資訊請參閱 https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    pluginName = properties("pluginName")
    version = properties("platformVersion")
    type = properties("platformType")

    // 插件依賴項
    // 範例：platformPlugins = properties("platformPlugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) }
    plugins = properties("platformPlugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) }
}

// 配置 Gradle Changelog Plugin
// 詳細資訊請參閱 https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = properties("pluginRepositoryUrl")
}

// 配置 Gradle Qodana Plugin
// 詳細資訊請參閱 https://github.com/JetBrains/gradle-qodana-plugin
qodana {
    cachePath = provider { file(".qodana").canonicalPath }
    reportPath = provider { file("build/reports/inspections").canonicalPath }
    saveReport = true
    showReport = environment("QODANA_SHOW_REPORT").map { it.toBoolean() }.getOrElse(false)
}

// 配置 Gradle Kover Plugin
// 詳細資訊請參閱 https://github.com/Kotlin/kotlinx-kover#configuration
koverReport {
    defaults {
        xml {
            onCheck = true
        }
    }
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }

    // Disable building Searchable Options during build/verifier to avoid headless IDE leaks on JBR 21
    buildSearchableOptions {
        enabled = false
    }

    patchPluginXml {
        version = properties("pluginVersion")
        sinceBuild = properties("pluginSinceBuild")
        untilBuild = properties("pluginUntilBuild")

        // 從 CHANGELOG.md 檔案中提取變更日誌並提供給插件的元資料
        changeNotes = provider {
            with(changelog) {
                renderItem(
                    (getOrNull(properties("pluginVersion").get()) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }
    }

    // 配置 UI 測試插件
    // 詳細資訊請參閱 https://github.com/JetBrains/intellij-ui-test-robot
    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }

    signPlugin {
        certificateChain = environment("CERTIFICATE_CHAIN")
        privateKey = environment("PRIVATE_KEY")
        password = environment("PRIVATE_KEY_PASSWORD")
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token = environment("PUBLISH_TOKEN")
        // pluginVersion 基於 SemVer (https://semver.org) 和 IntelliJ Platform 版本相容性
        channels = listOf("default")
    }

    // Restrict verifier to the declared platform version to avoid missing build downloads
    runPluginVerifier {
        ideVersions = listOf(properties("platformVersion").get())
    }
}
