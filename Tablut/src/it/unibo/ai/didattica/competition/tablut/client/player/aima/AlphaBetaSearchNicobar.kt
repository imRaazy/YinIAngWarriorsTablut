package it.unibo.ai.didattica.competition.tablut.client.player.aima

import aima.core.search.adversarial.Game
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch
import it.unibo.ai.didattica.competition.tablut.domain.Action
import it.unibo.ai.didattica.competition.tablut.domain.State

class AlphaBetaSearchNicobar(game: Game<State, Action, State.Turn>?, utilMin: Double, utilMax: Double, time: Int) :
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

    //milo's black code
    private fun evalBlack(state: State): Double {
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

    //start here
    private fun evalWhite(state: State): Double {
        return when (getNextMove(state)) {
            -1 -> 100000.toDouble()
            0 -> moveTowardsWinLine(state)*100  //good line found to win -> go towards victory
            1 -> moveWhiteFarFromKing(state) //king surrounded by whites -> move white away to get the king gang banged from blacks
            2 -> moveWhiteCloseToKing(state)*10 //king feels alone & it's gonna be eat soon -> move white towards king and try to eat some blacks
            //3 -> eatBlacks(state)*100 //three black around king -> eat them with some white to free the king
            else -> Double.NEGATIVE_INFINITY
        }
    }

    private fun getNextMove(state: State): Int {
        val kingPosition = getKing(state)!!
        var whiteCounter = 0
        var blackCounter = 0
        var emptyCounter = 0

        val pawnsSurrounding = getPawnSurrounding(kingPosition, state)
        pawnsSurrounding.values.forEach { if ( it == State.Pawn.WHITE ) whiteCounter++ else if ( it == State.Pawn.BLACK ) blackCounter++ else emptyCounter++ }

        return if ( canWin(kingPosition) ) -1 //if i win then win
        else if ( kingPosition!!.first == 2 || kingPosition.first == 6 || kingPosition.second == 2 || kingPosition.second == 6 ) 0 //horizontal or vertical free -> go towards victory
        else if ( whiteCounter > 1 ) 1 //king surrounded by whites -> move white away to get black around
        else if ( shallIMoveWhiteClose(pawnsSurrounding) ) 2 //king feels alone & it's gonna be eat soon -> move white towards king and try to eat some blacks
        else 2 //if it doesnt know what to do, move some white close to the king
    }

    private fun shallIMoveWhiteClose(kingSurrounding: MutableMap<String, State.Pawn>): Boolean {
        var blacks = 0
        kingSurrounding.values.forEach { if ( it == State.Pawn.BLACK ) blacks++ }
        return kingSurrounding.values.all { it != State.Pawn.WHITE } && blacks >= 2
    }
    private fun moveTowardsWinLine(state: State): Double {
        //todo: refactoring needed here!
        var score = 0
        var obstacles = 0
        var pos = 0
        var kingSurpassed = false
        val kingPosition = getKing(state)!!

        if( kingPosition.first == 2 || kingPosition.first == 6 ) {
            //check row
            score = 0
            pos = 0
            obstacles = 0
            kingSurpassed = false
            getRow(kingPosition.first, state).forEach {
                if( it == 'B' || it == 'W' )
                    obstacles++
                if( !kingSurpassed && pos == kingPosition.second  || kingSurpassed && pos == state.board.size - 1 ) {
                    if ( obstacles == 0 )
                        score += 50
                    kingSurpassed = true
                    obstacles = 0
                }
                pos++
            }
        }
        else if( kingPosition.second == 2 || kingPosition.second == 6 ) {
            //check col
            score = 0
            pos = 0
            obstacles = 0
            kingSurpassed = false
            getCol(kingPosition.second, state).forEach {
                if( it == 'B' || it == 'W' )
                    obstacles++
                if( !kingSurpassed && pos == kingPosition.first  || kingSurpassed && pos == state.board.size - 1 ) {
                    if ( obstacles == 0 )
                        score += 50
                    kingSurpassed = true
                    obstacles = 0
                }
                pos++
            }
        }
        return score.toDouble()
    }

    //move white towards king to avoid match lost and try to eat blacks
    private fun moveWhiteCloseToKing(state: State): Double {
        val kingPosition = getKing(state)!!
        var kingEncirclement = 0

        val pawnsSurrounding = getPawnSurrounding(kingPosition, state)

        // buffing weights as king gets alone
        pawnsSurrounding.values.forEach { if ( it == State.Pawn.WHITE ) kingEncirclement += 100 }

        //buffing weights as whites eats blacks around king
        kingEncirclement += getBlackScoreAround(state, getKing(state)!!, 50)
        
        return kingEncirclement.toDouble()
    }

    //move white away to get the king gang banged from blacks
    private fun moveWhiteFarFromKing (state: State): Double {
        val numberOfBlack = state.getNumberOf(State.Pawn.BLACK)
        val kingPosition = getKing(state)!!

        var oppositeSide = 0
        var kingFree = 0

        val pawnsSurrounding = getPawnSurrounding(kingPosition, state)

        // buffing weights as king gets alone
        pawnsSurrounding.values.forEach { if (it == State.Pawn.EMPTY) kingFree += 100 }

        //giving priority to opposite side
        if ( pawnsSurrounding["up"] == State.Pawn.EMPTY && pawnsSurrounding["down"] == State.Pawn.EMPTY ||
                pawnsSurrounding["left"] == State.Pawn.EMPTY && pawnsSurrounding["right"] == State.Pawn.EMPTY )
            oppositeSide = 50

        return (oppositeSide + kingFree - numberOfBlack).toDouble()
    }

    // Utility: return king's row and column
    private fun getKing(state: State): Pair<Int, Int>? {
        state.board.indices.forEach { r ->
            state.board.indices.forEach { c ->
                if ( state.getPawn(r, c) == State.Pawn.KING )
                    return Pair(r, c)
            }
        }
        return null
    }

    // Utility: look the cross around the pawn given, less black around -> score higher
    private fun getBlackScoreAround(state: State, position: Pair<Int, Int>, increaseFactor: Int): Int {
        var pawnEncirclement = 0
        listOf(-1, 1).forEach { r ->
            if ( (position.first + r) in state.board.indices  && state.getPawn(position.first + r, position.second) != State.Pawn.BLACK )
                pawnEncirclement += increaseFactor
        }
        listOf(-1, 1).forEach { c ->
            if ( (position.second + c) in state.board.indices  && state.getPawn(position.first, position.second + c) != State.Pawn.BLACK )
                pawnEncirclement += increaseFactor
        }
        return pawnEncirclement
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
    }

    private fun getPawnSurrounding(pawnPosition: Pair<Int, Int>, state: State): MutableMap<String, State.Pawn> {
        val pawnsSurrounding = mutableMapOf<String, State.Pawn>()
        if ( pawnPosition.first != 0 )
            pawnsSurrounding["up"] = state.getPawn(pawnPosition.first - 1, pawnPosition.second)
        if ( pawnPosition.first != state.board.size - 1 )
            pawnsSurrounding["down"] = state.getPawn(pawnPosition.first + 1, pawnPosition.second)
        if ( pawnPosition.second != 0 )
            pawnsSurrounding["left"] = state.getPawn(pawnPosition.first, pawnPosition.second - 1)
        if ( pawnPosition.second != state.board.size - 1 )
            pawnsSurrounding["right"] = state.getPawn(pawnPosition.first, pawnPosition.second + 1)
        return pawnsSurrounding;
    }

    private fun getCol(col: Int, state: State): String {
        var res = ""
        state.board[col].forEach { res += it }
        return res
    }

    private fun getRow(row: Int, state: State): String {
        var res = ""
        state.board.indices.forEach { res += state.board[row][it] }
        return res
    }

    private fun canWin(kingPosition: Pair<Int, Int>): Boolean {
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
}