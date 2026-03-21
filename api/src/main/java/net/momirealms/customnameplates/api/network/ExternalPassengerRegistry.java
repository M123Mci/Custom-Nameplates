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

package net.momirealms.customnameplates.api.network;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.momirealms.customnameplates.api.CNPlayer;
import net.momirealms.customnameplates.api.CustomNameplates;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mmu0621
 * @Desc 外部 passenger 提供者注册表，线程安全，异常隔离
 * @date 2026-03-21
 */
public final class ExternalPassengerRegistry {

    /* 已注册的提供者，key 为 provider.name() */
    private static final ConcurrentHashMap<String, ExternalPassengerProvider> PROVIDERS = new ConcurrentHashMap<>();

    private ExternalPassengerRegistry() {
    }

    /**
     * 注册一个外部 passenger 提供者；若同名提供者已存在，先安全 disable 旧实例再替换
     *
     * @param provider 提供者实例
     */
    public static void register(ExternalPassengerProvider provider) {
        ExternalPassengerProvider old = PROVIDERS.put(provider.name(), provider);
        if (old != null && old != provider) {
            try {
                old.disable();
            } catch (Throwable t) {
                CustomNameplates.getInstance().getPluginLogger()
                        .warn("ExternalPassengerProvider [" + provider.name() + "] 旧实例 disable 异常", t);
            }
        }
    }

    /**
     * 注销一个外部 passenger 提供者，并调用其 disable() 清理资源
     *
     * @param name 提供者标识名
     */
    public static void unregister(String name) {
        ExternalPassengerProvider removed = PROVIDERS.remove(name);
        if (removed != null) {
            try {
                removed.disable();
            } catch (Throwable t) {
                CustomNameplates.getInstance().getPluginLogger()
                        .warn("ExternalPassengerProvider [" + name + "] disable threw exception", t);
            }
        }
    }

    /**
     * 注销所有已注册提供者，逐个调用 disable() 清理资源
     */
    public static void unregisterAll() {
        for (String name : PROVIDERS.keySet()) {
            unregister(name);
        }
    }

    /**
     * 查询所有已注册提供者，合并返回指定 owner 对指定 viewer 的外部 passenger id 集合
     *
     * @param owner 被骑乘者
     * @param viewer 观察者
     * @return 合并后的外部 passenger id 集合
     */
    public static IntSet collectPassengers(CNPlayer owner, CNPlayer viewer) {
        IntSet result = new IntOpenHashSet();
        for (ExternalPassengerProvider provider : PROVIDERS.values()) {
            try {
                IntSet ids = provider.getPassengers(owner, viewer);
                if (ids != null && !ids.isEmpty()) {
                    result.addAll(ids);
                }
            } catch (Throwable t) {
                CustomNameplates.getInstance().getPluginLogger()
                        .warn("ExternalPassengerProvider [" + provider.name() + "] threw exception", t);
            }
        }
        return result;
    }
}
