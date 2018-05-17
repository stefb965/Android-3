mkdir "$ANDROID_HOME/licenses" || true;
echo "8933bad161af4178b1185d1a37fbf41ea5269c55" > "$ANDROID_HOME/licenses/android-sdk-license";
echo "d56f5187479451eabf01fb78af6dfcb131a6481e" > "$ANDROID_HOME/licenses/android-sdk-license";
sdkmanager tools;
yes | sdkmanager --licenses;
echo "Fetching ndk-bundle. Suppressing output to avoid travis 4MG size limit";
sdkmanager "ndk-bundle" >/dev/null;
echo "Fetching ndk-bundle complete";
sdkmanager "system-images;android-23;default;armeabi-v7a";
echo no | android create avd --force -n test -t android-23 --abi armeabi-v7a
emulator -avd test -no-audio -no-window &
