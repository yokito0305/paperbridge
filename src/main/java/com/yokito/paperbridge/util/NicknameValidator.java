package com.yokito.paperbridge.util;

public class NicknameValidator {

    private NicknameValidator() {
    }

    public static boolean isValid(String nickname) {
        return nickname != null && nickname.length() >= 1 && nickname.length() <= 10;
    }
}
