package eu.pb4.biometech.util;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.util.function.Function;

import static eu.pb4.biometech.util.ModUtil.id;

public class BBiomeTags {
    public static final TagKey<Biome> BANNED_BIOMES = TagKey.of(RegistryKeys.BIOME, id("banned_biomes"));
    public static final TagKey<Biome> TRADER_BANNED_BIOMES = TagKey.of(RegistryKeys.BIOME, id("trader_banned_biomes"));

    public static void createTags(Function<TagKey<Biome>, FabricTagProvider<Biome>.FabricTagBuilder> provider) {
        provider.apply(BANNED_BIOMES).add(BiomeKeys.THE_VOID).addOptional(new Identifier("terrablender", "deferred_placeholder"));

        provider.apply(TRADER_BANNED_BIOMES)
                .add(BiomeKeys.DEEP_DARK)
                .add(BiomeKeys.MUSHROOM_FIELDS)
                .addOptionalTag(BiomeTags.IS_NETHER)
                .addOptionalTag(BiomeTags.IS_END);
    }
}
