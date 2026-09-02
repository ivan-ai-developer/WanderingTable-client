package ru.gohasoft.wanderingtable.feature.main.mapper

import ru.gohasoft.wanderingtable.core.domain.exception.AppException
import ru.gohasoft.wanderingtable.core.domain.exception.NetworkException
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource.StringResource
import ru.gohasoft.wanderingtable.feature.main.R

/** The message a list or detail screen shows when its load fails. */
internal fun AppException.toLoadError(): TextResource = when (this) {
    is NetworkException.NoInternet -> StringResource(R.string.main_error_no_internet)
    is NetworkException.NotFound -> StringResource(R.string.main_error_not_found)
    is NetworkException.Unauthorized -> StringResource(R.string.main_error_session_expired)
    else -> StringResource(R.string.main_error_generic)
}

/**
 * `POST /events/{id}/join` answers 409 for three different situations — full, already started,
 * already a participant — and the body's wording is not stable enough to tell them apart, so one
 * message has to cover all three.
 */
internal fun AppException.toJoinError(): TextResource = when (this) {
    is NetworkException.Conflict -> StringResource(R.string.game_detail_error_join_conflict)
    is NetworkException.NotFound -> StringResource(R.string.main_error_not_found)
    is NetworkException.NoInternet -> StringResource(R.string.main_error_no_internet)
    else -> StringResource(R.string.main_error_generic)
}

/** 409 on leave means the play already started; the creator gets 409 too and must cancel instead. */
internal fun AppException.toLeaveError(): TextResource = when (this) {
    is NetworkException.Conflict -> StringResource(R.string.game_detail_error_leave_conflict)
    is NetworkException.NoInternet -> StringResource(R.string.main_error_no_internet)
    else -> StringResource(R.string.main_error_generic)
}

/** 409 on cancel means the play is already finished or cancelled. */
internal fun AppException.toCancelError(): TextResource = when (this) {
    is NetworkException.Conflict -> StringResource(R.string.game_detail_error_cancel_conflict)
    is NetworkException.Forbidden -> StringResource(R.string.game_detail_error_not_yours)
    is NetworkException.NoInternet -> StringResource(R.string.main_error_no_internet)
    else -> StringResource(R.string.main_error_generic)
}

/** 400 here is the server rejecting participant bounds the chosen game does not allow. */
internal fun AppException.toCreateRequestError(): TextResource = when (this) {
    is NetworkException.BadRequest -> StringResource(R.string.create_request_error_bad_bounds)
    is NetworkException.NotFound -> StringResource(R.string.create_request_error_unknown_game)
    is NetworkException.NoInternet -> StringResource(R.string.main_error_no_internet)
    else -> StringResource(R.string.main_error_generic)
}

/** 404 is the ordinary answer here: the manager typed an address nobody in the club uses. */
internal fun AppException.toMemberLookupError(): TextResource = when (this) {
    is NetworkException.NotFound -> StringResource(R.string.club_admin_error_no_such_member)
    is NetworkException.Forbidden -> StringResource(R.string.club_admin_error_forbidden)
    is NetworkException.NoInternet -> StringResource(R.string.main_error_no_internet)
    else -> StringResource(R.string.main_error_generic)
}

/**
 * 409 covers the server's two guards on revoking `CLUB_MANAGER`: taking it from yourself, or
 * from the last manager in the club. Both boil down to the same advice.
 */
internal fun AppException.toRoleUpdateError(): TextResource = when (this) {
    is NetworkException.Conflict -> StringResource(R.string.club_admin_error_last_manager)
    is NetworkException.Forbidden -> StringResource(R.string.club_admin_error_forbidden)
    is NetworkException.BadRequest -> StringResource(R.string.club_admin_error_bad_role)
    is NetworkException.NoInternet -> StringResource(R.string.main_error_no_internet)
    else -> StringResource(R.string.main_error_generic)
}

/** Game names are unique club-wide, so 409 here always means "that name is taken". */
internal fun AppException.toCreateGameError(): TextResource = when (this) {
    is NetworkException.Conflict -> StringResource(R.string.create_game_error_name_taken)
    is NetworkException.Forbidden -> StringResource(R.string.create_game_error_forbidden)
    is NetworkException.BadRequest -> StringResource(R.string.create_game_error_bad_request)
    is NetworkException.NoInternet -> StringResource(R.string.main_error_no_internet)
    else -> StringResource(R.string.main_error_generic)
}

/** 403 means the account lost the `NEWS_CREATOR` role since the screen was opened. */
internal fun AppException.toPostNewsError(): TextResource = when (this) {
    is NetworkException.Forbidden -> StringResource(R.string.create_news_error_forbidden)
    is NetworkException.BadRequest -> StringResource(R.string.create_news_error_bad_request)
    is NetworkException.NoInternet -> StringResource(R.string.main_error_no_internet)
    else -> StringResource(R.string.main_error_generic)
}
