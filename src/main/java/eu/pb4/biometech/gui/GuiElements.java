package eu.pb4.biometech.gui;

import eu.pb4.biometech.item.ButtonItem;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Items;

public class GuiElements {
    public static GuiElementBuilder getActivate(boolean texture, boolean activate, boolean enabled) {
        if (texture) {
            return GuiElementBuilder.from(activate ? enabled ? ButtonItem.get("activate") : ButtonItem.get("activate_blocked") : ButtonItem.get("activate_off"));
        } else {
            return new GuiElementBuilder(activate ? Items.SLIME_BALL : Items.MAGMA_CREAM);
        }
    }

    public static GuiElementBuilder getPlus(boolean texture) {
        if (texture) {
            return GuiElementBuilder.from(ButtonItem.get("plus"));
        } else {
            return new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(GuiHeadTextures.GUI_ADD);
        }
    }

    public static GuiElementBuilder getMinus(boolean texture) {
        if (texture) {
            return GuiElementBuilder.from(ButtonItem.get("minus"));
        } else {
            return new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(GuiHeadTextures.GUI_REMOVE);
        }
    }

    public static void register() {
        ButtonItem.register("activate", 0);
        ButtonItem.register("activate_blocked", 1);
        ButtonItem.register("activate_off", 2);
        ButtonItem.register("plus", 3);
        ButtonItem.register("minus", 4);
    }
}
