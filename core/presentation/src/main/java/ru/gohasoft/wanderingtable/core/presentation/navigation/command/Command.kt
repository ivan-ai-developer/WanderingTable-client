package ru.gohasoft.wanderingtable.core.presentation.navigation.command

import ru.gohasoft.wanderingtable.core.presentation.navigation.command.context.nav3.Navigation3CommandContext

/** A single navigation operation. Feature modules may implement their own. */
fun interface Command {
    suspend fun execute(context: Navigation3CommandContext)
}