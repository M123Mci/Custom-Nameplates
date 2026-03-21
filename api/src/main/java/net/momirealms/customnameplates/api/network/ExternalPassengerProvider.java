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

import it.unimi.dsi.fastutil.ints.IntSet;
import net.momirealms.customnameplates.api.CNPlayer;

/**
 * @author Mmu0621
 * @Desc 外部插件 passenger 提供者接口，用于在 mount 包合并时查询外部 passenger
 * @date 2026-03-21
 */
public interface ExternalPassengerProvider {

    /**
     * 查询指定 owner 对指定 viewer 应附加的外部 passenger entity id 集合
     *
     * @param owner 骑乘载具的玩家（被骑乘者）
     * @param viewer 观察者
     * @return 需要合并的外部 passenger id 集合，无则返回空集合
     */
    IntSet getPassengers(CNPlayer owner, CNPlayer viewer);

    /**
     * 提供者的唯一标识名
     *
     * @return 标识名
     */
    String name();

    /**
     * 生命周期：注销时调用，用于清理资源
     */
    default void disable() {
    }
}
