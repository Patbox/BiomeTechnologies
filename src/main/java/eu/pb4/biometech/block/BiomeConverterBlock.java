package eu.pb4.biometech.block;

import eu.pb4.biometech.block.entity.BBlockEntities;
import eu.pb4.biometech.block.entity.BiomeConverterBlockEntity;
import eu.pb4.polymer.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BiomeConverterBlock extends BlockWithEntity implements PolymerBlock {
    public static BooleanProperty ACTIVE = BooleanProperty.of("active");

    public BiomeConverterBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(ACTIVE, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
        super.appendProperties(builder);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.BARRIER;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BiomeConverterBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (hand == Hand.MAIN_HAND) {

            if (world.getBlockEntity(pos) instanceof BiomeConverterBlockEntity be && player instanceof ServerPlayerEntity serverPlayer) {
                be.use(serverPlayer, hit);
                return ActionResult.SUCCESS;
            }
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (newState.getBlock() != this) {
            if (world.getBlockEntity(pos) instanceof BiomeConverterBlockEntity blockEntity) {
                ItemScatterer.spawn(world, pos, blockEntity);
            }
        }

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, ServerPlayerEntity player) {
        return Blocks.AMETHYST_BLOCK.getDefaultState();
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return !world.isClient ? checkType(type, BBlockEntities.BIOME_CONVERTER, BiomeConverterBlock::ticker) : null;
    }

    private static <T extends BlockEntity> void ticker(World world, BlockPos pos, BlockState state, T t) {
        if (t instanceof BiomeConverterBlockEntity b) {
            b.tick();
        }
    }
}
