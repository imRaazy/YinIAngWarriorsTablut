package it.unibo.ai.didattica.competition.tablut.client.player.heuristic

import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicElement
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.getCol
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.checkWhiteWinLineObstacles
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.checkWhiteGoodLineObstacles
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.getKing
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.getPawnEncirclement
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.getRow
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.goodLine
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.pawnToPawnManhattanDistance
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.winLine
import it.unibo.ai.didattica.competition.tablut.domain.State
import it.unibo.ai.didattica.competition.tablut.util.BoardBox
import kotlin.math.abs

class WhiteHeuristic {
    companion object {
        fun genericWhiteEval(state: State): Double {
            val heuristicInfluenceElement = mutableListOf<HeuristicElement>()
            val kingPosition = getKing(state)!!
            var numberOfBlack = 0
            var numberOfWhite = 0
            val kingRow = getRow(kingPosition.first, state)
            val kingCol = getCol(kingPosition.second, state)
//            var kingEncirclement = getPawnEncirclement(state, kingPosition) { it == State.Pawn.WHITE }
//            when (kingEncirclement) {
//                4 -> kingEncirclement = 0
//                3 -> kingEncirclement = 1
//                1 -> kingEncirclement = 3
//            }
            state.board.indices.forEach { r ->
                state.board.indices.forEach { c ->
                    if (state.getPawn(r, c) == State.Pawn.WHITE) {
                        numberOfWhite++
                    }
                    if (state.getPawn(r, c) == State.Pawn.BLACK)
                        numberOfBlack++
                }
            }
            heuristicInfluenceElement.add(HeuristicElement("KingPositioning", evaluateKingWinPosition(kingPosition, kingRow, kingCol).toDouble(), 0, 4, 0.5))
            //heuristicInfluenceElement.add(HeuristicElement("KingEncirclement", kingEncirclement.toDouble(), 0, 3, 0.3))
            heuristicInfluenceElement.add(HeuristicElement("NumberOfPawns", 2.0 * numberOfWhite/(numberOfBlack+2*numberOfWhite), 0, 1, 0.2))
            return  when {
                blackWin(state, kingPosition) -> 0.0
                else -> HeuristicUtil.weightedAverage(heuristicInfluenceElement.map { Pair(HeuristicUtil.normalizeValue(it.value, it.min, it.max), it.factor) })
            }
        }

        fun newBornOfWhiteEval(state: State): Double {
            val heuristicInfluenceElement = mutableListOf<HeuristicElement>()
            val kingPosition = getKing(state)!!
            val kingRow = getRow(kingPosition.first, state)
            val kingCol = getCol(kingPosition.second, state)
            val kingEncirclement = getPawnEncirclement(state, kingPosition) { it == State.Pawn.WHITE } //MAX: 4 MIN: 0
            var numberOfBlack = 0 //MAX 16 MIN:0
            var numberOfWhite = 0 //MAX: 8 MIN: 0
            var whiteManhattanDistance = 115 //MAX: 115 MIN: 0
            var blackManhattanDistance = 0 //MAX: 115 MIN: 0

            state.board.indices.forEach { r ->
                state.board.indices.forEach { c ->
                    if (state.getPawn(r, c) == State.Pawn.WHITE) {
                        numberOfWhite++
                        whiteManhattanDistance -= pawnToPawnManhattanDistance(kingPosition, Pair(r, c)) //King says: "I want to feel protect"
                    }
                    if (state.getPawn(r, c) == State.Pawn.BLACK) {
                        numberOfBlack++
                        blackManhattanDistance += pawnToPawnManhattanDistance(kingPosition, Pair(r, c)) //King says: "I want to stay away from blacks"
                    }
                }
            }

            heuristicInfluenceElement.add(HeuristicElement("KingEncirclement", kingEncirclement.toDouble(), 0, 3, 0.1))
            heuristicInfluenceElement.add(HeuristicElement("KingPositioning", evaluateKingPosition(kingPosition, kingRow, kingCol).toDouble(), -22, 12, 0.2))
            heuristicInfluenceElement.add(HeuristicElement("WhiteManhattanDistance", whiteManhattanDistance.toDouble(), 0, 115, 0.4))
            heuristicInfluenceElement.add(HeuristicElement("NumberOfPawns", 2.0 * numberOfWhite/(numberOfBlack+2*numberOfWhite), 0, 1, 1.0))
            //heuristicInfluenceElement.add(HeuristicElement("BlackManhattanDistanceReverse", blackManhattanDistance.toDouble(), 0, 208, 1.2))
            //heuristicInfluenceElement.add(HeuristicElement("KingWinPosition", evaluateKingWinPosition(kingPosition, kingRow, kingCol).toDouble(), 0, 4, 1.5))

            return  when {
                        blackWin(state, kingPosition) -> 0.0
                        else -> HeuristicUtil.weightedAverage(heuristicInfluenceElement.map { Pair(HeuristicUtil.normalizeValue(it.value, it.min, it.max), it.factor) })
                    }
        }

        /*
        * WHITE MIN-MAX
        * assuming lines 4 as worst lines and lines 2-6 as best
        * the worst gives me -3 (4 citadels and 1 throne)
        * the best gives me +2 (2 escapes)
        * assuming the worst case scenario as all blacks in a line they give me -8 (-1 each)
        * assuming the best case scenario as all whites in a line they give me +8 (+1 each)
        * MIN: since we have 16 blacks the min value can be found when the king is on throne
        * and is surrounded by all black in each line (row and col)
        * this means: - 3 - 3 - 8 - 8 = -22
        * MAX: since we have only 8 whites the max value can be found when the king is in a winning row and col
        * at the same time with one line full of whites and one line empty
        * this means: + 2 + 2 + 8 = +12
        */
        private fun evaluateKingPosition(kingPosition: Pair<Int, Int>, kingRow: String, kingCol: String): Int {
            return getWhiteLineScore(kingRow, kingPosition.first) + getWhiteLineScore(kingCol, kingPosition.second)
        }

        //WHITE: citadels -1, throne +1, black -1, white +1, escapes +1
        private fun getWhiteLineScore(line: String, boardLineIndex: Int): Int {
            var score = 0
            var i = 0
            line.forEach { l ->
                if(l != 'K') {
                    if (Pair(boardLineIndex, i) in BoardBox.CITADEL.boxes) score--
                    if (Pair(boardLineIndex, i) in BoardBox.THRONE.boxes) score++
                    if (Pair(boardLineIndex, i) in BoardBox.ESCAPE.boxes) score++
                    if (l == 'B') score--
                    if (l == 'W') score++
                }
                i++
            }
            return score
        }

        private fun evaluateKingWinPosition(kingPosition: Pair<Int, Int>, kingRow: String, kingCol: String): Int {
            var kingPositioning = 0
            if (kingPosition.first in winLine) kingPositioning += checkWhiteWinLineObstacles(kingRow, kingPosition.first)
            if (kingPosition.second in winLine) kingPositioning += checkWhiteWinLineObstacles(kingCol, kingPosition.second)
            if (kingPosition.first in goodLine) kingPositioning += checkWhiteGoodLineObstacles(kingRow, kingPosition.first)
            if (kingPosition.second in goodLine) kingPositioning += checkWhiteGoodLineObstacles(kingCol, kingPosition.second)
            return kingPositioning
        }

        private fun blackWin(state: State, kingPosition: Pair<Int, Int>): Boolean {
            if (kingPosition in BoardBox.KING_SAFE.boxes &&
                    getPawnEncirclement(state, kingPosition) { it == State.Pawn.BLACK || it == State.Pawn.THRONE } == 4)
                return true
            listOf(-1, 1).forEach { r ->
                val pawn = kingPosition.first + r to kingPosition.second
                if (pawn in BoardBox.CITADEL.boxes || (kingPosition !in  BoardBox.KING_SAFE.boxes && state.getPawn(pawn.first, pawn.second) == State.Pawn.BLACK) &&
                        state.getPawn(kingPosition.first - r, kingPosition.second) == State.Pawn.BLACK)
                    return true
            }
            listOf(-1, 1).forEach { c ->
                val pawn = kingPosition.first to kingPosition.second + c
                if (pawn in BoardBox.CITADEL.boxes || (kingPosition !in  BoardBox.KING_SAFE.boxes && state.getPawn(pawn.first, pawn.second) == State.Pawn.BLACK) &&
                        state.getPawn(kingPosition.first, kingPosition.second - c) == State.Pawn.BLACK)
                    return true
            }
            return false
        }
    }
}