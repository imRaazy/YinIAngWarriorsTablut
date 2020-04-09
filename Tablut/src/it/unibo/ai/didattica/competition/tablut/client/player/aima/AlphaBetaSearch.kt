package it.unibo.ai.didattica.competition.tablut.client.player.aima

import aima.core.search.adversarial.Game
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch
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
        if (game.isTerminal(state))
            return game.getUtility(state, turn)
        var evalWhite = 0.0
        var evalBlack = 0.0
        // Placeholder for the real heuristic function
        state.board.forEach { it.forEach { p ->
            if ( p == State.Pawn.WHITE || p == State.Pawn.KING )
                evalWhite++
            else if ( p == State.Pawn.BLACK )
                evalBlack++
        }}
        return if (turn == State.Turn.WHITE) evalWhite else evalBlack
    }
}