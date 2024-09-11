package io.quarkus.devoxx24.automation.util;

public final class Strings {

    public static String sanitize(String value) {
        if (value == null) {
            return null;
        }

        return value.replace("---", "___");
    }
}
