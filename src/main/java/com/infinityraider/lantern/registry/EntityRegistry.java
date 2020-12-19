package com.infinityraider.lantern.registry;

import com.infinityraider.infinitylib.entity.EntityTypeBase;
import com.infinityraider.infinitylib.reference.Constants;
import com.infinityraider.lantern.entity.EntityLantern;
import com.infinityraider.lantern.reference.Names;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;

public class EntityRegistry {
    private static final EntityRegistry INSTANCE = new EntityRegistry();

    public static EntityRegistry getInstance() {
        return INSTANCE;
    }

    public final EntityType<EntityLantern> entityLantern;

    private EntityRegistry() {
        this.entityLantern = EntityTypeBase.entityTypeBuilder(Names.Blocks.LANTERN, EntityLantern.class, EntityLantern.SpawnFactory.getInstance(),
                EntityClassification.MISC, EntitySize.fixed(6 * Constants.UNIT, 11 * Constants.UNIT))
                .setTrackingRange(32)
                .setVelocityUpdates(true)
                .setUpdateInterval(1)
                .setRenderFactory(EntityLantern.RenderFactory.getInstance())
                .build();
    }
}
