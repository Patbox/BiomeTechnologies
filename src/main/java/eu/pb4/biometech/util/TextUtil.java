package eu.pb4.biometech.util;

import eu.pb4.biometech.ModInit;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class TextUtil {
    public static MutableText gui(String id, Object... args) {
        return Text.translatable("gui." + ModUtil.MOD_ID + "." + id, args);
    }
}
