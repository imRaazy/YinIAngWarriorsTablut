package it.unibo.ai.didattica.competition.tablut.client.player.heuristic

import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicElement
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.blackWin
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.checkWhiteGoodLineObstacles
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.checkWhiteWinLineObstacles
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.getCol
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.getKing
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.getRow
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.goodLine
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.normalizeValue
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.weightedAverage
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.winLine
import it.unibo.ai.didattica.competition.tablut.domain.State
import it.unibo.ai.didattica.competition.tablut.util.BoardBox
import kotlin.math.abs

class BlackHeuristic {
    companion object {
        fun genericBlackEval(state: State): Double {
            val heuristicInfluenceElement = mutableListOf<HeuristicElement>()
            var numberOfBlack = 0
            var numberOfWhite = 0
            var kingEncirclement = 0
            var kingPositioning = 8
            var whiteEncirclement = 0
            state.board.indices.forEach { r ->
                state.board.indices.forEach { c ->
                    if (state.getPawn(r, c) == State.Pawn.KING) {
                        kingEncirclement += HeuristicUtil.getPawnEncirclement(state, Pair(r, c)) { it == State.Pawn.BLACK || it == State.Pawn.THRONE }
                        if (r in winLine) kingPositioning -= 4
                        if (c in winLine) kingPositioning -= 4
                        if (r in goodLine) kingPositioning -= 2
                        if (c in goodLine) kingPositioning -= 2
                    }
                    if (state.getPawn(r, c) == State.Pawn.WHITE) {
                        whiteEncirclement += HeuristicUtil.getPawnEncirclement(state, Pair(r, c)) { it == State.Pawn.BLACK || it == State.Pawn.THRONE }
                        numberOfWhite++
                    }
                    if (state.getPawn(r, c) == State.Pawn.BLACK)
                        numberOfBlack++
                }
            }
            heuristicInfluenceElement.add(HeuristicElement("KingEncirclement", kingEncirclement.toDouble(), 0, 4, 0.4))
            heuristicInfluenceElement.add(HeuristicElement("KingPositioning", kingPositioning.toDouble(), 0, 8, 0.6))
            heuristicInfluenceElement.add(HeuristicElement("NumberOfWhite", 1 / numberOfWhite.toDouble(), 0, 1, 0.2))
            heuristicInfluenceElement.add(HeuristicElement("WhiteEncirclement", whiteEncirclement.toDouble(), 0, numberOfBlack * 2, 0.1))
            //heuristicInfluenceElement.add(HeuristicElement("NumberOfBlack", numberOfBlack, 0, MAXBLACK, 0.2))
            //heuristicInfluenceElement.add(HeuristicElement("NumberOfPawns", 1.0 * numberOfBlack/(numberOfBlack+2*numberOfWhite), 0, 1, 0.2))

            return if (blackWin(state)) 1.0
                   else weightedAverage(heuristicInfluenceElement.map { Pair(normalizeValue(it.value, it.min, it.max), it.factor) })
        }

        //black heuristic function based on: KingPos, KingEncirc, ManhattanFromBlackPawnToKing, PawnDifference, NumOFWhitePawns
        fun bestBlackEval(state: State): Double {
            val heuristicInfluenceElement = mutableListOf<HeuristicElement>()
            var numberOfBlack = 0 //MAX: 16, MIN: 0
            var numberOfWhite = 0
            var kingEncirclement = 0 //MAX: 4, MIN: 0
            var manhattanDistance = 208 //MAX: 208, MIN: 0
            val kingPosition = getKing(state)!!

            //checking the board status
            state.board.indices.forEach { r ->
                state.board.indices.forEach { c ->
                    if (state.getPawn(r, c) == State.Pawn.KING) {
                        kingEncirclement += HeuristicUtil.getPawnEncirclement(state, Pair(r, c)) { it == State.Pawn.BLACK || it == State.Pawn.THRONE } //king enciclement
                    }
                    if (state.getPawn(r, c) == State.Pawn.WHITE) numberOfWhite++ //number of white
                    if (state.getPawn(r, c) == State.Pawn.BLACK) {
                        numberOfBlack++ //number of black
                        manhattanDistance -= pawnToKingManhattanDistance(kingPosition, Pair(r, c)) //total manhattandistance
                    }
                }
            }
            val kingRow = getRow(kingPosition.first, state)
            val kingCol = getCol(kingPosition.second, state)

            heuristicInfluenceElement.add(HeuristicElement("KingPositioning", evaluateKingPosition(kingPosition, kingRow, kingCol).toDouble(), -12, 22, 0.2))
            heuristicInfluenceElement.add(HeuristicElement("ManhattanDistance", manhattanDistance.toDouble(), 0, 208, 0.6))
            heuristicInfluenceElement.add(HeuristicElement("KingEncirclement", kingEncirclement.toDouble(), 0, 4, 1.5))
            heuristicInfluenceElement.add(HeuristicElement("PawnDifference", numberOfBlack.toDouble()/(numberOfBlack+2*numberOfWhite), 0, 1, 2.0))
            return  when {
                        blackWin(state) -> 1.0
                        whiteWin(kingPosition, kingRow, kingCol, state.turn) -> 0.0
                        else -> weightedAverage(heuristicInfluenceElement.map { Pair(normalizeValue(it.value, it.min, it.max), it.factor) })
                    }
        }

        /* BLACK MIN-MAX:
        * assuming lines 4 as worst lines and lines 2-6 as best
        * the worst gives me +3 (4 citadels and 1 throne)
        * the best gives me -2 (2 escapes)
        * assuming the best case scenario as all blacks in a line they give me +8 (+1 each)
        * assuming the worst case scenario as all whites in a line they give me -8 (-1 each)
        * MIN: since we have only 8 whites the min value can be found when the king is in a winning row and col
        * at the same time with one line full of whites and one line empty
        * this means: - 2 - 2 - 8 = -12
        * MAX: since we have 16 blacks the max value can be found when the king is on throne
        * and is surrounded by all black in each line (row and col)
        * this means: + 3 + 3 + 8 + 8 = +22
        */
        private fun whiteWin(kingPosition: Pair<Int, Int>, kingRow: String, kingCol: String, turn: State.Turn): Boolean {
            return  (turn == State.Turn.BLACK && (checkWhiteWinLineObstacles(kingRow, kingPosition.first) == 2 || checkWhiteWinLineObstacles(kingCol, kingPosition.second) == 2)) ||
                    (turn == State.Turn.BLACK && (checkWhiteGoodLineObstacles(kingRow, kingPosition.first) == 1 && checkWhiteGoodLineObstacles(kingCol, kingPosition.second) == 1)) ||
                    (turn == State.Turn.WHITE && (checkWhiteWinLineObstacles(kingRow, kingPosition.first) + checkWhiteWinLineObstacles(kingCol, kingPosition.second) > 0)) ||
                    (turn == State.Turn.WHITE && (checkWhiteGoodLineObstacles(kingRow, kingPosition.first) + checkWhiteGoodLineObstacles(kingCol, kingPosition.second) > 0))
        }

        private fun evaluateKingPosition(kingPosition: Pair<Int, Int>, kingRow: String, kingCol: String): Int {
            return getBlackLineScore(kingRow, kingPosition.first) + getBlackLineScore(kingCol, kingPosition.second)
        }

        //BLACK: citadels +1, throne -1, black +1, white -1, escapes -1, empty 0
        private fun getBlackLineScore(line: String, boardLineIndex: Int): Int {
            var score = 0
            var i = 0
            line.forEach { l ->
                if(l != 'K') {
                    if (Pair(boardLineIndex, i) in BoardBox.CITADEL.boxes) score++
                    if (Pair(boardLineIndex, i) in BoardBox.THRONE.boxes) score--
                    if (Pair(boardLineIndex, i) in BoardBox.ESCAPE.boxes) score--
                    if (l == 'B') score++
                    if (l == 'W') score--
                }
                i++
            }
            return score
        }

        //manhattan distance from pawn to pawn (one is king)
        private fun pawnToKingManhattanDistance(kingPosition: Pair<Int, Int>, pawnPosition: Pair<Int, Int>): Int {
            return abs(kingPosition.first - pawnPosition.first) + abs(kingPosition.second - pawnPosition.second)
        }
    }
}