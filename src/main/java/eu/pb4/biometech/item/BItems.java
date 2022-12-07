package eu.pb4.biometech.item;

import eu.pb4.biometech.block.BBlocks;
import eu.pb4.biometech.data.BDataGenerator;
import eu.pb4.biometech.util.BBiomeTags;
import eu.pb4.biometech.util.ModUtil;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;

import static eu.pb4.biometech.util.ModUtil.id;

public class BItems {


    public static final Item BIOME_CONVERTER = register("biome_converter", new BiomeConverterBlockItem(BBlocks.BIOME_CONVERTER, new Item.Settings()));
    public static final Item POCKET_BIOME_CONVERTER = register("pocket_biome_converter", new PocketBiomeConverterItem(new Item.Settings().maxCount(1)));
    public static final Item BIOME_ESSENCE = register("biome_essence", new BiomeEssenceItem(new Item.Settings().maxCount(16)));

    public static final Item UI_BUTTON = register("ui_button", new ButtonItem(new Item.Settings().maxCount(1)));

    public static final ItemGroup GROUP = PolymerItemGroupUtils.builder(id("group"))
            .displayName(Text.literal("Biome Technologies™"))
            .icon(() -> BItems.BIOME_CONVERTER.getDefaultStack())
            .entries((f, e, op) -> {
                e.add(BIOME_CONVERTER);

                if (ModUtil.server != null) {
                    var reg = ModUtil.server.getRegistryManager().get(RegistryKeys.BIOME);
                    var keys = new ArrayList<>(reg.getKeys());
                    keys.sort(Comparator.comparing(RegistryKey::getValue));
                    for (var biome : keys) {
                        if (!reg.getEntry(biome).get().isIn(BBiomeTags.BANNED_BIOMES)) {
                            var stack = new ItemStack(BIOME_ESSENCE);
                            stack.getOrCreateNbt().putString("Biome", biome.getValue().toString());
                            e.add(stack);
                        }
                    }
                }

            }).build();

    public static void register() {

    }
    private static <T extends Item> T register(String path, T block) {
        return Registry.register(Registries.ITEM, id(path), block);
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
