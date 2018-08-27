package otf.project.otf.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.io.IOException;
import java.net.ServerSocket;

import otf.project.otf.OTFApp;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by denismalcev on 22.05.17.
 */

public class ConnectionUtils {

    public static boolean isWifiConnectionEnabled() {
        ConnectivityManager connManager = (ConnectivityManager) OTFApp.instance.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

    public static int findFreePort() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            socket.setReuseAddress(true);
            int port = socket.getLocalPort();
            try {
                socket.close();
            } catch (IOException e) { }

            return port;

        } catch (IOException e) {
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        throw new IllegalStateException("Could not find a free TCP/IP port");
    }

    public static String getIPAddress() {
        WifiManager wifiManager = (WifiManager) OTFApp.instance.getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();

        String ipString = String.format(
                "%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));

        return ipString;
    }

//    public static String getIPAddress() {
//        String ip = "";
//        try {
//            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
//                    .getNetworkInterfaces();
//            while (enumNetworkInterfaces.hasMoreElements()) {
//                NetworkInterface networkInterface = enumNetworkInterfaces
//                        .nextElement();
//                Enumeration<InetAddress> enumInetAddress = networkInterface
//                        .getInetAddresses();
//                while (enumInetAddress.hasMoreElements()) {
//                    InetAddress inetAddress = enumInetAddress
//                            .nextElement();
//
//                    String address = inetAddress.getHostAddress();
//                    CustomEvent event = new CustomEvent("InetAddress");
//                    event.putCustomAttribute("IP", inetAddress.getHostAddress());
//
//                    Answers.getInstance().logCustom(event);
//                    if (inetAddress.isSiteLocalAddress()) {
//                        ip = inetAddress.getHostAddress();
//                    }
//                }
//            }
//
//        } catch (SocketException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            ip = null;
//        }
//        return ip;
//    }
}
