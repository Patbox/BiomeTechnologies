package eu.pb4.biometech.util;

import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameRules;

import static eu.pb4.biometech.util.ModUtil.id;

public class BGameRules {
    public static CustomGameRuleCategory CATEGORY = new CustomGameRuleCategory(id("gamerules"), Text.literal("Biome Technologies™").formatted(Formatting.YELLOW, Formatting.BOLD));

    public static GameRules.Key<GameRules.IntRule> REQUIRED_FUEL_PER_CHANGE = GameRuleRegistry.register(
            id("fuel_per_change").toString(), CATEGORY, GameRuleFactory.createIntRule(100, 1, 2000)
    );

    public static GameRules.Key<GameRules.IntRule> USES_PER_ESSENCE = GameRuleRegistry.register(
            id("uses_per_essence").toString(), CATEGORY, GameRuleFactory.createIntRule(60, 1, 2000)
    );

    public static GameRules.Key<GameRules.IntRule> DELAY_BETWEEN_CHANGES = GameRuleRegistry.register(
            id("delay_between_changes").toString(), CATEGORY, GameRuleFactory.createIntRule(2, 0, 2000)
    );

    public static GameRules.Key<GameRules.IntRule> MAX_CHECKS_PER_TICK = GameRuleRegistry.register(
            id("max_checks_per_tick").toString(), CATEGORY, GameRuleFactory.createIntRule(512, 1, 16384)
    );

    public static GameRules.Key<GameRules.IntRule> MAX_RADIUS = GameRuleRegistry.register(
            id("max_radius").toString(), CATEGORY, GameRuleFactory.createIntRule(64, 1, 512)
    );

    public static GameRules.Key<GameRules.BooleanRule> WANDERING_TRADERS_OFFERS = GameRuleRegistry.register(
            id("wandering_traders_sell_essence").toString(), CATEGORY, GameRuleFactory.createBooleanRule(true)
    );

    public static void register() {

    }
}
