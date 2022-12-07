package eu.pb4.biometech.block;

import eu.pb4.biometech.data.BDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

import java.util.function.Function;

import static eu.pb4.biometech.util.ModUtil.id;

public class BBlocks {
    private static final AbstractBlock.TypedContextPredicate<EntityType<?>> BLOCK_SPAWNS = (state, world, pos, type) -> false;

    public static final Block BIOME_CONVERTER = register("biome_converter", new BiomeConverterBlock(
            AbstractBlock.Settings.of(Material.AMETHYST).strength(3.0F).luminance((ctx -> ctx.get(BiomeConverterBlock.ACTIVE) ? 14 : 5)).nonOpaque()
    ));


    public static void register() {

    }

    private static <T extends Block> T register(String path, T block) {
        return Registry.register(Registries.BLOCK, id(path), block);
    }

    public static void createDrops(BDataGenerator.BlockLootTableProvider provider) {
        provider.addDrop(BIOME_CONVERTER);
    }

    public static void createTags(Function<TagKey<Block>, FabricTagProvider<Block>.FabricTagBuilder> provider) {
        provider.apply(BlockTags.PICKAXE_MINEABLE).add(BIOME_CONVERTER);
    }
}
