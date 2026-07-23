package ru.gohasoft.wanderingtable.core.presentation.navigation.command

import ru.gohasoft.wanderingtable.core.presentation.navigation.screen.compose.ComposableScreen
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.context.nav3.Navigation3CommandContext

/** Clear the stack and start from a new root. */
class NewRoot(private val screen: ComposableScreen) : Command {
    override suspend fun execute(context: Navigation3CommandContext) {
        with(context.backStack) {
            clear()
            add(screen)
        }
    }
}