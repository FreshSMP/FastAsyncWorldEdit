package com.fastasyncworldedit.bukkit.util;

import com.fastasyncworldedit.core.util.FoliaSupport;
import com.fastasyncworldedit.core.util.TaskManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FoliaTaskManager extends TaskManager {

    private final AtomicInteger idCounter = new AtomicInteger();

    @Override
    public int repeatAsync(@NotNull final Runnable runnable, final int interval) {
        Bukkit.getAsyncScheduler().runAtFixedRate(
                WorldEditPlugin.getInstance(),
                asAsyncConsumer(runnable),
                0,
                ticksToMs(interval),
                TimeUnit.MILLISECONDS
        );
        return idCounter.getAndIncrement();
    }

    @Override
    public void async(@NotNull final Runnable runnable) {
        Bukkit.getAsyncScheduler().runNow(WorldEditPlugin.getInstance(), asAsyncConsumer(runnable));
    }

    @Override
    public void task(@NotNull final Runnable runnable, @NotNull final World world, final int chunkX, final int chunkZ) {
        Bukkit.getRegionScheduler().run(
                WorldEditPlugin.getInstance(),
                BukkitAdapter.adapt(world),
                chunkX,
                chunkZ,
                asTickConsumer(runnable)
        );
    }

    @Override
    public void taskGlobal(final Runnable runnable) {
        Bukkit.getGlobalRegionScheduler().run(WorldEditPlugin.getInstance(), asTickConsumer(runnable));
    }

    @Override
    public void later(@NotNull final Runnable runnable, final Location location, final int delay) {
        Bukkit.getRegionScheduler().runDelayed(
                WorldEditPlugin.getInstance(),
                BukkitAdapter.adapt(location),
                asTickConsumer(runnable),
                delay
        );
    }

    @Override
    public void laterGlobal(@NotNull final Runnable runnable, final int delay) {
        Bukkit.getGlobalRegionScheduler().runDelayed(
                WorldEditPlugin.getInstance(),
                asTickConsumer(runnable),
                delay
        );
    }

    @Override
    public void laterAsync(@NotNull final Runnable runnable, final int delay) {
        Bukkit.getAsyncScheduler().runDelayed(
                WorldEditPlugin.getInstance(),
                asAsyncConsumer(runnable),
                ticksToMs(delay),
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void cancel(final int task) {
        fail("Not implemented");
    }

    @Override
    public <T> T syncAt(final Supplier<T> supplier, final World world, final int chunkX, final int chunkZ) {
        final org.bukkit.World adapt = BukkitAdapter.adapt(world);
        if (Bukkit.isOwnedByCurrentRegion(adapt, chunkX, chunkZ)) {
            return supplier.get();
        }
        ensureOffTickThread();
        final FutureTask<T> task = new FutureTask<>(supplier::get);
        Bukkit.getRegionScheduler().run(
                WorldEditPlugin.getInstance(),
                adapt,
                chunkX,
                chunkZ,
                asTickConsumer(task)
        );
        try {
            return task.get();
        } catch (final InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T syncWith(final Supplier<T> supplier, final Player context) {
        final org.bukkit.entity.Player adapt = BukkitAdapter.adapt(context);
        if (Bukkit.isOwnedByCurrentRegion(adapt)) {
            return supplier.get();
        }
        ensureOffTickThread();
        final FutureTask<T> task = new FutureTask<>(supplier::get);
        adapt.getScheduler().execute(WorldEditPlugin.getInstance(), task, null, 0);
        try {
            return task.get();
        } catch (final InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T syncGlobal(final Supplier<T> supplier) {
        // FAWE start - Fix Folia compatibility: In Folia, there is no "primary thread"
        // Instead, we need to check if we're on a tick thread and handle accordingly
        if (FoliaSupport.isTickThread()) {
            return supplier.get();
        }
        // FAWE end
        final FutureTask<T> task = new FutureTask<>(supplier::get);
        Bukkit.getGlobalRegionScheduler().run(WorldEditPlugin.getInstance(), asTickConsumer(task));
        try {
            return task.get();
        } catch (final InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    // --- helpers ---

    private <R> Consumer<R> asTickConsumer(final Runnable runnable) {
        return __ -> {
            FoliaSupport.enterTickThread();
            try {
                runnable.run();
            } finally {
                FoliaSupport.exitTickThread();
            }
        };
    }

    private <R> Consumer<R> asAsyncConsumer(final Runnable runnable) {
        return __ -> runnable.run();
    }

    private void ensureOffTickThread() {
        if (FoliaSupport.isTickThread()) {
            throw new IllegalStateException("Expected to be off tick thread");
        }
    }

    private int ticksToMs(final int ticks) {
        // 1 tick = 50ms
        return ticks * 50;
    }

    private <T> T fail(final String message) {
        throw new UnsupportedOperationException(message);
    }

}
