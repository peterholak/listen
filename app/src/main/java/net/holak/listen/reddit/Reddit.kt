package net.holak.listen.reddit

import android.os.AsyncTask
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Protocol
import net.dean.jraw.RedditClient
import net.dean.jraw.http.OkHttpAdapter
import net.dean.jraw.http.UserAgent
import net.dean.jraw.http.oauth.Credentials
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.*
import java.util.concurrent.locks.ReentrantLock

val LISTEN_APP_ID = "app_id_here"

class Reddit(val appId: String = LISTEN_APP_ID) {

    lateinit var reddit: RedditClient
    var ready = ReentrantLock()

    init {
        ready.lock()

        object : AsyncTask<Unit, Unit, Unit>() {
            override fun doInBackground(vararg params: Unit?) {
                val userAgent = UserAgent.of("android", "net.holak.listen", "wip", "DoListening")
                reddit = RedditClient(userAgent/*, proxyHttp()*/)
                // val deviceUuid = UUID.fromString(Settings.Secure.ANDROID_ID)
                val deviceUuid = UUID(0, 0)
                val authData = reddit.oAuthHelper.easyAuth(Credentials.userlessApp(appId, deviceUuid))
                reddit.authenticate(authData)
            }

            private fun proxyHttp(): OkHttpAdapter {
                val client = OkHttpClient()
                client.proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress("10.0.2.2", 8888))
                return OkHttpAdapter(client, Protocol.HTTP_1_1)
            }

            override fun onPostExecute(result: Unit?) {
                ready.unlock()
            }
        }.execute()
    }
}