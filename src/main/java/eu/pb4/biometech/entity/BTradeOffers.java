package eu.pb4.biometech.entity;

import eu.pb4.biometech.item.BItems;
import eu.pb4.biometech.util.BBiomeTags;
import eu.pb4.sgui.api.gui.MerchantGui;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BTradeOffers {
    public static final TradeOffers.Factory BIOME_ESSENCE_TRADER = new TradeOffers.Factory() {
        @Nullable
        @Override
        public TradeOffer create(Entity entity, Random random) {
            var reg = entity.getServer().getRegistryManager().get(Registry.BIOME_KEY);

            var biomes = reg.streamEntries().filter(x -> !x.isIn(BBiomeTags.BANNED_BIOMES) && !x.isIn(BBiomeTags.TRADER_BANNED_BIOMES))
                    .collect(Collectors.toList());
            var biome = biomes.get(random.nextInt(biomes.size()));

            if (biome != null) {
                var stack = new ItemStack(BItems.BIOME_ESSENCE, random.nextBetween(2, 6));
                stack.getOrCreateNbt().putString("Biome", biome.getKey().get().getValue().toString());


                return new TradeOffer(new ItemStack(Items.EMERALD, random.nextBetween(4, 12)), stack, random.nextBetween(8, 13), 0, 0);
            }

            return null;
        }
    };

    public static final TradeOffers.Factory BIOME_ESSENCE_CARTOGRAPHER = new TradeOffers.Factory() {
        @Nullable
        @Override
        public TradeOffer create(Entity entity, Random random) {
            var reg = entity.getServer().getRegistryManager().get(Registry.BIOME_KEY);

            var biomes = reg.streamEntries().filter(x -> !x.isIn(BBiomeTags.BANNED_BIOMES) && !x.isIn(BBiomeTags.TRADER_BANNED_BIOMES))
                    .collect(Collectors.toList());
            var biome = biomes.get(random.nextInt(biomes.size()));

            if (biome != null) {
                var stack = new ItemStack(BItems.BIOME_ESSENCE, random.nextBetween(3, 8));
                stack.getOrCreateNbt().putString("Biome", biome.getKey().get().getValue().toString());


                return new TradeOffer(new ItemStack(Items.EMERALD, random.nextBetween(8, 16)), stack, random.nextBetween(3, 8), 0, 0);
            }

            return null;
        }
    };

    public static void register() {
        var list = new ArrayList<>(List.of(TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(VillagerProfession.CARTOGRAPHER).get(MerchantGui.VillagerLevel.MASTER.ordinal())));
        list.add(BIOME_ESSENCE_CARTOGRAPHER);
        list.add(BIOME_ESSENCE_CARTOGRAPHER);
        TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(VillagerProfession.CARTOGRAPHER).put(MerchantGui.VillagerLevel.MASTER.ordinal(), list.toArray(new TradeOffers.Factory[0]));
    }
}
