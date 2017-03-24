package com.baojie;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

public class HahaIP {

	private HahaIP() {

	}

	private static final String IP_ERROR = "127.0.0.1";

	private static final String NET_INTERFACE_ERROR = "error";

	private static final String BRIAGE = ":";

	private static final boolean IS_WINDOWS;

	static {
		boolean isWin = false;
		try {
			String osName = System.getProperty("os.name");
			if (null == osName) {
				throw new IOException("os.name not found");
			}
			osName = osName.toLowerCase(Locale.ENGLISH);
			if (osName.contains("windows")) {
				isWin = true;
			} else if (osName.contains("linux") || osName.contains("mpe/ix") || osName.contains("freebsd")
					|| osName.contains("irix") || osName.contains("digital unix") || osName.contains("unix")
					|| osName.contains("mac os x")) {
				isWin = false;
			}
		} catch (final Throwable ex) {
			ex.printStackTrace();
			throw new IllegalStateException("os.name not found");
		}
		IS_WINDOWS = isWin;
	}

	public static String getIpAndNetInterfaceName() {
		if (IS_WINDOWS) {
			return getWinIpAndNetInterfaceName();
		} else {
			return getLinuxIpAndNetInterfaceName();
		}
	}

	private static String getWinIpAndNetInterfaceName() {
		String winIP = null;
		String networkInterfaceName = null;
		NetworkInterface ni = null;
		final InetAddress inetAddress = getWinInetAddress();
		if (null != inetAddress) {
			winIP = inetAddress.getHostAddress();
			ni = getWinNetInterfaceName(inetAddress);
			if (null != ni) {
				networkInterfaceName = ni.getName();
				return winIP + ":" + networkInterfaceName;
			} else {
				return winIP + BRIAGE + NET_INTERFACE_ERROR;
			}
		} else {
			return IP_ERROR + BRIAGE + NET_INTERFACE_ERROR;
		}
	}

	private static InetAddress getWinInetAddress() {
		InetAddress netAddress = null;
		try {
			netAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			netAddress = null;
			e.printStackTrace();
		}
		return netAddress;
	}

	private static NetworkInterface getWinNetInterfaceName(final InetAddress inetAddress) {
		NetworkInterface ni = null;
		try {
			ni = NetworkInterface.getByInetAddress(inetAddress);
		} catch (SocketException e) {
			ni = null;
			e.printStackTrace();
		}
		return ni;
	}

	private static String getLinuxIpAndNetInterfaceName() {
		final List<String> ipAndNetInterfaceName = new ArrayList<String>();
		final Enumeration<NetworkInterface> netInterfaces = getNetworkAllInterface();
		if (null == netInterfaces) {
			return IP_ERROR + BRIAGE + NET_INTERFACE_ERROR;
		} else {
			checkEveryNetInterface(netInterfaces, ipAndNetInterfaceName);
		}
		final int listSize = ipAndNetInterfaceName.size();
		if (listSize > 1) {
			System.out.println("windows or linux should be never happen.");
		}
		if (0 == listSize) {
			System.out.println("ips and netinterfaces must not be empty in list.");
			return IP_ERROR + BRIAGE + NET_INTERFACE_ERROR;
		}
		return ipAndNetInterfaceName.get(0);
	}

	private static void checkEveryNetInterface(final Enumeration<NetworkInterface> netInterfaces,
			final List<String> ipAndNetInterfaceName) {
		NetworkInterface ni = null;
		while (netInterfaces.hasMoreElements()) {
			ni = getOneNetworkInterface(netInterfaces);
			if (null == ni) {
				continue;
			} else if (isLoopback(ni)) {
				continue;
			} else if (isVisual(ni)) {
				continue;
			} else if (!isUp(ni)) {
				continue;
			} else {
				checkEveryInetAddress(ni, ipAndNetInterfaceName);
			}
		}
	}

	private static void checkEveryInetAddress(final NetworkInterface networkInterface,
			final List<String> ipAndNetInterfaceName) {
		final Enumeration<InetAddress> address = networkInterface.getInetAddresses();
		final String networkInterfaceName = networkInterface.getName();
		InetAddress inetAddress = null;
		while (address.hasMoreElements()) {
			inetAddress = getInetAddressElement(address);
			if (null == inetAddress) {
				continue;
			} else if (!inetAddress.isSiteLocalAddress()) {
				continue;
			} else if (inetAddress instanceof Inet6Address) {
				continue;
			} else {
				fillIpAndNetInterfaceName(inetAddress, networkInterfaceName, ipAndNetInterfaceName);
			}
		}
	}

	private static void fillIpAndNetInterfaceName(final InetAddress inetAddress, final String networkInterfaceName,
			final List<String> ipAndNetInterfaceName) {
		final String realIP = inetAddress.getHostAddress();
		if (null == realIP) {
			return;
		} else {
			if ((realIP.contains(".")) && (!realIP.contains(":"))) {
				if ((!realIP.contains("::")) && (!realIP.contains("0:0:")) && (!realIP.contains("fe80"))) {
					ipAndNetInterfaceName.add(realIP.trim() + ":" + networkInterfaceName);
				} else {
					return;
				}
			} else {
				return;
			}
		}
	}

	private static Enumeration<NetworkInterface> getNetworkAllInterface() {
		Enumeration<NetworkInterface> netInterfaces = null;
		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			netInterfaces = null;
			e.printStackTrace();
		}
		return netInterfaces;
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

	private static NetworkInterface getOneNetworkInterface(final Enumeration<NetworkInterface> netInterfaces) {
		NetworkInterface ni = null;
		try {
			ni = netInterfaces.nextElement();
		} catch (NoSuchElementException e) {
			ni = null;
			e.printStackTrace();
		}
		return ni;
	}

	public static void main(String args[]) {
		System.out.println(HahaIP.getIpAndNetInterfaceName());
	}

}
