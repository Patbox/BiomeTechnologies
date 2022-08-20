package eu.pb4.biometech.item;

import eu.pb4.biometech.block.BBlocks;
import eu.pb4.biometech.data.BDataGenerator;
import eu.pb4.polymer.api.item.PolymerItemGroup;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;

import static eu.pb4.biometech.util.ModUtil.id;

public class BItems {
    public static final ItemGroup GROUP = PolymerItemGroup.create(id("group"), Text.literal("Biome Technologies™"), () -> BItems.BIOME_CONVERTER.getDefaultStack());

    public static final Item BIOME_CONVERTER = register("biome_converter", new BiomeConverterBlockItem(BBlocks.BIOME_CONVERTER, new Item.Settings().group(GROUP)));
    public static final Item BIOME_ESSENCE = register("biome_essence", new BiomeEssenceItem(new Item.Settings().group(GROUP).maxCount(16)));
    public static final Item UI_BUTTON = register("ui_button", new ButtonItem(new Item.Settings().maxCount(1)));


    public static void register() {

    }
    private static <T extends Item> T register(String path, T block) {
        return Registry.register(Registry.ITEM, id(path), block);
    }

    public static void createRecipes(BDataGenerator.RecipeBuilder recipeBuilder) {
        recipeBuilder.createShaped(BIOME_CONVERTER, 1, (b) -> {
            b.pattern("XOX");
            b.pattern("ASA");
            b.pattern("XCX");
            b.input('X', Items.COBBLED_DEEPSLATE);
            b.input('A', Items.AMETHYST_BLOCK);
            b.input('S', Items.NETHER_STAR);
            b.input('C', Items.CLAY);
            b.input('O', Items.DISPENSER);

            b.criterion("item_required", InventoryChangedCriterion.Conditions.items(Items.AMETHYST_BLOCK));
            b.criterion("item_required2", InventoryChangedCriterion.Conditions.items(Items.NETHER_STAR));
            b.criterion("item_required3", InventoryChangedCriterion.Conditions.items(Items.DISPENSER));
            b.criterion("item_use", InventoryChangedCriterion.Conditions.items(BIOME_ESSENCE));
        });
    }
}
