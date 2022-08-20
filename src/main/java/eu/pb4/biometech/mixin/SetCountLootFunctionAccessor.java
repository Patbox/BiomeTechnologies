package eu.pb4.biometech.mixin;

import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.LootNumberProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SetCountLootFunction.class)
public interface SetCountLootFunctionAccessor {
    @Invoker("<init>")
    static SetCountLootFunction createSetCountLootFunction(LootCondition[] conditions, LootNumberProvider countRange, boolean add) {
        throw new UnsupportedOperationException();
    }
}
