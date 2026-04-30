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

package net.momirealms.customnameplates.bukkit.util;

import net.momirealms.customnameplates.common.util.ReflectionUtils;

public class EntityDataValue {

    public static final Object Serializers$BYTE;
    public static final Object Serializers$INT;
    public static final Object Serializers$LONG;
    public static final Object Serializers$FLOAT;
    public static final Object Serializers$STRING;
    public static final Object Serializers$COMPONENT;
    public static final Object Serializers$OPTIONAL_COMPONENT;
    public static final Object Serializers$ITEM_STACK;
    public static final Object Serializers$BLOCK_STATE;
    public static final Object Serializers$OPTIONAL_BLOCK_STATE;
    public static final Object Serializers$BOOLEAN;
    public static final Object Serializers$PARTICLE;
    public static final Object Serializers$PARTICLES;
    public static final Object Serializers$ROTATIONS;
    public static final Object Serializers$BLOCK_POS;
    public static final Object Serializers$OPTIONAL_BLOCK_POS;
    public static final Object Serializers$DIRECTION;
    public static final Object Serializers$OPTIONAL_UUID;
    public static final Object Serializers$OPTIONAL_LIVING_ENTITY_REFERENCE;
    public static final Object Serializers$OPTIONAL_GLOBAL_POS;
    public static final Object Serializers$COMPOUND_TAG;
    public static final Object Serializers$VILLAGER_DATA;
    public static final Object Serializers$OPTIONAL_UNSIGNED_INT;
    public static final Object Serializers$POSE;
    public static final Object Serializers$CAT_VARIANT;
    public static final Object Serializers$WOLF_VARIANT;
    public static final Object Serializers$WOLF_SOUND_VARIANT;
    public static final Object Serializers$FROG_VARIANT;
    public static final Object Serializers$PIG_VARIANT;
    public static final Object Serializers$PIG_SOUND_VARIANT;
    public static final Object Serializers$ZOMBIE_NAUTILUS_VARIANT;
    public static final Object Serializers$PAINTING_VARIANT;
    public static final Object Serializers$ARMADILLO_STATE;
    public static final Object Serializers$SNIFFER_STATE;
    public static final Object Serializers$WEATHERING_COPPER_STATE;
    public static final Object Serializers$COPPER_GOLEM_STATE;
    public static final Object Serializers$VECTOR3;
    public static final Object Serializers$QUATERNION;
    public static final Object Serializers$RESOLVABLE_PROFILE;
    public static final Object Serializers$HUMANOID_ARM;

    static {
        try {
            Serializers$BYTE = initSerializersByName("BYTE");
            Serializers$INT = initSerializersByName("INT");
            Serializers$LONG = initSerializersByName("LONG");
            Serializers$FLOAT = initSerializersByName("FLOAT");
            Serializers$STRING = initSerializersByName("STRING");
            Serializers$COMPONENT = initSerializersByName("COMPONENT");
            Serializers$OPTIONAL_COMPONENT = initSerializersByName("OPTIONAL_COMPONENT");
            Serializers$ITEM_STACK = initSerializersByName("ITEM_STACK");
            Serializers$BLOCK_STATE = initSerializersByName("BLOCK_STATE");
            Serializers$OPTIONAL_BLOCK_STATE = initSerializersByName("OPTIONAL_BLOCK_STATE");
            Serializers$BOOLEAN = initSerializersByName("BOOLEAN");
            Serializers$PARTICLE = initSerializersByName("PARTICLE");
            Serializers$PARTICLES = initSerializersByName("PARTICLES");
            Serializers$ROTATIONS = initSerializersByName("ROTATIONS");
            Serializers$BLOCK_POS = initSerializersByName("BLOCK_POS");
            Serializers$OPTIONAL_BLOCK_POS = initSerializersByName("OPTIONAL_BLOCK_POS");
            Serializers$DIRECTION = initSerializersByName("DIRECTION");
            Serializers$OPTIONAL_UUID = null;
            Serializers$OPTIONAL_LIVING_ENTITY_REFERENCE = initSerializersByName("OPTIONAL_LIVING_ENTITY_REFERENCE");
            Serializers$OPTIONAL_GLOBAL_POS = initSerializersByName("OPTIONAL_GLOBAL_POS");
            Serializers$COMPOUND_TAG = initSerializersByName("COMPOUND_TAG");
            Serializers$VILLAGER_DATA = initSerializersByName("VILLAGER_DATA");
            Serializers$OPTIONAL_UNSIGNED_INT = initSerializersByName("OPTIONAL_UNSIGNED_INT");
            Serializers$POSE = initSerializersByName("POSE");
            Serializers$CAT_VARIANT = initSerializersByName("CAT_VARIANT");
            Serializers$WOLF_VARIANT = initSerializersByName("WOLF_VARIANT");
            Serializers$WOLF_SOUND_VARIANT = initSerializersByName("WOLF_SOUND_VARIANT");
            Serializers$FROG_VARIANT = initSerializersByName("FROG_VARIANT");
            Serializers$PIG_VARIANT = initSerializersByName("PIG_VARIANT");
            Serializers$PIG_SOUND_VARIANT = initSerializersByName("PIG_SOUND_VARIANT");
            Serializers$ZOMBIE_NAUTILUS_VARIANT = initSerializersByName("ZOMBIE_NAUTILUS_VARIANT");
            Serializers$PAINTING_VARIANT = initSerializersByName("PAINTING_VARIANT");
            Serializers$ARMADILLO_STATE = initSerializersByName("ARMADILLO_STATE");
            Serializers$SNIFFER_STATE = initSerializersByName("SNIFFER_STATE");
            Serializers$WEATHERING_COPPER_STATE = initSerializersByName("WEATHERING_COPPER_STATE");
            Serializers$COPPER_GOLEM_STATE = initSerializersByName("COPPER_GOLEM_STATE");
            Serializers$VECTOR3 = initSerializersByName("VECTOR3");
            Serializers$QUATERNION = initSerializersByName("QUATERNION");
            Serializers$RESOLVABLE_PROFILE = initSerializersByName("RESOLVABLE_PROFILE");
            Serializers$HUMANOID_ARM = initSerializersByName("HUMANOID_ARM");
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static Object initSerializersByName(String name) throws ReflectiveOperationException {
        return ReflectionUtils.getDeclaredField(Reflections.clazz$EntityDataSerializers, name).get(null);
    }

    private EntityDataValue() {
        throw new IllegalAccessError("Utility class");
    }

    public static Object create(int id, Object serializer, Object value) {
        try {
            Object entityDataAccessor = Reflections.constructor$EntityDataAccessor.newInstance(id, serializer);
            return Reflections.method$SynchedEntityData$DataValue$create.invoke(null, entityDataAccessor, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
