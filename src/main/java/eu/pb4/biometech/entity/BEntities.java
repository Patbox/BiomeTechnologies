package eu.pb4.biometech.entity;

import eu.pb4.biometech.block.BBlocks;
import eu.pb4.biometech.block.entity.BiomeConverterBlockEntity;
import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.entity.PolymerEntityUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.registry.Registry;

import static eu.pb4.biometech.util.ModUtil.id;

public class BEntities {
    public static EntityType<ThrownBiomeEssenceEntity> THROWN_BIOME_ESSENCE =
            register("thrown_biome_essence", FabricEntityTypeBuilder.create().entityFactory(ThrownBiomeEssenceEntity::new).trackRangeChunks(4).build());

    public static void register() {

    }


    private static <T extends Entity> EntityType<T> register(String path, EntityType<T> block) {
        Registry.register(Registry.ENTITY_TYPE, id(path), block);
        PolymerEntityUtils.registerType(block);
        return block;
    }
}
