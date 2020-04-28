package it.unibo.ai.didattica.competition.tablut.client.player.heuristic

import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicElement
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.checkWhiteGoodLineObstacles
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.checkWhiteWinLineObstacles
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.getCol
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.getKing
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.getRow
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.normalizeValue
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.pawnToPawnManhattanDistance
import it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util.HeuristicUtil.Companion.weightedAverage
import it.unibo.ai.didattica.competition.tablut.domain.State
import it.unibo.ai.didattica.competition.tablut.util.BoardBox
import it.unibo.ai.didattica.competition.tablut.util.Zone

class BlackHeuristic {
    companion object {
        //black heuristic function based on: KingPos, KingEncirc, ManhattanFromBlackPawnToKing, PawnDifference, NumOFWhitePawns
        fun bestBlackEval(state: State): Double {
            val heuristicInfluenceElement = mutableListOf<HeuristicElement>()
            var numberOfBlack = 0 //MAX: 16, MIN: 0
            var numberOfWhite = 0
            var kingEncirclement = 0 //MAX: 4, MIN: 0
            var manhattanDistance = 208 //MAX: 208, MIN: 0
            val kingPosition = getKing(state)!!

            //checking the board status
            state.board.indices.forEach { r ->
                state.board.indices.forEach { c ->
                    if (state.getPawn(r, c) == State.Pawn.KING) {
                        kingEncirclement += HeuristicUtil.getPawnEncirclement(state, Pair(r, c)) { it == State.Pawn.BLACK || it == State.Pawn.THRONE } //king enciclement
                    }
                    if (state.getPawn(r, c) == State.Pawn.WHITE) numberOfWhite++ //number of white
                    if (state.getPawn(r, c) == State.Pawn.BLACK) {
                        numberOfBlack++ //number of black
                        manhattanDistance -= pawnToPawnManhattanDistance(kingPosition, Pair(r, c)) //total manhattandistance
                    }
                }
            }
            val kingRow = getRow(kingPosition.first, state)
            val kingCol = getCol(kingPosition.second, state)
            val kingZone = Zone.getZone(kingPosition)

            heuristicInfluenceElement.add(HeuristicElement("KingPositioning", evaluateKingPosition(kingPosition, kingRow, kingCol).toDouble(), -12, 22, 0.2))
            heuristicInfluenceElement.add(HeuristicElement("ManhattanDistance", manhattanDistance.toDouble(), 0, 208, 0.6))
            heuristicInfluenceElement.add(HeuristicElement("KingEncirclement", kingEncirclement.toDouble(), 0, 4, 1.5))
            heuristicInfluenceElement.add(HeuristicElement("PawnDifference", numberOfBlack.toDouble()/(numberOfBlack+2*numberOfWhite), 0, 1, 2.0))
            if (kingZone != Zone.NONE)
                heuristicInfluenceElement.add(HeuristicElement("ZoneWayOut", evaluateZoneWayOut(kingZone, state), 0, 4, 1.5))

            return  when {
                        whiteWin(kingPosition, kingRow, kingCol, state.turn) -> Double.NEGATIVE_INFINITY
                        else -> weightedAverage(heuristicInfluenceElement.map { Pair(normalizeValue(it.value, it.min, it.max), it.factor) })
                    }
        }
        /* BLACK MIN-MAX:
        * assuming lines 4 as worst lines and lines 2-6 as best
        * the worst gives me +3 (4 citadels and 1 throne)
        * the best gives me -2 (2 escapes)
        * assuming the best case scenario as all blacks in a line they give me +8 (+1 each)
        * assuming the worst case scenario as all whites in a line they give me -8 (-1 each)
        * MIN: since we have only 8 whites the min value can be found when the king is in a winning row and col
        * at the same time with one line full of whites and one line empty
        * this means: - 2 - 2 - 8 = -12
        * MAX: since we have 16 blacks the max value can be found when the king is on throne
        * and is surrounded by all black in each line (row and col)
        * this means: + 3 + 3 + 8 + 8 = +22
        */
        private fun evaluateKingPosition(kingPosition: Pair<Int, Int>, kingRow: String, kingCol: String): Int {
            return getBlackLineScore(kingRow, kingPosition.first) + getBlackLineScore(kingCol, kingPosition.second)
        }

        //BLACK: citadels +1, throne -1, black +1, white -1, escapes -1, empty 0
        private fun getBlackLineScore(line: String, boardLineIndex: Int): Int {
            var score = 0
            var i = 0
            line.forEach { l ->
                if(l != 'K') {
                    if (Pair(boardLineIndex, i) in BoardBox.CITADEL.boxes) score++
                    if (Pair(boardLineIndex, i) in BoardBox.THRONE.boxes) score++
                    if (Pair(boardLineIndex, i) in BoardBox.ESCAPE.boxes) score--
                    if (l == 'B') score++
                    if (l == 'W') score--
                }
                i++
            }
            return score
        }

        private fun whiteWin(kingPosition: Pair<Int, Int>, kingRow: String, kingCol: String, turn: State.Turn): Boolean {
            return  (turn == State.Turn.BLACK && (checkWhiteWinLineObstacles(kingRow, kingPosition.first) == 2 || checkWhiteWinLineObstacles(kingCol, kingPosition.second) == 2)) ||
                    (turn == State.Turn.BLACK && (checkWhiteGoodLineObstacles(kingRow, kingPosition.first) == 1 && checkWhiteGoodLineObstacles(kingCol, kingPosition.second) == 1)) ||
                    (turn == State.Turn.WHITE && (checkWhiteWinLineObstacles(kingRow, kingPosition.first) + checkWhiteWinLineObstacles(kingCol, kingPosition.second) > 0)) ||
                    (turn == State.Turn.WHITE && (checkWhiteGoodLineObstacles(kingRow, kingPosition.first) + checkWhiteGoodLineObstacles(kingCol, kingPosition.second) > 0))
        }
        
        private fun evaluateZoneWayOut(kingZone: Zone, state: State): Double {
            var res = 0.0
            val wayOut = BoardBox.getPairedWayOut(kingZone)
            wayOut.forEach {
                if (state.getPawn(it.first.first, it.first.second) == State.Pawn.BLACK) {
                    res++
                    if (state.getPawn(it.second.first, it.second.second) != State.Pawn.EMPTY) res -= 0.5
                }
            }
            return res
        }
    }
}