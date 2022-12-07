package eu.pb4.biometech.block.model;

import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class HeadModels {
    public static final String BASE = "ewogICJ0aW1lc3RhbXAiIDogMTY2MDY3ODI0OTM2MCwKICAicHJvZmlsZUlkIiA6ICJmODJmNTQ1MDIzZDA0MTFkYmVlYzU4YWI4Y2JlMTNjNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZXNwb25kZW50cyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85YmZjZGZhMGM2NTA0ZWMwZDZlOWFhMzE2Mjg0MGI2NTMwOGJhNTRjMzY3ZTQ3MWJhMjdmZjhmYjFlYjUzMzNmIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=";
    public static final String ACTIVE = "ewogICJ0aW1lc3RhbXAiIDogMTY2MDY3ODI2Njc3NCwKICAicHJvZmlsZUlkIiA6ICI5ZWEyMTQ0NGFiNjI0MWZkYjg5YjE2NDFhNDg2MGZiZiIsCiAgInByb2ZpbGVOYW1lIiA6ICI3QUJDSE9VTiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82Y2JmYmZkYzViYjVhYmM1NDY5NzEwMzBjNmQwZDliNGJkNTQ3MTNkMjg0ZTc1ODY5ZTc0MDdlOTExY2M5YWUzIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=";


    public static ItemStack create(boolean isActive) {
        var stack = new ItemStack(Items.PLAYER_HEAD);
        return set(stack, isActive ? ACTIVE : BASE);
    }

    public static ItemStack set(ItemStack stack, String texture) {
        stack.getOrCreateNbt().put("SkullOwner", PolymerUtils.createSkullOwner(texture));
        return stack;
    }
}
