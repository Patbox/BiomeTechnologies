package eu.pb4.biometech.gui;

import eu.pb4.biometech.util.ModUtil;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GuiUtils {
    public static final GuiElement EMPTY = new GuiElement(ItemStack.EMPTY, GuiElementInterface.EMPTY_CALLBACK);
    public static final GuiElement FILLER = new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE)
            .setName(Text.empty())
            .hideFlags().build();

    public static GuiElement backButton(ServerPlayerEntity player, Runnable callback, boolean back) {
        return new GuiElementBuilder(Items.BARRIER)
                .setName(Text.translatable(back ? "gui.back" : "gui." + ModUtil.MOD_ID + ".close").formatted(Formatting.RED))
                .hideFlags()
                .setCallback((x, y, z) -> {
                    playClickSound(player);
                    callback.run();
                }).build();
    }

    public static final void playClickSound(ServerPlayerEntity player) {
        player.playSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 1);
    }

    public static GuiElement nextPage(ServerPlayerEntity player, PageAware gui) {
        return new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.translatable("spectatorMenu.next_page").formatted(Formatting.WHITE))
                .hideFlags()
                .setSkullOwner(GuiHeadTextures.GUI_NEXT_PAGE)
                .setCallback((x, y, z) -> {
                    playClickSound(player);
                    gui.nextPage();
                }).build();
    }

    public static GuiElement previousPage(ServerPlayerEntity player, PageAware gui) {
        return new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.translatable("spectatorMenu.previous_page").formatted(Formatting.WHITE))
                .hideFlags()
                .setSkullOwner(GuiHeadTextures.GUI_PREVIOUS_PAGE)
                .setCallback((x, y, z) -> {
                    playClickSound(player);
                    gui.previousPage();
                }).build();
    }
}
