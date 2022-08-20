package eu.pb4.biometech.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import eu.pb4.biometech.item.BItems;
import eu.pb4.biometech.util.BBiomeTags;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BiomeEssenceEntry extends LeafEntry {
    private final TagKey<Biome> tagSource;

    protected BiomeEssenceEntry(int weight, int quality, LootCondition[] conditions, LootFunction[] functions, @Nullable TagKey<Biome> source) {
        super(weight, quality, conditions, functions);
        this.tagSource = source;
    }

    @Override
    protected void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
        var stream = context.getWorld().getRegistryManager().get(Registry.BIOME_KEY).streamEntries();

        if (this.tagSource != null) {
            stream = stream.filter(x -> x.isIn(this.tagSource));
        } else {
            stream = stream.filter(x -> !x.isIn(BBiomeTags.BANNED_BIOMES));
        }

        var list = stream.collect(Collectors.toList());

        if (!list.isEmpty()) {
            var biome = list.get(context.getRandom().nextInt(list.size()));

            var stack = new ItemStack(BItems.BIOME_ESSENCE);
            stack.getOrCreateNbt().putString("Biome", biome.getKey().get().getValue().toString());

            lootConsumer.accept(stack);
        }
    }

    @Override
    public LootPoolEntryType getType() {
        return BLootTables.BIOME_ESSENCE;
    }

    public static final class Serializer extends LeafEntry.Serializer<BiomeEssenceEntry> {
        @Override
        public void addEntryFields(JsonObject jsonObject, BiomeEssenceEntry leafEntry, JsonSerializationContext jsonSerializationContext) {
            super.addEntryFields(jsonObject, leafEntry, jsonSerializationContext);

            if (leafEntry.tagSource != null) {
                jsonObject.addProperty("SourceTag", leafEntry.tagSource.id().toString());
            }
        }

        @Override
        protected BiomeEssenceEntry fromJson(JsonObject entryJson, JsonDeserializationContext context, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
            TagKey<Biome> source = null;
            if (entryJson.has("SourceTag")) {
                var id = Identifier.tryParse(entryJson.get("SourceTag").getAsString());

                if (id != null) {
                    source = TagKey.of(Registry.BIOME_KEY, id);
                }
            }

            return new BiomeEssenceEntry(weight, quality, conditions, functions, source);
        }
    }
}
