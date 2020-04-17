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
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.whiteWin
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.winLine
import it.unibo.ai.didattica.competition.tablut.domain.State
import it.unibo.ai.didattica.competition.tablut.util.BoardBox

class WhiteHeuristic {
    companion object {
        fun genericWhiteEval(state: State): Double {
            val heuristicInfluenceElement = mutableListOf<HeuristicElement>()
            val kingPosition = getKing(state)!!
            var numberOfBlack = 0
            var numberOfWhite = 0
            var kingEncirclement = getPawnEncirclement(state, kingPosition) { it == State.Pawn.WHITE }
            var blackEncirclement = 0
            when (kingEncirclement) {
                4 -> kingEncirclement = 0
                3 -> kingEncirclement = 1
                1 -> kingEncirclement = 3
            }
            state.board.indices.forEach { r ->
                state.board.indices.forEach { c ->
                    if (state.getPawn(r, c) == State.Pawn.WHITE) {
                        blackEncirclement += getPawnEncirclement(state, Pair(r, c)) { it == State.Pawn.BLACK || it == State.Pawn.THRONE }
                        numberOfWhite++
                    }
                    if (state.getPawn(r, c) == State.Pawn.BLACK)
                        numberOfBlack++
                }
            }
            heuristicInfluenceElement.add(HeuristicElement("KingPositioning", evaluateKingPositioning(kingPosition, state).toDouble(), 0, 4, 0.5))
            heuristicInfluenceElement.add(HeuristicElement("KingEncirclement", kingEncirclement.toDouble(), 0, 3, 0.3))
            heuristicInfluenceElement.add(HeuristicElement("NumberOfBlack", 1 / numberOfBlack.toDouble(), 0, 1, 0.2))
            heuristicInfluenceElement.add(HeuristicElement("BlackEncirclement", blackEncirclement.toDouble(), 0, numberOfWhite*2, 0.1))
            //heuristicInfluenceElement.add(HeuristicElement("NumberOfPawns", 2.0 * numberOfWhite/(numberOfBlack+2*numberOfWhite), 0, 1, 0.2))
            return if (whiteWin(kingPosition)) 1.0
                   else HeuristicUtil.weightedAverage(heuristicInfluenceElement.map { Pair(HeuristicUtil.normalizeValue(it.value, it.min, it.max), it.factor) })
        }

        private fun evaluateKingPositioning(kingPosition: Pair<Int, Int>, state: State): Int {
            var kingPositioning = 0
            if (kingPosition.first in winLine) kingPositioning += checkWhiteWinLineObstacles(getRow(kingPosition.first, state))
            if (kingPosition.second in winLine) kingPositioning += checkWhiteWinLineObstacles(getCol(kingPosition.second, state))
            if (kingPosition.first in goodLine) kingPositioning += checkWhiteGoodLineObstacles(getRow(kingPosition.first, state))
            if (kingPosition.second in goodLine) kingPositioning += checkWhiteGoodLineObstacles(getCol(kingPosition.second, state))
            return kingPositioning
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
        private fun evaluateKingPosition(kingPosition: Pair<Int, Int>, state: State): Int {
            return getWhiteLineScore(getRow(kingPosition.first, state), kingPosition.first) + getWhiteLineScore(getCol(kingPosition.second, state), kingPosition.second)
        }

        //WHITE: citadels -1, throne +1, black -1, white +1, escapes +1, empty 0
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
    }
}