package io.github.countyclaaw4.carrotcraftsortingpatch.screen;

import net.minecraft.network.chat.Component;

public final class TitleScreenToast {

    private static final long DISPLAY_DURATION_NANOS = 5_000_000_000L;

    private static Message message;
    private static long visibleSince;

    private TitleScreenToast() {}

    public static synchronized void show(Component title, Component description) {
        message = new Message(title, description);
        visibleSince = 0L;
    }

    public static synchronized Message current() {
        if (message == null) {
            return null;
        }

        long now = System.nanoTime();
        if (visibleSince == 0L) {
            visibleSince = now;
        } else if (now - visibleSince >= DISPLAY_DURATION_NANOS) {
            message = null;
            visibleSince = 0L;
            return null;
        }

        return message;
    }

    public record Message(Component title, Component description) {}
}