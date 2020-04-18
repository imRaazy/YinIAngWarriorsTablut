package it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util

import it.unibo.ai.didattica.competition.tablut.domain.State

class HeuristicUtil {
    companion object {
        val winLine = listOf(2, 6)
        val goodLine = listOf(1, 7)

        fun normalizeValue(value: Double, min: Int, max: Int): Double {
            return (value - min) / (max - min)
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

        //return 0 if 2 obstacles, 1 if 1 obstacle, 2 if 0 obstacles
        fun checkWhiteWinLineObstacles(line: String, kingLine: Int): Int {
            var score = 0
            if (kingLine == 2 || kingLine == 6) {
                if (!line.substringBefore("K").contains("B") && !line.substringBefore("K").contains("W")) score++
                if (!line.substringAfter("K").contains("B") && !line.substringAfter("K").contains("W")) score++
            }
            return score
        }

        //return 0 if 1 obstacles, 1 if 0 obstacles
        fun checkWhiteGoodLineObstacles(line: String, kingLine: Int): Int {
            var score = 0
            if (kingLine == 1 || kingLine == 7) {
                if (line.indexOf("K") < 4) {
                    if (!line.substringBefore("K").contains("B") && !line.substringBefore("K").contains("W"))
                        score++
                } else {
                    if (!line.substringAfter("K").contains("B") && !line.substringAfter("K").contains("W"))
                        score++
                }
            }
            return score
        }

        fun getCol(col: Int, state: State): String {
            var res = ""
            state.board.indices.forEach { res += state.board[it][col] }
            return res
        }

        fun getRow(row: Int, state: State): String {
            var res = ""
            state.board.indices.forEach { res += state.board[row][it] }
            return res
        }
    }
}