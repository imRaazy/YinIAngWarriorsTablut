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
        val numberOfBlackFactor = 0.3
        val kingEncirclementFactor = 0.4
        val whiteEatingFactor = 0.3
        // NumberOfPawns
        val numberOfBlack = state.getNumberOf(State.Pawn.BLACK)
        val numberOfWhite = state.getNumberOf(State.Pawn.WHITE)
        // KingEncirclement
        var kingEncirclement = getKing(state)?.let { getPawnEncirclement(state, it, 20) }
        // WhiteEating
        var whiteEating = 0
        state.board.indices.forEach { r ->
            state.board.indices.forEach { c ->
                if (state.getPawn(r, c) == State.Pawn.WHITE)
                    whiteEating += getPawnEncirclement(state, Pair(r, c), 5)
            }
        }
        state.board.indices.forEach { r ->
            state.board.indices.forEach { c ->
                if (state.getPawn(r, c) == State.Pawn.WHITE)
                    whiteEating += getPawnEncirclement(state, Pair(r, c), 5)
            }
        }
        return kingEncirclementFactor * kingEncirclement!! + whiteEatingFactor * whiteEating - numberOfWhite
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
    private fun getPawnEncirclement(state: State, position: Pair<Int, Int>, increaseFactor: Int): Int {
        val boardRange = (0 .. state.board.size)
        var kingEncirclement = 0
        listOf(-1, 1).forEach { r ->
            if ((position.first + r) in boardRange && state.getPawn(position.first + r, position.second) == State.Pawn.BLACK)
                kingEncirclement += increaseFactor
        }
        listOf(-1, 1).forEach { c ->
            if ((position.second + c) in boardRange && state.getPawn(position.first, position.second + c) == State.Pawn.BLACK)
                kingEncirclement += increaseFactor
        }
        return kingEncirclement
    }
}