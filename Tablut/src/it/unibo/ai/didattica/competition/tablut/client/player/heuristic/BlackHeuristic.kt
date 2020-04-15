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
            var numberOfWhite = 1
            var kingEncirclement = 0
            var kingPositioning = 0
            var whiteEncirclement = 0
            state.board.indices.forEach { r ->
                state.board.indices.forEach { c ->
                    if (state.getPawn(r, c) == State.Pawn.KING) {
                        kingEncirclement += HeuristicUtil.getPawnEncirclement(state, Pair(r, c)) { it == State.Pawn.BLACK }
                        if (r in winLine || c in winLine)
                            kingPositioning = 2
                        else if (r in goodLine || c in goodLine)
                            kingPositioning = 1
                    }
                    if (state.getPawn(r, c) == State.Pawn.WHITE) {
                        whiteEncirclement += HeuristicUtil.getPawnEncirclement(state, Pair(r, c)) { it == State.Pawn.BLACK }
                        numberOfWhite++
                    }
                    if (state.getPawn(r, c) == State.Pawn.BLACK)
                        numberOfBlack++
                }
            }
            heuristicInfluenceElement.add(HeuristicElement("KingEncirclement", kingEncirclement, 0, 4, 0.4))
            heuristicInfluenceElement.add(HeuristicElement("KingPositioning", kingPositioning, 0, 2, 0.6))
            heuristicInfluenceElement.add(HeuristicElement("NumberOfWhite", 1 / numberOfWhite, 0, 1, 0.2))
            //heuristicInfluenceElement.add(HeuristicElement("NumberOfBlack", numberOfBlack, 0, MAXBLACK, 0.2))
            heuristicInfluenceElement.add(HeuristicElement("WhiteEncirclement", whiteEncirclement, 0, numberOfBlack * 2, 0.1))

            return if (blackWin(state)) 1.0
                   else HeuristicUtil.weightedAverage(heuristicInfluenceElement.map { Pair(normalizeValue(it.value, it.min, it.max), it.factor) })
        }
    }
}