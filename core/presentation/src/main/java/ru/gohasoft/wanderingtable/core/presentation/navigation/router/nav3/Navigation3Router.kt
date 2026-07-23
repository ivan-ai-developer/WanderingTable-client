package ru.gohasoft.wanderingtable.core.presentation.navigation.router.nav3

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Command
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.Router
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Navigation3Router @Inject constructor() : Router {

    // UNLIMITED buffer: commands sent before the UI attaches (or during
    // configuration change) are queued, not dropped.
    private val commandChannel = Channel<List<Command>>(Channel.UNLIMITED)

    /** Collected by [ru.gohasoft.wanderingtable.core.presentation.navigation.nav3.NavigationHost] only. */
    val commands: Flow<List<Command>> = commandChannel.receiveAsFlow()

    override fun execute(vararg commands: Command) {
        commandChannel.trySend(commands.toList())
    }
}