package ru.gohasoft.wanderingtable.core.presentation.navigation.command

import ru.gohasoft.wanderingtable.core.presentation.navigation.screen.Screen
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.context.nav3.Navigation3CommandContext

/** Pop until the given screen type is on top (or no-op if absent). */
class BackTo(private val screenClass: Class<out Screen>) : Command {
    override suspend fun execute(context: Navigation3CommandContext) {
        val stack = context.backStack
        val index = stack.indexOfLast { screenClass.isInstance(it) }
        if (index >= 0) {
            while (stack.size > index + 1) stack.removeLastOrNull()
        }
    }
}

inline fun <reified S : Screen> BackTo() = BackTo(S::class.java)