package ru.gohasoft.wanderingtable.feature.main.shell

/**
 * The three tabs that hold content. [navIndex] is the slot each one occupies in the bottom bar,
 * which has a fourth item — Create — that opens a sheet instead of switching tabs, and so has no
 * tab of its own.
 */
internal enum class MainTab(val navIndex: Int) {
    HOME(navIndex = 0),
    GAMES(navIndex = 1),
    PROFILE(navIndex = 3);

    internal companion object {
        const val CREATE_NAV_INDEX = 2

        fun fromNavIndex(navIndex: Int): MainTab? = entries.firstOrNull { it.navIndex == navIndex }
    }
}
