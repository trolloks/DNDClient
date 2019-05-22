package me.trolloks.dndclient.models

class AbilityScore(val name:String, val code:String) {
    var score : Int? = null

    fun getModifier() : Int{
        return if (score != null) ((score!! - 10) / 2) else 0
    }
}