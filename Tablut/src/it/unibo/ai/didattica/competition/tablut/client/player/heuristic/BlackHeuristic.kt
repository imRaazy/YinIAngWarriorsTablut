package it.unibo.ai.didattica.competition.tablut.client.player.heuristic

import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicElement
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.MAXBLACK
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.MAXWHITE
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.normalizeValue
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.whiteGoodLines
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.whiteMediumLines
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.PositionWeight.*
import it.unibo.ai.didattica.competition.tablut.domain.State

class BlackHeuristic {
    companion object {
        public fun genericBlackEval(state: State): Double {
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
                        kingPositioning = if (r in whiteGoodLines || c in whiteGoodLines)
                                            0
                                        else if (r in whiteMediumLines || c in whiteMediumLines)
                                            WHITEMEDIUMLINE.weight
                                        else
                                            WHITEGOODLINE.weight
                    }
                }
            }
            //Insert values in the map
            heuristicInfluenceElement.add(HeuristicElement("KingEncirclement", kingEncirclement, 0, 4 * KINGENCICLERMENT.weight, 0.4))
            heuristicInfluenceElement.add(HeuristicElement("KingPositioning", kingPositioning, 0, WHITEGOODLINE.weight, 0.4))
            heuristicInfluenceElement.add(HeuristicElement("NumberOfWhite", 1/numberOfWhite, 0, 1/MAXWHITE, 0.2))
            heuristicInfluenceElement.add(HeuristicElement("NumberOfBlack", numberOfBlack, 0, MAXBLACK, 0.2))
            heuristicInfluenceElement.add(HeuristicElement("WhiteEncirclement", whiteEncirclement, 0, numberOfBlack * 2, 0.1))

            return if (kingEncirclement == KINGENCICLERMENT.weight * 4) Double.POSITIVE_INFINITY
                   else HeuristicUtil.weightedAverage(heuristicInfluenceElement.map { Pair(normalizeValue(it.value, it.min, it.max), it.factor) })
        }
    }
}