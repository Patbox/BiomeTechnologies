package eu.pb4.biometech.util;

import com.mojang.authlib.GameProfile;
import eu.pb4.biometech.item.BItems;
import eu.pb4.biometech.item.BiomeEssenceItem;
import eu.pb4.biometech.mixin.BiomeAccessAccessor;
import eu.pb4.common.protection.api.CommonProtection;
import eu.pb4.polymer.api.networking.PolymerPacketUtils;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.impl.PolymerImplUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.MapColor;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BiomeTags;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ModUtil {
    public static final String MOD_ID = "biometech";
    public static final Identifier PACKET = id("version");
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final boolean IS_CLIENT = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;

    private static final Map<RegistryKey<Biome>, ItemStack> BIOME_ICONS = new Object2ObjectOpenHashMap<>();

    private static final Object2IntMap<RegistryKey<Biome>> BIOME_COLORS = new Object2IntOpenHashMap<>();

    public static MinecraftServer server;

    private static final BlockPos.Mutable POS = new BlockPos.Mutable();

    static {
        BIOME_COLORS.defaultReturnValue(-1);
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    public static boolean useResourcePack(ServerPlayerEntity player) {
        return PolymerRPUtils.hasPack(player) || hasMod(player);
    }

    public static boolean hasMod(ServerPlayerEntity player) {
        return player != null && PolymerPacketUtils.getSupportedVersion(player.networkHandler, PACKET) != -1;
    }

    public static boolean setBiome(ServerWorld world, int x, int y, int z, RegistryEntry<Biome> biome, @Nullable GameProfile profile, Consumer<WorldChunk> dirtyChunkConsumer) {
        {
            int i = x - 2;
            int j = y - 2;
            int k = z - 2;
            int l = i >> 2;
            int m = j >> 2;
            int n = k >> 2;
            double d = (double) (i & 3) / 4.0D;
            double e = (double) (j & 3) / 4.0D;
            double f = (double) (k & 3) / 4.0D;
            int o = 0;
            double g = 1.0D / 0.0;

            int p;
            for (p = 0; p < 8; ++p) {
                boolean bl = (p & 4) == 0;
                boolean bl2 = (p & 2) == 0;
                boolean bl3 = (p & 1) == 0;
                int q = bl ? l : l + 1;
                int r = bl2 ? m : m + 1;
                int s = bl3 ? n : n + 1;
                double h = bl ? d : d - 1.0D;
                double t = bl2 ? e : e - 1.0D;
                double u = bl3 ? f : f - 1.0D;
                double v = BiomeAccessAccessor.callMethod_38106(((BiomeAccessAccessor) world.getBiomeAccess()).getSeed(), q, r, s, h, t, u);
                if (g > v) {
                    o = p;
                    g = v;
                }
            }

            x = (o & 4) == 0 ? l : l + 1;
            y = (o & 2) == 0 ? m : m + 1;
            z = (o & 1) == 0 ? n : n + 1;
        }
        if (profile != null) {
            if (!CommonProtection.canPlaceBlock(world, POS.set(x, y, z), profile, world.getPlayerByUuid(profile.getId()))) {
                return false;
            }
        }

        var chunk = world.getChunk(BiomeCoords.toChunk(x), BiomeCoords.toChunk(z));

        var id = chunk.getSectionIndex(BiomeCoords.toBlock(y));

        if (id < 0 || id >= chunk.getSectionArray().length) {
            return false;
        }

        var section = chunk.getSection(id);

        if (section.getBiomeContainer() instanceof PalettedContainer<RegistryEntry<Biome>> container && container.get(
                x & 3, y & 3, z & 3
        ) != biome) {
            container.swapUnsafe(
                    x & 3, y & 3, z & 3,
                    biome
            );

            chunk.setNeedsSaving(true);
            dirtyChunkConsumer.accept(chunk);
            return true;
        }

        return false;
    }

    public static ItemStack getBiomeIcon(RegistryKey<Biome> key) {
        var out = BIOME_ICONS.get(key);

        if (out == null) {
            Item value;
            var path = key.getValue().getPath();

            if (path.contains("dark_forest")) {
                value = Items.DARK_OAK_SAPLING;
            } else if (path.contains("birch")) {
                value = Items.BIRCH_SAPLING;
            } else if (path.contains("crimson")) {
                value = Items.CRIMSON_FUNGUS;
            } else if (path.contains("warped")) {
                value = Items.WARPED_FUNGUS;
            } else if (path.contains("flower")) {
                value = Items.RED_TULIP;
            } else if (path.contains("savanna")) {
                value = Items.ACACIA_SAPLING;
            } else if (path.contains("mangrove")) {
                value = Items.MANGROVE_PROPAGULE;
            } else if (path.contains("spruce") || path.contains("taiga") || path.contains("grove")) {
                value = Items.SPRUCE_SAPLING;
            } else if (path.contains("lush_cave")) {
                value = Items.GLOW_BERRIES;
            } else if (path.contains("coral")) {
                value = Items.BUBBLE_CORAL_FAN;
            } else if (path.contains("jagged_peaks")) {
                value = Items.SNOWBALL;
            } else if (path.contains("meadow")) {
                value = Items.AZURE_BLUET;
            } else if (path.contains("dripstone")) {
                value = Items.POINTED_DRIPSTONE;
            } else if (path.contains("ice_spikes")) {
                value = Items.PACKED_ICE;
            } else if (path.contains("deep_dark")) {
                value = Items.ECHO_SHARD;
            } else if (path.contains("badlands")) {
                value = Items.TERRACOTTA;
            } else if (path.contains("basalt")) {
                value = Items.BASALT;
            } else if (path.contains("bamboo")) {
                value = Items.BAMBOO;
            } else if (path.contains("soul_sand")) {
                value = Items.SOUL_SAND;
            } else if (path.contains("gravel")) {
                value = Items.GRAVEL;
            } else if (path.contains("the_void")) {
                value = Items.BLACK_CONCRETE;
            } else if (path.contains("mushroom")) {
                value = Items.RED_MUSHROOM;
            } else if (path.contains("frozen") || path.contains("snow") || path.contains("icy")) {
                value = Items.ICE;
            } else if (path.contains("jungle")) {
                value = Items.JUNGLE_SAPLING;
            } else if (path.contains("beach") || path.contains("desert")) {
                value = Items.SAND;
            } else if (path.contains("plains")) {
                value = Items.GRASS;
            } else if (path.contains("stony") || path.contains("hill")) {
                value = Items.STONE;
            } else if (path.contains("ocean") || path.contains("river")) {
                value = Items.WATER_BUCKET;
            } else if (path.contains("forest")) {
                value = Items.OAK_SAPLING;
            } else if (path.contains("swamp")) {
                value = Items.SLIME_BLOCK;
            } else if (path.contains("nether")) {
                value = Items.NETHERRACK;
            } else if (path.contains("end")) {
                value = Items.END_STONE;
            } else {
                value = null;
            }

            if (value != null) {
                out = value.getDefaultStack();
            } else {
                out = new ItemStack(BItems.BIOME_ESSENCE);
                out.getOrCreateNbt().putString("Biome", key.getValue().toString());
            }
            BIOME_ICONS.put(key, out);
        }

        return out;
    }

    public static void updateChunk(WorldChunk chunk) {
        var world = ((ServerWorld) chunk.getWorld());
        for (var player : world.getChunkManager().threadedAnvilChunkStorage.getPlayersWatchingChunk(chunk.getPos())) {
            PolymerImplUtils.setPlayer(player);
            player.networkHandler.sendPacket(new ChunkDataS2CPacket(chunk, world.getLightingProvider(), null, null, true));

        }
        PolymerImplUtils.setPlayer(null);
    }

    public static int getBiomeColor(RegistryEntry<Biome> biome) {
        int color = BIOME_COLORS.getInt(biome);
        if (color == -1) {
            if (biome.isIn(BiomeTags.IS_END)) {
                color = 0xf3f7bb;
            } else if (biome.isIn(BiomeTags.IS_NETHER)) {
                color = biome.value().getFogColor();
                color = ColorHelper.Argb.getArgb(0,
                        (ColorHelper.Argb.getRed(color) + 10) & 0xFF,
                        (ColorHelper.Argb.getGreen(color) + 10) & 0xFF,
                        (ColorHelper.Argb.getBlue(color) + 10) & 0xFF
                );
            } else if (biome.isIn(ConventionalBiomeTags.DESERT)) {
                color = 0xe3dbb0;
            } else if (biome.isIn(BiomeTags.IS_MOUNTAIN) || biome.isIn(ConventionalBiomeTags.ICY)) {
                if (getBiomeIcon(biome.getKey().get()).getItem() instanceof BlockItem blockItem && blockItem.getBlock().getDefaultMapColor() != MapColor.CLEAR) {
                    color = blockItem.getBlock().getDefaultMapColor().getRenderColor(MapColor.Brightness.HIGH);
                    color = ColorHelper.Argb.getArgb(0,
                            ColorHelper.Argb.getBlue(color),
                            ColorHelper.Argb.getGreen(color),
                            ColorHelper.Argb.getRed(color)
                    );
                } else {
                    color = biome.value().getGrassColorAt(0, 0);
                }
            } else if (biome.isIn(BiomeTags.IS_OCEAN) || biome.isIn(BiomeTags.IS_RIVER)) {
                color = biome.value().getWaterColor();
            } else {
                color = biome.value().getGrassColorAt(0, 0);
            }

            BIOME_COLORS.put(biome.getKey().get(), color);
        }

        return color != -1 ? color : FoliageColors.getDefaultColor();
    }
}
