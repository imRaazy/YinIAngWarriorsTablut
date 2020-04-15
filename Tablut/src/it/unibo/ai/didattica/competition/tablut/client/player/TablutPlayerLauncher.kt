package it.unibo.ai.didattica.competition.tablut.client.player

fun main(args: Array<String>) {
    val role = args.getOrElse(0) { print("Insert player role: "); readLine()!!}
    val player = if(role == "white")
        TablutPlayer(role, "playerWHITE", 59)
    else
        TablutPlayer(role, "playerBLACK", 59)
    player.run()
}