package it.unibo.ai.didattica.competition.tablut.client.player

fun main(args: Array<String>) {
    val role = args.getOrElse(0) { print("Insert player role: "); readLine()!!}
    val player = if(role == "white")
        TablutPlayer(role, "playerWHITE", 58)
    else
        TablutPlayer(role, "playerBLACK", 58)
    player.run()
}