package eu.pb4.biometech.block.entity;

import eu.pb4.biometech.block.BBlocks;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static eu.pb4.biometech.util.ModUtil.id;

public class BBlockEntities {
    public static BlockEntityType<BiomeConverterBlockEntity> BIOME_CONVERTER =
            register("biome_converter", FabricBlockEntityTypeBuilder.create(BiomeConverterBlockEntity::new, BBlocks.BIOME_CONVERTER).build());

    public static void register() {

    }


    private static <T extends BlockEntity> BlockEntityType<T> register(String path, BlockEntityType<T> block) {
        Registry.register(Registries.BLOCK_ENTITY_TYPE, id(path), block);
        PolymerBlockUtils.registerBlockEntity(block);
        return block;
    }
}
