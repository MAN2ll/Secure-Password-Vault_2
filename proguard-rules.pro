name: Build APK

on:
  push:
    branches: [ main, master ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'gradle'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '8.10.2'

      - name: Download Gradle Wrapper JAR
        run: |
          mkdir -p gradle/wrapper
          curl -fsSL \
            "https://raw.githubusercontent.com/gradle/gradle/refs/tags/v8.10.2/gradle/wrapper/gradle-wrapper.jar" \
            -o gradle/wrapper/gradle-wrapper.jar
          ls -la gradle/wrapper/

      - name: Make gradlew executable
        run: chmod +x gradlew

      - name: Build Debug APK
        run: ./gradlew assembleDebug --stacktrace --no-daemon
        env:
          ANDROID_HOME: ${{ env.ANDROID_SDK_ROOT }}

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: SecureVault-debug
          path: app/build/outputs/apk/debug/app-debug.apk
          retention-days: 30

      - name: Show APK info
        run: |
          echo "=== APK готов к скачиванию во вкладке Artifacts ==="
          ls -lh app/build/outputs/apk/debug/
