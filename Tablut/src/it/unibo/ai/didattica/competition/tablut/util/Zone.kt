package it.unibo.ai.didattica.competition.tablut.util
/**
 * Enum the four board quadrants
 */
enum class Zone {
    NORTH_WEST, NORTH_EAST, SOUTH_WEST, SOUTH_EAST, NONE;
    companion object {
        /**
         * Get zone from position
         * @param position
         *      pawn position
         * @return pawn zone
         */
        fun getZone(position: Pair<Int, Int>): Zone {
            return when {
                position.first < 4 && position.second < 4 -> NORTH_WEST
                position.first < 4 && position.second > 4 -> NORTH_EAST
                position.first > 4 && position.second < 4 -> SOUTH_WEST
                position.first > 4 && position.second > 4 -> SOUTH_EAST
                else -> NONE
            }
        }
    }
}