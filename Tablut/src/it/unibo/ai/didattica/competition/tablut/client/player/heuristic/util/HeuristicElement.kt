package it.unibo.ai.didattica.competition.tablut.client.player.heuristic.util
/**
 * This class represent the heuristic elements, distinguished by
 *  @param name
 *      name of the heuristic element
 *  @param value
 *      numeric value
 *  @param min
 *      general minimum
 *  @param max
 *      general maximum
 *  @param factor
 *      weight of this element for the heuristic evaluation
 */
class HeuristicElement(val name: String, val value: Double, val min: Int, val max: Int, val factor: Double) {}