// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(app.plugins.android.application) apply false
    alias(app.plugins.android.library) apply false
    alias(app.plugins.android.kotlin) apply false
}
true // Needed to make the Suppress annotation work for the plugins block