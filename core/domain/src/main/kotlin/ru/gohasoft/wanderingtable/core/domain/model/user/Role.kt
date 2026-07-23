package ru.gohasoft.wanderingtable.core.domain.model.user

sealed class Role {

    class Gamer(
        val skillLevel: SkillLevel
    ) : Role()

    sealed class Creator : Role() {
        data object News : Creator()
        data object Event : Creator()
    }

}