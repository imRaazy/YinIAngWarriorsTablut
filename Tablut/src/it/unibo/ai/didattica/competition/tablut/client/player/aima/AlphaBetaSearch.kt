package it.unibo.ai.didattica.competition.tablut.client.player.aima

import aima.core.search.adversarial.Game
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.BlackHeuristic
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.WhiteHeuristic
import it.unibo.ai.didattica.competition.tablut.domain.Action
import it.unibo.ai.didattica.competition.tablut.domain.State

class AlphaBetaSearch(game: Game<State, Action, State.Turn>?, utilMin: Double, utilMax: Double, time: Int) :
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
        super.eval(state, turn)
        if (game.isTerminal(state))
            return game.getUtility(state, turn)
        return if (turn == State.Turn.BLACK) evalBlack(state) else evalWhite(state)
    }

    private fun evalBlack(state: State): Double {
        return BlackHeuristic.genericBlackEval(state)
    }

    private fun evalWhite(state: State): Double {
        return WhiteHeuristic.genericWhiteEval(state)
    }
    
}