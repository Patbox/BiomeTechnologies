package eu.pb4.biometech.block.entity;

import com.mojang.authlib.GameProfile;
import eu.pb4.biometech.item.BItems;
import eu.pb4.biometech.item.BiomeEssenceItem;
import eu.pb4.biometech.util.BGameRules;
import eu.pb4.biometech.util.BiomeConverterLike;
import eu.pb4.biometech.util.ModUtil;
import eu.pb4.biometech.block.BiomeConverterBlock;
import eu.pb4.biometech.block.model.ArmorStandHologramElement;
import eu.pb4.biometech.block.model.HeadModels;
import eu.pb4.biometech.gui.ConverterGui;
import eu.pb4.biometech.util.SidedRedirectedInventory;
import eu.pb4.holograms.api.elements.EmptyHologramElement;
import eu.pb4.holograms.api.elements.SpacingHologramElement;
import eu.pb4.holograms.api.holograms.WorldHologram;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;
import java.util.stream.IntStream;

public class BiomeConverterBlockEntity extends BlockEntity implements SidedRedirectedInventory, BiomeConverterLike {
    private final Set<WorldChunk> dirtChunks = new HashSet<>();
    public int radius = 20;
    public int energy = 0;
    @Nullable
    public RegistryKey<Biome> currentBiomeId = null;
    private WorldHologram hologram = null;
    private boolean isActive;
    private boolean isActiveTexture;
    private ArmorStandHologramElement mainElement;
    private double yDelta = 0;
    private int conversionProgress = 0;
    private int tickDelay = -1;
    private BlockPos.Mutable conversionPos = new BlockPos.Mutable();
    @Nullable
    private RegistryEntry<Biome> currentBiome = null;
    private List<StackedInv> allInventories = new ArrayList<>();
    public SimpleInventory fuelInventory = new SimpleInventory(3);
    public SimpleInventory essenceInventory = new SimpleInventory(6);
    public StackedInv fuelInventoryStack = StackedInv.add(this.allInventories, fuelInventory);
    public StackedInv essenceInventoryStack = StackedInv.add(this.allInventories, essenceInventory);
    public int essenceUsesLeft = 0;
    private int[] slots = IntStream.concat(IntStream.of(this.fuelInventoryStack.slots()), IntStream.of(this.essenceInventoryStack.slots())).toArray();
    @Nullable
    private GameProfile lastPlayer;


