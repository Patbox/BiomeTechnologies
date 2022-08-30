package eu.pb4.biometech.gui;

import eu.pb4.biometech.block.entity.BiomeConverterBlockEntity;
import eu.pb4.biometech.item.BItems;
import eu.pb4.biometech.util.BGameRules;
import eu.pb4.biometech.util.ModUtil;
import eu.pb4.biometech.util.TextUtil;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.layered.Layer;
import eu.pb4.sgui.api.gui.layered.LayeredGui;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static eu.pb4.biometech.util.ModUtil.id;

public class ConverterGui extends LayeredGui {
    private final BiomeConverterBlockEntity be;

    private final List<RegistryKey<Biome>> biomes;
    //private final PagedLayer selector;
    private final MainLayer mainLayer;
    //private LayerView selectorView;

    public ConverterGui(ServerPlayerEntity player, BiomeConverterBlockEntity be) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        this.be = be;
        this.setTitle(this.be.getCachedState().getBlock().getName());

        this.biomes = new ArrayList<>(player.getServer().getRegistryManager().get(Registry.BIOME_KEY).getKeys());
        this.biomes.sort(Comparator.comparing(RegistryKey::getValue));

        /*this.selector = new PagedLayer(player, 6, 9, true) {
            @Override
            protected int getElementAmount() {
                return ConverterGui.this.biomes.size();
            }

            @Override
            protected GuiElement getElement(int id) {
                var x = ConverterGui.this.biomes.get(id);
                var b = new GuiElementBuilder(ModUtil.getBiomeIcon(x))
                        .setName(Text.translatable(Util.createTranslationKey("biome", x.getValue())))
                        .setCallback((a, y, z, d) -> {
                            GuiUtils.playClickSound(player);
                            ConverterGui.this.be.setBiome(x);
                            ConverterGui.this.be.setActive(false);
                            ConverterGui.this.mainLayer.update();
                            this.updateDisplay();
                            ConverterGui.this.removeLayer(ConverterGui.this.selectorView);
                        });

                if (x.equals(ConverterGui.this.be.currentBiomeId)) {
                    b.glow();
                }

                return b.build();
            }
        };
        var index = this.biomes.indexOf(this.be.currentBiomeId);
        this.selector.setPage(index != -1 ? index / selector.pageSize : 0);*/

        this.mainLayer = new MainLayer(6, 9);
        this.addLayer(this.mainLayer, 0, 0).setZIndex(-5);

        if (!ModUtil.useResourcePack(player)) {
            while (this.getFirstEmptySlot() != -1) {
                this.addSlot(GuiUtils.FILLER);
            }
        }

