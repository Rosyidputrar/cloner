# Kembar R8 rules.
# AndroidX, Hilt, Compose and DataStore ship their own consumer rules; only app-specific
# needs are listed here.

# Type-safe navigation: route classes are @Serializable and looked up through their
# generated serializers at runtime. Keep the serializer accessors of our own classes.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers @kotlinx.serialization.Serializable class io.github.rosyidputrar.kembar.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class io.github.rosyidputrar.kembar.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep source file names and line numbers for readable crash reports; hide the original
# source file name.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
