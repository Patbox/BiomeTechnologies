package eu.pb4.biometech.block.model;

import com.mojang.datafixers.util.Pair;
import eu.pb4.holograms.api.elements.AbstractHologramElement;
import eu.pb4.holograms.api.holograms.AbstractHologram;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class ArmorStandHologramElement extends AbstractHologramElement {
    public final ArmorStandEntity entity;
    private boolean isDirty = false;

    public ArmorStandHologramElement() {
        super();
        this.entity = new ArmorStandEntity(EntityType.ARMOR_STAND, PolymerUtils.getFakeWorld());
        this.getEntityIds().add(this.entity.getId());
    }

    @Override
    public void createSpawnPackets(ServerPlayerEntity player, AbstractHologram hologram) {
        this.entity.setPosition(hologram.getElementPosition(this).add(this.offset));

        player.networkHandler.sendPacket(this.entity.createSpawnPacket());
        var l =  this.entity.getDataTracker().getChangedEntries();
        if (l != null) {
            player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(this.entity.getId(), l));
        }
        player.networkHandler.sendPacket(createEqUpdate());
    }

    @Override
    public void updatePosition(ServerPlayerEntity player, AbstractHologram hologram) {
        this.entity.setPosition(hologram.getElementPosition(this).add(this.offset));

        player.networkHandler.sendPacket(new EntityPositionS2CPacket(this.entity));
    }

    @Override
    public void onTick(AbstractHologram hologram) {
        if (this.isDirty) {
            var vec = hologram.getElementPosition(this).add(this.offset);
            this.entity.updatePositionAndAngles(vec.x, vec.y, vec.z, this.entity.getYaw(), 0);

            var packet = createEqUpdate();
            var list = this.entity.getDataTracker().getDirtyEntries();
            var packet2 = list != null ? new EntityTrackerUpdateS2CPacket(this.entity.getId(), list) : null;
            var packet3 = new EntityPositionS2CPacket(this.entity);


            for (ServerPlayerEntity player : hologram.getPlayerSet()) {
                player.networkHandler.sendPacket(packet);
                if (packet2 != null) {
                    player.networkHandler.sendPacket(packet2);
                }
                player.networkHandler.sendPacket(packet3);

            }

            this.isDirty = false;
        }

        super.onTick(hologram);
    }

    public void setOffset(Vec3d vec3d) {
        this.offset = vec3d;
        this.isDirty = true;
    }

    private EntityEquipmentUpdateS2CPacket createEqUpdate() {
        List<Pair<EquipmentSlot, ItemStack>> list = new ArrayList<>(EquipmentSlot.values().length);
        for (var slot : EquipmentSlot.values()) {
            list.add(new Pair<>(slot, this.entity.getEquippedStack(slot)));
        }

        return new EntityEquipmentUpdateS2CPacket(this.entity.getId(), list);
    }

    public void markDirty() {
        this.isDirty = true;
    }
}
