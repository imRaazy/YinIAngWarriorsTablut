package it.unibo.ai.didattica.competition.tablut.client.player.aima

import aima.core.search.adversarial.Game
import it.unibo.ai.didattica.competition.tablut.domain.Action
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut
import it.unibo.ai.didattica.competition.tablut.domain.State
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut

/*  Game interface implemented by  GameAshtonTablut is different
    form the Game interface implemented by PlayerGame: the first
    is a Tablut domain interface when the second belongs to aima */
class PlayerGame: GameAshtonTablut, Game<State, Action, State.Turn> {
    constructor(state: State?, repeated_moves_allowed: Int, cache_size: Int, logs_folder: String?, whiteName: String?, blackName: String?) :
            super(state, repeated_moves_allowed, cache_size, logs_folder, whiteName, blackName)
    constructor(repeated_moves_allowed: Int, cache_size: Int, logs_folder: String?, whiteName: String?, blackName: String?):
            super(repeated_moves_allowed, cache_size, logs_folder, whiteName, blackName)

    // Aima Game methods
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
        if (state !is State || turn !is State.Turn)
            return -1.0
        return 1.0
    }
    /**
     *  Returns the set of legal moves in a state
     *  @param state
     *      given state
     *  @return list of possible actions form state or emptyList
     */
    override fun getActions(state: State?): MutableList<Action> {
        val actions = mutableListOf<Action>()
        // ColumnMap and RowMap are redundant but thanks to them
        // is possible to reduce the complexity of the computation
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
        //println(actions)
        return actions
    }
    private fun movePawn(state: State, a: Action): State {
        val pawn = state.getPawn(a.rowFrom, a.columnFrom)
        val newBoard = state.board
        // libero il trono o una casella qualunque
        if (a.columnFrom == 4 && a.rowFrom == 4) {
            newBoard[a.rowFrom][a.columnFrom] = Pawn.THRONE
        } else {
            newBoard[a.rowFrom][a.columnFrom] = Pawn.EMPTY
        }

        // metto nel nuovo tabellone la pedina mossa
        newBoard[a.rowTo][a.columnTo] = pawn
        // aggiorno il tabellone
        state.board = newBoard
        // cambio il turno
        if (state.turn.equalsTurn(State.Turn.WHITE.toString())) {
            state.turn = State.Turn.BLACK
        } else {
            state.turn = State.Turn.WHITE
        }

        // a questo punto controllo lo stato per eventuali catture
        return when {
            state.turn.equalsTurn("W") -> checkCaptureBlack(state, a)
            state.turn.equalsTurn("B") -> checkCaptureWhite(state, a)
            else -> state
        }
    }
}