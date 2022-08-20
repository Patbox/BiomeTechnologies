package eu.pb4.biometech.item;

import eu.pb4.biometech.entity.BEntities;
import eu.pb4.biometech.entity.ThrownBiomeEssenceEntity;
import eu.pb4.biometech.util.BBiomeTags;
import eu.pb4.biometech.util.ModUtil;
import eu.pb4.polymer.api.client.PolymerClientDecoded;
import eu.pb4.polymer.api.client.PolymerKeepModel;
import eu.pb4.polymer.api.item.PolymerItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;

public class BiomeEssenceItem extends Item implements PolymerItem {
    public BiomeEssenceItem(Settings settings) {
        super(settings);
    }

    @Nullable
    public static RegistryEntry<Biome> getBiome(MinecraftServer server, ItemStack itemStack) {
        var id = getBiomeKey(itemStack);
        if (id != null) {
            var biome = server.getRegistryManager().get(Registry.BIOME_KEY).getEntry(id);
            return biome.orElse(null);
        }
        return null;
    }

    @Nullable
    public static Identifier getBiomeId(ItemStack stack) {
        return stack.hasNbt() ? Identifier.tryParse(stack.getNbt().getString("Biome")) : null;
    }

    @Nullable
    public static RegistryKey<Biome> getBiomeKey(ItemStack stack) {
        var id = getBiomeId(stack);

        return id != null ? RegistryKey.of(Registry.BIOME_KEY, id) : null;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.LINGERING_POTION;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        var out = PolymerItem.super.getPolymerItemStack(itemStack, player);

        if (player != null && itemStack.hasNbt()) {

            var type = getBiome(player.server, itemStack);

            if (type != null) {
                out.getOrCreateNbt().putInt("CustomPotionColor", ModUtil.getBiomeColor(type));
            } else {
                out.getOrCreateNbt().putInt("CustomPotionColor", 0);
            }
        } else {
            out.getOrCreateNbt().putInt("CustomPotionColor", 0);
        }


        return out;
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable(this.getOrCreateTranslationKey(), Text.translatable(Util.createTranslationKey("biome", getBiomeId(stack))));
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (!world.isClient) {
            var entity = new ThrownBiomeEssenceEntity(BEntities.THROWN_BIOME_ESSENCE, world);
            entity.setItem(itemStack);
            entity.setOwner(user);
            entity.setPosition(user.getEyePos());
            entity.setVelocity(user, user.getPitch(), user.getYaw(), -20.0F, 0.5F, 1.0F);
            world.spawnEntity(entity);
        }

        user.incrementStat(Stats.USED.getOrCreateStat(this));
        if (!user.getAbilities().creativeMode) {
            itemStack.decrement(1);
        }

        return TypedActionResult.success(itemStack, world.isClient());
    }

    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if (this.isIn(group) && ModUtil.server != null) {
            var reg = ModUtil.server.getRegistryManager().get(Registry.BIOME_KEY);
            var keys = new ArrayList<>(reg.getKeys());
            keys.sort(Comparator.comparing(RegistryKey::getValue));
            for (var biome : keys) {
                if (!reg.getEntry(biome).get().isIn(BBiomeTags.BANNED_BIOMES)) {
                    var stack = new ItemStack(this);
                    stack.getOrCreateNbt().putString("Biome", biome.getValue().toString());
                    stacks.add(stack);
                }
            }
        }
    }
}
