package io.github.jlmc.kafkaexamples.commons.utils;

public final class VeryLongProcess {

    public static void runProcess(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private VeryLongProcess() {
        throw new AssertionError();
    }
}
