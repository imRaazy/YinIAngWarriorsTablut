package it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util

import it.unibo.ai.didattica.competition.tablut.domain.State
class HeuristicUtil {
    companion object {
        val MAXWHITE = 9
        val MAXBLACK = 16
        val whiteGoodLines = listOf(2, 6)
        val whiteMediumLines = listOf(1, 7)

        fun normalizeValue(value: Int, min: Int, max: Int): Double {
            return (value - min).toDouble() / (max - min).toDouble()
        }

        fun weightedAverage(element: List<Pair<Double, Double>>): Double {
            var numerator = 0.0
            var denominator = 0.0
            element.forEach{ numerator += it.first * it.second; denominator += it.second}
            return numerator/denominator
        }

        fun getKing(state: State): Pair<Int, Int>? {
            state.board.indices.forEach { r ->
                state.board.indices.forEach { c ->
                    if (state.getPawn(r, c) == State.Pawn.KING)
                        return Pair(r, c)
                }
            }
            return null
        }
        fun getPawnEncirclement(state: State, position: Pair<Int, Int>, predicate: (State.Pawn) -> Boolean): Int {
            var pawnEncirclement = 0
            listOf(-1, 1).forEach { r ->
                if ((position.first + r) in state.board.indices &&
                        predicate.invoke(state.getPawn(position.first + r, position.second)))
                    pawnEncirclement++
            }
            listOf(-1, 1).forEach { c ->
                if ((position.second + c) in state.board.indices &&
                        predicate.invoke(state.getPawn(position.first, position.second + c)))
                    pawnEncirclement++
            }
            return pawnEncirclement
        }
    }
}