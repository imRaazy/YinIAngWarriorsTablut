package it.unibo.ai.didattica.competition.tablut.client.player.aima

import aima.core.search.adversarial.Game
import it.unibo.ai.didattica.competition.tablut.util.Column
import it.unibo.ai.didattica.competition.tablut.domain.Action
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut
import it.unibo.ai.didattica.competition.tablut.domain.State

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
     *  Return all possible actions available from the current state
     *  @param state current state
     *  @return list of possible actions form state p0 or emptyList
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
                        (if (state.turn == State.Turn.WHITE) state.getPawn(r, c) == State.Pawn.KING else true)) {
                    rowMap.getOrPut(r) { mutableSetOf(c) }.add(c)
                    columnMap.getOrPut(c) { mutableSetOf(r) }.add(r)
                }
            }
        }
//        println(rowMap)
//        println(columnMap)
        rowMap.keys.forEach { r ->
            state.board.indices.forEach { new_c ->
                rowMap[r]?.filter { it != new_c }?.forEach { c ->
                    val action = Action("${Column.getCol(c)}${r+1}", "${Column.getCol(new_c)}${r+1}", state.turn)
                    if (isAllowed(state, action))
                        actions.add(action)
                }
            }
        }
        columnMap.keys.forEach { c ->
            state.board.indices.forEach { new_r ->
                columnMap[c]?.filter { it != new_r }?.forEach { r ->
                    val action = Action("${Column.getCol(c)}${r+1}", "${Column.getCol(c)}${new_r+1}", state.turn)
                    if (isAllowed(state, action))
                        actions.add(action)
                }
            }
        }
        return actions
    }
    override fun getInitialState(): State {
        TODO("Not yet implemented")
    }

    override fun getResult(p0: State?, p1: Action?): State {
        TODO("Not yet implemented")
    }

    override fun getPlayer(p0: State?): State.Turn {
        TODO("Not yet implemented")
    }

    override fun getPlayers(): Array<State.Turn> {
        TODO("Not yet implemented")
    }

    override fun getUtility(p0: State?, p1: State.Turn?): Double {
        TODO("Not yet implemented")
    }

    override fun isTerminal(p0: State?): Boolean {
        TODO("Not yet implemented")
    }

}