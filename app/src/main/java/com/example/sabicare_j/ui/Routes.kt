package com.example.sabicare_j.ui

/** Compose Navigation routes for the whole app */
object Routes {
    const val HOME = "home"
    const val TRACKER = "tracker"
    const val RESULTS = "results"
    const val PROFILE = "profile"

    const val ADD_ENTRY = "add_entry?type={type}"
    fun addEntry(type: String? = null) = "add_entry?type=${type.orEmpty()}"

    const val ADD_CHILD = "add_child?childId={childId}"
    fun addChild(childId: Long? = null) = "add_child?childId=${childId ?: -1L}"

    const val SETTINGS = "settings"
    const val ONBOARDING = "onboarding"

    /** Bottom-bar destinations (always show bar on these) */
    val bottomBarRoutes = setOf(HOME, TRACKER, RESULTS, PROFILE)
}
