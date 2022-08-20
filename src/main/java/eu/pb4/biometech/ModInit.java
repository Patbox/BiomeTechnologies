package eu.pb4.biometech;

import eu.pb4.biometech.block.BBlocks;
import eu.pb4.biometech.block.entity.BBlockEntities;
import eu.pb4.biometech.entity.BEntities;
import eu.pb4.biometech.entity.BTradeOffers;
import eu.pb4.biometech.gui.GuiElements;
import eu.pb4.biometech.item.BItems;
import eu.pb4.biometech.loot.BLootTables;
import eu.pb4.biometech.util.BGameRules;
import eu.pb4.biometech.util.ModUtil;
import eu.pb4.polymer.api.networking.PolymerPacketUtils;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.api.utils.PolymerUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.color.world.GrassColors;

import javax.imageio.ImageIO;

public class ModInit implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.

	@Override
	public void onInitialize() {
		PolymerRPUtils.addAssetSource(ModUtil.MOD_ID);
		BGameRules.register();
		BBlocks.register();
		BItems.register();
		BEntities.register();
		BBlockEntities.register();
		BLootTables.register();
		BTradeOffers.register();
		GuiElements.register();

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
			try {
				var jar = PolymerUtils.getClientJar();
				var image = ImageIO.read(jar.getInputStream(jar.getEntry("assets/minecraft/textures/colormap/foliage.png")));
				var image2 = ImageIO.read(jar.getInputStream(jar.getEntry("assets/minecraft/textures/colormap/grass.png")));
				jar.close();
				var array = new int[image.getHeight() * image.getWidth()];
				var array2 = new int[image.getHeight() * image.getWidth()];

				for (int x = 0; x < image.getWidth(); x++) {
					for (int y = 0; y < image.getHeight(); y++) {
						array[x + y * image.getWidth()] = image.getRGB(x, y);
					}
				}

				for (int x = 0; x < image.getWidth(); x++) {
					for (int y = 0; y < image2.getHeight(); y++) {
						array2[x + y * image2.getWidth()] = image2.getRGB(x, y);
					}
				}

				FoliageColors.setColorMap(array);
				GrassColors.setColorMap(array2);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		ServerLifecycleEvents.SERVER_STARTING.register((x) -> ModUtil.server = x);
		ServerLifecycleEvents.SERVER_STOPPED.register((x) -> ModUtil.server = null);

		PolymerPacketUtils.registerServerPacket(ModUtil.PACKET, 0);
	}

}
