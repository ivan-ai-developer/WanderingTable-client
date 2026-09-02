package ru.gohasoft.wanderingtable.feature.main.createrequest

/**
 * The skill pills on Create Request.
 *
 * Client-side only: an ordinary club play has no skill field on the server, so the choice is
 * never sent and every card in the schedule reads "Any level". The control is kept because the
 * design calls for it and the field is expected to land server-side later.
 */
internal enum class SkillLevelUi {
    BEGINNER,
    ANY,
    EXPERT,
}
