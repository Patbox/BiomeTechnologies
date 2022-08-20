package eu.pb4.biometech.mixin;

import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ArmorStandEntity.class)
public interface ArmorStandEntityAccessor {
    @Invoker
    void callSetMarker(boolean marker);

    @Invoker
    void callSetSmall(boolean small);
}
