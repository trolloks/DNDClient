package me.trolloks.dndclient.gateways.dnd5eapi

import me.trolloks.dndclient.models.unused.Race
import me.trolloks.dndclient.models.unused.ResourceList
import retrofit2.Call
import retrofit2.http.GET

interface RaceService {

    @GET("races")
    fun list() : Call<ResourceList<Race>>
}