        this.open();
    }

    @Override
    public void onTick() {
        if (this.be.isRemoved() || this.be.getPos().getSquaredDistanceFromCenter(this.getPlayer().getX(), this.getPlayer().getY(), this.getPlayer().getZ()) > 256) {
            this.close();
            return;
        }
        super.onTick();
        this.mainLayer.tick();
    }

    protected void playClickSound() {
        this.playSound(SoundEvents.UI_BUTTON_CLICK, 0.5f, 1f);
    }

    protected void playSound(SoundEvent event, float volume, float pitch) {
        this.getPlayer().networkHandler.sendPacket(new PlaySoundS2CPacket(event, SoundCategory.MASTER, this.getPlayer().getX(), this.getPlayer().getY(), this.getPlayer().getZ(), volume, pitch, 0));
    }

    private class MainLayer extends Layer {
        private static final String[] FIRE_ICON = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c" };
        private RegistryKey<Biome> biomeRegistryKey;
        private int radius = 0;
        private boolean isEmpty;
        private boolean active;
        private int energyLevel;

        public MainLayer(int height, int width) {
            super(height, width);
            this.update();

            for (int i = 0; i < 3; i++) {
                this.setSlotRedirect(9 * 4 + 5 + i, new Slot(be.fuelInventory, i, 0, 0) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return AbstractFurnaceBlockEntity.canUseAsFuel(stack);
                    }
                });

                this.setSlotRedirect(9 * 4 + 1 + i, new Slot(be.essenceInventory, i + 3, 0, 0) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return stack.isOf(BItems.BIOME_ESSENCE);
                    }
                });

                this.setSlotRedirect(9 * 3 + 1 + i, new Slot(be.essenceInventory, i, 0, 0) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return stack.isOf(BItems.BIOME_ESSENCE);
                    }
                });
            }

        }


        public void tick() {
            var be = ConverterGui.this.be;

            if (
                    this.biomeRegistryKey != be.currentBiomeId
                            || this.radius != be.radius
                            || this.isEmpty != be.essenceInventory.isEmpty()
                            || this.active != be.isActivated()
                            || this.energyLevel != (be.energy > 0 ? Math.min(be.energy / (ConverterGui.this.getPlayer().world.getGameRules().getInt(BGameRules.REQUIRED_FUEL_PER_CHANGE) * 20), 12) : -1)
            ) {
                this.update();
            }
        }

        public void update() {
            var be = ConverterGui.this.be;
            this.biomeRegistryKey = be.currentBiomeId;
            this.radius = be.radius;
            this.isEmpty = be.essenceInventory.isEmpty();
            this.active = be.isActivated();
            this.energyLevel = be.energy > 0 ? Math.min(be.energy / (ConverterGui.this.getPlayer().world.getGameRules().getInt(BGameRules.REQUIRED_FUEL_PER_CHANGE) * 20), 12) : -1;

            boolean textures = ModUtil.useResourcePack(ConverterGui.this.getPlayer());

            var maxRadius = ConverterGui.this.getPlayer().world.getGameRules().getInt(BGameRules.MAX_RADIUS);

            var activate = !be.isActivated();

            if (textures) {
                boolean l = this.radius > 9;
                ConverterGui.this.setTitle(
                        Text.empty().append(
                                Text.literal("-0.").setStyle(Style.EMPTY.withFont(id("gui")).withColor(Formatting.WHITE))
                        ).append(
                                Text.literal("ą" + (l ? "" : "ć") + this.radius + (l ? "łłń" : "łń")).setStyle(Style.EMPTY.withFont(id("radius2")).withColor(Formatting.GRAY))
                        ).append(
                                Text.literal("" + this.radius + (l ? "" : "ź") + (l ? "żż" : "ż") + "ę" + (l ? "łłłł" : "łł")).setStyle(Style.EMPTY.withFont(id("radius")).withColor(4210752))
                        ).append(
                                this.energyLevel == -1 ? Text.empty() : Text.literal("." + FIRE_ICON[this.energyLevel] + "-").setStyle(Style.EMPTY.withFont(id("fire")).withColor(Formatting.WHITE))
                        ).append(
                                be.getCachedState().getBlock().getName()
                        )
                );
            }

            if (activate) {
                this.setSlot(9 * 1 + 3, GuiElements.getMinus(textures)
                        .setName(Text.literal("-").formatted(be.radius <= 8 ? Formatting.DARK_GRAY : Formatting.WHITE)
                        ).setCallback((a, b, c, d) -> {
                            if (be.radius > 8) {
                                ConverterGui.this.playClickSound();
                                be.radius -= 4;
                                this.update();
                            }
                        }));

                this.setSlot(9 * 1 + 5, GuiElements.getPlus(textures)
                        .setName(Text.literal("+").formatted(be.radius >= maxRadius ? Formatting.DARK_GRAY : Formatting.WHITE)
                        ).setCallback((a, b, c, d) -> {
                            if (be.radius < maxRadius) {
                                ConverterGui.this.playClickSound();
                                be.radius += 4;
                                this.update();
                            }
                        }));
            } else {
                this.clearSlot(9 * 1 + 3);
                this.clearSlot(9 * 1 + 5);
            }

            if (!textures) {
                this.setSlot(9 * 1 + 4, new GuiElementBuilder(Items.TORCH)
                        .setCount(be.radius)
                        .setName(TextUtil.gui("radius", Text.literal("" + be.radius).formatted(Formatting.WHITE)).formatted(Formatting.GRAY)
                        ));
            }

            {
                var b = (be.currentBiomeId != null ? GuiElementBuilder.from(ModUtil.getBiomeIcon(be.currentBiomeId)) : new GuiElementBuilder(Items.BARRIER))
                        .setName(be.currentBiomeId != null
                                ? Text.translatable(Util.createTranslationKey("biome", be.currentBiomeId.getValue()))
                                : TextUtil.gui("no_biome").formatted(Formatting.RED)
                        ).setCallback((a, s, c, d) -> {
                            if (activate && be.currentBiomeId != null) {
                                GuiUtils.playClickSound(ConverterGui.this.getPlayer());
                                be.setBiome(null);
                            }
                        });

                if (activate && be.currentBiomeId != null) {
                    b.addLoreLine(TextUtil.gui("button.clear_biome").formatted(Formatting.RED));
                }

                this.setSlot(9 * 1 + 1, b);
            }

            if (!textures) {
                this.setSlot(9 * 3 + 6, new GuiElementBuilder(this.energyLevel == -1 ? Items.COAL : Items.BLAZE_POWDER).setName(TextUtil.gui("fuel.brackets", TextUtil.gui("fuel").formatted(Formatting.GRAY)).formatted(Formatting.DARK_GRAY)));
            }

            this.setSlot(9 * 1 + 7, GuiElements.getActivate(textures, activate, be.currentBiomeId != null || !this.isEmpty)
                    .setName(TextUtil.gui(activate ? "button.activate" : "button.deactivate").formatted(activate ? (be.currentBiomeId != null || !this.isEmpty ? Formatting.GREEN : Formatting.DARK_GRAY) : Formatting.RED))
                    .setCallback((x, y, z, d) -> {
                        if (be.currentBiomeId != null || !this.isEmpty) {
                            GuiUtils.playClickSound(ConverterGui.this.getPlayer());
                            be.setActive(!be.isActivated(), ConverterGui.this.getPlayer().getGameProfile());
                            this.update();
                        }
                    })
            );
        }
    }
}