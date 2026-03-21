/*
 *  Copyright (C) <2024> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customnameplates.bukkit.compatibility.cosmetic;

import com.magicrealms.magiccosmetic.api.events.*;
import com.magicrealms.magiccosmetic.config.Settings;
import com.magicrealms.magiccosmetic.cosmetic.CosmeticSlot;
import com.magicrealms.magiccosmetic.user.CosmeticUser;
import com.magicrealms.magiccosmetic.user.CosmeticUsers;
import com.magicrealms.magiccosmetic.user.manager.UserBackpackManager;
import com.magicrealms.magiccosmetic.user.manager.UserEntity;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.momirealms.customnameplates.api.CNPlayer;
import net.momirealms.customnameplates.api.CustomNameplates;
import net.momirealms.customnameplates.api.feature.tag.UnlimitedTagManager;
import net.momirealms.customnameplates.api.network.ExternalPassengerProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * @author Mmu0621
 * @Desc MagicCosmetic 背包 passenger 合并提供者（快照模式），
 *       主线程通过事件/定时任务维护快照，异步线程只读快照
 * @date 2026-03-21
 */
public class MagicCosmeticBackpackProvider implements ExternalPassengerProvider, Listener {

    /* 提供者标识名 */
    private static final String NAME = "MagicCosmetic";

    /* 背包快照，key 为 owner UUID */
    private final ConcurrentHashMap<UUID, BackpackSnapshot> snapshots = new ConcurrentHashMap<>();

    /* 低频定时刷新任务 */
    private BukkitTask refreshTask;

    /* 持有的插件实例引用 */
    private Plugin plugin;

    /**
     * 启用提供者：注册事件监听并启动定时刷新
     * @param plugin Bukkit 插件实例
     */
    public void enable(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        /* 立即做一次全量快照初始化，避免注册后初始周期内快照为空 */
        runSafely(this::refreshAllSnapshots, "初始全量快照刷新");
        refreshTask = Bukkit.getScheduler().runTaskTimer(plugin,
                () -> runSafely(this::refreshAllSnapshots, "定时全量快照刷新"), 20L, 20L);
    }

