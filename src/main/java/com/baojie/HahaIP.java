package com.baojie;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

public class HahaIP {

	public static String getIps() {
		final List<String> allIps = new ArrayList<String>();
		final Enumeration<NetworkInterface> netInterfaces = gEnumeration();
		InetAddress ip = null;
		NetworkInterface ni = null;
		String netip = null;
		Enumeration<InetAddress> address = null;
		if (null == netInterfaces) {
			return "127.0.0.1";
		} else {
			while (netInterfaces.hasMoreElements()) {
				ni = getNetworkInterface(netInterfaces);
				if (null == ni) {
					continue;
				} else if (isLoopback(ni)) {
					continue;
				} else if (isVisual(ni)) {
					continue;
				} else if (!isUp(ni)) {
					continue;
				} else {
					address = ni.getInetAddresses();
					while (address.hasMoreElements()) {
						ip = getInetAddressElement(address);
						if (null == ip) {
							continue;
						} else if (!ip.isSiteLocalAddress()) {
							continue;
						} else if (ip instanceof Inet6Address) {
							continue;
						} else {
							netip = ip.getHostAddress();
							if (null == netip) {
								continue;
							} else {
								if ((netip.contains(".")) && (!netip.contains(":"))) {
									allIps.add(netip.trim());
								} else {
									continue;
								}
							}
						}
					}
				}
			}
		}
		if(allIps.size()>1){
			System.out.println("look");
		}
		return allIps.get(0);
	}

	private static boolean isLoopback(final NetworkInterface ni) {
		boolean isLoopback = true;
		try {
			isLoopback = ni.isLoopback();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return isLoopback;
	}

	private static boolean isVisual(final NetworkInterface ni) {
		boolean isVirsual = true;
		isVirsual = ni.isVirtual();
		return isVirsual;
	}

	private static boolean isUp(final NetworkInterface ni) {
		boolean isUp = false;
		try {
			isUp = ni.isUp();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return isUp;

	}

	private static InetAddress getInetAddressElement(final Enumeration<InetAddress> address) {
		InetAddress ip = null;
		try {
			ip = address.nextElement();
		} catch (NoSuchElementException e) {
			ip = null;
			e.printStackTrace();
		}
		return ip;
	}

	private static NetworkInterface getNetworkInterface(final Enumeration<NetworkInterface> netInterfaces) {
		NetworkInterface ni = null;
		try {
			ni = netInterfaces.nextElement();
		} catch (NoSuchElementException e) {
			ni = null;
			e.printStackTrace();
		}
		return ni;

	}

	private static Enumeration<NetworkInterface> gEnumeration() {
		Enumeration<NetworkInterface> netInterfaces = null;
		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			netInterfaces = null;
			e.printStackTrace();
		}
		return netInterfaces;
	}

	public static void main(String args[]) {
		String list = getIps();
		System.out.println(list);
	}

}
