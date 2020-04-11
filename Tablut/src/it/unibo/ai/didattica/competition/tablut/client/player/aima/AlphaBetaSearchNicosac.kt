package it.unibo.ai.didattica.competition.tablut.client.player.aima

import aima.core.search.adversarial.Game
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch
import it.unibo.ai.didattica.competition.tablut.domain.Action
import it.unibo.ai.didattica.competition.tablut.domain.State
import kotlin.math.absoluteValue




class AlphaBetaSearchNicosac(game: Game<State, Action, State.Turn>?, utilMin: Double, utilMax: Double, time: Int) :
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
        return Double.NEGATIVE_INFINITY
    }

    private fun evalWhite(state: State): Double {
        val numberOfBlackfact = 0.55        //weight of black pawns of the final formila
        val kingisCircled=0.25              //weight of the king pawn being encircled by white pawns
        var kinginAgoodRow=0
        var kinginAgoodColumn=0
        val good_row_cols_factor=0.20    // weight of the king being in a good row or col for the final formula
        var black_up_down=0                // black pawns up and down king position counter
        var black_l_r=0                   // black pawns left and right king position counter
        var kingencirclement=0
        /*val distance_from_escapes_factor=0.10   //weight of king distance from escapes for the final formula
        var distance_from_escapes=0*/
        val numofBlack=state.getNumberOf(State.Pawn.BLACK)
        val kingposition=getKing(state)
        (-2..2).forEach { row ->
            (-2..2).forEach { col ->
                if (kingposition != null) {
                    if ((row in (-1..1)) && (col in (-1..1))) {
                        // check the amount of white pawns surrounding the king position
                        if (state.getPawn(kingposition.first + row, kingposition.second + col)
                                == State.Pawn.WHITE) {
                            kingencirclement += if (row.absoluteValue != col.absoluteValue)
                                15   //more weight if white pawns are not in the diagonal of the king
                            else 5   // less weight id white pawns are on the diagonal of the king
                        }
                        //check if there are clack pawns couples up_down or right left to king
                        // in order to apply the "final strategy"
                        if (state.getPawn(kingposition.first + row, kingposition.second + col)
                                == State.Pawn.BLACK) {// so there are some black pawns
                            if(isKinginCenter(kingposition)) //check if the king is still in the center of the board
                            {
                                if((row.absoluteValue==1)&& (col==0))
                                {   // so black pawns are up or down king's position
                                    kinginAgoodColumn+=2
                                    black_up_down +=1}
                                else if ( (row==0) && ( col.absoluteValue==1)){
                                    // so black pawns are on the left or the right of king's position
                                    kinginAgoodRow+=2
                                    black_l_r+=1
                                }
                            }
                        }
                    }//i'm in the small square surroinding the king
                    if((row.absoluteValue==2) || (col.absoluteValue==2)){ //i'm in the more external rows/colums
                        if (state.getPawn(kingposition.first + row, kingposition.second + col)
                                == State.Pawn.EMPTY) { //check if i can move toward that box
                            if (black_up_down == 2) {
                                // so black pawns are up or down king's position
                                if ((row == 0) && (col.absoluteValue == 2) && isthereAnyBlackCol(Pair(row,col),state))
                                    kinginAgoodColumn+=50
                            } else if (black_l_r == 2) {
                                // so black pawns are left or right king's position
                                if ((col==0) && (row.absoluteValue == 1) && isthereAnyBlackRaw(Pair(row,col),state))
                                    kinginAgoodRow+=50
                            }
                        }// empty box check
                    } //i'm in the external edge of the bigger square
                    if(black_up_down==2)
                        kinginAgoodColumn+=5 // the king can move toward a more winning column
                    else if ( black_l_r==2)
                        kinginAgoodRow+=5   // the king can move toward a more winning row
                }//il re è in una posizione regolare
            }//iterating on columns
        }//iterating on rows
        print("numero di pedine nere: $numofBlack\t accerchiamento di bianchi:$kingencirclement\t")
        return numberOfBlackfact*(numofBlack)+kingisCircled*kingencirclement+
                kinginAgoodColumn*(good_row_cols_factor/2)+kinginAgoodRow*(good_row_cols_factor/2)
        // the weigh sum of all the factor described above:
        // number of black pawns on boards : 45 %
        // number of white pawns surrounding the king : 35%
        // king  possibility to go to a better row or column : 20%
        // tipically the last one will appear in the latest phase of the game
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
    private fun isKinginCenter( posizioneRe : Pair<Int,Int>? ) :Boolean {
        var res : Boolean = false
        if (posizioneRe != null) {
            if((posizioneRe.first==5) && (posizioneRe.second==5)) // il re è al centro
                res=true // quindi torno valore vero
        }else
            res=false
        return res
    }

    //function to evaluate if there are black pawns in a specified row

    private fun isthereAnyBlackRaw (  posizione: Pair<Int,Int>?, state : State) : Boolean {
        var res: Boolean = false
        if (posizione!=null){
            for (i in (0..9)){ // righe
                for (j in (0..9)){ // colonne
                    if ( i == posizione.first) {
                        if(state.getPawn(i,j) == State.Pawn.BLACK)
                            res=true
                    }
                }//col
            }//row
        } //external
        return res;
    }

    //function to evaluate if there are black pawns in a specified column
    //return true if there are some black pawns, false otherwise
    private fun isthereAnyBlackCol ( posizione: Pair<Int,Int>? , state : State ) : Boolean {
        var res: Boolean = false
        if (posizione!=null){
            for (i in (0..9)){ // righe
                for (j in (0..9)){ // colonne
                    if ( j == posizione.second) {
                        if(state.getPawn(i,j) == State.Pawn.BLACK)
                            res=true
                    }
                }//col
            }//row
        } //external
        return res;
    }







}