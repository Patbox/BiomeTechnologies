package eu.pb4.biometech.entity;

import eu.pb4.biometech.item.BItems;
import eu.pb4.biometech.item.BiomeEssenceItem;
import eu.pb4.biometech.util.ModUtil;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ThrownBiomeEssenceEntity extends ThrownItemEntity implements PolymerEntity {
    public ThrownBiomeEssenceEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected Item getDefaultItem() {
        return BItems.BIOME_ESSENCE;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        if (this.getServer() != null) {
            var biome = BiomeEssenceItem.getBiome(this.getServer(), this.getStack());
            if (biome != null) {
                var color = ModUtil.getBiomeColor(biome);
                BlockPos pos;
                if (hitResult instanceof BlockHitResult hitResult1) {
                    pos = hitResult1.getBlockPos();
                } else {
                    pos = this.getBlockPos();
                }

                ModUtil.setBiome((ServerWorld) this.world, pos.getX(), pos.getY(), pos.getZ(), biome,
                        this.getOwner() instanceof PlayerEntity player ? player.getGameProfile() : null, ModUtil::updateChunk);
                this.world.syncWorldEvent(2007, this.getBlockPos(), color);
            }
        }
        this.discard();
    }

    @Override
    public EntityType<?> getPolymerEntityType(ServerPlayerEntity player) {
        return EntityType.POTION;
    }
}
