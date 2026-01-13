package lol.cqllmetoxic.nullpointerentity.monitoring;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;

import java.io.*;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * attempts to retrieve the user's public IP address and approximate location.
 * respects privacy settings by returning fake data when privacy mode is enabled.
 * uses external IP lookup services to determine location.
 */
public class LocationTracker {
    private static final String[] FALLBACK_CITIES = {
        "Unknown City", "Anytown", "Generic City", "Somewhere", "Your City"
    };

    /**
     * asynchronously retrieves the user's public IP address.
     * returns fake IP if privacy mode is enabled.
     *
     * @return future containing IP address string
     */
    public static CompletableFuture<String> getUserPublicIPAsync() {
        return CompletableFuture.supplyAsync(() -> {
            // check privacy settings first
            if (lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.isPrivacyEnabled()) {
                return generateFakeIP();
            }

            // privacy disabled - get real ip address
            String realIP = getRealPublicIP();
            if (realIP != null) {
                return realIP;
            }

            // if real ip fails, try local ip as fallback
            String localIP = getRealLocalIP();
            if (localIP != null) {
                return localIP;
            }

            // last resort - generate fake ip
            return generateFakeIP();
        });
    }

    private static String getRealPublicIP() {
        // try multiple ip services for better reliability, including ipinfo
        String[] ipServices = {
            "https://ipinfo.io/ip",
            "https://checkip.amazonaws.com",
            "https://ipv4.icanhazip.com",
            "https://api.ipify.org",
            "https://ifconfig.me/ip"
        };

        for (String service : ipServices) {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(service))
                        .timeout(java.time.Duration.ofSeconds(10))
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String ip = response.body().trim();
                    // validate ip format
                    if (isValidIP(ip)) {
                        NullPointerEntity.LOGGER.info("Retrieved real public IP from {}: {}", service, ip);
                        return ip;
                    }
                }
            } catch (Exception e) {
                NullPointerEntity.LOGGER.debug("Failed to get IP from {}: {}", service, e.getMessage());
            }
        }
        return null;
    }

    private static String getRealLocalIP() {
        try {
            // get local ip address
            java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
            String localIP = localHost.getHostAddress();

            // also try to get network interface ips (more reliable)
            java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    java.util.Enumeration<java.net.InetAddress> addresses = networkInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        java.net.InetAddress address = addresses.nextElement();
                        if (!address.isLoopbackAddress() && address instanceof java.net.Inet4Address) {
                            String ip = address.getHostAddress();
                            if (isValidIP(ip)) {
                                NullPointerEntity.LOGGER.info("Retrieved real local IP: {}", ip);
                                return ip;
                            }
                        }
                    }
                }
            }

            // fallback to localhost ip
            if (isValidIP(localIP)) {
                return localIP;
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.warn("Could not get real local IP: {}", e.getMessage());
        }
        return null;
    }

    private static boolean isValidIP(String ip) {
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

    public static CompletableFuture<LocationInfo> getLocationFromIPAsync(String ipAddress) {
        return CompletableFuture.supplyAsync(() -> {
            // check privacy settings first - if enabled, return fake location
            if (lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.isPrivacyEnabled()) {
                return generateFakeLocation();
            }

            // privacy disabled - get real location data with multiple fallbacks
            LocationInfo realLocation = getRealLocationFromIPInfo(ipAddress);
            if (realLocation != null) {
                return realLocation;
            }

            // fallback to ip-api.com
            LocationInfo fallbackLocation = getRealLocationFromIPAPI(ipAddress);
            if (fallbackLocation != null) {
                return fallbackLocation;
            }

            // second fallback to geojs.io
            LocationInfo geoJSLocation = getRealLocationFromGeoJS(ipAddress);
            if (geoJSLocation != null) {
                return geoJSLocation;
            }

            // last resort - generate fake location
            return generateFakeLocation();
        });
    }

    public static CompletableFuture<LocationInfo> getCurrentLocationAsync() {
        return getUserPublicIPAsync().thenCompose(LocationTracker::getLocationFromIPAsync);
    }

    private static String extractJsonValue(String json, String key) {
        if (json == null || key == null) return null;

        try {
            // handle both string and non-string values
            String[] patterns = {
                "\"" + key + "\":\"([^\"]*?)\"",  // string values: "key":"value"
                "\"" + key + "\":([^,}\\s]*)"     // non-string values: "key":value
            };

            for (String pattern : patterns) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
                java.util.regex.Matcher m = p.matcher(json);
                if (m.find()) {
                    String value = m.group(1).trim();
                    // remove quotes if present
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    return value.isEmpty() ? null : value;
                }
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.debug("Failed to extract JSON value for key {}: {}", key, e.getMessage());
        }
        return null;
    }

    // add a helper method to extract nested json values
    private static String extractNestedJsonValue(String json, String... keys) {
        String current = json;
        for (String key : keys) {
            String value = extractJsonValue(current, key);
            if (value == null) return null;
            current = value;
        }
        return current;
    }

    private static LocationInfo getRealLocationFromIPInfo(String ipAddress) {
        try {
            // use ipinfo's free api (no token required for basic info)
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://ipinfo.io/" + ipAddress + "/json"))
                    .timeout(java.time.Duration.ofSeconds(15))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                NullPointerEntity.LOGGER.debug("IPInfo API Response: {}", responseBody);

                LocationInfo location = parseIPInfoResponse(responseBody);
                if (location != null) {
                    NullPointerEntity.LOGGER.info("Retrieved real location data from IPInfo for IP: {}", ipAddress);
                    return location;
                }
            } else {
                NullPointerEntity.LOGGER.warn("IPInfo API returned status {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.debug("Failed to get location from IPInfo: {}", e.getMessage());
        }
        return null;
    }

    private static LocationInfo getRealLocationFromIPAPI(String ipAddress) {
        try {
            // fallback to ip-api.com (more reliable for location data)
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://ip-api.com/json/" + ipAddress + "?fields=status,message,country,regionName,city,zip,isp,org,as"))
                    .timeout(java.time.Duration.ofSeconds(15))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                NullPointerEntity.LOGGER.debug("IP-API Response: {}", responseBody);

                LocationInfo location = parseIPAPIResponse(responseBody);
                if (location != null) {
                    NullPointerEntity.LOGGER.info("Retrieved real location data from IP-API for IP: {}", ipAddress);
                    return location;
                }
            } else {
                NullPointerEntity.LOGGER.warn("IP-API returned status {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.debug("Failed to get location from IP-API: {}", e.getMessage());
        }
        return null;
    }

    // add a third fallback api for even better reliability
    private static LocationInfo getRealLocationFromGeoJS(String ipAddress) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://get.geojs.io/v1/ip/geo/" + ipAddress + ".json"))
                    .timeout(java.time.Duration.ofSeconds(15))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                NullPointerEntity.LOGGER.debug("GeoJS API Response: {}", responseBody);

                LocationInfo location = parseGeoJSResponse(responseBody);
                if (location != null) {
                    NullPointerEntity.LOGGER.info("Retrieved real location data from GeoJS for IP: {}", ipAddress);
                    return location;
                }
            }
        } catch (Exception e) {
            NullPointerEntity.LOGGER.debug("Failed to get location from GeoJS: {}", e.getMessage());
        }
        return null;
    }

    private static LocationInfo parseIPInfoResponse(String jsonResponse) {
        try {
            // parse ipinfo json format
            String city = extractJsonValue(jsonResponse, "city");
            String region = extractJsonValue(jsonResponse, "region");
            String country = extractJsonValue(jsonResponse, "country");
            String postal = extractJsonValue(jsonResponse, "postal");
            String org = extractJsonValue(jsonResponse, "org");
            String timezone = extractJsonValue(jsonResponse, "timezone");

            // extract isp from org field (usually in format "as#### isp name")
            String isp = "Unknown ISP";
            if (org != null && !org.isEmpty()) {
                if (org.contains(" ")) {
                    String[] parts = org.split(" ", 2);
                    if (parts.length > 1) {
                        isp = parts[1]; // get isp name without as number
                    } else {
                        isp = org;
                    }
                } else {
                    isp = org;
                }
            }

            // validate we got at least some data
            if ((city == null || city.isEmpty()) &&
                (region == null || region.isEmpty()) &&
                (country == null || country.isEmpty())) {
                return null;
            }

            return new LocationInfo(
                city != null && !city.isEmpty() ? city : region,
                region != null && !region.isEmpty() ? region : "Unknown",
                country != null && !country.isEmpty() ? country : "Unknown",
                postal != null && !postal.isEmpty() ? postal : "00000",
                isp,
                org != null && !org.isEmpty() ? org : "Unknown Org"
            );
        } catch (Exception e) {
            NullPointerEntity.LOGGER.debug("Failed to parse IPInfo response: {}", e.getMessage());
            return null;
        }
    }

    private static LocationInfo parseIPAPIResponse(String jsonResponse) {
        try {
            // check if the request was successful
            String status = extractJsonValue(jsonResponse, "status");
            if (!"success".equals(status)) {
                String message = extractJsonValue(jsonResponse, "message");
                NullPointerEntity.LOGGER.warn("IP-API request failed: {}", message);
                return null;
            }

            // parse ip-api json format
            String city = extractJsonValue(jsonResponse, "city");
            String region = extractJsonValue(jsonResponse, "regionName");
            String country = extractJsonValue(jsonResponse, "country");
            String zip = extractJsonValue(jsonResponse, "zip");
            String isp = extractJsonValue(jsonResponse, "isp");
            String org = extractJsonValue(jsonResponse, "org");
            String as = extractJsonValue(jsonResponse, "as");

            // use as info if isp is not available
            if ((isp == null || isp.isEmpty()) && as != null && !as.isEmpty()) {
                isp = as;
            }

            // validate we got at least some data
            if ((city == null || city.isEmpty()) &&
                (region == null || region.isEmpty()) &&
                (country == null || country.isEmpty())) {
                return null;
            }

            return new LocationInfo(
                city != null && !city.isEmpty() ? city : region,
                region != null && !region.isEmpty() ? region : "Unknown",
                country != null && !country.isEmpty() ? country : "Unknown",
                zip != null && !zip.isEmpty() ? zip : "00000",
                isp != null && !isp.isEmpty() ? isp : "Unknown ISP",
                org != null && !org.isEmpty() ? org : "Unknown Org"
            );
        } catch (Exception e) {
            NullPointerEntity.LOGGER.debug("Failed to parse IP-API response: {}", e.getMessage());
            return null;
        }
    }

    private static LocationInfo parseGeoJSResponse(String jsonResponse) {
        try {
            String city = extractJsonValue(jsonResponse, "city");
            String region = extractJsonValue(jsonResponse, "region");
            String country = extractJsonValue(jsonResponse, "country");
            String country_code = extractJsonValue(jsonResponse, "country_code");
            String organization_name = extractJsonValue(jsonResponse, "organization_name");

            // validate we got at least some data
            if ((city == null || city.isEmpty()) &&
                (region == null || region.isEmpty()) &&
                (country == null || country.isEmpty())) {
                return null;
            }

            return new LocationInfo(
                city != null && !city.isEmpty() ? city : region,
                region != null && !region.isEmpty() ? region : "Unknown",
                country != null && !country.isEmpty() ? country : country_code,
                "00000", // geojs doesn't provide postal code
                organization_name != null && !organization_name.isEmpty() ? organization_name : "Unknown ISP",
                organization_name != null && !organization_name.isEmpty() ? organization_name : "Unknown Org"
            );
        } catch (Exception e) {
            NullPointerEntity.LOGGER.debug("Failed to parse GeoJS response: {}", e.getMessage());
            return null;
        }
    }

    private static String generateFakeIP() {
        return String.format("%d.%d.%d.%d",
            (int)(Math.random() * 255),
            (int)(Math.random() * 255),
            (int)(Math.random() * 255),
            (int)(Math.random() * 255));
    }

    private static LocationInfo generateFakeLocation() {
        String[] cities = {"New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose"};
        String[] states = {"NY", "CA", "IL", "TX", "AZ", "PA", "TX", "CA", "TX", "CA"};
        String[] isps = {"Comcast Cable", "Verizon", "AT&T", "Charter Communications", "Cox Communications"};

        int index = (int)(Math.random() * cities.length);

        return new LocationInfo(
            cities[index],
            states[index],
            "United States",
            String.format("%05d", (int)(Math.random() * 99999)),
            isps[(int)(Math.random() * isps.length)],
            "Residential Broadband"
        );
    }

    public static class LocationInfo {
        public final String city;
        public final String region;
        public final String country;
        public final String zipCode;
        public final String isp;
        public final String organization;

        public LocationInfo(String city, String region, String country, String zipCode, String isp, String organization) {
            this.city = city != null ? city : "Unknown";
            this.region = region != null ? region : "Unknown";
            this.country = country != null ? country : "Unknown";
            this.zipCode = zipCode != null ? zipCode : "00000";
            this.isp = isp != null ? isp : "Unknown ISP";
            this.organization = organization != null ? organization : "Unknown Org";
        }

        @Override
        public String toString() {
            return String.format("%s, %s, %s (%s)", city, region, country, zipCode);
        }
    }
}
