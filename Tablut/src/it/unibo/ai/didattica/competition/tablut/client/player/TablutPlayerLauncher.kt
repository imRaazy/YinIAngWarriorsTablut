package it.unibo.ai.didattica.competition.tablut.client.player

fun main(args: Array<String>) {
    val role = args.getOrElse(0) { print("Insert player role: "); readLine()!!}
    val player = if(role == "white")
        TablutPlayer(role, "YinIAngWarriors", 57)
    else
        TablutPlayer(role, "YinIAngWarriors", 57)
    player.run()
}