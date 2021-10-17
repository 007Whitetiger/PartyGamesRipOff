package me.whitetiger.partygames.utils;

import java.util.List;

public class ListUtils {
    public static <T> T getFromListOrNull(List<T> someList, int index) {
        try {
            return someList.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
}
