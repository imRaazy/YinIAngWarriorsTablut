package it.unibo.ai.didattica.competition.tablut.client.player

fun main(args: Array<String>) {
    val role = args.getOrElse(0) { print("Insert player role: "); readLine()!!}
    val player = TablutPlayer(role, "player")
    player.run()
}