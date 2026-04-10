// Top-level build file. Plugins declared here are available to subprojects via alias().
plugins {
    alias(libs.plugins.android.application)  apply false
    alias(libs.plugins.kotlin.android)       apply false
    alias(libs.plugins.kotlin.compose)       apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
