package ru.gohasoft.wanderingtable.core.presentation.navigation.command

import ru.gohasoft.wanderingtable.core.presentation.navigation.screen.compose.ComposableScreen
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.context.nav3.Navigation3CommandContext

/** Replace the topmost screen. */
class Replace(private val screen: ComposableScreen) : Command {
    override suspend fun execute(context: Navigation3CommandContext) {
        with(context.backStack) {
            removeLastOrNull()
            add(screen)
        }
    }
}