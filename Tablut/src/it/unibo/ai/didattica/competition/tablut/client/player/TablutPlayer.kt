package it.unibo.ai.didattica.competition.tablut.client.player

import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch
import it.unibo.ai.didattica.competition.tablut.client.TablutClient
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
//        val search: IterativeDeepeningAlphaBetaSearch<State, Action, State.Turn>
        println("You are player $role!")
        // Setting up the initial state and player turn
        initialState = StateTablut()
        initialState.turn = role?.let { State.Turn.valueOf(it.toUpperCase()) }
        println("Current state:\n${initialState}")
        // Setting up the game model
        game = PlayerGame(initialState,99, 0, "garbage", "placeholder", "placeholder")
        game.getActions(initialState).forEach { println(it) }
        // Setting up the search strategy
//        search = IterativeDeepeningAlphaBetaSearch(game, Double.MIN_VALUE, Double.MAX_VALUE, timeout)
//        declareName()
//        while (true) {
//            read()
//            println("Current state:\n$currentState")
//        }
    }

}