package com.example.ecopoints.app.data.local.db

import com.example.ecopoints.app.data.EcoChallenge
import com.example.ecopoints.app.data.model.PetState
import com.example.ecopoints.app.data.model.User
import com.example.ecopoints.app.data.model.UserProgress

fun UserEntity.toDomain() = User(id = id, name = name, email = email)

fun ProgressEntity.toDomain() = UserProgress(
    balance = balance,
    historicalPoints = historicalPoints,
    streak = streak,
    bestStreak = maxOf(bestStreak, streak),
    feedCount = feedCount,
    playCount = playCount
)

fun PetEntity.toDomain() = PetState(species = species, name = name, hunger = hunger, happiness = happiness)

fun ChallengeEntity.toDomain() = EcoChallenge(
    id = id,
    icon = icon,
    title = title,
    description = description,
    durationDays = durationDays,
    startMillis = startMillis,
    completed = completed,
    completedMillis = completedMillis,
    evidencePath = evidencePath,
    remindedDeadline = remindedDeadline
)
