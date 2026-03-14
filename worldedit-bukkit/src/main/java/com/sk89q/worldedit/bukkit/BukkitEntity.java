/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.bukkit;

import com.fastasyncworldedit.core.util.FoliaUtil;
import com.fastasyncworldedit.core.util.TaskManager;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.entity.metadata.EntityProperties;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.NullWorld;
import io.papermc.lib.PaperLib;
import org.bukkit.entity.EntityType;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An adapter to adapt a Bukkit entity into a WorldEdit one.
 */
//FAWE start - made class public
public class BukkitEntity implements Entity {
//FAWE end

    private final WeakReference<org.bukkit.entity.Entity> entityRef;
    //FAWE start
    private final EntityType type;
    //FAWE end

    /**
     * Create a new instance.
     *
     * @param entity the entity
     */
    public BukkitEntity(org.bukkit.entity.Entity entity) {
        checkNotNull(entity);
        //FAWE start
        this.type = entity.getType();
        //FAWE end
        this.entityRef = new WeakReference<>(entity);
    }

    @Override
    public Extent getExtent() {
        org.bukkit.entity.Entity entity = entityRef.get();
        if (entity != null) {
            return BukkitAdapter.adapt(entity.getWorld());
        } else {
            return NullWorld.getInstance();
        }
    }

    @Override
    public Location getLocation() {
        org.bukkit.entity.Entity entity = entityRef.get();
        if (entity != null) {
            return BukkitAdapter.adapt(entity.getLocation());
        } else {
            return new Location(NullWorld.getInstance());
        }
    }

    @Override
    public boolean setLocation(Location location) {
        org.bukkit.entity.Entity entity = entityRef.get();
        if (entity != null) {
            if (PaperLib.isPaper()) {
                entity.teleportAsync(BukkitAdapter.adapt(location));
                return true;
            }
            return entity.teleport(BukkitAdapter.adapt(location));
        } else {
            return false;
        }
    }

    @Override
    public BaseEntity getState() {
        org.bukkit.entity.Entity entity = entityRef.get();
        if (entity != null) {
            if (entity instanceof Player) {
                return null;
            }

            BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
            if (adapter != null) {
                if (FoliaUtil.isFoliaServer()) {
                    CompletableFuture<BaseEntity> future = new CompletableFuture<>();
                    // Use entity scheduler (not region scheduler) to guarantee we run on
                    // the entity's owning region thread. regionScheduler.run(location) would
                    // target the region that owns that location, which may differ from the
                    // region that owns the entity itself, causing Folia thread-check failures.
                    boolean scheduled = entity.getScheduler().execute(
                            WorldEditPlugin.getInstance(),
                            () -> {
                                BaseEntity base = adapter.getEntity(entity);
                                // Force-evaluate the lazy NBT tag while we are still on the
                                // entity's region thread. LazyBaseEntity.getNbt() would otherwise
                                // run the supplier via TaskManager.sync(), which on Folia is a
                                // no-op and executes on whatever (async) thread calls it later.
                                if (base != null) {
                                    base.getNbt();
                                }
                                future.complete(base);
                            },
                            () -> future.complete(null), // retired: entity was removed
                            1L
                    );
                    if (!scheduled) {
                        // Entity scheduler rejected the task (e.g. plugin disabled); fall back
                        // to returning null so the copy operation skips this entity cleanly.
                        return null;
                    }
                    return future.join();
                }
                return adapter.getEntity(entity);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean remove() {
        if (FoliaUtil.isFoliaServer()) {
            return TaskManager.taskManager().syncWhenFree(() -> {
                org.bukkit.entity.Entity entity = entityRef.get();
                if (entity != null) {
                    try {
                        entity.getScheduler().execute(WorldEditPlugin.getInstance(), entity::remove, null, 1);
                        return true;
                    } catch (UnsupportedOperationException e) {
                        return false;
                    }
                } else {
                    return true;
                }
            });
        }
        // synchronize the whole method, not just the remove operation as we always need to synchronize and
        // can make sure the entity reference was not invalidated in the few milliseconds between the next available tick (lol)
        return TaskManager.taskManager().sync(() -> {
            org.bukkit.entity.Entity entity = entityRef.get();
            if (entity != null) {
                try {
                    entity.remove();
                } catch (UnsupportedOperationException e) {
                    return false;
                }
                return entity.isDead();
            } else {
                return true;
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        org.bukkit.entity.Entity entity = entityRef.get();
        if (entity != null && EntityProperties.class.isAssignableFrom(cls)) {
            return (T) new BukkitEntityProperties(entity);
        } else {
            return null;
        }
    }

}