    /**
     * 停用提供者：注销事件监听、取消定时任务、清理快照
     */
    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
        snapshots.clear();
        plugin = null;
    }

    @Override
    public String name() {
        return NAME;
    }

    /**
     * 异步安全：仅读取快照，不访问 Bukkit/MagicCosmetic 可变运行态
     * @param owner 被骑乘者
     * @param viewer 观察者
     * @return 需要合并的外部 passenger id 集合
     */
    @Override
    public IntSet getPassengers(CNPlayer owner, CNPlayer viewer) {
        BackpackSnapshot snapshot = snapshots.get(owner.uuid());
        if (snapshot == null) {
            return IntSets.EMPTY_SET;
        }
        if (snapshot.bridgeId == 0) {
            return IntSets.EMPTY_SET;
        }
        if (!snapshot.viewerUUIDs.contains(viewer.uuid())) {
            return IntSets.EMPTY_SET;
        }
        IntSet result = new IntOpenHashSet(1);
        result.add(snapshot.bridgeId);
        return result;
    }

    /* ==================== 异常隔离 ==================== */

    /**
     * 安全执行 MagicCosmetic 相关操作，隔离运行态异常防止打死主线程任务
     * @param action 待执行的操作
     * @param context 操作描述，用于日志
     */
    private void runSafely(Runnable action, String context) {
        try {
            action.run();
        } catch (Throwable t) {
            if (plugin != null) {
                plugin.getLogger().log(Level.WARNING, "[MagicCosmetic] " + context + " 异常", t);
            }
        }
    }

    /* ==================== 事件监听（主线程） ==================== */

    /**
     * 玩家装饰数据加载完成，延迟刷新快照（等待背包生成）
     * 额外补一次更晚的刷新（20 tick），兜底 join 阶段 viewer 列表/bridge 尚未稳定的窗口
     * @param event 加载事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLoad(PlayerLoadEvent event) {
        UUID uuid = event.getUniqueId();
        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> runSafely(() -> refreshSnapshot(uuid), "onPlayerLoad 刷新快照"),
                5L
        );
        /* 兜底延迟：覆盖 join 早期 viewer/bridge 尚未就绪的窗口 */
        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> runSafely(() -> refreshSnapshot(uuid), "onPlayerLoad 兜底刷新快照"),
                20L
        );
    }

    /**
     * 背包装备完成后延迟 1 tick 刷新快照（等待背包实体建立完成，避免取到旧快照）
     * @param event 装备事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPostEquip(PlayerCosmeticPostEquipEvent event) {
        if (event.getCosmetic().getSlot() == CosmeticSlot.BACKPACK) {
            UUID uuid = event.getUniqueId();
            Bukkit.getScheduler().runTaskLater(plugin, () -> runSafely(() -> refreshSnapshot(uuid), "onPostEquip 刷新快照"), 1L);
        }
    }

    /**
     * 背包移除后延迟 1 tick 刷新快照（等待 MagicCosmetic 内部状态变更完成）
     * @param event 移除事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRemove(PlayerCosmeticRemoveEvent event) {
        if (event.getCosmetic().getSlot() == CosmeticSlot.BACKPACK) {
            UUID uuid = event.getUniqueId();
            Bukkit.getScheduler().runTaskLater(plugin, () -> runSafely(() -> refreshSnapshot(uuid), "onRemove 刷新快照"), 1L);
        }
    }

    /**
     * 装饰隐藏后延迟 1 tick 刷新快照（等待 MagicCosmetic 内部状态变更完成）
     * @param event 隐藏事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHide(PlayerCosmeticHideEvent event) {
        UUID uuid = event.getUniqueId();
        Bukkit.getScheduler().runTaskLater(plugin, () -> runSafely(() -> refreshSnapshot(uuid), "onHide 刷新快照"), 1L);
    }

    /**
     * 装饰显示后延迟 1 tick 刷新快照（等待 MagicCosmetic 内部状态变更完成）
     * @param event 显示事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShow(PlayerCosmeticShowEvent event) {
        UUID uuid = event.getUniqueId();
        Bukkit.getScheduler().runTaskLater(plugin, () -> runSafely(() -> refreshSnapshot(uuid), "onShow 刷新快照"), 1L);
    }

    /**
     * 玩家数据卸载时移除快照
     * @param event 卸载事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onUnload(PlayerUnloadEvent event) {
        snapshots.remove(event.getUniqueId());
    }

    /* ==================== 快照刷新逻辑（主线程） ==================== */

    /**
     * 刷新单个玩家的背包快照，检测变更后触发 passenger 重发
     * @param ownerUUID 玩家 UUID
     */
    private void refreshSnapshot(UUID ownerUUID) {
        BackpackSnapshot oldSnapshot = snapshots.get(ownerUUID);

        CosmeticUser user = CosmeticUsers.getUser(ownerUUID);
        if (user == null) {
            snapshots.remove(ownerUUID);
            if (oldSnapshot != null && oldSnapshot.bridgeId != 0) {
                triggerPassengerRefresh(ownerUUID);
            }
            return;
        }
        /* 衣柜中 / 全局隐藏 / 用户隐藏 → 无背包 */
        if (user.isInWardrobe() || Settings.isAllPlayersHidden() || user.isHidden()) {
            snapshots.put(ownerUUID, BackpackSnapshot.EMPTY);
            if (oldSnapshot != null && oldSnapshot.bridgeId != 0) {
                triggerPassengerRefresh(ownerUUID);
            }
            return;
        }
        UserBackpackManager backpackManager = user.getUserBackpackManager();
        if (backpackManager == null) {
            snapshots.put(ownerUUID, BackpackSnapshot.EMPTY);
            if (oldSnapshot != null && oldSnapshot.bridgeId != 0) {
                triggerPassengerRefresh(ownerUUID);
            }
            return;
        }
        int bridgeId = backpackManager.getBridgeArmorStandId1();
        if (bridgeId == 0 || backpackManager.isBackpackHidden()) {
            snapshots.put(ownerUUID, BackpackSnapshot.EMPTY);
            if (oldSnapshot != null && oldSnapshot.bridgeId != 0) {
                triggerPassengerRefresh(ownerUUID);
            }
            return;
        }
        UserEntity entityManager = backpackManager.getEntityManager();
        Set<UUID> viewerUUIDs;
        if (entityManager == null) {
            viewerUUIDs = Set.of();
        } else {
            List<Player> viewers = entityManager.getViewers();
            if (viewers == null || viewers.isEmpty()) {
                viewerUUIDs = Set.of();
            } else {
                Set<UUID> temp = new HashSet<>(viewers.size());
                for (Player p : viewers) {
                    temp.add(p.getUniqueId());
                }
                viewerUUIDs = Set.copyOf(temp);
            }
        }
        BackpackSnapshot newSnapshot = new BackpackSnapshot(bridgeId, viewerUUIDs);
        snapshots.put(ownerUUID, newSnapshot);

        /* 检测快照变更：bridgeId 变化或 viewer 集合变化时触发 passenger 重发 */
        if (oldSnapshot == null
                || oldSnapshot.bridgeId != newSnapshot.bridgeId
                || !oldSnapshot.viewerUUIDs.equals(newSnapshot.viewerUUIDs)) {
            triggerPassengerRefresh(ownerUUID);
        }
    }

    /**
     * 触发指定 owner 的 passenger 重发
     * @param ownerUUID 玩家 UUID
     */
    private void triggerPassengerRefresh(UUID ownerUUID) {
        CustomNameplates cn = CustomNameplates.getInstance();
        CNPlayer owner = cn.getPlayer(ownerUUID);
        if (owner == null) return;
        UnlimitedTagManager tagManager = cn.getUnlimitedTagManager();
        if (tagManager != null) {
            tagManager.refreshPassengers(owner);
        }
    }

    /**
     * 定时全量刷新所有在线玩家的背包快照
     */
    private void refreshAllSnapshots() {
        Set<UUID> activeUUIDs = new HashSet<>();
        for (CosmeticUser user : CosmeticUsers.values()) {
            UUID uuid = user.getUniqueId();
            activeUUIDs.add(uuid);
            refreshSnapshot(uuid);
        }
        /* 清理已离线玩家的残留快照 */
        snapshots.keySet().removeIf(uuid -> !activeUUIDs.contains(uuid));
    }

    /**
     * @author Mmu0621
     * @Desc 背包快照，不可变，线程安全
     * @date 2026-03-21
     */
    private static final class BackpackSnapshot {

        /* 空快照常量 */
        static final BackpackSnapshot EMPTY = new BackpackSnapshot(0, Set.of());

        /* 桥接盔甲架1实体ID */
        final int bridgeId;
        /* 可见该背包的观察者 UUID 集合（不可变） */
        final Set<UUID> viewerUUIDs;

        /**
         * 构造背包快照
         * @param bridgeId 桥接盔甲架1实体ID
         * @param viewerUUIDs 观察者 UUID 集合
         */
        BackpackSnapshot(int bridgeId, Set<UUID> viewerUUIDs) {
            this.bridgeId = bridgeId;
            this.viewerUUIDs = viewerUUIDs;
        }
    }
}
