package eu.pb4.biometech.client;

import eu.pb4.biometech.item.BItems;
import eu.pb4.biometech.item.ButtonItem;
import eu.pb4.biometech.mixin.client.ModelPredicateProviderRegistryAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class ModInitClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelPredicateProviderRegistryAccessor.getITEM_SPECIFIC().computeIfAbsent(BItems.UI_BUTTON, (x) -> new HashMap<>()).put(new Identifier("texture"), (itemStack, clientWorld, livingEntity, i) -> {
            if (itemStack.hasNbt()) {
                var texture = itemStack.getNbt().getString("Texture");
                return ButtonItem.TEXTURE_ID.getOrDefault(texture, -1);
            }
            return -1;
        });
    }
}
