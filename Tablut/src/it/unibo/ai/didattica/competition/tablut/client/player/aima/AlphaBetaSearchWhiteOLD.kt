package it.unibo.ai.didattica.competition.tablut.client.player.aima

import aima.core.search.adversarial.Game
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch
import it.unibo.ai.didattica.competition.tablut.domain.Action
import it.unibo.ai.didattica.competition.tablut.domain.State

class AlphaBetaSearchWhiteOLD(game: Game<State, Action, State.Turn>?, utilMin: Double, utilMax: Double, time: Int) :
        IterativeDeepeningAlphaBetaSearch<State, Action, State.Turn>(game, utilMin, utilMax, time) {
    /**
     * OLD FILE FOR BACKUP NOT FOR USE
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
        val numberOfWhiteFactor = 0.3
        val numberOfBlackFactor = 0.3
        val kingEncirclementFactor = 0.4
        val whiteEatingFactor = 0.3
        // NumberOfPawns
        val numberOfBlack = state.getNumberOf(State.Pawn.BLACK)
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

    //trying to move white paws away from king and close to black pawns
    private fun evalWhite(state: State): Double {
        /*println("\nCOLONNA")
        println(getCol(4, state))
        println("\nRIGA")
        println(getRow(4, state))*/

        //check if i can win
        if(state.turn == State.Turn.WHITEWIN) {
            return Double.POSITIVE_INFINITY
        }
        return when (getNextMove(state)) {
            0 -> moveTowardsWinLine(state)  //horizontal or vertical free -> todo: go towards victory
            1 -> moveWhiteFarFromKing(state) //king surrounded by whites -> move white away to get the king gang banged from blacks
            2 -> moveWhiteCloseToKing(state) //king feels alone & it's gonna be eat soon -> move white towards king
            3 -> eatBlacks(state) //three black around king -> eat them with some white to free the king
            else -> Double.NEGATIVE_INFINITY
        }
    }

    private fun getNextMove(state: State): Int {
        val kingPosition = getKing(state)
        val pawnsSurrounding = mutableMapOf<String, State.Pawn>()
        var whiteCounter = 0
        var blackCounter = 0
        var emptyCounter = 0

        if (kingPosition != null) {
            if (kingPosition.first != 0)
                pawnsSurrounding["up"] = state.getPawn(kingPosition.first - 1, kingPosition.second)
            if (kingPosition.first != state.board.size-1)
                pawnsSurrounding["down"] = state.getPawn(kingPosition.first + 1, kingPosition.second)
            if (kingPosition.second != 0)
                pawnsSurrounding["left"] = state.getPawn(kingPosition.first, kingPosition.second - 1)
            if (kingPosition.second != state.board.size-1)
                pawnsSurrounding["right"] = state.getPawn(kingPosition.first, kingPosition.second + 1)
        }

        pawnsSurrounding.values.forEach { if ( it == State.Pawn.WHITE ) whiteCounter++ else if ( it == State.Pawn.BLACK ) blackCounter++ else emptyCounter++ }
        //pawnsSurrounding.values.forEach { if ( it == State.Pawn.WHITE ) whiteCounter++ else blackCounter++ }

        return if( pawnsSurrounding["up"] == State.Pawn.EMPTY && pawnsSurrounding["down"] == State.Pawn.EMPTY ||
                pawnsSurrounding["left"] == State.Pawn.EMPTY && pawnsSurrounding["right"] == State.Pawn.EMPTY ) 0  //horizontal or vertical free -> go towards victory
        else if ( whiteCounter > 0 ) 1 //king surrounded by whites -> move white away to get black around
        else if( pawnsSurrounding.values.all { it != State.Pawn.WHITE } ) 2 //king feels alone & it's gonna be eat soon -> move white towards king
        else 3 //three black around king -> eat them with some white to free the king
    }

    private fun moveTowardsWinLine(state: State): Double {
        var score = 0
        val kingPosition = getKing(state)
        if(kingPosition != null)
            if( kingPosition.first == 2 || kingPosition.first == 6 ) {
                //check row
                getRow(kingPosition.first, state).forEach {
                    var pos = 0
                    var obstacles = 0
                    if(it == 'B' || it == 'W')
                        obstacles++
                    if(pos == kingPosition.second || pos == state.board.size-1) {
                        if ( obstacles == 0 )
                            score += 50
                        obstacles = 0
                    }
                    pos++
                }
            }
            else if( kingPosition.second == 2 || kingPosition.second == 6 ) {
                //check col
                getCol(kingPosition.first, state).forEach {
                    var pos = 0
                    var obstacles = 0
                    if(it == 'B' || it == 'W')
                        obstacles++
                    if(pos == kingPosition.first || pos == state.board.size-1) {
                        if ( obstacles == 0 )
                            score += 50
                        obstacles = 0
                    }
                    pos++
                }
            }
        return 0.toDouble()
    }

    //eat some black pawns to free the king
    private fun eatBlacks (state: State): Double {
        //todo: move in order to eat on the next move
        var score = 0;
        var white = 0
        /*state.board.indices.forEach { r ->
            var row = getRow(r, state)
            if(row.trim(State.Pawn.EMPTY.toString()[0]).matches(Regex.fromLiteral("WBW"))) {
                score += 10
                white += countWhiteSpaces(row)
            }
            var col = getCol(r, state)
            if(col.trim(State.Pawn.EMPTY.toString()[0]).matches(Regex.fromLiteral("WBW"))) {
                score += 10
                white += countWhiteSpaces(col)
            }
        }*/
        return score + getPawnEncirclement(state, getKing(state)!!, 50) + 1/white
    }

    //move white towards king to avoid match lost
    private fun moveWhiteCloseToKing(state: State): Double {
        val kingEncirclementFactor = 0.65
        val numberOfBlack = state.getNumberOf(State.Pawn.BLACK)
        val kingPosition = getKing(state)
        var kingEncirclement = 0

        // buffing weights as king gets alone
        if (kingPosition != null) {
            if (kingPosition.first != 0) {
                kingEncirclement += if (state.getPawn(kingPosition.first-1, kingPosition.second) == State.Pawn.WHITE) 100 else 0
            }
            if (kingPosition.second != 0) {
                kingEncirclement += if (state.getPawn(kingPosition.first, kingPosition.second-1) == State.Pawn.WHITE) 100 else 0
            };
            if (kingPosition.first != state.board.size-1) {
                kingEncirclement += if (state.getPawn(kingPosition.first+1, kingPosition.second) == State.Pawn.WHITE) 100 else 0
            };
            if (kingPosition.second != state.board.size-1) {
                kingEncirclement += if (state.getPawn(kingPosition.first, kingPosition.second+1) == State.Pawn.WHITE) 100 else 0
            };
        };

        //print("white numb: $numberOfWhite\t black numb: $numberOfBlack\t\n")
        //println("node weight: " + (kingEncirclementFactor*kingEncirclement - numberOfBlack))
        return kingEncirclementFactor * kingEncirclement - numberOfBlack
    }

    // move white away to get the king gang banged from blacks
    private fun moveWhiteFarFromKing (state: State): Double {
        val kingFreeFactor = 0.65
        val buffBlackFactor = 0.35
        val numberOfBlack = state.getNumberOf(State.Pawn.BLACK)
        val kingPosition = getKing(state)

        var buffBlack = 0
        var kingFree = 0
        var buffSize = 0

        // buffing weights as king gets alone
        if (kingPosition != null) {
            if (kingPosition.first != 0) {
                kingFree += if (state.getPawn(kingPosition.first-1, kingPosition.second) == State.Pawn.EMPTY) 100 else 0
            }
            if (kingPosition.second != 0) {
                kingFree += if (state.getPawn(kingPosition.first, kingPosition.second-1) == State.Pawn.EMPTY) 100 else 0
            };
            if (kingPosition.first != state.board.size-1) {
                kingFree += if (state.getPawn(kingPosition.first+1, kingPosition.second) == State.Pawn.EMPTY) 100 else 0
            };
            if (kingPosition.second != state.board.size-1) {
                kingFree += if (state.getPawn(kingPosition.first, kingPosition.second+1) == State.Pawn.EMPTY) 100 else 0
            };
        };

        //buffing weights as whites paws moves close to blacks
        state.board.indices.forEach { r ->
            state.board.indices.forEach { c ->
                if (state.getPawn(r, c) == State.Pawn.WHITE) {
                    if (r != 0) {
                        buffBlack += if (state.getPawn(r - 1, c) == State.Pawn.BLACK) 10 else 0
                    };
                    if (c != 0) {
                        buffBlack += if (state.getPawn(r, c - 1) == State.Pawn.BLACK) 10 else 0
                    };
                    if (r != state.board.size-1) {
                        buffBlack += if (state.getPawn(r + 1, c) == State.Pawn.BLACK) 10 else 0
                    };
                    if (c != state.board.size-1) {
                        buffBlack += if (state.getPawn(r, c + 1) == State.Pawn.BLACK) 10 else 0
                    };
                }
            }
        }

        /*//giving priority to the opposite side of king
            if (kingPosition != null &&
                    ((state.getPawn(kingPosition.first - 1, kingPosition.second) != State.Pawn.WHITE &&
                            state.getPawn(kingPosition.first + 1, kingPosition.second) != State.Pawn.WHITE ) ||
                            (    state.getPawn(kingPosition.first, kingPosition.second - 1) != State.Pawn.WHITE &&
                                    state.getPawn(kingPosition.first + 1, kingPosition.second + 1) != State.Pawn.WHITE )))
            buffSize = 50
*/
        //print("white numb: $numberOfWhite\t black numb: $numberOfBlack\t\n")
        //println("node weight: " + (kingFree*kingFreeFactor + pawnBlackBuff*pawnBlackFactor - numberOfBlack))
        return kingFree * kingFreeFactor + buffBlack * buffBlackFactor - numberOfBlack + buffSize
    }

    // Utility: return king's row and column
    private fun getKing(state: State): Pair<Int, Int>? {
        state.board.indices.forEach { r ->
            state.board.indices.forEach { c ->
                if (state.getPawn(r, c) == State.Pawn.KING)
                    return Pair(r, c)
            }
        }
        return null
    }

    // Utility: look the cross around the pawn given
    private fun getPawnEncirclement(state: State, position: Pair<Int, Int>, increaseFactor: Int): Double {
        var pawnEncirclement = 0
        listOf(-1, 1).forEach { r ->
            if ((position.first + r) in state.board.indices  && state.getPawn(position.first + r, position.second) != State.Pawn.BLACK)
                pawnEncirclement += increaseFactor
        }
        listOf(-1, 1).forEach { c ->
            if ((position.second + c) in state.board.indices  && state.getPawn(position.first, position.second + c) != State.Pawn.BLACK)
                pawnEncirclement += increaseFactor
        }
        return pawnEncirclement.toDouble()
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

    private fun countWhiteSpaces(lineOrig: String) : Int {
        var line = lineOrig
        if(line.contains(("WB")))
            line = reverseLine(line)
        line = line.substringBefore("BW")
        while(line.contains("W")) { line = line.substringAfter("W") }
        return line.length
    }

    private fun reverseLine (line: String) : String {
        var reversed = ""
        for(i in 9 downTo 0) reversed += line[i]
        return reversed
    }

    private fun getPawnEncirclementBlack(state: State, position: Pair<Int, Int>, increaseFactor: Int): Int {
        var kingEncirclement = 0
        listOf(-1, 1).forEach { r ->
            if ((position.first + r) in state.board.indices && state.getPawn(position.first + r, position.second) == State.Pawn.BLACK)
                kingEncirclement += increaseFactor
        }
        listOf(-1, 1).forEach { c ->
            if ((position.second + c) in state.board.indices && state.getPawn(position.first, position.second + c) == State.Pawn.BLACK)
                kingEncirclement += increaseFactor
        }
        return kingEncirclement
    }

    // Unused: maybe useful in the future?
    // areKingAdiacentBoxFreeFromPawns?
    // return 0 if there are no FreeFrom pawns surrounding the king
    // return 1 if there are no FreeFrom pawns adiacent to the king in the same row
    // return 2 if there are no FreeFrom pawns adiacent to the king in the same column
    // return 3 if free from FreeFrom pawns on each 4 side boxes
    private fun areKingAdiacentBoxFreeFromPawns(state: State, freeFrom: State.Pawn): Int {
        val kingPosition = getKing(state)
        var freeUp = false;
        var freeDown = false;
        var freeLeft = false;
        var freeRight = false;

        if (kingPosition != null) {
            if (kingPosition.first != 0) {
                freeUp = state.getPawn(kingPosition.first-1, kingPosition.second) != freeFrom
            };
            if (kingPosition.first != state.board.size-1) {
                freeDown = state.getPawn(kingPosition.first+1, kingPosition.second) != freeFrom
            };
            if (kingPosition.second != 0) {
                freeLeft = state.getPawn(kingPosition.first, kingPosition.second-1) != freeFrom
            };
            if (kingPosition.second != state.board.size-1) {
                freeRight = state.getPawn(kingPosition.first, kingPosition.second+1) != freeFrom
            };
        };

        return if(freeLeft && freeRight) 1
        else if(freeUp && freeDown) 2
        else if(freeUp && freeDown && freeLeft && freeRight) 3
        else 0
    }
}