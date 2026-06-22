# Keep kotlinx.serialization metadata
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keep,includedescriptorclasses class com.fitin60.app.**$$serializer { *; }
-keepclassmembers class com.fitin60.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.fitin60.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
