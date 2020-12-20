package com.infinityraider.lantern.lantern;

import com.infinityraider.lantern.item.ItemLantern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.tuple.MutablePair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class LanternItemCache {
    private static final LanternItemCache INSTANCE = new LanternItemCache();

    public static LanternItemCache getInstance() {
        return INSTANCE;
    }

    public static final int PURGE_PERIOD = 20;

    private Map<ItemStack, MutablePair<ItemHandlerLantern, Integer>> cache;

    private LanternItemCache() {
        this.cache = new IdentityHashMap<>();
    }

    @Nullable
    public ItemHandlerLantern getLantern(LivingEntity entity, Hand hand) {
        return this.getLantern(entity, entity.getHeldItem(hand));
    }

    @Nullable
    public ItemHandlerLantern getLantern(Entity entity, ItemStack stack) {
        ItemHandlerLantern lantern = this.getLantern(stack);
        return lantern == null ? null : lantern.setEntity(entity);
    }

    @Nullable
    public ItemHandlerLantern getLantern(ItemStack stack) {
        if(stack != null && stack.getItem() instanceof ItemLantern) {
            if(cache.containsKey(stack)) {
                MutablePair<ItemHandlerLantern, Integer> value = cache.get(stack);
                value.setRight(0);
                return value.getLeft();
            } else {
                ItemHandlerLantern lantern = new ItemHandlerLantern(stack);
                cache.put(stack, new MutablePair<>(lantern, 0));
                return lantern;
            }
        }
        return null;
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onTick(TickEvent.WorldTickEvent event) {
        List<ItemStack> purgeList = new ArrayList<>();
        if(event.phase == TickEvent.Phase.END) {
            for(Map.Entry<ItemStack, MutablePair<ItemHandlerLantern, Integer>> entry : this.cache.entrySet()) {
                MutablePair<ItemHandlerLantern, Integer> value = entry.getValue();
                value.setRight(value.getRight() + 1);
                if(value.getRight() >= PURGE_PERIOD) {
                    purgeList.add(entry.getKey());
                }
            }
            for(ItemStack stack : purgeList) {
                cache.remove(stack);
            }
        }
    }
}
