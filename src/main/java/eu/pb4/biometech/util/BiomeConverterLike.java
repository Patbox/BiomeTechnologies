package eu.pb4.biometech.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;

public interface BiomeConverterLike {
    Text getConvName();

    boolean shouldClose(ServerPlayerEntity player);

    Inventory fuelInventory();

    Inventory essenceInventory();

    boolean isActivated();

    int radius();

    RegistryKey<Biome> currentBiomeId();

    int energy();

    void setBiome(RegistryKey<Biome> key);

    void setActive(boolean active, GameProfile gameProfile);

    void radius(int i);
}
