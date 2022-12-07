package eu.pb4.biometech.data;

import eu.pb4.biometech.block.BBlocks;
import eu.pb4.biometech.item.BItems;
import eu.pb4.biometech.util.BBiomeTags;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.biome.Biome;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class BDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        var pack = fabricDataGenerator.createPack();
        pack.addProvider(RecipeProvider::new);
        pack.addProvider(BlockTagProvider::new);
        pack.addProvider(BiomeTagProvider::new);
        pack.addProvider(BlockLootTableProvider::new);

        /*fabricDataGenerator.addProvider(ItemTagProvider::new);*/
    }

    private static class RecipeProvider extends FabricRecipeProvider {

        public RecipeProvider(FabricDataOutput output) {
            super(output);
        }

        @Override
        public void generate(Consumer<RecipeJsonProvider> exporter) {
            BItems.createRecipes(new RecipeBuilder(exporter));
        }
    }

    public record RecipeBuilder(Consumer<RecipeJsonProvider> exporter) {
        public void createShapeless(Item result, int count, Object[] items, Consumer<CraftingRecipeJsonBuilder> modifier) {
            var b = new ShapelessRecipeJsonBuilder(RecipeCategory.MISC, result, count);

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
            var b = new ShapedRecipeJsonBuilder(RecipeCategory.MISC, result, count);
            modifier.accept(b);
            b.offerTo(exporter);
        }
    }

    private static class BlockTagProvider extends FabricTagProvider.BlockTagProvider {
        public BlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup arg) {
            BBlocks.createTags((tag) -> this.getOrCreateTagBuilder(tag));

        }
    }

    public static class BiomeTagProvider extends FabricTagProvider<Biome> {


        public BiomeTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, RegistryKeys.BIOME, registriesFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup arg) {
            BBiomeTags.createTags((tag) -> this.getOrCreateTagBuilder(tag));
        }
    }

    public static class BlockLootTableProvider extends FabricBlockLootTableProvider {
        protected BlockLootTableProvider(FabricDataOutput dataOutput) {
            super(dataOutput);
        }

        @Override
        public void generate() {
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
