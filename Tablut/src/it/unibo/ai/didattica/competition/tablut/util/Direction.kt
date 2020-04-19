package it.unibo.ai.didattica.competition.tablut.util

enum class Direction() {
    UP, DOWN, LEFT, RIGHT, NONE;
    companion object {
        fun formValue(value: Int): Direction {
            return when (value) {
                -1 -> LEFT
                1 -> RIGHT
                else -> NONE
            }
        }
    }
}