    public BiomeConverterBlockEntity(BlockPos pos, BlockState state) {
        super(BBlockEntities.BIOME_CONVERTER, pos, state);
        this.isActive = state.get(BiomeConverterBlock.ACTIVE);
        this.isActiveTexture = this.isActive;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        if (this.currentBiomeId != null) {
            nbt.putString("BiomeId", this.currentBiomeId.getValue().toString());
        }
        nbt.putInt("Radius", this.radius);
        nbt.putInt("Progress", this.conversionProgress);
        nbt.putInt("Delay", this.tickDelay);
        nbt.putInt("Energy", this.energy);
        nbt.put("CurrentPos", NbtHelper.fromBlockPos(this.conversionPos));
        nbt.put("FuelItems", this.fuelInventory.toNbtList());
        nbt.put("EssenceItems", this.essenceInventory.toNbtList());
        if (this.lastPlayer != null) {
            nbt.put("LastPlayer", NbtHelper.writeGameProfile(new NbtCompound(), this.lastPlayer));
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        var id = Identifier.tryParse(nbt.getString("BiomeId"));
        if (id != null) {
            this.setBiome(RegistryKey.of(RegistryKeys.BIOME, id));
        }

        this.radius = nbt.getInt("Radius");
        this.conversionProgress = nbt.getInt("Progress");
        this.conversionPos = NbtHelper.toBlockPos(nbt.getCompound("CurrentPos")).mutableCopy();
        this.tickDelay = nbt.getInt("Delay");
        this.energy = nbt.getInt("Energy");

        this.fuelInventory.readNbtList(nbt.getList("FuelItems", NbtElement.COMPOUND_TYPE));
        this.essenceInventory.readNbtList(nbt.getList("EssenceItems", NbtElement.COMPOUND_TYPE));

        if (nbt.contains("LastPlayer", NbtElement.COMPOUND_TYPE)) {
            this.lastPlayer = NbtHelper.toGameProfile(nbt.getCompound("LastPlayer"));
        }
    }

    public void tick() {
        var world = (ServerWorld) this.world;
        double armorStandHeight = 1.8;//0.9;

        if (hologram == null) {
            this.hologram = new WorldHologram(world, Vec3d.ofBottomCenter(this.getPos()));
            this.mainElement = new ArmorStandHologramElement();
            //((ArmorStandEntityAccessor) this.mainElement.entity).callSetMarker(true);
            //((ArmorStandEntityAccessor) this.mainElement.entity).callSetSmall(true);
            this.mainElement.entity.setInvisible(true);
            this.mainElement.entity.equipStack(EquipmentSlot.HEAD, HeadModels.create(this.isActiveTexture));
            this.mainElement.setOffset(new Vec3d(0, -armorStandHeight + 0.5, 0));
            this.hologram.addElement(new EmptyHologramElement());
            this.hologram.addElement(new SpacingHologramElement(1.3));
            this.hologram.addElement(this.mainElement);


            this.hologram.show();
        }



        if (this.currentBiomeId == null && this.isActive) {
            this.consumeEssence();
            if (this.currentBiomeId == null) {
                this.setActive(false, null);
                return;
            }
        }

        if (this.currentBiome == null) {
            var biome = this.world.getRegistryManager().get(RegistryKeys.BIOME).getEntry(this.currentBiomeId);
            if (biome.isPresent()) {
                this.currentBiome = biome.get();
            } else {
                this.setActive(false, null);
                this.currentBiomeId = null;
                return;
            }
        }


        if (this.yDelta != 0) {
            var yPos = (Math.sin(this.world.getTime() / 20d) + 0.3) * this.yDelta * 0.12 - 0.1;

            if (yPos < -0.06) {
                yPos = Math.abs(yPos) - 0.12;
            }

            this.mainElement.entity.setYaw((float) (this.mainElement.entity.getYaw() + this.yDelta * this.yDelta * 4));
            this.mainElement.setOffset(new Vec3d(0, yPos - armorStandHeight + 0.5, 0));

            //var base = MathHelper.hsvToRgb((float) (MathHelper.wrapDegrees(this.world.getTime() / 2d) * MathHelper.RADIANS_PER_DEGREE), 0.9f, 0.9f);


            var base = ModUtil.getBiomeColor(this.currentBiome);

            var color = new Vector3f((float) ((ColorHelper.Argb.getRed(base) / 256f) * this.yDelta), (float) ((ColorHelper.Argb.getGreen(base) / 256f) * this.yDelta), (float) ((ColorHelper.Argb.getBlue(base) / 256f) * this.yDelta));
            //var color = new Vec3f(ColorHelper.Argb.getRed(base) / 255f, ColorHelper.Argb.getGreen(base) / 255f, ColorHelper.Argb.getBlue(base) / 255f);
            var particle2 = new DustParticleEffect(color, (float) (this.yDelta * 1.5));
            sendParticle(world, true, particle2, this.pos.getX() + 0.5, this.pos.getY() + 0.75 + yPos, this.pos.getZ() + 0.5, 0, (float) (Math.random() * 0.01), 10, (float) (Math.random() * 0.01), 1);

            if (this.world.getTime() % 3 == 0) {
                int lines = Math.max(4, (int) (this.radius * 0.6));
                int lines2 = Math.max(4, (int) (this.conversionProgress * 0.6));
                float angle = MathHelper.TAU / lines;
                float angle2 = MathHelper.TAU / lines2;
                float startAngle = MathHelper.RADIANS_PER_DEGREE * MathHelper.wrapDegrees(this.world.getTime());
                var conversionRadius = (this.conversionProgress + 1);

                sendParticle(world, true, particle2, this.pos.getX() + 0.5, this.pos.getY() + 1 + (this.world.getTime() % (conversionRadius * 2)) / 2, this.pos.getZ() + 0.5, (float) (Math.random() * 0.01), 10, (float) (Math.random() * 0.01), 10, 0);

                for (int i = 0; i < conversionRadius; i++) {
                    var currentAngle = this.world.random.nextFloat() * MathHelper.TAU;

                    var rRadius = this.world.random.nextFloat() * conversionRadius;

                    double x = MathHelper.sin(currentAngle) * rRadius;
                    double z = MathHelper.cos(currentAngle) * rRadius;

                    var currentAngle2 = this.world.random.nextFloat() * MathHelper.TAU;

                    double mult = MathHelper.cos(currentAngle2);
                    double y = MathHelper.sin(currentAngle2);

                    sendParticle(world, true, particle2, this.pos.getX() + 0.5 + x * mult, this.pos.getY() + 0.5 + y * rRadius, this.pos.getZ() + 0.5 + z * mult, 0, 0, 0, 0, 0);
                }

                float currentAngle = startAngle;
                float currentAngle2 = startAngle;

                for (int i = 0; i <= lines2; i++) {
                    double x = -MathHelper.sin(currentAngle) * conversionRadius;
                    double z = MathHelper.cos(currentAngle) * this.conversionProgress;

                    for (int i2 = 0; i2 < lines2; i2++) {
                        double mult = MathHelper.cos(currentAngle2);
                        double y = MathHelper.sin(currentAngle2);

                        var particle = new DustParticleEffect(color, (float) ((float) this.yDelta * (3 - y) * Math.min(conversionRadius / 8d, 1)));

                        sendParticle(world, this.radius != this.conversionProgress, particle, this.pos.getX() + 0.5 + x * mult, this.pos.getY() + 0.5 + y * conversionRadius, this.pos.getZ() + 0.5 + z * mult, 0, 0, 0, 0, 0);

                        currentAngle2 = currentAngle2 + angle2;

                        if (currentAngle2 > MathHelper.TAU) {
                            currentAngle2 -= MathHelper.TAU;
                        }
                    }
                    currentAngle2 = startAngle;
                    currentAngle += angle2;
                    if (currentAngle > MathHelper.TAU) {
                        currentAngle -= MathHelper.TAU;
                    }
                }

                var radius = this.radius + 1;

                if (this.radius != this.conversionProgress) {
                    for (int i = 0; i <= lines; i++) {
                        double x = MathHelper.sin(currentAngle) * radius;
                        double z = MathHelper.cos(currentAngle) * radius;

                        for (int i2 = 0; i2 < lines; i2++) {
                            double mult = MathHelper.cos(currentAngle2);
                            double y = MathHelper.sin(currentAngle2);

                            var particle = new DustParticleEffect(color, (float) ((float) this.yDelta * (3 - y) * 0.6));

                            sendParticle(world, false, particle, this.pos.getX() + 0.5 + x * mult, this.pos.getY() + 0.5 + y * radius, this.pos.getZ() + 0.5 + z * mult, 0, 0, 0, 0, 0);

                            currentAngle2 = currentAngle2 + angle;

                            if (currentAngle2 > MathHelper.TAU) {
                                currentAngle2 -= MathHelper.TAU;
                            }
                        }
                        currentAngle2 = startAngle;
                        currentAngle += angle;
                        if (currentAngle > MathHelper.TAU) {
                            currentAngle -= MathHelper.TAU;
                        }
                    }
                }
            }
        }

        if (this.isActive && (this.yDelta < 0.5 || (this.yDelta < 1 && this.isActiveTexture))) {
            this.yDelta = Math.min(this.yDelta + 0.05, 1);
        } else if (!this.isActive && this.yDelta != 0) {
            this.yDelta = Math.max(this.yDelta - 0.03, 0);
        }

        int energyRequired = this.world.getGameRules().getInt(BGameRules.REQUIRED_FUEL_PER_CHANGE);

        if (this.isActive && this.yDelta >= 0.5 && this.tickDelay <= 0 && this.energy > 0 && this.essenceUsesLeft > 0) {
            int maxChecks = this.world.getGameRules().getInt(BGameRules.MAX_CHECKS_PER_TICK);
            int delay = this.world.getGameRules().getInt(BGameRules.DELAY_BETWEEN_CHANGES);
            boolean forward = true;

            int cycles = 0;

            while (this.radius + 1 != this.conversionProgress && forward) {
                if (this.conversionPos.getSquaredDistance(BlockPos.ORIGIN) < this.radius * this.radius
                        && ModUtil.setBiome(world,
                        this.getPos().getX() + this.conversionPos.getX(), this.getPos().getY() + this.conversionPos.getY(), this.getPos().getZ() + this.conversionPos.getZ(),
                        this.currentBiome, this.lastPlayer, this.dirtChunks::add
                )
                ) {
                    if (delay > 0) {
                        forward = false;
                    }
                    this.tickDelay = delay;
                    this.energy -= energyRequired;
                    this.essenceUsesLeft -= 1;
                    if (!this.isActiveTexture && this.yDelta > 0.5 && this.isActive) {
                        this.setTexture(true);
                    }
                }

                if (this.conversionPos.getY() == -this.conversionProgress || this.conversionPos.getY() == this.conversionProgress) {
                    if (this.conversionPos.getX() >= this.conversionProgress) {
                        this.conversionPos.setX(-this.conversionProgress);
                        this.conversionPos.move(0, 0, 1);
                    } else {
                        this.conversionPos.move(1, 0, 0);
                    }

                    if (this.conversionPos.getZ() > this.conversionProgress) {
                        this.conversionPos.set(-this.conversionProgress, this.conversionPos.getY() + 1, -this.conversionProgress);
                    }
                } else {
                    /*if (this.conversionPos.getX() == -this.conversionProgress || this.conversionPos.getX() == this.conversionProgress) {
                        this.conversionPos.setX(-this.conversionProgress);
                        this.conversionPos.move(0, 0, 1);
                    }*/
                    if (this.conversionPos.getX() == -this.conversionProgress || this.conversionPos.getX() == this.conversionProgress) {
                        this.conversionPos.move(0, 0, 1);
                    } else {
                        this.conversionPos.move(0, 0, this.conversionProgress * 2);
                    }

                    if (this.conversionPos.getZ() > this.conversionProgress) {
                        this.conversionPos.set(this.conversionPos.getX() + 1, this.conversionPos.getY(), -this.conversionProgress);
                        if (this.conversionPos.getX() > this.conversionProgress) {
                            this.conversionPos.set(-this.conversionProgress, this.conversionPos.getY() + 1, -this.conversionProgress);
                        }
                    }


                    if (this.conversionPos.getY() > this.conversionProgress) {
                        this.conversionProgress++;
                        this.conversionPos.set(-this.conversionProgress, -this.conversionProgress, -this.conversionProgress);
                    }
                }


                cycles++;

                if (cycles >= maxChecks) {
                    break;
                }
            }

            if (this.radius <= this.conversionProgress) {
                this.setActive(false, null);
                //this.hologram.setText(0, Text.literal("Status: OFF"));
            } else {
                //this.hologram.setText(0, Text.literal("Status: " + this.conversionProgress + "/" + this.radius));
            }
        } else {
            this.tickDelay--;
        }

        if (this.energy <= energyRequired) {
            if (this.isActive) {
                for (int i = 0; i < 3; i++) {
                    var stack = this.fuelInventory.getStack(i);
                    var value = FuelRegistry.INSTANCE.get(stack.getItem());
                    if (value != null && value > 0) {
                        while (!stack.isEmpty() && this.energy <= energyRequired) {
                            if (stack.isOf(Items.LAVA_BUCKET)) {
                                this.fuelInventory.setStack(i, new ItemStack(Items.BUCKET));
                            } else {
                                stack.decrement(1);
                            }
                            this.energy += value;
                        }
                    }
                }
            }

            if (this.energy <= 0) {
                if (this.isActiveTexture) {
                    this.setTexture(false);
                    this.yDelta = 0.6;
                }
            }
        }

        this.consumeEssence();

        if (this.world.getTime() % 40 == 0 && this.dirtChunks.size() > 0) {
            for (var chunk : this.dirtChunks) {
                ModUtil.updateChunk(chunk);
            }
            this.dirtChunks.clear();
        }
    }

    private void sendParticle(ServerWorld world, boolean requireIn, DustParticleEffect particle, double x, double y, double z, float offsetX, float offsetY, float offsetZ, float speed, int i4) {
        var packet = new ParticleS2CPacket(particle, false, x, y, z, offsetX, offsetY, offsetZ, speed, i4);

        for (var player : world.getPlayers()) {
            var delta = player.getPos().subtract(x, y, z);
            double length2 = delta.lengthSquared();
            if (length2 > 24 * 24) {
                continue;
            }

            if (requireIn && !this.getPos().isWithinDistance(player.getPos(), this.radius)) {
                continue;
            }

            var rotation = player.getRotationVec(1.0F);
            double dot = (delta.multiply(1.0 / Math.sqrt(length2))).dotProduct(rotation);
            if (dot > 0.0) {
                continue;
            }

            player.networkHandler.sendPacket(packet);
        }
    }

    private void setTexture(boolean active) {
        if (this.isActiveTexture != active) {
            this.mainElement.entity.equipStack(EquipmentSlot.HEAD, HeadModels.create(active));
            this.isActiveTexture = active;
        }
    }

    public void setBiome(RegistryKey<Biome> biome) {
        if (!Objects.equals(biome, this.currentBiomeId)) {
            this.currentBiomeId = biome;
            this.currentBiome = null;
            this.essenceUsesLeft = 0;
            this.conversionPos.set(0, 0, 0);
            this.conversionProgress = 0;
        }
    }

    public void consumeEssence() {
        if (this.essenceUsesLeft <= 0) {

            if (this.isActive) {
                for (int i = 0; i < 6; i++) {
                    var stack = this.essenceInventory.getStack(i);

                    if (stack.isOf(BItems.BIOME_ESSENCE) && !stack.isEmpty()) {

                        if (this.currentBiomeId == null) {
                            this.currentBiomeId = BiomeEssenceItem.getBiomeKey(stack);
                        } else if (!this.currentBiomeId.equals(BiomeEssenceItem.getBiomeKey(stack))) {
                            continue;
                        }

                        stack.decrement(1);
                        this.essenceUsesLeft += this.world.getGameRules().getInt(BGameRules.USES_PER_ESSENCE);
                        break;
                    }
                }
            }

            if (this.essenceUsesLeft <= 0) {
                if (this.isActiveTexture) {
                    this.setTexture(false);
                    this.yDelta = 0.6;
                }
            }
        }
    }

    @Override
    public void markRemoved() {
        if (this.hologram != null) {
            this.hologram.hide();
        }

        if (this.world.getTime() % 10 == 0 && this.dirtChunks.size() > 0) {
            for (var chunk : this.dirtChunks) {
                ModUtil.updateChunk(chunk);
            }
            this.dirtChunks.clear();
        }

        this.hologram = null;
    }

    public void setActive(boolean active, @Nullable GameProfile profile) {
        this.lastPlayer = profile;
        if (this.isActive != active) {
            this.isActive = active;
            this.world.setBlockState(pos, this.getCachedState().with(BiomeConverterBlock.ACTIVE, active));
            this.conversionProgress = 0;
            this.conversionPos.set(0, 0, 0);
            if (this.mainElement != null && !active && this.isActiveTexture) {
                this.setTexture(false);
                this.mainElement.markDirty();
            }

            if (active) {
                for (int i = 0; i < 6; i++) {
                    var stack = this.essenceInventory.getStack(i);
                    if (!stack.isEmpty() && !Objects.equals(this.currentBiomeId, BiomeEssenceItem.getBiomeKey(stack))) {
                        this.setBiome(null);
                        break;
                    }
                }
            }
        }
    }

    public void use(ServerPlayerEntity serverPlayer, BlockHitResult hit) {
        //var active = this.getCachedState().get(BiomeConverterBlock.ACTIVE);
        //this.setActive(!active);

        new ConverterGui(serverPlayer, this);
    }

    @Override
    public Text getConvName() {
        return this.getCachedState().getBlock().getName();
    }

    @Override
    public void radius(int i) {
        this.radius = i;
    }

    public boolean shouldClose(ServerPlayerEntity player) {
        return this.isRemoved() || this.getPos().getSquaredDistanceFromCenter(player.getX(), player.getY(), player.getZ()) > 256;
    }

    @Override
    public Inventory fuelInventory() {
        return this.fuelInventory;
    }

    @Override
    public Inventory essenceInventory() {
        return this.essenceInventory;
    }

    public boolean isActivated() {
        return this.isActive;
    }

    @Override
    public int radius() {
        return this.radius;
    }

    @Override
    public RegistryKey<Biome> currentBiomeId() {
        return this.currentBiomeId;
    }

    @Override
    public int energy() {
        return this.energy;
    }

    @Override
    public List<StackedInv> getInventories() {
        return this.allInventories;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return this.slots;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return (this.fuelInventoryStack.contains(slot) && AbstractFurnaceBlockEntity.canUseAsFuel(stack))
                || (this.essenceInventoryStack.contains(slot) && stack.isOf(BItems.BIOME_ESSENCE));
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return (this.fuelInventoryStack.contains(slot) && !AbstractFurnaceBlockEntity.canUseAsFuel(stack))
                || (this.essenceInventoryStack.contains(slot) && !stack.isOf(BItems.BIOME_ESSENCE));    }
}
