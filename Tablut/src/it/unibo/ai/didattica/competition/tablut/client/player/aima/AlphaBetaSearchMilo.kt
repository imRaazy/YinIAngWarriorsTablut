package it.unibo.ai.didattica.competition.tablut.client.player.aima

import aima.core.search.adversarial.Game
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch
import it.unibo.ai.didattica.competition.tablut.domain.Action
import it.unibo.ai.didattica.competition.tablut.domain.State

class AlphaBetaSearchMilo(game: Game<State, Action, State.Turn>?, utilMin: Double, utilMax: Double, time: Int) :
        IterativeDeepeningAlphaBetaSearch<State, Action, State.Turn>(game, utilMin, utilMax, time) {
    /**
     * Heuristic function that evaluate the correctness of the given
     * state if it is not terminal otherwise return the value of getUtils
     * @param state
     *          given state
     * @param turn
     *          player role
     * @return evaluation
     */
    override fun eval(state: State, turn: State.Turn): Double {
        if (game.isTerminal(state))
            return game.getUtility(state, turn)
        return if (turn == State.Turn.BLACK) evalBlack(state) else evalWhite(state)
    }

    private fun evalBlack(state: State): Double {
        val numberOfWhiteFactor = 0.3
        val kingEncirclementFactor = 0.7
        val numberOfWhite = state.getNumberOf(State.Pawn.WHITE)
        val kingPosition = getKing(state)
        var kingEncirclement = 0
        (-1 .. 1).forEach { r ->
            (-1 .. 1).forEach { c ->
                if (kingPosition != null) {
                    if (state.getPawn(kingPosition.first + r, kingPosition.second + c) == State.Pawn.BLACK)
                        kingEncirclement += 50
                }
            }
        }
//        print("numero di bianchi: $numberOfWhite\t accerchiamento:$kingEncirclement\t")
//        println(numberOfWhiteFactor * numberOfWhite/0.7 + kingEncirclementFactor * kingEncirclement)
        return numberOfWhiteFactor * numberOfWhite/0.7 + kingEncirclementFactor * kingEncirclement
    }

    private fun evalWhite(state: State): Double {
        return Double.NEGATIVE_INFINITY
    }

    private fun getKing(state: State): Pair<Int, Int>? {
        state.board.indices.forEach { r ->
            state.board.indices.forEach { c ->
                if (state.getPawn(r, c) == State.Pawn.KING)
                    return Pair(r, c)
            }
        }
        return null
    }
}