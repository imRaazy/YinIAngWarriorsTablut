package it.unibo.ai.didattica.competition.tablut.client.player

import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch
import it.unibo.ai.didattica.competition.tablut.client.TablutClient
import it.unibo.ai.didattica.competition.tablut.client.player.aima.AlphaBetaSearch
import it.unibo.ai.didattica.competition.tablut.client.player.aima.PlayerGame
import it.unibo.ai.didattica.competition.tablut.domain.*
import kotlin.system.exitProcess

class TablutPlayer(private val role: String?, name: String?, val timeout: Int, ipAddress: String?): TablutClient(role, name, timeout, ipAddress) {
    constructor(role: String?, name: String?, timeout: Int): this(role, name, timeout, "localhost")
    constructor(role: String?, name: String?): this(role, name, 58, "localhost")

    override fun run() {
        val initialState: State
        val game: PlayerGame
        val search: IterativeDeepeningAlphaBetaSearch<State, Action, State.Turn>
        var state: State
        println("You are player $role!")
        initialState = StateTablut()
        initialState.turn = role?.let { State.Turn.valueOf(it.toUpperCase()) }
        game = PlayerGame(
                initialState,
                99,
                0,
                "garbage",
                "placeholder",
                "placeholder"
        )
        search = AlphaBetaSearch(game, 0.0, 1.0, timeout)
        search.setLogEnabled(true)
        declareName()
        while (true) {
            read()
            state = currentState
            println("Current state:\n$state")
            if (state.turn == player) {
                val (action, duration) = executeAndMeasureTimeSeconds { search.makeDecision(state) }
                println("Chosen move = $action\nTakes: $duration seconds")
                write(action)
            } else {
                when (state.turn) {
                    State.Turn.WHITEWIN -> {
                        if (player == State.Turn.WHITE) println("YOU WIN")
                        else if (player == State.Turn.BLACK) println("YOU LOOSE")
                        exitProcess(0)
                    }
                    State.Turn.BLACKWIN -> {
                        if (player == State.Turn.BLACK) println("YOU WIN")
                        else if (player == State.Turn.WHITE) println("YOU LOOSE")
                        exitProcess(0)
                    }
                    State.Turn.DRAW -> {
                        println("DRAW")
                        exitProcess(0)
                    }
                    else -> println("Waiting for the opposite move...")
                }
            }
        }
    }

    /**
     * Measure the time required by a function
     * @param block
     *      function to be measured
     * @return time required
     */
    private fun <R> executeAndMeasureTimeSeconds(block: () -> R): Pair<R, Double> {
        val start = System.currentTimeMillis()
        val result = block()
        return result to ((System.currentTimeMillis() - start) / 1000.0)
    }
}