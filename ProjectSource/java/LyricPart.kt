package com.example.guessthelyrics


class LyricPart(private val partID: Int, val songID: Int, val lyrics: String) :
    Comparable<LyricPart> {

    override fun toString(): String {
        return "$partID : $songID :: $lyrics"
    }

    /**
     * If lyric parts are the same return 0, if they are different return 1 or -1
     */
    override fun compareTo(other: LyricPart): Int {
        return when {
            this.songID == other.songID -> 0
            this.songID < other.songID -> -1
            else -> 1
        }
    }

}
