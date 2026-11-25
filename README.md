# مدير رقم الجهاز SSAID (`com.android.ssaid.abdullah`)

تطبيق أندرويد مبني بلغة **Kotlin** وواجهة **Jetpack Compose (Material 3)** لقراءة وتعديل معرّف الجهاز (`ANDROID_ID / SSAID`) بعدة طرق تدعم العمل **بدون روت** و**مع روت**.

## أوضاع العمل المدعومة
1. **بدون روت (ADB / `WRITE_SECURE_SETTINGS`)**:
   ```bash
   adb shell pm grant com.android.ssaid.abdullah android.permission.WRITE_SECURE_SETTINGS
   ```
2. **Shizuku (بدون حاسوب)**:
   يدعم ربط `ShellUserService` عبر AIDL لمنح `WRITE_SECURE_SETTINGS` تلقائياً وتنفيذ أوامر `settings get/put secure android_id`.
3. **LSPatch / LSPosed Hook (بدون روت على أندرويد 8 إلى 15+)**:
   يحتوي المشروع على وحدة Xposed/LSPatch جاهزة (`SsaidHookEntry.kt`) مع `HookConfigProvider` لتخصيص SSAID مستقل لكل تطبيق بدون تعديل ملفات النظام وبدون إعادة تشغيل.
4. **Root (النظام)**:
   يقرأ ويعدّل `/data/system/users/0/settings_ssaid.xml` مع دعم فك وإعادة ترميز **ABX** على أندرويد 12 فأعلى (`abx2xml` / `xml2abx`) وأخذ نسخة احتياطية تلقائية قبل التعديل.

## البناء التلقائي عبر GitHub Actions
يحتوي المشروع على ملف `.github/workflows/android-build.yml` جاهز للبناء الفوري باستخدام JDK 17 و Gradle 8.7 دون الحاجة لتحميل مستودعات خارجية غير مستقرة.
