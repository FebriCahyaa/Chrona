# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.

# JNI entry points: R8 must preserve native method names and the bridge class.
-keep class com.febricahyaa.clockapp.nativelayer.ChronaNativeBridge { *; }
-keepclasseswithmembernames class com.febricahyaa.clockapp.nativelayer.ChronaNativeBridge {
    native <methods>;
}

# Room schema/annotation reflection and generated adapters.
-keep @androidx.room.Database class com.febricahyaa.clockapp.data.local.** { *; }
-keep @androidx.room.Entity class com.febricahyaa.clockapp.data.local.** { *; }
-keep @androidx.room.Dao interface com.febricahyaa.clockapp.data.local.** { *; }
-keep class com.febricahyaa.clockapp.data.local.** { *; }

# Gson models are populated from GitHub JSON field names.
-keep class com.febricahyaa.clockapp.data.update.GitHubReleaseDto { *; }
-keep class com.febricahyaa.clockapp.data.update.GitHubAssetDto { *; }

# Hilt-generated application entry points are referenced by the Android runtime.
-keep class com.febricahyaa.clockapp.Hilt_** { *; }

# Crashlytics should retain file/line metadata in optimized release mappings.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# AIDL/Binder stubs are generated and referenced by the Android framework.
-keep class com.febricahyaa.clockapp.ipc.IChronaSystemService$Stub { *; }
-keep class com.febricahyaa.clockapp.ipc.IChronaSystemService { *; }

# Lottie ships consumer R8 metadata; no blanket keep rule is required here.
