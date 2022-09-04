package eu.pb4.biometech.item;

import eu.pb4.biometech.util.ModUtil;
import eu.pb4.polymer.api.client.PolymerClientDecoded;
import eu.pb4.polymer.api.client.PolymerKeepModel;
import eu.pb4.polymer.api.item.PolymerItem;
import eu.pb4.polymer.api.resourcepack.PolymerModelData;
import eu.pb4.polymer.api.resourcepack.PolymerRPBuilder;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.pb4.biometech.util.ModUtil.id;

public class ButtonItem extends Item implements PolymerItem, PolymerClientDecoded, PolymerKeepModel {
    public static final Map<String, PolymerModelData> TEXTURE_MAP = new HashMap<>();
    public static final Object2IntMap<String> TEXTURE_ID = new Object2IntOpenHashMap<>();
    public static final List<PolymerModelData> TEXTURE_LIST = new ArrayList<>();

    private static boolean shouldRegister = true;

    public ButtonItem(Settings settings) {
        super(settings);
    }

    public static void register(String texture, int i) {
        var model = PolymerRPUtils.requestModel(Items.BAMBOO, id("gui/" + texture));
        TEXTURE_MAP.put(texture, model);
        while (TEXTURE_LIST.size() <= i) {
            TEXTURE_LIST.add(null);
        }
        TEXTURE_LIST.set(i, model);
        TEXTURE_ID.put(texture, i);
        /*if (shouldRegister) {
            shouldRegister = false;
            PolymerRPUtils.RESOURCE_PACK_CREATION_EVENT.register(ButtonItem::createModels);
        }*/
    }

    private static void createModels(PolymerRPBuilder polymerRPBuilder) {
        for (var value : TEXTURE_MAP.values()) {
            var json = """
                      {
                      "parent": "minecraft:item/handheld",
                      "textures": {
                        "layer0": "{ID}"
                      },
                      "display": {
                        "gui": {
                            "rotation": [ 0, 0, 0 ],
                            "translation": [ 0, 0, 0 ],
                            "scale": [ 2.0, 2.0, 2.0 ]
                        }
                      }
                    }
                    """.replace("{ID}", value.modelPath().toString());


            polymerRPBuilder.addData("assets/" + value.modelPath().getNamespace() + "/models/" + value.modelPath().getPath() + ".json", json.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static ItemStack get(String texture) {
        var stack = new ItemStack(BItems.UI_BUTTON);
        stack.getOrCreateNbt().putString("Texture", texture);
        return stack;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.BAMBOO;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        if (ModUtil.hasMod(player)) {
            return itemStack;
        } else {
            return PolymerItem.super.getPolymerItemStack(itemStack, player);
        }
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        if (itemStack.hasNbt()) {
            var x = TEXTURE_MAP.get(itemStack.getNbt().getString("Texture"));
            if (x != null) {
                return x.value();
            }
        }
        return -1;
    }
}
