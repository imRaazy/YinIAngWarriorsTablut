package it.unibo.ai.didattica.competition.tablut.client.player.heuristic

import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicElement
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.getCol
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.getKing
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.getPawnEncirclement
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.getRow
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.goodLine
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.whiteWin
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.winLine
import it.unibo.ai.didattica.competition.tablut.domain.State

class WhiteHeuristic {
    companion object {
        fun genericWhiteEval(state: State): Double {
            val heuristicInfluenceElement = mutableListOf<HeuristicElement>()
            val kingPosition = getKing(state)!!
            var numberOfBlack = 0
            var numberOfWhite = 1
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
                        blackEncirclement += getPawnEncirclement(state, Pair(r, c)) { it == State.Pawn.BLACK }
                        numberOfWhite++
                    }
                    if (state.getPawn(r, c) == State.Pawn.BLACK)
                        numberOfBlack++
                }
            }
            heuristicInfluenceElement.add(HeuristicElement("KingPositioning", evaluateKingPositioning(kingPosition, state), 0, 4, 0.5))
            heuristicInfluenceElement.add(HeuristicElement("KingEncirclement", kingEncirclement, 0, 3, 0.3))
            //heuristicInfluenceElement.add(HeuristicElement("NumberOfWhite", numberOfWhite, 0, MAXWHITE, 0.2))
            heuristicInfluenceElement.add(HeuristicElement("NumberOfBlack", 1 / numberOfBlack, 0, 1, 0.2))
            heuristicInfluenceElement.add(HeuristicElement("BlackEncirclement", blackEncirclement, 0, numberOfWhite*2, 0.1))
            return if (whiteWin(kingPosition)) 1.0
                   else HeuristicUtil.weightedAverage(heuristicInfluenceElement.map { Pair(HeuristicUtil.normalizeValue(it.value, it.min, it.max), it.factor) })
        }

        private fun evaluateKingPositioning(kingPosition: Pair<Int, Int>, state: State): Int {
            var kingPositioning = 0
            if (kingPosition.first in winLine) kingPositioning += checkWinningLineObstacles(getRow(kingPosition.first, state))
            if (kingPosition.second in winLine) kingPositioning += checkWinningLineObstacles(getCol(kingPosition.second, state))
            if (kingPosition.first in goodLine) kingPositioning += checkGoodLineObstacles(getRow(kingPosition.first, state))
            if (kingPosition.second in goodLine) kingPositioning += checkGoodLineObstacles(getCol(kingPosition.second, state))
            return kingPositioning
        }

        //return 0 if 2 obstacles, 1 if 1 obstacle, 2 if 0 obstacles
        private fun checkWinningLineObstacles(line: String): Int {
            var score = 0
            if (!line.substringBefore("K").contains("B") && !line.substringBefore("K").contains("W")) score++
            if (!line.substringAfter("K").contains("B") && !line.substringAfter("K").contains("W")) score++
            return score
        }

        //return 0 if 1 obstacles, 1 if 0 obstacles
        private fun checkGoodLineObstacles(line: String): Int {
            var score = 0
            if (line.indexOf("K") < 4) {
                if (!line.substringBefore("K").contains("B") && !line.substringBefore("K").contains("W"))
                    score++
            } else {
                if (!line.substringAfter("K").contains("B") && !line.substringAfter("K").contains("W"))
                    score++
            }
            return score
        }
    }
}