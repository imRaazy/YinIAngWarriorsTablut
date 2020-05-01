package it.unibo.ai.didattica.competition.tablut.client.player.heuristic

import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicElement
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.checkWhiteGoodLineObstacles
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.checkWhiteWinLineObstacles
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.getCol
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.getKing
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.getPawnEncirclement
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.getRow
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.goodLine
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.normalizeValue
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.pawnToPawnManhattanDistance
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.weightedAverage
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.winLine
import it.unibo.ai.didattica.competition.tablut.domain.State
import it.unibo.ai.didattica.competition.tablut.util.BoardBox
import it.unibo.ai.didattica.competition.tablut.util.Direction

class WhiteHeuristic {
    companion object {
        /**
         * Heuristic function based on a mix of heuristic elements:
         *      PawnsDifference (MAX: 1, MIN: 0, WEIGHT: 1.4),
         *      KingPositioning (MAX: 2, MIN: -24, WEIGHT: 0.2),
         *      WhiteManhattanDistance (MAX: 115, MIN: 0, WEIGHT: 0.6),
         *      SuitablePosition (MAX: 4, MIN: 0, WEIGHT: 0.4)
         * @param state
         *      game state
         * @return white heuristic evaluation
         */
        fun whiteEval(state: State): Double {
            val heuristicInfluenceElement = mutableListOf<HeuristicElement>()
            val kingPosition = getKing(state)!!
            val kingRow = getRow(kingPosition.first, state)
            val kingCol = getCol(kingPosition.second, state)
            var numberOfBlack = 0 // MAX 16 MIN:0
            var numberOfWhite = 0 // MAX: 8 MIN: 0
            var whiteManhattanDistance = 115
            state.board.indices.forEach { r ->
                state.board.indices.forEach { c ->
                    if (state.getPawn(r, c) == State.Pawn.WHITE) {
                        numberOfWhite++
                        whiteManhattanDistance -= pawnToPawnManhattanDistance(kingPosition, Pair(r, c))
                    }
                    if (state.getPawn(r, c) == State.Pawn.BLACK) numberOfBlack++
                }
            }
            heuristicInfluenceElement.add(HeuristicElement("KingPositioning", evaluateKingPosition(kingPosition, kingRow, kingCol).toDouble(), -24, 4, 0.2))
            heuristicInfluenceElement.add(HeuristicElement("SuitablePosition", evaluateKingWinPosition(kingPosition, kingRow, kingCol).toDouble(), 0, 4, 0.4))
            heuristicInfluenceElement.add(HeuristicElement("WhiteManhattanDistance", whiteManhattanDistance.toDouble(), 0, 115, 0.6))
            heuristicInfluenceElement.add(HeuristicElement("PawnsDifference", 2.0 * numberOfWhite / (numberOfBlack + 2 * numberOfWhite), 0, 1, 1.4))
            return when {
                blackWin(state, kingPosition, kingRow, kingCol) -> Double.NEGATIVE_INFINITY
                else -> weightedAverage(heuristicInfluenceElement.map { Pair(normalizeValue(it.value, it.min, it.max), it.factor) })
            }
        }

        /**
         * Heuristic function used to give weight to moves that brings the king in better lines (weight explained below)
         * Assuming lines 4 as worst lines and lines 2-6 as best, the worst gives me -5 (4 citadels and 1 throne)
         * and the best gives me +2 (2 escapes). Assuming the worst case scenario as all blacks in a line,
         * they give me -8 (-1 each). Assuming the best case scenario as all whites in a line they give me +0
         * MIN: since we have 16 blacks the min value can be found when the king is on throne and is surrounded
         * by all black in each line (row and col), this means: -7 -7 -5 -5 = -24
         * MAX: since we have only 8 whites the max value can be found when the king is in a winning row and col
         * at the same time with one line full of whites and one line empty, this means: + 2 + 2 = +4
         * @param kingPosition
         *      king position as Pair(row, col)
         * @param kingRow
         *      king row as string
         * @param kingCol
         *      king col as string
         * @return king position evaluation
         */
        private fun evaluateKingPosition(kingPosition: Pair<Int, Int>, kingRow: String, kingCol: String): Int {
            return getLineScore(kingRow, kingPosition.first) + getLineScore(kingCol, kingPosition.second)
        }
        /**
         * Weight to compute a better line to move king, (or pawns)
         * Gives the following score: citadels -1, throne -1, black -1, escapes +1
         * @param line
         *      row or col as string
         * @param boardLineIndex
         *      index of the line
         * @return line score
         */
        private fun getLineScore(line: String, boardLineIndex: Int): Int {
            var score = 0
            var i = 0
            line.forEach { l ->
                if (l != 'K') {
                    if (Pair(boardLineIndex, i) in BoardBox.CITADEL.boxes) score--
                    if (Pair(boardLineIndex, i) in BoardBox.THRONE.boxes) score++
                    if (Pair(boardLineIndex, i) in BoardBox.ESCAPE.boxes) score--
                    if (l == 'B') score--
                }
                i++
            }
            return score
        }
        /**
         * Heuristic function used to give weight to moves that bring the king in lines suitable for a win
         * (lines 2-6 called as winLines, and lines 1-7 called as goodLines)
         * @param kingPosition
         *      king position as Pair(row, col)
         * @param kingRow
         *      king row as string
         * @param kingCol
         *      king col as string
         * @return king suitable position evaluation
         */
        private fun evaluateKingWinPosition(kingPosition: Pair<Int, Int>, kingRow: String, kingCol: String): Int {
            var kingPositioning = 0
            if (kingPosition.first in winLine) kingPositioning += checkWhiteWinLineObstacles(kingRow, kingPosition.first)
            if (kingPosition.second in winLine) kingPositioning += checkWhiteWinLineObstacles(kingCol, kingPosition.second)
            if (kingPosition.first in goodLine) kingPositioning += checkWhiteGoodLineObstacles(kingRow, kingPosition.first)
            if (kingPosition.second in goodLine) kingPositioning += checkWhiteGoodLineObstacles(kingCol, kingPosition.second)
            return kingPositioning
        }
        /**
         * Check if white move bring black to win
         * Three different cases according to Tablut's rules:
         * 1)   King is in Throne or Throne is next to the king, then king must be surrounded
         *      by 4 black (if in the throne) of from three black in the other case scenario
         * 2)   King is outside the "safe areas" and then black player wins if he
         *      surrounds the king with only two pawn. If the king is next to a black we check
         *      if the otherSide box can be reached by a black pawn if empty
         * @param state
         *      state of the game
         * @param kingPosition
         *      king position as Pair(row, col)
         * @param kingRow
         *      king row as string
         * @param kingCol
         *      king col as string
         * @return true if black win in the next move false otherwise
        */
        private fun blackWin(state: State, kingPosition: Pair<Int, Int>, kingRow: String, kingCol: String): Boolean {
            if (state.turn == State.Turn.WHITE)
                return false
            if (kingPosition in BoardBox.KING_SAFE.boxes &&
                    getPawnEncirclement(state, kingPosition) { it == State.Pawn.BLACK || it == State.Pawn.THRONE } == 3 &&
                    getPawnEncirclement(state, kingPosition) { it == State.Pawn.EMPTY } == 1) {
                if (checkKingEmptyDirection(state, getKingEmptyDirection(state, kingPosition)!!, kingPosition, kingRow, kingCol))
                    return true
            }
            listOf(-1, 1).forEach { r ->
                val pawn = kingPosition.first + r to kingPosition.second
                val otherSidePawn = kingPosition.first - r to kingPosition.second
                val direction = if (r == -1) Direction.DOWN else Direction.UP
                if ((pawn in BoardBox.CITADEL.boxes || (kingPosition !in BoardBox.KING_SAFE.boxes && state.getPawn(pawn.first, pawn.second) == State.Pawn.BLACK)) &&
                        state.getPawn(otherSidePawn.first, otherSidePawn.second) == State.Pawn.EMPTY && checkKingEmptyDirection(state, direction, kingPosition, kingRow, kingCol))
                    return true
            }
            listOf(-1, 1).forEach { c ->
                val pawn = kingPosition.first to kingPosition.second + c
                val otherSidePawn = kingPosition.first to kingPosition.second - c
                val direction = if (c == -1) Direction.RIGHT else Direction.LEFT
                if ((pawn in BoardBox.CITADEL.boxes || (kingPosition !in BoardBox.KING_SAFE.boxes && state.getPawn(pawn.first, pawn.second) == State.Pawn.BLACK)) &&
                        state.getPawn(otherSidePawn.first, otherSidePawn.second) == State.Pawn.EMPTY && checkKingEmptyDirection(state, direction, kingPosition, kingRow, kingCol))
                    return true
            }
            return false
        }
        /**
         * Function to call checkHalfLine and checkPerpendicularFullLine in order to check
         * if the empty box next to the king can be reached by a black from the line of the
         * empty box of from the perpendicular line
         */
        private fun checkKingEmptyDirection(state: State, kingEmptyDirection: Direction, kingPosition: Pair<Int, Int>, kingRow: String, kingCol: String): Boolean {
            when (kingEmptyDirection) {
                Direction.UP -> {
                    if (checkHalfLine(kingCol, Direction.UP) || checkPerpendicularFullLine(getRow(kingPosition.first - 1, state), kingPosition.second))
                        return true
                }
                Direction.LEFT -> {
                    if (checkHalfLine(kingRow, Direction.LEFT) || checkPerpendicularFullLine(getCol(kingPosition.second - 1, state), kingPosition.first))
                        return true
                }
                Direction.DOWN -> {
                    if (checkHalfLine(kingCol, Direction.DOWN) || checkPerpendicularFullLine(getRow(kingPosition.first + 1, state), kingPosition.second))
                        return true
                }
                Direction.RIGHT -> {
                    if (checkHalfLine(kingRow, Direction.RIGHT) || checkPerpendicularFullLine(getCol(kingPosition.second + 1, state), kingPosition.first))
                        return true
                }
            }
            return false
        }
        /**
         * Function to check if the perpendicular column to an empty box next to the king
         * has some black that can reach that position in the next move.
         * It checks half column and the other with checkHalfLine function.
         */
        private fun checkPerpendicularFullLine(perpendicularLine: String, kingIndex: Int): Boolean {
            val line = perpendicularLine.replaceRange(kingIndex, kingIndex + 1, "K")
            return checkHalfLine(line, Direction.UP) || checkHalfLine(line, Direction.DOWN)
        }
        /**
         * Function used to check if the empty box next to the king can be reached by a black pawn
         */
        private fun checkHalfLine(kingLine: String, freeDirection: Direction): Boolean {
            if (freeDirection == Direction.UP || freeDirection == Direction.LEFT) {
                if (kingLine.substringBefore("K").contains("B")) {
                    var afterBLine = kingLine.substringBefore("K")
                    while (afterBLine.contains("B"))
                        afterBLine = afterBLine.substringAfter("B")
                    if (!afterBLine.contains("W"))
                        return true
                }
            } else if (freeDirection == Direction.DOWN || freeDirection == Direction.RIGHT) {
                if (kingLine.substringAfter("K").contains("B")) {
                    if (!kingLine.substringAfter("K").substringBefore("B").contains("W"))
                        return true
                }
            }
            return false
        }
        /**
         * Return the king's empty position surrounding him
         */
        private fun getKingEmptyDirection(state: State, kingPosition: Pair<Int, Int>): Direction? {
            listOf(-1, 1).forEach { r ->
                if ((kingPosition.first + r) in state.board.indices && (state.getPawn(kingPosition.first + r, kingPosition.second) == State.Pawn.EMPTY)) {
                    if (r == -1) return Direction.UP
                    if (r == 1) return Direction.DOWN
                }
            }
            listOf(-1, 1).forEach { c ->
                if ((kingPosition.second + c) in state.board.indices && (state.getPawn(kingPosition.first, kingPosition.second + c) == State.Pawn.EMPTY)) {
                    if (c == -1) return Direction.LEFT
                    if (c == 1) return Direction.RIGHT
                }
            }
            return null
        }
    }
}