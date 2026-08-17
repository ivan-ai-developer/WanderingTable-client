package ru.gohasoft.wanderingtable.gate

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.exception.UnknownException
import ru.gohasoft.wanderingtable.core.domain.model.Session
import ru.gohasoft.wanderingtable.core.domain.model.user.User
import ru.gohasoft.wanderingtable.gate.fake.FakeAppEntryScreens
import ru.gohasoft.wanderingtable.gate.fake.FakeAuthRepository

class AppGateViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var appEntryScreens: FakeAppEntryScreens

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        appEntryScreens = FakeAppEntryScreens()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `existing session resolves to the home screen`() = runTest {
        authRepository.getSessionResult =
            Result.Success(Session(User(id = "1", name = "", email = "player@example.com")))

        val viewModel = AppGateViewModel(authRepository, appEntryScreens)

        assertThat(viewModel.startDestination.value).isEqualTo(appEntryScreens.home())
    }

    @Test
    fun `no session resolves to the login screen`() = runTest {
        authRepository.getSessionResult = Result.Success(null)

        val viewModel = AppGateViewModel(authRepository, appEntryScreens)

        assertThat(viewModel.startDestination.value).isEqualTo(appEntryScreens.login())
    }

    @Test
    fun `an unexpected error falls back to the login screen`() = runTest {
        authRepository.getSessionResult = Result.Error(UnknownException())

        val viewModel = AppGateViewModel(authRepository, appEntryScreens)

        assertThat(viewModel.startDestination.value).isEqualTo(appEntryScreens.login())
    }
}
