package ru.skillbranch.skillarticles.data.remote.interceptors

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.remote.NetworkManager
import ru.skillbranch.skillarticles.data.remote.req.RefreshReq


class TokenAuthenticator : Authenticator {
    private val prefs = PrefManager
    private val api by lazy { NetworkManager.api }

    override fun authenticate(route: Route?, response: Response): Request? {
        return if (response.code != 401) null
        else {
            //request new access token by refresh token (sync)
            val res = api.refreshAccessToken(RefreshReq(prefs.refreshToken)).execute()

            return if (!res.isSuccessful) null
            else{
                //save new refresh /access token
                val newAccessToken = res.body()!!.accessToken
                prefs.accessToken = "Bearer $newAccessToken"
                prefs.refreshToken = res.body()!!.refreshToken

                //retry request with new access token
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken ")
                    .build()
            }
        }
    }
}