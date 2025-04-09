package com.resonate.util;

import java.util.UUID;

public class TestUtil {

    public static final String ARTIST_ID_STRING = "00000000-0000-0000-0000-000000000000";
    public static final String FAN_ID_STRING = "00000000-0000-0000-0000-000000000001";

    public static final UUID ARTIST_UUID = UUID.fromString(ARTIST_ID_STRING);
    public static final UUID FAN_UUID = UUID.fromString(FAN_ID_STRING);


    public static String genUUID() {
        return UUID.randomUUID().toString();
    }

    public static String cleanString(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().replaceAll("\\s+", " ");
    }
}
