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

