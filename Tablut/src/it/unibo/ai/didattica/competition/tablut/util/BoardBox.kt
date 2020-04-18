package it.unibo.ai.didattica.competition.tablut.util

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
                     Pair(5, 4), Pair(4, 5)))
}