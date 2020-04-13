package it.unibo.ai.didattica.competition.tablut.client.player.heuristic

import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicElement
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.goodSquare
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.normalizeValue
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.winnigSquare
import it.unibo.ai.didattica.competition.tablut.domain.State

class BlackHeuristic {
    companion object {
        fun genericBlackEval(state: State): Double {
            val heuristicInfluenceElement = mutableListOf<HeuristicElement>()
            val numberOfBlack = state.getNumberOf(State.Pawn.BLACK)
            val numberOfWhite = state.getNumberOf(State.Pawn.WHITE)
            var kingEncirclement = 0
            var kingPositioning = 0
            var whiteEncirclement = 0
            state.board.indices.forEach { r ->
                state.board.indices.forEach { c ->
                    if (state.getPawn(r, c) == State.Pawn.WHITE)
                        whiteEncirclement += HeuristicUtil.getPawnEncirclement(state, Pair(r, c)) { it == State.Pawn.BLACK }
                    if (state.getPawn(r, c) == State.Pawn.KING) {
                        kingEncirclement += HeuristicUtil.getPawnEncirclement(state, Pair(r, c)) { it == State.Pawn.BLACK }
                        if (r in winnigSquare || c in winnigSquare)
                            kingPositioning = 2
                        else if (r in goodSquare || c in goodSquare)
                            kingPositioning = 1
                    }
                }
            }
            heuristicInfluenceElement.add(HeuristicElement("KingEncirclement", kingEncirclement, 0, 4, 0.4))
            heuristicInfluenceElement.add(HeuristicElement("KingPositioning", kingPositioning, 0, 2, 0.8))
            heuristicInfluenceElement.add(HeuristicElement("NumberOfWhite", 1 / numberOfWhite, 0, 1, 0.2))
            //heuristicInfluenceElement.add(HeuristicElement("NumberOfBlack", numberOfBlack, 0, MAXBLACK, 0.2))
            heuristicInfluenceElement.add(HeuristicElement("WhiteEncirclement", whiteEncirclement, 0, numberOfBlack * 2, 0.1))

            return if (kingEncirclement ==  4) Double.POSITIVE_INFINITY
                   else HeuristicUtil.weightedAverage(heuristicInfluenceElement.map { Pair(normalizeValue(it.value, it.min, it.max), it.factor) })
        }
    }
}