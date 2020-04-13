package it.unibo.ai.didattica.competition.tablut.client.player.aima

import aima.core.search.adversarial.Game
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.BlackHeuristic
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.checkLineObstacles
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.getCol
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.getCrossPawnSurrounding
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.getDiagonalPawnSurrounding
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.getKing
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.getRow
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.normalizeValue
import it.unibo.ai.didattica.competition.tablut.domain.Action
import it.unibo.ai.didattica.competition.tablut.domain.State

class AlphaBetaSearchNicobar(game: Game<State, Action, State.Turn>?, utilMin: Double, utilMax: Double, time: Int) :
        IterativeDeepeningAlphaBetaSearch<State, Action, State.Turn>(game, utilMin, utilMax, time) {
    /**
     * WITHOUT STATES
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

    //milo's black code
    private fun evalBlack(state: State): Double {
        return BlackHeuristic.genericBlackEval(state)
    }

    private fun evalWhite(state: State): Double {
        //val numberOfBlack = state.getNumberOf(State.Pawn.BLACK) //go from 0 to 16
        val moveTowardsWinLineFactor = 0.45
        val moveWhiteCloseToKingFactory = 0.31
        val moveWhiteFarFromKingFactor = 0.24
        return if (canWin(state))
            1.00
        else {
            moveTowardsWinLine(state) * moveTowardsWinLineFactor +
                    moveWhiteCloseToKing(state) * moveWhiteCloseToKingFactory +
                    moveWhiteFarFromKing(state) * moveWhiteFarFromKingFactor
        }
    }

    private fun moveTowardsWinLine(state: State): Double {
        var score = 0
        var weight = 50
        val kingPosition = getKing(state)!!

        score = if( kingPosition.first == 2 || kingPosition.first == 6 ) checkLineObstacles(getRow(kingPosition.first, state), state)
        else if( kingPosition.second == 2 || kingPosition.second == 6 ) checkLineObstacles(getCol(kingPosition.second, state), state)
        else 0

        return normalizeValue(score * weight.toDouble(), 100, 0)
    }

    //move white towards king to avoid match lost and try to eat blacks
    private fun moveWhiteCloseToKing(state: State): Double {
        val kingPosition = getKing(state)!!
        val blackAroundKingScoreFactor = 0.65//0.50
        val whiteOnDiagonalScoreFactor = 0.35
        val whiteAroundKingScoreFactor = 0.15
        var blackAroundKingScore = 0
        var whiteOnDiagonalScore = 0
        var whiteAroundKingScore = 0
        val crossPawnsSurrounding = getCrossPawnSurrounding(kingPosition, state)
        val diagonalPawnsSurrounding = getDiagonalPawnSurrounding(kingPosition, state)
        var numWhite = state.getNumberOf(State.Pawn.WHITE)

        //max score if i eat black
        crossPawnsSurrounding.values.forEach { if (it != State.Pawn.BLACK) blackAroundKingScore += 24 }

        //plus 30 if i move white on diagonal
        var blackOrEmptyAroundKing = 0
        crossPawnsSurrounding.values.forEach { if ( it == State.Pawn.BLACK ) blackOrEmptyAroundKing++ }

        diagonalPawnsSurrounding.forEach {
            if ( it.value == State.Pawn.WHITE && blackOrEmptyAroundKing > 1 ) whiteOnDiagonalScore += 24
        }

        /*//plus 10 for white on cross to avoid loss
        crossPawnsSurrounding.values.forEach { if ( it == State.Pawn.WHITE) whiteAroundKingScore += 24 } */

        return normalizeValue(blackAroundKingScore * blackAroundKingScoreFactor + whiteOnDiagonalScore *
                whiteOnDiagonalScoreFactor /*+ whiteAroundKingScore * whiteAroundKingScoreFactor*/, 96, 0)
    }

    //move white away to get the king gang banged from blacks
    private fun moveWhiteFarFromKing (state: State): Double {
        val kingPosition = getKing(state)!!
        val kingFreeFactor = 1.00//0.50
        var kingFree = 0

        var crossPawnsSurrounding = getCrossPawnSurrounding(kingPosition, state)

        //buff weight as more whites are around the king.
        crossPawnsSurrounding.values.forEach { if (it != State.Pawn.WHITE) kingFree += 32 }

        return normalizeValue(kingFree * kingFreeFactor, 96, 0)
    }

    private fun canWin(state: State): Boolean {
        val kingPosition = getKing(state)!!
        if (    kingPosition.first == 0 && kingPosition.second == 1 ||
                kingPosition.first == 0 && kingPosition.second == 2 ||
                kingPosition.first == 0 && kingPosition.second == 6 ||
                kingPosition.first == 0 && kingPosition.second == 7 ||
                kingPosition.first == 1 && kingPosition.second == 0 ||
                kingPosition.first == 1 && kingPosition.second == 8 ||
                kingPosition.first == 2 && kingPosition.second == 0 ||
                kingPosition.first == 2 && kingPosition.second == 8 ||
                kingPosition.first == 6 && kingPosition.second == 0 ||
                kingPosition.first == 6 && kingPosition.second == 8 ||
                kingPosition.first == 7 && kingPosition.second == 0 ||
                kingPosition.first == 7 && kingPosition.second == 8 ||
                kingPosition.first == 8 && kingPosition.second == 1 ||
                kingPosition.first == 8 && kingPosition.second == 2 ||
                kingPosition.first == 8 && kingPosition.second == 6 ||
                kingPosition.first == 8 && kingPosition.second == 7 )
            return true
        return false
    }

    //Stupid black for testing
    /*private fun evalBlack(state: State): Double {
        val kingEncirclementFactor = 0.4
        val whiteEatingFactor = 0.3
        // NumberOfPawns
        val numberOfWhite = state.getNumberOf(State.Pawn.WHITE)
        // KingEncirclement
        var kingEncirclement = getKing(state)?.let { getPawnEncirclementBlack(state, it, 20) }
        // WhiteEating
        var whiteEating = 0
        state.board.indices.forEach { r ->
            state.board.indices.forEach { c ->
                if (state.getPawn(r, c) == State.Pawn.WHITE)
                    whiteEating += getPawnEncirclementBlack(state, Pair(r, c), 5)
            }
        }
        state.board.indices.forEach { r ->
            state.board.indices.forEach { c ->
                if (state.getPawn(r, c) == State.Pawn.WHITE)
                    whiteEating += getPawnEncirclementBlack(state, Pair(r, c), 5)
            }
        }
        return kingEncirclementFactor * kingEncirclement!! + whiteEatingFactor * whiteEating - numberOfWhite
    }

    //Utility: for black pawns player
    private fun getPawnEncirclementBlack(state: State, position: Pair<Int, Int>, increaseFactor: Int): Int {
        var kingEncirclement = 0
        listOf(-1, 1).forEach { r ->
            if ( (position.first + r) in state.board.indices && state.getPawn(position.first + r, position.second) == State.Pawn.BLACK )
                kingEncirclement += increaseFactor
        }
        listOf(-1, 1).forEach { c ->
            if ( (position.second + c) in state.board.indices && state.getPawn(position.first, position.second + c) == State.Pawn.BLACK )
                kingEncirclement += increaseFactor
        }
        return kingEncirclement
    }*/
}