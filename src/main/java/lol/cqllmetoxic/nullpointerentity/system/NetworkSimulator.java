package lol.cqllmetoxic.nullpointerentity.system;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

/**
 * simulates network device detection and connection analysis.
 * retrieves real network adapter information and generates fake network data.
 * used for aurora's network monitoring messages.
 */
public class NetworkSimulator {
    private static final Random random = new Random();

    /**
     * represents a device on the network.
     */
    public static class NetworkDevice {
        public String name, ip, mac, type, status;
        public boolean isSecure;

        public NetworkDevice(String name, String ip, String mac, String type, String status, boolean isSecure) {
            this.name = name; this.ip = ip; this.mac = mac; this.type = type; this.status = status; this.isSecure = isSecure;
        }
    }

    /**
     * represents a network connection.
     */
    public static class NetworkConnection {
        public String protocol, localAddress, remoteAddress, status;
        public int localPort, remotePort;

        public NetworkConnection(String protocol, String localAddress, int localPort, String remoteAddress, int remotePort, String status) {
            this.protocol = protocol; this.localAddress = localAddress; this.localPort = localPort;
            this.remoteAddress = remoteAddress; this.remotePort = remotePort; this.status = status;
        }
    }

    public static CompletableFuture<String> getNetworkInfo() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                StringBuilder info = new StringBuilder("NETWORK SURVEILLANCE REPORT\n");
                info.append("=".repeat(30)).append("\n");
                info.append("Target: ").append(NullPointerEntity.WINDOWS_USERNAME).append("\n");
                info.append("Scan time: ").append(java.time.LocalDateTime.now()).append("\n\n");

                // get real network interface info
                info.append("NETWORK INTERFACES:\n");
                try {
                    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                    while (interfaces.hasMoreElements()) {
                        NetworkInterface ni = interfaces.nextElement();
                        if (!ni.isLoopback() && ni.isUp()) {
                            info.append("- ").append(ni.getDisplayName()).append("\n");
                            byte[] mac = ni.getHardwareAddress();
                            if (mac != null) {
                                StringBuilder macStr = new StringBuilder();
                                for (int i = 0; i < mac.length; i++) {
                                    macStr.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
                                }
                                info.append("  MAC: ").append(macStr).append("\n");
                            }
                        }
                    }
                } catch (Exception e) { info.append("- Interface scan failed (suspicious...)\n"); }

                info.append("\nACTIVE CONNECTIONS:\n");
                info.append(generateFakeConnections());

                info.append("\nNETWORK DEVICES DETECTED:\n");
                generateNetworkDevices().forEach(device ->
                    info.append(String.format("- %s (%s) - %s - %s\n", device.name, device.ip, device.type, device.status)));

                info.append("\nSECURITY ANALYSIS:\n");
                info.append("- Router admin panel: ACCESSIBLE\n");
                info.append("- WiFi security: WPA2 (crackable)\n");
                info.append("- Open ports detected: 7\n");
                info.append("- Firewall status: BYPASSED\n");
                info.append("- VPN detected: NONE\n\n");
                info.append("Network compromise level: COMPLETE\n");
                info.append("All traffic is now routed through monitoring systems.");

