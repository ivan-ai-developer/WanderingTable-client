package ru.gohasoft.wanderingtable.feature.main.clubadmin

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.exception.NetworkException
import ru.gohasoft.wanderingtable.core.domain.model.user.Role
import ru.gohasoft.wanderingtable.core.domain.model.user.User
import ru.gohasoft.wanderingtable.feature.main.fake.FakeRouter
import ru.gohasoft.wanderingtable.feature.main.fake.FakeUserRepository

class ClubAdminViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var router: FakeRouter
    private lateinit var userRepository: FakeUserRepository

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        router = FakeRouter()
        userRepository = FakeUserRepository().apply {
            profile = FakeUserRepository.profile(
                id = MANAGER_ID,
                roles = listOf(Role.PLAYER, Role.CLUB_MANAGER),
            )
        }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the manager's own roles load into the first section`() = runTest {
        val viewModel = createViewModel()

        assertThat(viewModel.state.value.myRoles)
            .containsExactlyInAnyOrder(Role.PLAYER, Role.CLUB_MANAGER)
        assertThat(viewModel.state.value.myUserId).isEqualTo(MANAGER_ID)
    }

    /**
     * The endpoint replaces the whole set, so granting one role must resend the existing ones —
     * otherwise the manager would strip their own `CLUB_MANAGER` by turning on `GAME_CREATOR`.
     */
    @Test
    fun `granting yourself a role keeps the roles you already hold`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(ClubAdminEvent.OnMyRoleChanged(Role.GAME_CREATOR, granted = true))

        val update = userRepository.roleUpdates.single()
        assertThat(update.userId).isEqualTo(MANAGER_ID)
        assertThat(update.roles)
            .containsExactlyInAnyOrder(Role.PLAYER, Role.CLUB_MANAGER, Role.GAME_CREATOR)
    }

    @Test
    fun `the state reflects the roles the server answered with`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(ClubAdminEvent.OnMyRoleChanged(Role.GAME_CREATOR, granted = true))

        assertThat(viewModel.state.value.myRoles)
            .containsExactlyInAnyOrder(Role.PLAYER, Role.CLUB_MANAGER, Role.GAME_CREATOR)
        assertThat(viewModel.state.value.savingRole).isNull()
    }

    @Test
    fun `revoking a role sends the set without it`() = runTest {
        userRepository.profile = FakeUserRepository.profile(
            id = MANAGER_ID,
            roles = listOf(Role.PLAYER, Role.CLUB_MANAGER, Role.NEWS_CREATOR),
        )
        val viewModel = createViewModel()

        viewModel.onEvent(ClubAdminEvent.OnMyRoleChanged(Role.NEWS_CREATOR, granted = false))

        assertThat(userRepository.roleUpdates.single().roles)
            .containsExactlyInAnyOrder(Role.PLAYER, Role.CLUB_MANAGER)
    }

    @Test
    fun `searching an unknown email reports it without clearing the section`() = runTest {
        userRepository.findByEmailResult = Result.Error(NetworkException.NotFound())
        val viewModel = createViewModel()
        viewModel.onEvent(ClubAdminEvent.OnEmailChanged("ghost@example.com"))

        viewModel.onEvent(ClubAdminEvent.OnSearchClick)

        assertThat(viewModel.state.value.searchError).isNotNull()
        assertThat(viewModel.state.value.member).isNull()
        assertThat(viewModel.state.value.isSearching).isEqualTo(false)
    }

    @Test
    fun `a found member arrives with the roles they already hold`() = runTest {
        userRepository.findByEmailResult = Result.Success(member())
        val viewModel = createViewModel()
        viewModel.onEvent(ClubAdminEvent.OnEmailChanged("mia@example.com"))

        viewModel.onEvent(ClubAdminEvent.OnSearchClick)

        val found = requireNotNull(viewModel.state.value.member)
        assertThat(found.id).isEqualTo(MEMBER_ID)
        assertThat(found.initials).isEqualTo("MR")
        assertThat(found.roles).containsExactlyInAnyOrder(Role.PLAYER, Role.NEWS_CREATOR)
    }

    @Test
    fun `granting a member a role preserves theirs, not the manager's`() = runTest {
        userRepository.findByEmailResult = Result.Success(member())
        val viewModel = createViewModel()
        viewModel.onEvent(ClubAdminEvent.OnEmailChanged("mia@example.com"))
        viewModel.onEvent(ClubAdminEvent.OnSearchClick)

        viewModel.onEvent(ClubAdminEvent.OnMemberRoleChanged(Role.GAME_CREATOR, granted = true))

        val update = userRepository.roleUpdates.single()
        assertThat(update.userId).isEqualTo(MEMBER_ID)
        assertThat(update.roles)
            .containsExactlyInAnyOrder(Role.PLAYER, Role.NEWS_CREATOR, Role.GAME_CREATOR)
    }

    @Test
    fun `an empty email does not hit the network`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(ClubAdminEvent.OnSearchClick)

        assertThat(viewModel.state.value.member).isNull()
        assertThat(viewModel.state.value.searchError).isNull()
    }

    @Test
    fun `a rejected role change leaves the shown roles untouched`() = runTest {
        userRepository.updateRolesResult = Result.Error(NetworkException.Conflict())
        val viewModel = createViewModel()

        viewModel.onEvent(ClubAdminEvent.OnMyRoleChanged(Role.CLUB_MANAGER, granted = false))

        assertThat(viewModel.state.value.myRoles)
            .containsExactlyInAnyOrder(Role.PLAYER, Role.CLUB_MANAGER)
        assertThat(viewModel.state.value.savingRole).isNull()
    }

    @Test
    fun `clearing the member resets the lookup form`() = runTest {
        userRepository.findByEmailResult = Result.Success(member())
        val viewModel = createViewModel()
        viewModel.onEvent(ClubAdminEvent.OnEmailChanged("mia@example.com"))
        viewModel.onEvent(ClubAdminEvent.OnSearchClick)

        viewModel.onEvent(ClubAdminEvent.OnClearMemberClick)

        assertThat(viewModel.state.value.member).isNull()
        assertThat(viewModel.state.value.email).isEmpty()
    }

    private fun member(): User = User(
        id = MEMBER_ID,
        name = "Mia Rodriguez",
        email = "mia@example.com",
        roles = listOf(Role.PLAYER, Role.NEWS_CREATOR),
    )

    private fun createViewModel() = ClubAdminViewModel(
        router = router,
        userRepository = userRepository,
    )

    private companion object {
        const val MANAGER_ID = "me"
        const val MEMBER_ID = "u2"
    }
}
