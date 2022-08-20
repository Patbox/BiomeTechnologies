package eu.pb4.biometech.data;

import eu.pb4.biometech.block.BBlocks;
import eu.pb4.biometech.item.BItems;
import eu.pb4.biometech.util.BBiomeTags;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;

import java.util.function.Consumer;

public class BDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        fabricDataGenerator.addProvider(RecipeProvider::new);
        fabricDataGenerator.addProvider(BlockTagProvider::new);
        fabricDataGenerator.addProvider(BiomeTagProvider::new);
        fabricDataGenerator.addProvider(BlockLootTableProvider::new);

        /*fabricDataGenerator.addProvider(ItemTagProvider::new);*/
    }

    private static class RecipeProvider extends FabricRecipeProvider {
        private RecipeProvider(FabricDataGenerator dataGenerator) {
            super(dataGenerator);
        }

        @Override
        protected void generateRecipes(Consumer<RecipeJsonProvider> exporter) {
            BItems.createRecipes(new RecipeBuilder(exporter));
        }
    }

    public record RecipeBuilder(Consumer<RecipeJsonProvider> exporter) {
        public void createShapeless(Item result, int count, Object[] items, Consumer<CraftingRecipeJsonBuilder> modifier) {
            var b = new ShapelessRecipeJsonBuilder(result, count);

            for (var obj : items) {
                if (obj instanceof ItemConvertible item) {
                    b.input(item);
                } else if (obj instanceof TagKey item) {
                    b.input(item);
                } else if (obj instanceof Ingredient item) {
                    b.input(item);
                }
            }

            modifier.accept(b);

            b.offerTo(exporter);
        }

        public void createShaped(Item result, int count, Consumer<ShapedRecipeJsonBuilder> modifier) {
            var b = new ShapedRecipeJsonBuilder(result, count);
            modifier.accept(b);
            b.offerTo(exporter);
        }
    }

    private static class BlockTagProvider extends FabricTagProvider.BlockTagProvider {
        private BlockTagProvider(FabricDataGenerator dataGenerator) {
            super(dataGenerator);
        }

        @Override
        protected void generateTags() {
            BBlocks.createTags((tag) -> this.getOrCreateTagBuilder(tag));
        }
    }

    public static class BiomeTagProvider extends FabricTagProvider.DynamicRegistryTagProvider<Biome> {
        protected BiomeTagProvider(FabricDataGenerator dataGenerator) {
            super(dataGenerator, Registry.BIOME_KEY);
        }

        @Override
        protected void generateTags() {
            BBiomeTags.createTags((tag) -> this.getOrCreateTagBuilder(tag));

        }

    }

    public static class BlockLootTableProvider extends FabricBlockLootTableProvider {
        private BlockLootTableProvider(FabricDataGenerator dataGenerator) {
            super(dataGenerator);
        }

        @Override
        protected void generateBlockLootTables() {
            BBlocks.createDrops(this);
        }
    }


    /*public static void createSimpleDrop(Block block) {
        PACK.addLootTable(id(block.getLootTableId().toString()),
                loot("minecraft:block")
                        .pool(pool()
                                .rolls(1)
                                .entry(entry()
                                        .type("minecraft:item")
                                        .name(Registry.ITEM.getId(block.asItem()).toString()))
                                .condition(predicate("minecraft:survives_explosion")))
        );
    }*/
}
