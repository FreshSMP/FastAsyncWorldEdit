package com.fastasyncworldedit.bukkit.util.scheduler;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class BukkitScheduler implements Scheduler {

    private final Plugin plugin;

    public BukkitScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runTaskTimer(Plugin plugin, Runnable runnable, long delay, long period) {
        if (plugin == null || runnable == null) {
            throw new IllegalArgumentException("Plugin and runnable cannot be null");
        }
        plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period);
    }

    @Override
    public void runTask(Plugin plugin, Runnable runnable) {
        if (plugin == null || runnable == null) {
            throw new IllegalArgumentException("Plugin and runnable cannot be null");
        }
        plugin.getServer().getScheduler().runTask(plugin, runnable);
    }

    @Override
    public void runTaskAsynchronously(Plugin plugin, Runnable runnable) {
        if (plugin == null || runnable == null) {
            throw new IllegalArgumentException("Plugin and runnable cannot be null");
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    @Override
    public void runTaskLater(Plugin plugin, Runnable runnable, long delay) {
        if (plugin == null || runnable == null) {
            throw new IllegalArgumentException("Plugin and runnable cannot be null");
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay);
    }

    @Override
    public void runTaskLaterAsynchronously(Plugin plugin, Runnable runnable, long delay) {
        if (plugin == null || runnable == null) {
            throw new IllegalArgumentException("Plugin and runnable cannot be null");
        }
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
    }

    @Override
    public void runTaskTimer(Runnable runnable, long delay, long period) {
        if (runnable == null) {
            throw new IllegalArgumentException("Runnable cannot be null");
        }
        plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period);
    }

    @Override
    public void runTask(Runnable runnable) {
        if (runnable == null) {
            throw new IllegalArgumentException("Runnable cannot be null");
        }
        plugin.getServer().getScheduler().runTask(plugin, runnable);
    }

    @Override
    public void runTaskAsynchronously(Runnable runnable) {
        if (runnable == null) {
            throw new IllegalArgumentException("Runnable cannot be null");
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    @Override
    public void runTaskLater(Runnable runnable, long delay) {
        if (runnable == null) {
            throw new IllegalArgumentException("Runnable cannot be null");
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay);
    }

    @Override
    public void runTaskLaterAsynchronously(Runnable runnable, long delay) {
        if (runnable == null) {
            throw new IllegalArgumentException("Runnable cannot be null");
        }
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
    }

    @Override
    public void scheduleSyncDelayedTask(Plugin plugin, Runnable runnable, long delay) {
        if (plugin == null || runnable == null) {
            throw new IllegalArgumentException("Plugin and runnable cannot be null");
        }
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, runnable, delay);
    }

    @Override
    public CancellableTask runTaskCancellable(Plugin plugin, Runnable runnable) {
        if (plugin == null || runnable == null) {
            throw new IllegalArgumentException("Plugin and runnable cannot be null");
        }
        BukkitTask task = plugin.getServer().getScheduler().runTask(plugin, runnable);
        return new BukkitCancellableTask(task);
    }

    @Override
    public CancellableTask runTaskLaterCancellable(Plugin plugin, Runnable runnable, long delay) {
        if (plugin == null || runnable == null) {
            throw new IllegalArgumentException("Plugin and runnable cannot be null");
        }
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay);
        return new BukkitCancellableTask(task);
    }

    @Override
    public CancellableTask runTaskTimerCancellable(Plugin plugin, Runnable runnable, long delay, long period) {
        if (plugin == null || runnable == null) {
            throw new IllegalArgumentException("Plugin and runnable cannot be null");
        }
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period);
        return new BukkitCancellableTask(task);
    }

    @Override
    public CancellableTask runTaskCancellable(Runnable runnable) {
        if (runnable == null) {
            throw new IllegalArgumentException("Runnable cannot be null");
        }
        BukkitTask task = plugin.getServer().getScheduler().runTask(plugin, runnable);
        return new BukkitCancellableTask(task);
    }

    @Override
    public CancellableTask runTaskLaterCancellable(Runnable runnable, long delay) {
        if (runnable == null) {
            throw new IllegalArgumentException("Runnable cannot be null");
        }
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay);
        return new BukkitCancellableTask(task);
    }

    @Override
    public CancellableTask runTaskTimerCancellable(Runnable runnable, long delay, long period) {
        if (runnable == null) {
            throw new IllegalArgumentException("Runnable cannot be null");
        }
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period);
        return new BukkitCancellableTask(task);
    }

    /**
     * Wrapper for BukkitTask to implement CancellableTask interface.
     */
    private static class BukkitCancellableTask implements CancellableTask {
        private final BukkitTask task;

        public BukkitCancellableTask(BukkitTask task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            if (task != null) {
                task.cancel();
            }
        }

        @Override
        public boolean isCancelled() {
            return task == null || task.isCancelled();
        }
    }
}
