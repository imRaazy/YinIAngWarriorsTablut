package it.unibo.ai.didattica.competition.tablut.client.player.aima

import aima.core.search.adversarial.Game
import it.unibo.ai.didattica.competition.tablut.domain.Action
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut
import it.unibo.ai.didattica.competition.tablut.domain.State
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut

class PlayerGame(state: State?, repeated_moves_allowed: Int, cache_size: Int, logs_folder: String?, whiteName: String?, blackName: String?):
        GameAshtonTablut(state, repeated_moves_allowed, cache_size, logs_folder, whiteName, blackName), Game<State, Action, State.Turn> {
    /**
     * Get initial game state
     * @return initial game state
     */
    override fun getInitialState(): State {
        return StateTablut()
    }
    /**
     * A terminal test, which is true when the game is over and false TERMINAL STATES otherwise.
     * States where the game has ended are called terminal states
     * @param state
     *          given state
     * @return true if the state is terminal or false vice versa
     */
    override fun isTerminal(state: State?): Boolean {
        return state?.turn == State.Turn.BLACKWIN || state?.turn == State.Turn.WHITEWIN ||
               state?.turn == State.Turn.DRAW
    }
    /**
     * Defines which player has the move in a state
     * @param state
     *          given state
     * @return player
     */
    override fun getPlayer(state: State?): State.Turn {
        return state?.turn!!
    }
    /**
     * Get players
     * @return players
     */
    override fun getPlayers(): Array<State.Turn> {
        return arrayOf(State.Turn.BLACK, State.Turn.WHITE)
    }
    /**
     * The transition model, which defines the result of a move
     * @param state
     *          given state
     * @param action
     *          given and allowed action
     * @return
     *      updated state
     */
    override fun getResult(state: State?, action: Action?): State {
        if (state != null && action != null)
            return movePawn(state.clone(), action)
        return initialState
    }
    /**
     * A utility function (also called an objective function or payoff function),
     * defines the final numeric value for a game that ends in terminal state s for a player p
     * @param state
     *          given state
     * @param turn
     *          player role
     * @return evaluation
     */
    override fun getUtility(state: State?, turn: State.Turn?): Double {
        return  if (state !is State || turn !is State.Turn)
                    Double.NEGATIVE_INFINITY
                else when {
                            (turn == State.Turn.BLACK && state.turn == State.Turn.BLACKWIN) ||
                            (turn == State.Turn.WHITE && state.turn == State.Turn.WHITEWIN) -> Double.POSITIVE_INFINITY
                            (turn == State.Turn.BLACK && state.turn == State.Turn.WHITEWIN) ||
                            (turn == State.Turn.WHITE && state.turn == State.Turn.BLACKWIN) -> Double.NEGATIVE_INFINITY
                            else -> 0.5
                        }
    }
    /**
     *  Returns the set of legal moves in a state
     *  @param state
     *      given state
     *  @return list of possible actions form state or emptyList
     */
    override fun getActions(state: State?): MutableList<Action> {
        val actions = mutableListOf<Action>()
        val columnMap = mutableMapOf<Int, MutableSet<Int>>()
        val rowMap = mutableMapOf<Int, MutableSet<Int>>()
        if (state !is State)
            return actions
        if (state.turn == State.Turn.WHITEWIN || state.turn == State.Turn.BLACKWIN)
            return actions
        state.board.indices.forEach { r ->
            state.board.indices.forEach { c ->
                if (state.getPawn(r, c).equalsPawn(state.turn.toString()) ||
                        (if (state.turn == State.Turn.WHITE) state.getPawn(r, c) == Pawn.KING else true)) {
                    rowMap.getOrPut(r) { mutableSetOf(c) }.add(c)
                    columnMap.getOrPut(c) { mutableSetOf(r) }.add(r)
                }
            }
        }
        rowMap.keys.forEach { r ->
            state.board.indices.forEach { new_c ->
                rowMap[r]?.filter { it != new_c }?.forEach { c ->
                    val action = Action(state.getBox(r, c), state.getBox(r, new_c), state.turn)
                    if (isAllowed(state, action))
                        actions.add(action)
                }
            }
        }
        columnMap.keys.forEach { c ->
            state.board.indices.forEach { new_r ->
                columnMap[c]?.filter { it != new_r }?.forEach { r ->
                    val action = Action(state.getBox(r, c), state.getBox(new_r, c), state.turn)
                    if (isAllowed(state, action))
                        actions.add(action)
                }
            }
        }
        return actions
    }
    private fun movePawn(state: State, a: Action): State {
        val pawn = state.getPawn(a.rowFrom, a.columnFrom)
        val newBoard = state.board
        if (a.columnFrom == 4 && a.rowFrom == 4) {
            newBoard[a.rowFrom][a.columnFrom] = Pawn.THRONE
        } else {
            newBoard[a.rowFrom][a.columnFrom] = Pawn.EMPTY
        }
        newBoard[a.rowTo][a.columnTo] = pawn
        state.board = newBoard
        if (state.turn.equalsTurn(State.Turn.WHITE.toString())) {
            state.turn = State.Turn.BLACK
        } else {
            state.turn = State.Turn.WHITE
        }
        return when {
            state.turn.equalsTurn("W") -> checkCaptureBlack(state, a, false)
            state.turn.equalsTurn("B") -> checkCaptureWhite(state, a, false)
            else -> state
        }
    }
}