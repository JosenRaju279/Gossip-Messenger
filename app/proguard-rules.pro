# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Picasso
-dontwarn com.squareup.okhttp.**

# Firebase - keep model classes for serialization
-keepattributes Signature
-keepattributes *Annotation*

-keepclassmembers class com.example.gossipmessenger.Users {
    *;
}
-keepclassmembers class com.example.gossipmessenger.msgmodelClass {
    *;
}

# CircleImageView
-dontwarn de.hdodenhof.circleimageview.**