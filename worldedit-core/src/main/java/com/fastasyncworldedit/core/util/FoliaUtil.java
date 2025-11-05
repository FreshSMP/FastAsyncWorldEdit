package com.fastasyncworldedit.core.util;

public class FoliaUtil {

    private static final Boolean FOLIA_DETECTED = detectFolia();

    public static boolean isFoliaServer() {
        return FOLIA_DETECTED;
    }

    private static boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
