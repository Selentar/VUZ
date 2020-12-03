package ru.ncfu.selentar.environment

data class Observation(
    val forward: MapSurface,
    val position: Position,
    val direction: Direction,
    val canceled: Boolean,
    val finished: Boolean,
    val lost: Boolean,
    val time: Int,
    val startCount: Int,
    val turnsCount: Int
    )