package com.infinityraider.boatlantern.registry;

import com.infinityraider.boatlantern.entity.EntityLantern;
import com.infinityraider.boatlantern.reference.Reference;
import com.infinityraider.infinitylib.entity.EntityRegistryEntry;

public class EntityRegistry {
    private static final EntityRegistry INSTANCE = new EntityRegistry();

    public static EntityRegistry getInstance() {
        return INSTANCE;
    }

    public final EntityRegistryEntry<EntityLantern> entityLantern;

    private EntityRegistry() {
        this.entityLantern = new EntityRegistryEntry<>(EntityLantern.class, Reference.MOD_ID.toLowerCase() + ".entityLantern")
                .setTrackingDistance(32)
                .setVelocityUpdates(true)
                .setUpdateFrequency(1)
                .setRenderFactory(EntityLantern.RenderFactory.FACTORY);
    }
}
