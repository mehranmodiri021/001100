# ═══════════════════════════════════════════════════════════════
# ProGuard / R8 Rules for Arena Clash
# ═══════════════════════════════════════════════════════════════
#
# این فایل قواعد نگه‌داشتن کلاس‌هایی که ProGuard نباید حذف یا obfuscate کنه
# رو تعریف می‌کنه. بدون این قواعد، اپ در حالت Release کرش می‌کنه.
#
# ─── حفظ اطلاعات دیباگ ───
# این خطوط به حفظ شماره خطوط برای گزارش خطای دقیق‌تر کمک می‌کنه
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# حفظ annotation ها برای reflection
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# ═══════════════════════════════════════════════════════════════
# ─── Tapsell Plus SDK (تبلیغات) ───
# ═══════════════════════════════════════════════════════════════
# تپسل از reflection و callback استفاده می‌کنه، پس باید کامل حفظ بشه
-keep class ir.tapsell.plus.** { *; }
-keep interface ir.tapsell.plus.** { *; }
-dontwarn ir.tapsell.plus.**

-keep class ir.tapsell.sdk.** { *; }
-keep interface ir.tapsell.sdk.** { *; }
-dontwarn ir.tapsell.sdk.**

# کتابخانه‌های داخلی تپسل
-keep class ir.tapsell.mediation.** { *; }
-dontwarn ir.tapsell.mediation.**

# ═══════════════════════════════════════════════════════════════
# ─── Poolakey (IAP کافه‌بازار) ───
# ═══════════════════════════════════════════════════════════════
-keep class ir.cafebazaar.poolakey.** { *; }
-keep interface ir.cafebazaar.poolakey.** { *; }
-dontwarn ir.cafebazaar.poolakey.**

# کتابخانه‌های داخلی کافه‌بازار
-keep class com.farsitel.bazaar.** { *; }
-dontwarn com.farsitel.bazaar.**

-keep class ir.cafebazaar.** { *; }
-dontwarn ir.cafebazaar.**

# کتابخانه‌های IAP
-keep class com.android.vending.billing.** { *; }
-dontwarn com.android.vending.billing.**

# ═══════════════════════════════════════════════════════════════
# ─── Room (دیتابیس محلی) ───
# ═══════════════════════════════════════════════════════════════
# Room از annotation processing استفاده می‌کنه، پس کلاس‌های تولیدشده باید حفظ بشن
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }
-dontwarn androidx.room.paging.**

# حفظ نام فیلدهای Entity برای دسترسی Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}

# ═══════════════════════════════════════════════════════════════
# ─── Moshi (JSON Parser) ───
# ═══════════════════════════════════════════════════════════════
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# حفظ annotation های Moshi
-keep @com.squareup.moshi.JsonQualifier interface *
-keepclassmembers class ** {
    @com.squareup.moshi.Json <fields>;
    @com.squareup.moshi.Json <methods>;
}

# Moshi codegen
-keep class **JsonAdapter { *; }
-keepnames @com.squareup.moshi.JsonClass class *

# ═══════════════════════════════════════════════════════════════
# ─── Kotlin ───
# ═══════════════════════════════════════════════════════════════
# حفظ metadata کاتلین برای reflection
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# حفظ کلاس‌های coroutines
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ═══════════════════════════════════════════════════════════════
# ─── OkHttp / Retrofit (اگه استفاده می‌کنی) ───
# ═══════════════════════════════════════════════════════════════
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations

# ═══════════════════════════════════════════════════════════════
# ─── Jetpack Compose ───
# ═══════════════════════════════════════════════════════════════
# Compose از reflection استفاده می‌کنه
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ═══════════════════════════════════════════════════════════════
# ─── AndroidX General ───
# ═══════════════════════════════════════════════════════════════
-keep class androidx.lifecycle.** { *; }
-keep class androidx.navigation.** { *; }

# ═══════════════════════════════════════════════════════════════
# ─── کلاس Application و Activity های پروژه ───
# ═══════════════════════════════════════════════════════════════
# این‌ها رو باید صریحاً حفظ کنیم چون اندروید از طریق Manifest صدا می‌زنه
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# ═══════════════════════════════════════════════════════════════
# ─── مدل‌های پروژه (data classes) ───
# ═══════════════════════════════════════════════════════════════
# حفظ مدل‌های داده در پکیج پروژه
-keep class com.aistudio.challengearena.vxpqz.data.model.** { *; }
-keep class com.aistudio.challengearena.vxpqz.data.local.entity.** { *; }
