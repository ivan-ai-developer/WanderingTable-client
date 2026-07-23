package ru.gohasoft.wanderingtable.core.presentation.navigation.command

import ru.gohasoft.wanderingtable.core.presentation.navigation.command.context.nav3.Navigation3CommandContext

/** Pop the topmost screen. No-op guard: never empties the stack completely. */
class Back : Command {
    override suspend fun execute(context: Navigation3CommandContext) {
        if (context.backStack.size > 1) context.backStack.removeLastOrNull()
    }
}