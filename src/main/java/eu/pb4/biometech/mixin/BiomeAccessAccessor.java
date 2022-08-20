package eu.pb4.biometech.mixin;

import net.minecraft.world.biome.source.BiomeAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BiomeAccess.class)
public interface BiomeAccessAccessor {
    @Invoker
    static double callMethod_38106(long l, int i, int j, int k, double d, double e, double f) {
        throw new UnsupportedOperationException();
    }

    @Accessor
    long getSeed();
}
