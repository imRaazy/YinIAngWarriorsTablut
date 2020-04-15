package it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util

import it.unibo.ai.didattica.competition.tablut.domain.State
import it.unibo.ai.didattica.competition.tablut.util.BoardBox

class HeuristicUtil {
    companion object {
        const val MAXWHITE = 9
        const val MAXBLACK = 16
        val winLine = listOf(2, 6)
        val goodLine = listOf(1, 7)

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

        fun getCrossPawnSurrounding(pawnPosition: Pair<Int, Int>, state: State): MutableMap<String, State.Pawn> {
            var crossPawnSurrounding = mutableMapOf<String, State.Pawn>()
            if ( pawnPosition.first != 0 )
                crossPawnSurrounding["up"] = state.getPawn(pawnPosition.first - 1, pawnPosition.second)
            if ( pawnPosition.first != state.board.size - 1 )
                crossPawnSurrounding["down"] = state.getPawn(pawnPosition.first + 1, pawnPosition.second)
            if ( pawnPosition.second != 0 )
                crossPawnSurrounding["left"] = state.getPawn(pawnPosition.first, pawnPosition.second - 1)
            if ( pawnPosition.second != state.board.size - 1 )
                crossPawnSurrounding["right"] = state.getPawn(pawnPosition.first, pawnPosition.second + 1)
            return crossPawnSurrounding;
        }

        fun getDiagonalPawnSurrounding(pawnPosition: Pair<Int, Int>, state: State): MutableMap<String, State.Pawn> {
            var diagonalPawnSurrounding = mutableMapOf<String, State.Pawn>()
            if ( pawnPosition.first != 0 && pawnPosition.second != 0)
                diagonalPawnSurrounding["upleft"] = state.getPawn(pawnPosition.first - 1, pawnPosition.second - 1)
            if ( pawnPosition.first != state.board.size - 1 && pawnPosition.second != state.board.size - 1 )
                diagonalPawnSurrounding["downright"] = state.getPawn(pawnPosition.first + 1, pawnPosition.second + 1)
            if ( pawnPosition.first != state.board.size - 1 && pawnPosition.second != 0 )
                diagonalPawnSurrounding["downleft"] = state.getPawn(pawnPosition.first + 1, pawnPosition.second - 1)
            if ( pawnPosition.first != 0 && pawnPosition.second != state.board.size - 1 )
                diagonalPawnSurrounding["upright"] = state.getPawn(pawnPosition.first - 1, pawnPosition.second + 1)
            return diagonalPawnSurrounding;
        }

        fun getCol(col: Int, state: State): String {
            var res = ""
            state.board[col].forEach { res += it }
            return res
        }

        fun getRow(row: Int, state: State): String {
            var res = ""
            state.board.indices.forEach { res += state.board[row][it] }
            return res
        }

        fun whiteWin(kingPosition: Pair<Int, Int>): Boolean {
            return BoardBox.ESCAPE.boxes.contains(kingPosition)
        }

        fun blackWin(state: State): Boolean {
            val kingPosition = getKing(state)!!
            if (getPawnEncirclement(state, kingPosition) { it == State.Pawn.BLACK || it == State.Pawn.THRONE} >= 4 )
                return true
            listOf(-1, 1).forEach { r ->
                if (BoardBox.CITADEL.boxes.contains(Pair(kingPosition.first + r, kingPosition.second)) &&
                    state.getPawn(kingPosition.first -r, kingPosition.second) == State.Pawn.BLACK )
                    return true
            }
            listOf(-1, 1).forEach { c ->
                if (BoardBox.CITADEL.boxes.contains(Pair(kingPosition.first, kingPosition.second + c)) &&
                    state.getPawn(kingPosition.first, kingPosition.second - c) == State.Pawn.BLACK )
                    return true
            }
            return false
        }
    }
}