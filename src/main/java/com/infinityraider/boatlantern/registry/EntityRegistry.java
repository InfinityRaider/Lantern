package com.infinityraider.boatlantern.registry;

import com.infinityraider.boatlantern.entity.EntityBoatLantern;
import com.infinityraider.boatlantern.reference.Reference;
import com.infinityraider.infinitylib.entity.EntityRegistryEntry;

public class EntityRegistry {
    private static final EntityRegistry INSTANCE = new EntityRegistry();

    public static EntityRegistry getInstance() {
        return INSTANCE;
    }

    public final EntityRegistryEntry<EntityBoatLantern> entityBoatLantern;

    private EntityRegistry() {
        this.entityBoatLantern = new EntityRegistryEntry<>(EntityBoatLantern.class, Reference.MOD_ID.toLowerCase() + ".entityBoatLantern")
                .setTrackingDistance(64)
                .setVelocityUpdates(true)
                .setUpdateFrequency(1)
                .setRenderFactory(EntityBoatLantern.RenderFactory.FACTORY);
    }
}
