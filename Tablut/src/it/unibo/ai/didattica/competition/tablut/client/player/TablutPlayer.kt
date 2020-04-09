package it.unibo.ai.didattica.competition.tablut.client.player

import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch
import it.unibo.ai.didattica.competition.tablut.client.TablutClient
import it.unibo.ai.didattica.competition.tablut.client.player.aima.AlphaBetaSearch
import it.unibo.ai.didattica.competition.tablut.client.player.aima.PlayerGame
import it.unibo.ai.didattica.competition.tablut.domain.*

class TablutPlayer(private val role: String?, name: String?, val timeout: Int, ipAddress: String?): TablutClient(role, name, timeout, ipAddress) {
    /**
     * Constructors
     */
    constructor(role: String?, name: String?, timeout: Int): this(role, name, timeout, "localhost")
    constructor(role: String?, name: String?, ipAddress: String?): this(role, name, 60, ipAddress)
    constructor(role: String?, name: String?): this(role, name, 60, "localhost")

    override fun run() {
        val initialState: State
        val game: PlayerGame
        val search: IterativeDeepeningAlphaBetaSearch<State, Action, State.Turn>
        var state: State
        println("You are player $role!")
        // Setting up the initial state and player turn
        initialState = StateTablut()
        initialState.turn = role?.let { State.Turn.valueOf(it.toUpperCase()) }
        // Setting up the game model
        game = PlayerGame(
                initialState,
                99,
                0,
                "garbage",
                "placeholder",
                "placeholder"
        )
        // Setting up the search strategy
        search = AlphaBetaSearch(game, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, timeout)
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
                println("Waiting for the opposite move...")
            }
        }
    }

    private fun <R> executeAndMeasureTimeSeconds(block: () -> R): Pair<R, Double> {
        val start = System.currentTimeMillis()
        val result = block()
        return result to ((System.currentTimeMillis() - start) / 1000.0)
    }
}