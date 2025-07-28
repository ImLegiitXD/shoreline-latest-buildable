package net.shoreline.client.impl.module.misc;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
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
    Config<Boolean> switchxd = register(new BooleanConfig("Switch", "Automatically switch to shulker boxes when needed", true));

    private int timeoutTicks = 0;

    public AutoFrameDupeModule()
    {
        super("AutoFrameDupe", "Automatically dupes using frames", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (mc.world == null || mc.player == null || mc.interactionManager == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof ItemFrameEntity frame && mc.player.distanceTo(frame) <= range.getValue()) {
                if (timeoutTicks >= ticks.getValue()) {
                    ItemStack displayedItem = frame.getHeldItemStack();
                    boolean hasItem = !displayedItem.isEmpty();
                    boolean isHolding = !mc.player.getMainHandStack().isEmpty();

                    if (switchxd.getValue() && (!isHolding || !isShulkerBox(mc.player.getMainHandStack()))) {
                        int shulkerSlot = findShulkers();
                        if (shulkerSlot != -1) {
                            mc.player.getInventory().selectedSlot = shulkerSlot;
                            isHolding = true; 
                        }
                    }

                    if (!hasItem && isHolding) {
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


    private boolean isShulkerBox(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.SHULKER_BOX ||
                item == Items.WHITE_SHULKER_BOX ||
                item == Items.ORANGE_SHULKER_BOX ||
                item == Items.MAGENTA_SHULKER_BOX ||
                item == Items.LIGHT_BLUE_SHULKER_BOX ||
                item == Items.YELLOW_SHULKER_BOX ||
                item == Items.LIME_SHULKER_BOX ||
                item == Items.PINK_SHULKER_BOX ||
                item == Items.GRAY_SHULKER_BOX ||
                item == Items.LIGHT_GRAY_SHULKER_BOX ||
                item == Items.CYAN_SHULKER_BOX ||
                item == Items.PURPLE_SHULKER_BOX ||
                item == Items.BLUE_SHULKER_BOX ||
                item == Items.BROWN_SHULKER_BOX ||
                item == Items.GREEN_SHULKER_BOX ||
                item == Items.RED_SHULKER_BOX ||
                item == Items.BLACK_SHULKER_BOX;
    }

    private int findShulkers() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (isShulkerBox(stack)) {
                return i;
            }
        }
        return -1;
    }

}