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
import it.unibo.ai.didattica.competition.tablut.domain.Action
import it.unibo.ai.didattica.competition.tablut.domain.State

class AlphaBetaSearchNicobarWithStates(game: Game<State, Action, State.Turn>?, utilMin: Double, utilMax: Double, time: Int) :
        IterativeDeepeningAlphaBetaSearch<State, Action, State.Turn>(game, utilMin, utilMax, time) {
    /**
     * FIRST TRY FIRST TRY
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

    //start here
    private fun evalWhite(state: State): Double {
        return when (getNextMove(state)) {
            -1 -> {
                println("WINWINWINWINWINWINWIN")
                100000.toDouble()
            }
            0 -> moveTowardsWinLine(state)*150  //good line found to win -> go towards victory
            1 -> moveWhiteCloseToKing(state)*10 //king feels alone & it's gonna be eat soon -> move white towards king and try to eat some blacks
            2 -> moveWhiteFarFromKing(state) //king surrounded by whites -> move white away to get the king gang banged from blacks
            else -> Double.NEGATIVE_INFINITY
        }
    }

    private fun getNextMove(state: State): Int {
        val kingPosition = getKing(state)!!
        var whiteCounter = 0
        var blackCounter = 0
        var emptyCounter = 0

        val crossPawnsSurrounding = getCrossPawnSurrounding(kingPosition, state)
        crossPawnsSurrounding.values.forEach { if ( it == State.Pawn.WHITE ) whiteCounter++ else if ( it == State.Pawn.BLACK ) blackCounter++ else emptyCounter++ }

        return if ( canWin(state) ) -1 //if i win then win
        else if ( kingPosition!!.first == 2 || kingPosition.first == 6 || kingPosition.second == 2 || kingPosition.second == 6 ) 0 //king can move in a possbile win line -> go towards victory
        else if ( blackCounter >= 2 ) 1 //king feels alone & it's gonna be eat soon -> move white towards king and try to eat some blacks //was shallIMoveWhiteClose(pawnsSurrounding)
        else if ( whiteCounter > 0 ) 2 //king surrounded by whites -> move white away to get black around //was > 1
        else 1 //if it doesnt know what to do, move some white close to the king //was 2
    }

    private fun moveTowardsWinLine(state: State): Double {
        println("TOWARD WIN LINE")
        var score = 0
        var weight = 50
        val kingPosition = getKing(state)!!

        score = if( kingPosition.first == 2 || kingPosition.first == 6 ) checkLineObstacles(getRow(kingPosition.first, state), state)
        else if( kingPosition.second == 2 || kingPosition.second == 6 ) checkLineObstacles(getCol(kingPosition.second, state), state)
        else 0

        return ( score * weight ).toDouble()
    }

    //move white towards king to avoid match lost and try to eat blacks
    private fun moveWhiteCloseToKing(state: State): Double {
        println("MOVE WHITE CLOSE TO KING")
        val kingPosition = getKing(state)!!
        var blackAroundKingScore = 0
        var whiteOnDiagonalScore = 0
        var whiteAroundKingScore = 0
        val numberOfBlack = state.getNumberOf(State.Pawn.BLACK)
        val crossPawnsSurrounding = getCrossPawnSurrounding(kingPosition, state)
        val diagonalPawnsSurrounding = getDiagonalPawnSurrounding(kingPosition, state)

        //max score if i eat black
        crossPawnsSurrounding.values.forEach { if ( it != State.Pawn.BLACK) blackAroundKingScore += 100 }

        //plus 30 if i move white on diagonal
        diagonalPawnsSurrounding.values.forEach { if ( it == State.Pawn.WHITE ) whiteOnDiagonalScore += 30 }

        //plus 10 for white on cross to avoid loss
        crossPawnsSurrounding.values.forEach { if ( it == State.Pawn.WHITE) whiteAroundKingScore += 10 }

        return ( blackAroundKingScore + whiteOnDiagonalScore + whiteAroundKingScore - numberOfBlack ).toDouble()
    }

    //move white away to get the king gang banged from blacks
    private fun moveWhiteFarFromKing (state: State): Double {
        println("WHITE FAR TO KING")
        val numberOfBlack = state.getNumberOf(State.Pawn.BLACK)
        val kingPosition = getKing(state)!!

        var buffBlack = 0
        var oppositeSide = 0
        var kingFree = 0

        var crossPawnsSurrounding = getCrossPawnSurrounding(kingPosition, state)

        // buffing weights as king gets alone
        crossPawnsSurrounding.values.forEach { if (it != State.Pawn.WHITE) kingFree += 100 }

        //giving priority to opposite side
        if ( crossPawnsSurrounding["up"] != State.Pawn.WHITE && crossPawnsSurrounding["down"] != State.Pawn.WHITE ||
                crossPawnsSurrounding["left"] != State.Pawn.WHITE && crossPawnsSurrounding["right"] != State.Pawn.WHITE )
            oppositeSide = 50

        //buffing weights as whites paws moves close to blacks
        state.board.indices.forEach { r ->
            state.board.indices.forEach { c ->
                if (state.getPawn(r, c) == State.Pawn.WHITE)
                    if (r != 0) buffBlack += if (state.getPawn(r - 1, c) == State.Pawn.BLACK) 3 else 0
                if (c != 0) buffBlack += if (state.getPawn(r, c - 1) == State.Pawn.BLACK) 3 else 0
                if (r != state.board.size - 1) buffBlack += if (state.getPawn(r + 1, c) == State.Pawn.BLACK) 3 else 0
                if (c != state.board.size - 1) buffBlack += if (state.getPawn(r, c + 1) == State.Pawn.BLACK) 3 else 0
            }
        }
        return (buffBlack + oppositeSide + kingFree - numberOfBlack).toDouble()
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
    // Utility: for black pawns player
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