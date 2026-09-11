/*
 *  Copyright (C) <2024> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customnameplates.api.helper;

import net.momirealms.customnameplates.common.plugin.NameplatesPlugin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * VersionHelper is a utility class that provides methods for managing and checking version-related information,
 * including plugin updates and server version details. It implements the VersionManager interface.
 */
public class VersionHelper {

    private static final int TARGET_VERSION = 260200;
    private static final Pattern SERVER_VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?(?:[-.+].*)?$");

    /**
     * A function to check for plugin updates asynchronously by comparing the plugin's current version with the latest version available.
     */
    public static final Function<NameplatesPlugin, CompletableFuture<Boolean>> UPDATE_CHECKER = (plugin) -> {
        CompletableFuture<Boolean> updateFuture = new CompletableFuture<>();
        plugin.getScheduler().async().execute(() -> {
            try {
                URL url = new URL("https://api.polymart.org/v1/getResourceInfoSimple/?resource_id=2723&key=version");
                URLConnection conn = url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(60000);
                InputStream inputStream = conn.getInputStream();
                String newest = new BufferedReader(new InputStreamReader(inputStream)).readLine();
                String current = plugin.getPluginVersion();
                inputStream.close();
                if (!compareVer(newest, current)) {
                    updateFuture.complete(false);
                    return;
                }
                updateFuture.complete(true);
            } catch (Exception exception) {
                plugin.getPluginLogger().warn("Error occurred when checking update.");
                updateFuture.completeExceptionally(exception);
            }
        });
        return updateFuture;
    };

    private static int version;
    private static boolean mojmap;
    private static boolean folia;
    private static boolean mohist;
    private static boolean paper;

    /**
     * Initializes version-specific settings based on the server version.
     * This method checks if the server is running Mojmap, Folia, Mohist, or Paper.
     *
     * @param serverVersion The server version string.
     */
    public static void init(String serverVersion) {
        version = parseVersionToInteger(serverVersion);
        if (version != TARGET_VERSION) {
            throw new IllegalStateException("CustomNameplates only supports Paper 26.2 servers, current server version: " + serverVersion);
        }
        checkMojMap();
        checkFolia();
        checkMohist();
        checkPaper();
        boolean isModdedServer = mohist;
        paper = paper && !isModdedServer;
    }

    public static int parseVersionToInteger(String versionString) {
        Matcher matcher = SERVER_VERSION_PATTERN.matcher(versionString);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid version: " + versionString);
        }
        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(2));
        int patch = matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3));
        return 10000 * major + minor * 100 + patch;
    }

    /**
     * Gets the current server version as an encoded integer.
     *
     * @return The server version as an encoded integer.
     */
    public static int version() {
        return version;
    }

    /**
     * Checks if the server is running Mojmap.
     */
    private static void checkMojMap() {
        try {
            Class.forName("net.minecraft.network.protocol.game.ClientboundBossEventPacket");
            mojmap = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    /**
     * Checks if the server is running Folia.
     */
    private static void checkFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    /**
     * Checks if the server is running Mohist.
     */
    private static void checkMohist() {
        try {
            Class.forName("com.mohistmc.api.ServerAPI");
            mohist = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    /**
     * Checks if the server is running Paper or its forks.
     */
    private static void checkPaper() {
        try {
            Class.forName("com.destroystokyo.paper.Metrics");
            paper = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    public static boolean isVersion26_1_2() { return version == 260102; }

    public static boolean isVersion26_2() {
        return version == TARGET_VERSION;
    }

    /**
     * Checks if the server is running Folia.
     *
     * @return True if the server is running Folia, otherwise false.
     */
    public static boolean isFolia() {
        return folia;
    }

    /**
     * Checks if the server is running Mohist.
     *
     * @return True if the server is running Mohist, otherwise false.
     */
    public static boolean isMohist() {
        return mohist;
    }

    /**
     * Checks if the server is running Paper or its forks.
     *
     * @return True if the server is running Paper or its forks, otherwise false.
     */
    public static boolean isPaperOrItsForks() {
        return paper;
    }

    /**
     * Checks if the server is using Mojmap.
     *
     * @return True if the server is using Mojmap, otherwise false.
     */
    public static boolean isMojmap() {
        return mojmap;
    }

    /**
     * Compares two version strings to determine if the first version is newer than the second.
     *
     * @param newV The new version string.
     * @param currentV The current version string.
     * @return True if the new version is newer than the current version, otherwise false.
     */
    private static boolean compareVer(String newV, String currentV) {
        if (newV == null || currentV == null || newV.isEmpty() || currentV.isEmpty()) {
            return false;
        }
        String[] newVS = newV.split("\\.");
        String[] currentVS = currentV.split("\\.");
        int maxL = Math.min(newVS.length, currentVS.length);
        for (int i = 0; i < maxL; i++) {
            try {
                String[] newPart = newVS[i].split("-");
                String[] currentPart = currentVS[i].split("-");
                int newNum = Integer.parseInt(newPart[0]);
                int currentNum = Integer.parseInt(currentPart[0]);
                if (newNum > currentNum) {
                    return true;
                } else if (newNum < currentNum) {
                    return false;
                }
            } catch (NumberFormatException e) {
                // handle error
            }
        }
        return false;
    }
}
