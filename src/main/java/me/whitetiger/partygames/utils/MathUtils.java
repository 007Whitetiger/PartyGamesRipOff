package me.whitetiger.partygames.utils;

import java.util.Random;

public class MathUtils {

    private static final Random random = new Random();

    public static int getRandomIntUnderNumber(int num) {
        return random.nextInt(num);
    }
}
