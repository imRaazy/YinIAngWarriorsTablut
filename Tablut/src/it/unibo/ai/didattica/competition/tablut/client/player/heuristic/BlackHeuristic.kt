package it.unibo.ai.didattica.competition.tablut.client.player.heuristic

import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicElement
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.blackWin
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.goodLine
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.normalizeValue
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.winLine
import it.unibo.ai.didattica.competition.tablut.domain.State

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
                   else HeuristicUtil.weightedAverage(heuristicInfluenceElement.map { Pair(normalizeValue(it.value, it.min, it.max), it.factor) })
        }
    }
}