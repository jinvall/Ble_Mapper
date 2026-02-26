# DriftBucket — Project Snapshot

## 1. Final Project Goal
{{FINAL_GOAL}}

## 2. Build Configuration
### Gradle Version
8.5

### Android Gradle Plugin (AGP)
8.2.2

### Kotlin Version
1.9.22

### Java Toolchain
17

## 3. Module: app
### Plugins
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

### Android Block
android {
    namespace = "com.jamjam.blemapper"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jamjam.blemapper"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

### Dependencies
dependencies {
    // Compose BOM for version alignment
    val composeBom = platform("androidx.compose:compose-bom:2024.01.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose core
	implementation("androidx.compose.ui:ui")
	implementation("androidx.compose.ui:ui-tooling-preview")

	// Material3 core
	implementation("androidx.compose.material3:material3:1.2.1")
	implementation("androidx.compose.material3:material3-window-size-class:1.2.1")

	// Material3 adaptive (REAL versions)
	implementation("androidx.compose.material3:material3-adaptive:1.0.0-alpha04")
	implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.0.0-alpha04")




    // Activity + Compose integration
    implementation("androidx.activity:activity-compose:1.8.2")

    // Lifecycle (for state handling)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // BLE scanning uses Android framework APIs — no extra libs needed
    implementation("androidx.core:core-ktx:1.12.0")

    // Debug tools
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

## 4. Root build.gradle(.kts)
// Root build.gradle.kts for AGP 8.2.2 + Gradle 8.5

plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

## 5. Settings.gradle(.kts)
// /data/data/com.termux/files/home/Blu/BleMapper/settings.gradle.kts

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "BleMapper"
include(":app")

## 6. Most Recent Changes
{{RECENT_CHANGES}}

## 7. Upcoming Changes
{{UPCOMING_CHANGES}}

## 8. Known Issues
{{KNOWN_ISSUES}}

## 9. Required Next Action
{{NEXT_ACTION}}

## 10. Notes
```text
```

## 11. Generator Script (generate_driftbucket.sh)
```bash
#!/bin/bash

TEMPLATE="DriftBucket.template.md"
OUTPUT="DriftBucket.md"

# Extract versions directly from the correct files
GRADLE_VERSION=$(./gradlew --version | grep "Gradle" | awk '{print $2}')
AGP_VERSION=$(grep 'com.android.application' build.gradle.kts | sed -E 's/.*version "([^"]+)".*/\1/')
KOTLIN_VERSION=$(grep 'org.jetbrains.kotlin.android' build.gradle.kts | sed -E 's/.*version "([^"]+)".*/\1/')
JAVA_TOOLCHAIN=$(grep 'jvmTarget' app/build.gradle.kts | sed -E 's/.*"([^"]+)".*/\1/')

# Extract module info
APP_PLUGINS=$(sed -n '/plugins {/,/}/p' app/build.gradle.kts)
APP_ANDROID_BLOCK=$(sed -n '/android {/,/}/p' app/build.gradle.kts)
APP_DEPENDENCIES=$(sed -n '/dependencies {/,/}/p' app/build.gradle.kts)

# Root build
ROOT_BUILD=$(cat build.gradle.kts)

# Settings
SETTINGS_GRADLE=$(cat settings.gradle.kts)

# Notes placeholder (user‑editable)
NOTES_CONTENT=$(cat notes.txt 2>/dev/null)

# Generate DriftBucket.md
awk \
  -v GRADLE_VERSION="$GRADLE_VERSION" \
  -v AGP_VERSION="$AGP_VERSION" \
  -v KOTLIN_VERSION="$KOTLIN_VERSION" \
  -v JAVA_TOOLCHAIN="$JAVA_TOOLCHAIN" \
  -v APP_PLUGINS="$APP_PLUGINS" \
  -v APP_ANDROID_BLOCK="$APP_ANDROID_BLOCK" \
  -v APP_DEPENDENCIES="$APP_DEPENDENCIES" \
  -v ROOT_BUILD="$ROOT_BUILD" \
  -v SETTINGS_GRADLE="$SETTINGS_GRADLE" \
  -v NOTES_CONTENT="$NOTES_CONTENT" \
'
{
    gsub("{{GRADLE_VERSION}}", GRADLE_VERSION);
    gsub("{{AGP_VERSION}}", AGP_VERSION);
    gsub("{{KOTLIN_VERSION}}", KOTLIN_VERSION);
    gsub("{{JAVA_TOOLCHAIN}}", JAVA_TOOLCHAIN);
    gsub("{{APP_PLUGINS}}", APP_PLUGINS);
    gsub("{{APP_ANDROID_BLOCK}}", APP_ANDROID_BLOCK);
    gsub("{{APP_DEPENDENCIES}}", APP_DEPENDENCIES);
    gsub("{{ROOT_BUILD}}", ROOT_BUILD);
    gsub("{{SETTINGS_GRADLE}}", SETTINGS_GRADLE);
    gsub("{{NOTES}}", NOTES_CONTENT);
    print;
}
' "$TEMPLATE" > "$OUTPUT"

# Append #10 Notes
{
  echo ""
  echo "## 10. Notes"
  echo '```text'
  [ -n "$NOTES_CONTENT" ] && echo "$NOTES_CONTENT"
  echo '```'
} >> "$OUTPUT"

# Append #11 Generator Script
{
  echo ""
  echo "## 11. Generator Script (generate_driftbucket.sh)"
  echo '```bash'
  cat generate_driftbucket.sh
  echo '```'
} >> "$OUTPUT"

# Append #12 Template File
{
  echo ""
  echo "## 12. Template File (DriftBucket.template.md)"
  echo '```markdown'
  cat DriftBucket.template.md
  echo '```'
} >> "$OUTPUT"

echo "DriftBucket.md generated."

```

## 12. Template File (DriftBucket.template.md)
```markdown
# DriftBucket — Project Snapshot

## 1. Final Project Goal
{{FINAL_GOAL}}

## 2. Build Configuration
### Gradle Version
{{GRADLE_VERSION}}

### Android Gradle Plugin (AGP)
{{AGP_VERSION}}

### Kotlin Version
{{KOTLIN_VERSION}}

### Java Toolchain
{{JAVA_TOOLCHAIN}}

## 3. Module: app
### Plugins
{{APP_PLUGINS}}

### Android Block
{{APP_ANDROID_BLOCK}}

### Dependencies
{{APP_DEPENDENCIES}}

## 4. Root build.gradle(.kts)
{{ROOT_BUILD}}

## 5. Settings.gradle(.kts)
{{SETTINGS_GRADLE}}

## 6. Most Recent Changes
{{RECENT_CHANGES}}

## 7. Upcoming Changes
{{UPCOMING_CHANGES}}

## 8. Known Issues
{{KNOWN_ISSUES}}

## 9. Required Next Action
{{NEXT_ACTION}}
```
