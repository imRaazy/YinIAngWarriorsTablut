package it.unibo.ai.didattica.competition.tablut.client.player

fun main(args: Array<String>) {
    var address = "localhost"
    var timeout = 57
    var role = ""
    if(args.size != 6) {
        println("Bad arguments\nUSAGE: java -jar <jarName>.jar -p <playerRole> -h <ipAddress> -t <timeout>")
        return
    }
    (0..4 step 2).forEach {
        when {
            args[it] == "-h" -> address = args[it+1].trim()
            args[it] == "-p" -> role = args[it+1].trim()
            args[it] == "-t" -> timeout = args[it+1].toInt()
            else -> { println("USAGE: java -jar <jarName>.jar -p <playerRole> -h <ipAddress> -t <timeout>"); return }
        }
    }
    if(role != "white" && role != "black")
        println("Bad player role given")
    else if(timeout !in 0..60)
        println("Bad timeout given, range 0<=Timeout<=60")
    else {
        TablutPlayer(role, "YinIAngWarriors", timeout, address).run()
    }
}