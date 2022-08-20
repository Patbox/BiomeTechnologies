package eu.pb4.biometech.mixin;

import eu.pb4.biometech.entity.BTradeOffers;
import eu.pb4.biometech.util.BGameRules;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingTraderEntityMixin extends MerchantEntity {
    public WanderingTraderEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "fillRecipes", at = @At("TAIL"))
    private void biometech$addTrades(CallbackInfo ci) {
        if (this.world.getGameRules().getBoolean(BGameRules.WANDERING_TRADERS_OFFERS)) {
            if (this.random.nextDouble() < 0.9) {

                var x = BTradeOffers.BIOME_ESSENCE_TRADER.create(this, this.random);
                if (x != null) {
                    this.getOffers().add(x);
                }
            }
            if (this.random.nextDouble() < 0.2) {
                var x = BTradeOffers.BIOME_ESSENCE_TRADER.create(this, this.random);
                if (x != null) {
                    this.getOffers().add(x);
                }
            }
        }
    }
}
