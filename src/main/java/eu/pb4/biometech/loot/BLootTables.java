package eu.pb4.biometech.loot;

import eu.pb4.biometech.mixin.LootPoolAccessor;
import eu.pb4.biometech.mixin.LootTableBuilderAccessor;
import eu.pb4.biometech.mixin.SetCountLootFunctionAccessor;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableSource;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import static eu.pb4.biometech.util.ModUtil.id;

public class BLootTables {
    public static final LootPoolEntryType BIOME_ESSENCE = Registry.register(Registries.LOOT_POOL_ENTRY_TYPE, id("biome_essence"), new LootPoolEntryType(new BiomeEssenceEntry.Serializer()));

    public static void register() {
        LootTableEvents.MODIFY.register(BLootTables::modifyLoot);
    }

    private static void modifyLoot(ResourceManager resourceManager, LootManager lootManager, Identifier identifier, LootTable.Builder builder, LootTableSource source) {
        if (source.isBuiltin() && identifier.equals(LootTables.PIGLIN_BARTERING_GAMEPLAY)) {
            var og = ((LootTableBuilderAccessor) builder).getPools().get(0);

            var list = new ArrayList<>(List.of(og.entries));
            list.add(new BiomeEssenceEntry(20, 0, new LootCondition[0], new LootFunction[]{
                    SetCountLootFunctionAccessor.createSetCountLootFunction(new LootCondition[0], UniformLootNumberProvider.create(1, 6), true)
            }, BiomeTags.IS_NETHER));

            ((LootTableBuilderAccessor) builder).getPools().set(0, LootPoolAccessor.createLootPool(list.toArray(new LootPoolEntry[0]), og.conditions, og.functions, og.rolls, og.bonusRolls));
        } else if (source.isBuiltin() && identifier.equals(LootTables.END_CITY_TREASURE_CHEST)) {
            var og = ((LootTableBuilderAccessor) builder).getPools().get(0);

            var list = new ArrayList<>(List.of(og.entries));
            list.add(new BiomeEssenceEntry(8, 0, new LootCondition[0], new LootFunction[]{
                    SetCountLootFunctionAccessor.createSetCountLootFunction(new LootCondition[0], UniformLootNumberProvider.create(3, 12), true)
            }, BiomeTags.IS_END));

            ((LootTableBuilderAccessor) builder).getPools().set(0, LootPoolAccessor.createLootPool(list.toArray(new LootPoolEntry[0]), og.conditions, og.functions, og.rolls, og.bonusRolls));
        } else if (source.isBuiltin() && identifier.equals(LootTables.SHIPWRECK_TREASURE_CHEST)) {
            var og = ((LootTableBuilderAccessor) builder).getPools().get(0);

            var list = new ArrayList<>(List.of(og.entries));
            list.add(new BiomeEssenceEntry(10, 0, new LootCondition[0], new LootFunction[]{
                    SetCountLootFunctionAccessor.createSetCountLootFunction(new LootCondition[0], UniformLootNumberProvider.create(6, 20), true)
            }, BiomeTags.IS_OCEAN));

            ((LootTableBuilderAccessor) builder).getPools().set(0, LootPoolAccessor.createLootPool(list.toArray(new LootPoolEntry[0]), og.conditions, og.functions, og.rolls, og.bonusRolls));
        }
    }
}
