import os

os.makedirs("app/src/main/res/values", exist_ok=True)
os.makedirs("app/src/main/res/drawable", exist_ok=True)
os.makedirs("app/src/main/res/mipmap-anydpi-v26", exist_ok=True)

manifest = """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:name=".SmartCollegeApp"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.SmartCollege">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.SmartCollege"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
"""

strings = """<resources>
    <string name="app_name">Smart College</string>
    <string name="college_name">Lokmata Devi Ahilya Bai Holkar Rajkiya Engineering College, Mainpuri</string>
    <string name="college_tagline">॥ ज्ञानं परमं ध्येयम् ॥</string>
</resources>
"""

colors = """<resources>
    <color name="primary_navy">#1A2A49</color>
    <color name="accent_orange">#E8721A</color>
    <color name="success_green">#2D8A56</color>
    <color name="error_red">#D8524F</color>
    <color name="bg_light">#F4F6F9</color>
    <color name="white">#FFFFFF</color>
</resources>
"""

themes = """<resources>
    <style name="Theme.SmartCollege" parent="android:Theme.Material.Light.NoActionBar">
        <item name="android:statusBarColor">#1A2A49</item>
        <item name="android:windowLightStatusBar">false</item>
    </style>
</resources>
"""

os.makedirs("app/src/main/res/xml", exist_ok=True)
backup_rules = """<?xml version="1.0" encoding="utf-8"?>
<full-backup-content/>
"""
data_extraction_rules = """<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules/>
"""

with open("app/src/main/AndroidManifest.xml", "w", encoding="utf-8") as f:
    f.write(manifest)
with open("app/src/main/res/values/strings.xml", "w", encoding="utf-8") as f:
    f.write(strings)
with open("app/src/main/res/values/colors.xml", "w", encoding="utf-8") as f:
    f.write(colors)
with open("app/src/main/res/values/themes.xml", "w", encoding="utf-8") as f:
    f.write(themes)
with open("app/src/main/res/xml/backup_rules.xml", "w", encoding="utf-8") as f:
    f.write(backup_rules)
with open("app/src/main/res/xml/data_extraction_rules.xml", "w", encoding="utf-8") as f:
    f.write(data_extraction_rules)

# Vector Drawables for launcher icon
ic_launcher = """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#1A2A49"
        android:pathData="M0,0h108v108h-108z" />
    <path
        android:fillColor="#E8721A"
        android:pathData="M54,28L28,42l26,14l26,-14z" />
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M34,50v14c0,11 9,20 20,20s20,-9 20,-20v-14l-20,10.7z" />
</vector>
"""

with open("app/src/main/res/drawable/ic_launcher_foreground.xml", "w", encoding="utf-8") as f:
    f.write(ic_launcher)

ic_launcher_xml = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/primary_navy" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
"""
with open("app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml", "w", encoding="utf-8") as f:
    f.write(ic_launcher_xml)
with open("app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml", "w", encoding="utf-8") as f:
    f.write(ic_launcher_xml)

print("Resources created successfully.")
