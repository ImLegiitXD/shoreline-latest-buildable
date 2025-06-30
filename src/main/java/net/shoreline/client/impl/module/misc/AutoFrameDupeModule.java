package net.shoreline.client.impl.module.misc;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.eventbus.annotation.EventListener;

/**
 * @author ImLegiitXD
 * @since 1.0
 */

// pasted from ???
public class AutoFrameDupeModule extends ToggleModule
{
    Config<Float> range = register(new NumberConfig<>("Range", "The maximum distance to interact with item frames", 1f, 5f, 7f));
    Config<Integer> turns = register(new NumberConfig<>("Turns", "How many times to rotate the item in the frame", 1, 5, 10));
    Config<Integer> ticks = register(new NumberConfig<>("Ticks", "Delay between interactions in ticks", 1, 5, 10));

    private int timeoutTicks = 0;

    public AutoFrameDupeModule()
    {
        super("AutoFrameDupe", "Automatically dupes using frames", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null || mc.interactionManager == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof ItemFrameEntity frame && mc.player.distanceTo(frame) <= range.getValue()) {
                if (timeoutTicks >= ticks.getValue()) {
                    ItemStack displayedItem = frame.getHeldItemStack();
                    boolean hasItem = !displayedItem.isEmpty();
                    boolean playerHoldingItem = !mc.player.getMainHandStack().isEmpty();

                    if (!hasItem && playerHoldingItem) {
                        mc.interactionManager.interactEntity(mc.player, frame, Hand.MAIN_HAND);
                    }

                    if (hasItem) {
                        for (int i = 0; i < turns.getValue(); i++) {
                            mc.interactionManager.interactEntity(mc.player, frame, Hand.MAIN_HAND);
                        }
                        mc.interactionManager.attackEntity(mc.player, frame);
                    }

                    timeoutTicks = 0;
                } else {
                    timeoutTicks++;
                }
            }
        }
    }
}