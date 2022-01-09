package hu.elte.sbzbxr.model;

import java.io.*;
import java.net.*;
import java.util.*;
import static java.lang.System.out;

/**
 * @see <a href="https://docs.oracle.com/javase/tutorial/networking/nifs/listing.html">...</a>
 */
public class ListNets {

    public static void run() throws SocketException {
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets))
            displayInterfaceInformation(netint);
    }

    static void displayInterfaceInformation(NetworkInterface netint) {
        out.printf("Display name: %s\n", netint.getDisplayName());
        out.printf("Name: %s\n", netint.getName());
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            out.printf("InetAddress: %s\n", inetAddress);
        }
        out.printf("\n");
    }
}
