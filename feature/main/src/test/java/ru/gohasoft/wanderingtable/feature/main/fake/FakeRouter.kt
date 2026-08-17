package ru.gohasoft.wanderingtable.feature.main.fake

import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Command
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.Router

internal class FakeRouter : Router {
    val executedCommands = mutableListOf<Command>()

    override fun execute(vararg commands: Command) {
        executedCommands += commands
    }
}
