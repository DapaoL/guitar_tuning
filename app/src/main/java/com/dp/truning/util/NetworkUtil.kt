import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build

object NetworkUtil {
    /**
     * 判断是否已具备 internet connection。
     */
    fun hasInternetConnection(context: Context): Boolean {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val builder = NetworkRequest.Builder()
            builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            val request = builder.build()
            val networkCallback = CustomNetworkCallback()
            connectivityManager.registerNetworkCallback(request, networkCallback)
            return networkCallback.isConnected
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))) {
                return true
            }
        }
        return false
    }

    private class CustomNetworkCallback : NetworkCallback() {
        var isConnected = false
            private set

        /**
         * 在网络可用时更新连接状态。
         */
        override fun onAvailable(network: Network) {
            isConnected = true
        }

        /**
         * 在网络断开时更新连接状态。
         */
        override fun onLost(network: Network) {
            isConnected = false
        }
    }
}