package com.fastasyncworldedit.core.util;

public final class FoliaSupport {

    private FoliaSupport() {
    }

    private static final boolean IS_FOLIA;
    private static final Class<?> TICK_THREAD_CLASS;

    private static final ThreadLocal<Boolean> TL_TICK = new ThreadLocal<>();

    public static void enterTickThread() {
        TL_TICK.set(Boolean.TRUE);
    }

    public static void exitTickThread() {
        TL_TICK.remove();
    }

    static {
        boolean isFolia = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
            isFolia = true;
        } catch (final Throwable unused) {
            // Unused impl.
        }
        IS_FOLIA = isFolia;

        Class<?> tickThreadClass = null;
        if (IS_FOLIA) {
            try {
                tickThreadClass = Class.forName("io.papermc.paper.util.TickThread");
            } catch (final ClassNotFoundException ignored) {
                // absent on Canvas; caller must fallback
            }
        }
        TICK_THREAD_CLASS = tickThreadClass;
    }

    public static boolean isFolia() {
        return IS_FOLIA;
    }

    public static boolean isTickThread() {
        if (TICK_THREAD_CLASS != null) {
            return TICK_THREAD_CLASS.isInstance(Thread.currentThread());
        }
        return Boolean.TRUE.equals(TL_TICK.get());
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Throwable;
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Throwable;
    }

    public static void runRethrowing(final ThrowingRunnable runnable) {
        getRethrowing(() -> {
            runnable.run();
            return null;
        });
    }

    public static <T> T getRethrowing(final ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (final RuntimeException | Error e) {
            throw e;
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
