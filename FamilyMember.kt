package com.sherakbar.family.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Gender {
    MALE, FEMALE
}

enum class MaritalStatus {
    SINGLE, MARRIED, WIDOWED
}

enum class MemberCategory {
    ALL, MALE, FEMALE, CHILDREN, ELDERS, DECEASED
}

enum class MemberSortOrder {
    NAME_ASC, NAME_DESC, GENERATION_ASC
}

@Entity(tableName = "family_members")
data class FamilyMember(
    @PrimaryKey val id: String = "",
    val fullName: String = "",
    val fullNameUrdu: String = "",
    val photoUri: String? = null,
    val drawableResName: String? = null,
    val fatherId: String? = null,
    val fatherName: String = "",
    val motherId: String? = null,
    val motherName: String = "",
    val gender: Gender = Gender.MALE,
    val dateOfBirth: String = "",
    val dateOfDeath: String? = null,
    val maritalStatus: MaritalStatus = MaritalStatus.SINGLE,
    val spouseId: String? = null,
    val spouseName: String = "",
    val contactNumber: String = "",
    val address: String = "",
    val generation: Int = 1,
    val biography: String = "",
    val biographyUrdu: String = "",
    val isElder: Boolean = false,
    val isChild: Boolean = false,
    val isDeceased: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
