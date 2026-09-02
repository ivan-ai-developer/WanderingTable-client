package ru.gohasoft.wanderingtable.feature.main.clubadmin

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.firstSuccessOrErrorResult
import ru.gohasoft.wanderingtable.core.domain.model.user.Role
import ru.gohasoft.wanderingtable.core.domain.model.user.User
import ru.gohasoft.wanderingtable.core.domain.repository.UserRepository
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Back
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.ShowSnackbar
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.Router
import ru.gohasoft.wanderingtable.core.presentation.utils.SnackbarScreenConfig
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource.StringResource
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviViewModel
import ru.gohasoft.wanderingtable.feature.main.R
import ru.gohasoft.wanderingtable.feature.main.clubadmin.ClubAdminEvent.OnBackClick
import ru.gohasoft.wanderingtable.feature.main.clubadmin.ClubAdminEvent.OnClearMemberClick
import ru.gohasoft.wanderingtable.feature.main.clubadmin.ClubAdminEvent.OnEmailChanged
import ru.gohasoft.wanderingtable.feature.main.clubadmin.ClubAdminEvent.OnMemberRoleChanged
import ru.gohasoft.wanderingtable.feature.main.clubadmin.ClubAdminEvent.OnMyRoleChanged
import ru.gohasoft.wanderingtable.feature.main.clubadmin.ClubAdminEvent.OnRetryClick
import ru.gohasoft.wanderingtable.feature.main.clubadmin.ClubAdminEvent.OnSearchClick
import ru.gohasoft.wanderingtable.feature.main.mapper.toInitials
import ru.gohasoft.wanderingtable.feature.main.mapper.toLoadError
import ru.gohasoft.wanderingtable.feature.main.mapper.toMemberLookupError
import ru.gohasoft.wanderingtable.feature.main.mapper.toRoleUpdateError

/**
 * Club administration: the manager's own roles, and granting roles to another member by email.
 *
 * Both halves work the same way, because `PATCH /users/{id}/roles` **replaces** the role set
 * rather than adding to it — every toggle sends the full set the member should end up with,
 * which is why the current set has to be known first.
 */
@HiltViewModel
internal class ClubAdminViewModel @Inject constructor(
    private val router: Router,
    private val userRepository: UserRepository,
) : MviViewModel<ClubAdminState, ClubAdminEvent, Unit>() {

    private val _state = MutableStateFlow(ClubAdminState())
    override val state: StateFlow<ClubAdminState> = _state.asStateFlow()

    init {
        load()
    }

    override fun onEvent(event: ClubAdminEvent) {
        when (event) {
            OnBackClick -> router.execute(Back())
            is OnEmailChanged -> _state.update {
                it.copy(email = event.email, searchError = null)
            }

            OnSearchClick -> search()
            OnClearMemberClick -> _state.update { it.copy(member = null, email = "") }
            is OnMyRoleChanged -> updateMyRoles(event.role, event.granted)
            is OnMemberRoleChanged -> updateMemberRoles(event.role, event.granted)
            OnRetryClick -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = userRepository.getProfile().firstSuccessOrErrorResult()
            val profile = result?.data
            _state.update { current ->
                when {
                    result is Result.Error -> current.copy(
                        isLoading = false,
                        error = result.error.toLoadError(),
                    )

                    profile == null -> current.copy(
                        isLoading = false,
                        error = StringResource(R.string.main_error_generic),
                    )

                    else -> current.copy(
                        isLoading = false,
                        error = null,
                        myUserId = profile.user.id,
                        myRoles = profile.user.roles.toSet(),
                    )
                }
            }
        }
    }

    private fun search() {
        val email = _state.value.email.trim()
        if (email.isEmpty() || _state.value.isSearching) return

        _state.update { it.copy(isSearching = true, searchError = null, member = null) }
        viewModelScope.launch {
            val result = userRepository.findUserByEmail(email).firstSuccessOrErrorResult()
            val user = result?.data
            _state.update { current ->
                when {
                    result is Result.Error -> current.copy(
                        isSearching = false,
                        searchError = result.error.toMemberLookupError(),
                    )

                    user == null -> current.copy(
                        isSearching = false,
                        searchError = StringResource(R.string.club_admin_error_no_such_member),
                    )

                    else -> current.copy(
                        isSearching = false,
                        searchError = null,
                        member = user.toMemberUi(),
                    )
                }
            }
        }
    }

    private fun updateMyRoles(role: Role, granted: Boolean) {
        val current = _state.value
        if (current.savingRole != null || current.myUserId.isEmpty()) return
        applyRoles(
            userId = current.myUserId,
            role = role,
            roles = current.myRoles.toggled(role, granted),
            onSuccess = { user -> _state.update { it.copy(myRoles = user.roles.toSet()) } },
        )
    }

    private fun updateMemberRoles(role: Role, granted: Boolean) {
        val current = _state.value
        val member = current.member ?: return
        if (current.savingRole != null) return
        applyRoles(
            userId = member.id,
            role = role,
            roles = member.roles.toggled(role, granted),
            onSuccess = { user ->
                _state.update { it.copy(member = user.toMemberUi()) }
                // The manager may have been editing their own account through the email form;
                // keeping the top section in sync avoids showing two answers for one question.
                if (user.id == current.myUserId) {
                    _state.update { it.copy(myRoles = user.roles.toSet()) }
                }
            },
        )
    }

    private fun applyRoles(
        userId: String,
        role: Role,
        roles: Set<Role>,
        onSuccess: (User) -> Unit,
    ) {
        _state.update { it.copy(savingRole = role) }
        viewModelScope.launch {
            val result = userRepository.updateRoles(userId, roles).firstSuccessOrErrorResult()
            _state.update { it.copy(savingRole = null) }
            val user = result?.data
            if (result is Result.Error) {
                showSnackbar(result.error.toRoleUpdateError())
                return@launch
            }
            if (user == null) {
                showSnackbar(StringResource(R.string.main_error_generic))
                return@launch
            }
            onSuccess(user)
            showSnackbar(StringResource(R.string.club_admin_roles_saved))
        }
    }

    private fun Set<Role>.toggled(role: Role, granted: Boolean): Set<Role> =
        if (granted) this + role else this - role

    private fun User.toMemberUi(): ClubMemberUi = ClubMemberUi(
        id = id,
        name = name,
        email = email,
        initials = name.toInitials(),
        roles = roles.toSet(),
    )

    private fun showSnackbar(message: TextResource) {
        router.execute(ShowSnackbar(SnackbarScreenConfig { message(message) }))
    }
}
