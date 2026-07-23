package ru.gohasoft.wanderingtable.core.presentation.navigation.command

import ru.gohasoft.wanderingtable.core.presentation.navigation.screen.compose.ComposableScreen
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.context.nav3.Navigation3CommandContext

/** Push a screen on top of the stack. */
class Forward(private val screen: ComposableScreen) : Command {
    override suspend fun execute(context: Navigation3CommandContext) {
        context.backStack.add(screen)
    }
}