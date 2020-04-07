package it.unibo.ai.didattica.competition.tablut.util

enum class Column(val num: Int) {
    A(0), B(1), C(2), D(3), E(4),
    F(5), G(6), H(7), I(8);
    companion object {
        fun getCol(value: Int): String {
            return values()[value].name
        }
    }
}