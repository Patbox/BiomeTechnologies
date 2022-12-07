package eu.pb4.biometech.item;

import com.mojang.authlib.GameProfile;
import eu.pb4.biometech.gui.ConverterGui;
import eu.pb4.biometech.util.BiomeConverterLike;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

public class PocketBiomeConverterItem extends Item implements PolymerItem {
    public PocketBiomeConverterItem(Settings settings) {
        super(settings);
    }


    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);

        if (user.isSneaking()) {
            new ConverterGui((ServerPlayerEntity) user, new StackConv(hand, stack));

            return TypedActionResult.success(stack);
        }

        if (stack.hasNbt()) {
            return TypedActionResult.success(stack);
        }

        return super.use(world, user, hand);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.IRON_HORSE_ARMOR;
    }

    public record StackConv(Hand hand, ItemStack stack) implements BiomeConverterLike {
        @Override
        public Text getConvName() {
            return stack.getName();
        }

        @Override
        public boolean shouldClose(ServerPlayerEntity player) {
            return player.getStackInHand(hand) != stack;
        }

        @Override
        public Inventory fuelInventory() {
            var inv = new SimpleInventory(3) {
                @Override
                public void markDirty() {
                    super.markDirty();
                    stack.getOrCreateNbt().put("FuelInventory", this.toNbtList());
                }
            };

            inv.readNbtList(stack.getOrCreateNbt().getList("FuelInventory", NbtElement.COMPOUND_TYPE));


            return inv;
        }

        @Override
        public Inventory essenceInventory() {
            var inv = new SimpleInventory(6) {
                @Override
                public void markDirty() {
                    super.markDirty();
                    stack.getOrCreateNbt().put("EssenceInventory", this.toNbtList());
                }
            };

            inv.readNbtList(stack.getOrCreateNbt().getList("EssenceInventory", NbtElement.COMPOUND_TYPE));

            return inv;
        }

        @Override
        public boolean isActivated() {
            return stack.getOrCreateNbt().getBoolean("Active");
        }

        @Override
        public int radius() {
            return stack.getOrCreateNbt().getInt("Radius");
        }

        @Override
        public RegistryKey<Biome> currentBiomeId() {
            var id = stack.getOrCreateNbt().getString("Biome");
            if (id != null && !id.isEmpty()) {
                return RegistryKey.of(RegistryKeys.BIOME, new Identifier(id));
            }
            return null;
        }

        @Override
        public int energy() {
            return stack.getOrCreateNbt().getInt("Energy");
        }

        @Override
        public void setBiome(RegistryKey<Biome> key) {
            stack.getOrCreateNbt().putString("Biome", key != null ? key.getValue().toString() : "");
        }

        @Override
        public void setActive(boolean active, GameProfile gameProfile) {
            stack.getOrCreateNbt().putBoolean("Active", active);
        }

        @Override
        public void radius(int i) {
            stack.getOrCreateNbt().putInt("Radius", i);
        }
    }
}