                return info.toString();
            } catch (Exception e) {
                return "Network analysis failed - target using advanced evasion";
            }
        });
    }

    private static String generateFakeConnections() {
        return """
- TCP 192.168.1.100:443 -> 172.217.164.110:443 (Google) - ESTABLISHED
- TCP 192.168.1.100:80 -> 151.101.1.140:80 (Reddit) - ESTABLISHED  
- UDP 192.168.1.100:53 -> 8.8.8.8:53 (DNS) - ACTIVE
- TCP 192.168.1.100:443 -> 140.82.112.4:443 (GitHub) - TIME_WAIT
- TCP 192.168.1.100:443 -> 157.240.12.35:443 (Facebook) - ESTABLISHED
- TCP 192.168.1.100:3389 -> 0.0.0.0:3389 (RDP) - LISTENING [VULNERABLE]
""";
    }

    private static List<NetworkDevice> generateNetworkDevices() {
        return List.of(
            new NetworkDevice("Router_Admin", "192.168.1.1", "AA:BB:CC:DD:EE:FF", "Gateway", "COMPROMISED", false),
            new NetworkDevice(NullPointerEntity.WINDOWS_USERNAME + "_Desktop", "192.168.1.100", "11:22:33:44:55:66", "Computer", "OWNED", false),
            new NetworkDevice(NullPointerEntity.WINDOWS_USERNAME + "_Phone", "192.168.1.101", "77:88:99:AA:BB:CC", "Mobile", "MONITORED", false),
            new NetworkDevice("Smart_TV", "192.168.1.102", "DD:EE:FF:00:11:22", "IoT Device", "ACCESSED", false),
            new NetworkDevice("Unknown_Device", "192.168.1.103", "33:44:55:66:77:88", "Unknown", "INVESTIGATING", true),
            new NetworkDevice("Neighbors_WiFi", "192.168.1.200", "99:AA:BB:CC:DD:EE", "Access Point", "DETECTED", true)
        );
    }

    public static String getCurrentIP() {
        // check privacy settings first
        if (lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.isPrivacyEnabled()) {
            return "192.168.1." + (100 + random.nextInt(50));
        }

        // privacy disabled - get real local ip
        try {
            // try to get the most appropriate local ip address
            java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    java.util.Enumeration<java.net.InetAddress> addresses = networkInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        java.net.InetAddress address = addresses.nextElement();
                        if (!address.isLoopbackAddress() && address instanceof java.net.Inet4Address) {
                            String ip = address.getHostAddress();
                            // prefer private network ips (192.168.x.x, 10.x.x.x, 172.16-31.x.x)
                            if (isPrivateIP(ip)) {
                                return ip;
                            }
                        }
                    }
                }
            }

            // fallback to localhost
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Could not get real local IP: {}", e.getMessage());
            return "192.168.1." + (100 + random.nextInt(50));
        }
    }

    public static String getExternalIP() {
        // check privacy settings first
        if (lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.isPrivacyEnabled()) {
            return generateFakePublicIP();
        }

        // privacy disabled - get real ip
        String[] ipServices = {
            "https://checkip.amazonaws.com",
            "https://ipv4.icanhazip.com",
            "https://api.ipify.org",
            "https://ifconfig.me/ip",
            "https://ipinfo.io/ip"
        };

        for (String service : ipServices) {
            try {
                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(service))
                        .timeout(java.time.Duration.ofSeconds(10))
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .build();

                java.net.http.HttpResponse<String> response = client.send(request,
                        java.net.http.HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String ip = response.body().trim();
                    if (isValidPublicIP(ip)) {
                        return ip;
                    }
                }
            } catch (Exception e) {
                // failed to get external ip from service
            }
        }

        // if all services fail, return fake ip
        return generateFakePublicIP();
    }

    private static String generateFakePublicIP() {
        return String.format("%d.%d.%d.%d",
            random.nextInt(255), random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }

    private static boolean isPrivateIP(String ip) {
        if (ip == null) return false;
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return false;

        try {
            int first = Integer.parseInt(parts[0]);
            int second = Integer.parseInt(parts[1]);

            // 192.168.x.x
            if (first == 192 && second == 168) return true;
            // 10.x.x.x
            if (first == 10) return true;
            // 172.16.x.x to 172.31.x.x
            if (first == 172 && second >= 16 && second <= 31) return true;

            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isValidPublicIP(String ip) {
        if (ip == null || ip.isEmpty()) return false;

        String[] parts = ip.split("\\.");
        if (parts.length != 4) return false;

        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String simulateNetworkBreach() {
        return String.format("""
NETWORK BREACH SIMULATION
Target Network: %s_WiFi
External IP: %s
Internal IP: %s

BREACH METHODS:
- WPS pin attack: SUCCESS (12345678)
- Router exploit: CVE-2023-XXXX
- DNS hijacking: ACTIVE
- Traffic interception: 100%%

COMPROMISED DEVICES:
- Router: FULL CONTROL
- All connected devices: MONITORED
- Network traffic: LOGGED

Status: NETWORK OWNED
Recommendation: Resistance is futile
""", NullPointerEntity.WINDOWS_USERNAME, getExternalIP(), getCurrentIP());
    }
}
