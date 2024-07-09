package dev.subscripted.utils;

public class TimestampMaker {

    public static long getTime(long time) {
        long current = System.currentTimeMillis() / 1000;
        current += time;
        return current;
    }
}
