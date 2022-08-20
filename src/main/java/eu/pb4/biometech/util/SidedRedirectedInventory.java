package eu.pb4.biometech.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.IntStream;

public interface SidedRedirectedInventory extends SidedInventory {

    List<StackedInv> getInventories();

    @Nullable
    default StackedInv getInventoryForSlot(int slot) {
        for (var inv : getInventories()) {
            if (inv.contains(slot)) {
                return inv;
            }
        }

        return null;
    }

    default int size() {
        int i = 0;
        for (var inv : getInventories()) {
            i += inv.inventory.size();
        }
        return i;
    }

    default boolean isEmpty() {
        for (var inv : getInventories()) {
            if (!inv.inventory.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    default void clear() {
        for (var inv : getInventories()) {
            inv.inventory.clear();
        }
    }

    default ItemStack getStack(int slot) {
        var x = this.getInventoryForSlot(slot);
        return x.inventory.getStack(slot - x.slotDelta);
    }

    default ItemStack removeStack(int slot, int amount) {
        var x = this.getInventoryForSlot(slot);
        return x.inventory.removeStack(slot - x.slotDelta, amount);
    }

    default ItemStack removeStack(int slot) {
        var x = this.getInventoryForSlot(slot);
        return x.inventory.removeStack(slot - x.slotDelta);
    }

    default void setStack(int slot, ItemStack stack) {
        var x = this.getInventoryForSlot(slot);
        x.inventory.setStack(slot - x.slotDelta, stack);
    }

    default int getMaxCountPerStack() {
        return 64;
    }

    void markDirty();

    default boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    record StackedInv(Inventory inventory, int slotDelta, int[] slots) {
        public static StackedInv add(List<StackedInv> list, Inventory inventory) {
            if (list.isEmpty()) {
                var x = new StackedInv(inventory, 0, IntStream.range(0, inventory.size()).toArray());
                list.add(x);
                return x;
            } else {
                var last = list.get(list.size() - 1);
                var delta = last.slotDelta + last.inventory.size();
                var x = new StackedInv(inventory, delta, IntStream.range(delta, inventory.size() + delta).toArray());
                list.add(x);
                return x;
            }
        }

        public boolean contains(int slot) {
            return this.slotDelta <= slot && this.inventory.size() > slot - this.slotDelta;
        }
    }
}
