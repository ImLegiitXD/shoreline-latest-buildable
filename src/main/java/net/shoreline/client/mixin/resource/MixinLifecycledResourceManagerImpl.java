package net.shoreline.client.mixin.resource;

import net.minecraft.resource.LifecycledResourceManagerImpl;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Injects our custom resource manager to allow for remote resource loading (out of dev environments)
 *
 * @author bon
 */
@Mixin(LifecycledResourceManagerImpl.class)
public final class MixinLifecycledResourceManagerImpl
{
}
