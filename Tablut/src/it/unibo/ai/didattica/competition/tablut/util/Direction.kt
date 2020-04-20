package it.unibo.ai.didattica.competition.tablut.util

enum class Direction() {
    UP, DOWN, LEFT, RIGHT, NONE;
    companion object {
        fun fromValue(value: Int): Direction {
            return when (value) {
                -1 -> LEFT
                0 -> UP
                1 -> RIGHT
                2 -> DOWN
                else -> NONE
            }
        }
    }
}