package eu.pb4.biometech.gui;

import eu.pb4.biometech.util.TextUtil;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.layered.Layer;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public abstract class PagedLayer extends Layer implements PageAware {
    public final int pageSize;
    public final ServerPlayerEntity player;
    private final boolean withNavigation;
    protected int page = 0;

    public PagedLayer(ServerPlayerEntity player, int height, int width, boolean withNavigation) {
        super(height, width);
        this.withNavigation = withNavigation;
        this.player = player;
        this.pageSize = this.withNavigation ? (height - 1) * width : height * width;
    }


    protected void updateDisplay() {
        var offset = this.page * this.pageSize;

        for (int i = 0; i < this.pageSize; i++) {
            var element = this.getElementAmount() > offset + i ? this.getElement(offset + i) : GuiUtils.EMPTY;

            if (element == null) {
                element = GuiUtils.EMPTY;
            }

            this.setSlot(i, element);
        }

        for (int i = 0; i < this.width; i++) {
            var navElement = this.getNavElement(i);

            if (navElement == null) {
                navElement = GuiUtils.EMPTY;
            }

            this.setSlot(i + this.pageSize, navElement);
        }
    }

    public int getPage() {
        return this.page;
    }

    @Override
    public void setPage(int page) {
        this.page = page % Math.max(this.getPageAmount(), 1);
        this.updateDisplay();
    }

    @Override
    public int getPageAmount() {
        return (int) Math.ceil(this.getElementAmount() / (double) this.pageSize);
    }

    protected abstract int getElementAmount();

    protected abstract GuiElement getElement(int id);

    protected GuiElement getNavElement(int id) {
        int center = this.width / 2;

        int distance = this.width < 5 ? 1 : 2;

        if (id == center - distance) {
            return this.getPageAmount() > 1 ? GuiUtils.previousPage(this.player, this) : GuiUtils.FILLER;
        } else if (id == center) {
            return this.getPageAmount() > 1 ? new GuiElementBuilder(Items.BOOK)
                    .setName(TextUtil.gui("pages",
                                    Text.literal("" + (this.page + 1)).formatted(Formatting.WHITE),
                                    Text.literal("" + this.getPageAmount()).formatted(Formatting.WHITE)
                            ).formatted(Formatting.AQUA)
                    ).build() : GuiUtils.FILLER;
        } else if (id == center + distance) {
            return this.getPageAmount() > 1 ? GuiUtils.nextPage(player, this) : GuiUtils.FILLER;
        }


        return GuiUtils.FILLER;
    }
}
