package com.infinityraider.boatlantern.handler;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.IdentityHashMap;
import java.util.Map;

public class LightingHandler {
    private static final LightingHandler INSTANCE = new LightingHandler();

    public static LightingHandler getInstance() {
        return INSTANCE;
    }

    private final Map<Entity, BlockPos> lights;

    private LightingHandler() {
        this.lights = new IdentityHashMap<>();
    }

    public void spreadLight(Entity entity) {
        if(!lights.containsKey(entity)) {

        } else {
            this.removeLastLight(entity);
        }
    }

    public void removeLastLight(Entity entity) {
        if(lights.containsKey(entity)) {
            BlockPos light = lights.get(entity);
            lights.remove(entity);
        }
    }
}
