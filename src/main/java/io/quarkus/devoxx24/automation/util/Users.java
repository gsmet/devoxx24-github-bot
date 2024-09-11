package io.quarkus.devoxx24.automation.util;

public final class Users {

    private Users() {
    }

    public static boolean isBot(String login) {
        return login.endsWith("[bot]");
    }
}
