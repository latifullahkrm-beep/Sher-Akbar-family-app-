package com.sherakbar.family.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    ADMIN, MEMBER
}

enum class AccountStatus {
    PENDING, APPROVED, SUSPENDED
}

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey val uid: String,
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val role: UserRole = UserRole.MEMBER,
    val status: AccountStatus = AccountStatus.PENDING,
    val linkedMemberId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class FamilyStats(
    val totalMembers: Int = 0,
    val maleMembers: Int = 0,
    val femaleMembers: Int = 0,
    val totalGenerations: Int = 0
)
