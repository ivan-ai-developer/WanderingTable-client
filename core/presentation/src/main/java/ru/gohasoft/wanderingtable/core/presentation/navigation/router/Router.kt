package ru.gohasoft.wanderingtable.core.presentation.navigation.router

import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Command

/** Entry point for navigation. Injected into ViewModels. */
interface Router {
    /** Commands in one call run sequentially as a batch. */
    fun execute(vararg commands: Command)
}