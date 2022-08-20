package eu.pb4.biometech.mixin.client;

import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ModelPredicateProviderRegistry.class)
public interface ModelPredicateProviderRegistryAccessor {
    @Accessor
    static Map<Item, Map<Identifier, ModelPredicateProvider>> getITEM_SPECIFIC() {
        throw new UnsupportedOperationException();
    }
}
