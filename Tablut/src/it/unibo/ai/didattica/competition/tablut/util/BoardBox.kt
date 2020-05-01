package it.unibo.ai.didattica.competition.tablut.util
/**
 * Enum for relevant board boxes
 * @param boxes
 *      list of boxes'coordinates
 */
enum class BoardBox(val boxes: List<Pair<Int, Int>>) {
    THRONE(listOf(Pair(4, 4))),
    ESCAPE(listOf(Pair(0, 1), Pair(0, 2), Pair(0, 6), Pair(0, 7),
                  Pair(8, 1), Pair(8, 2), Pair(8, 6), Pair(8, 7),
                  Pair(1, 0), Pair(1, 8), Pair(2, 0), Pair(2, 8),
                  Pair(7, 0), Pair(7, 8), Pair(6, 0), Pair(6, 8))),
    CITADEL(listOf(Pair(0, 3), Pair(0, 4), Pair(0, 5), Pair(4, 1),
                   Pair(8, 3), Pair(8, 4), Pair(8, 5), Pair(1, 4),
                   Pair(3, 8), Pair(4, 8), Pair(5, 8), Pair(7, 4),
                   Pair(3, 0), Pair(4, 0), Pair(5, 0), Pair(4, 7))),
    KING_SAFE(listOf(Pair(4, 4), Pair(3, 4), Pair(4, 3),
                     Pair(5, 4), Pair(4, 5))),
    NORTH_WEST_CROSS_WAY_OUT(listOf(Pair(2, 4), Pair(3, 4), Pair(4, 2), Pair(4, 3))),
    NORTH_EAST_CROSS_WAY_OUT(listOf(Pair(2, 4), Pair(3, 4), Pair(4, 5), Pair(4, 6))),
    SOUTH_WEST_CROSS_WAY_OUT(listOf(Pair(5, 4), Pair(6, 4), Pair(4, 2), Pair(4, 3))),
    SOUTH_EAST_CROSS_WAY_OUT(listOf(Pair(5, 4), Pair(6, 4), Pair(4, 5), Pair(4, 6))),
    NORTH_WEST_WAY_OUT(listOf(Pair(2, 5), Pair(3, 5), Pair(5, 2), Pair(5, 3))),
    NORTH_EAST_WAY_OUT(listOf(Pair(2, 3), Pair(3, 3), Pair(5, 5), Pair(5, 6))),
    SOUTH_WEST_WAY_OUT(listOf(Pair(5, 5), Pair(6, 5), Pair(3, 2), Pair(3, 3))),
    SOUTH_EAST_WAY_OUT(listOf(Pair(5, 3), Pair(6, 3), Pair(3, 5), Pair(3, 6)));
    companion object {
        /**
         * Get escape positions based on king's zone
         * @param zone
         *      zone of the king
         * @return escapes
         */
        fun getPairedWayOut(zone: Zone): List<Pair<Pair<Int, Int>, Pair<Int, Int>>> {
            val range = NORTH_WEST_WAY_OUT.boxes.indices
            val pairedWayOut = mutableListOf<Pair<Pair<Int, Int>, Pair<Int, Int>>>()
            when (zone) {
                Zone.NORTH_WEST -> range.forEach { pairedWayOut.add(Pair(NORTH_WEST_WAY_OUT.boxes[it], NORTH_WEST_CROSS_WAY_OUT.boxes[it])) }
                Zone.NORTH_EAST -> range.forEach { pairedWayOut.add(Pair(NORTH_EAST_WAY_OUT.boxes[it], NORTH_EAST_CROSS_WAY_OUT.boxes[it])) }
                Zone.SOUTH_WEST -> range.forEach { pairedWayOut.add(Pair(SOUTH_WEST_WAY_OUT.boxes[it], SOUTH_WEST_CROSS_WAY_OUT.boxes[it])) }
                Zone.SOUTH_EAST -> range.forEach { pairedWayOut.add(Pair(SOUTH_EAST_WAY_OUT.boxes[it], SOUTH_EAST_CROSS_WAY_OUT.boxes[it])) }
                else -> {}
            }
            return pairedWayOut
        }
    }
}