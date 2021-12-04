package de.ellpeck.naturesaura.entities;

import de.ellpeck.naturesaura.api.aura.chunk.IAuraChunk;
import de.ellpeck.naturesaura.items.ModItems;
import de.ellpeck.naturesaura.packet.PacketHandler;
import de.ellpeck.naturesaura.packet.PacketParticles;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Mth;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.level.GameRules;
import net.minecraft.level.Level;
import net.minecraft.level.server.ServerLevel;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EntityMoverMinecart extends AbstractMinecartEntity {

    private final List<BlockPos> spotOffsets = new ArrayList<>();
    public boolean isActive;
    private BlockPos lastPosition = BlockPos.ZERO;

    public EntityMoverMinecart(EntityType<?> type, Level level) {
        super(type, level);
    }

    public EntityMoverMinecart(EntityType<?> type, Level level, double x, double y, double z) {
        super(type, level, x, y, z);
    }

    @Override
    public void moveMinecartOnRail(BlockPos railPos) {
        super.moveMinecartOnRail(railPos);
        if (!this.isActive)
            return;
        BlockPos pos = this.getPosition();

        if (!this.spotOffsets.isEmpty() && this.level.getGameTime() % 10 == 0)
            PacketHandler.sendToAllAround(this.level, pos, 32, new PacketParticles(
                    (float) this.getPosX(), (float) this.getPosY(), (float) this.getPosZ(), PacketParticles.Type.MOVER_CART,
                    Mth.floor(this.getMotion().getX() * 100F), Mth.floor(this.getMotion().getY() * 100F), Mth.floor(this.getMotion().getZ() * 100F)));

        if (pos.distanceSq(this.lastPosition) < 8 * 8)
            return;

        this.moveAura(this.level, this.lastPosition, this.level, pos);
        this.lastPosition = pos;
    }

    private void moveAura(Level oldLevel, BlockPos oldPos, Level newLevel, BlockPos newPos) {
        for (BlockPos offset : this.spotOffsets) {
            BlockPos spot = oldPos.add(offset);
            IAuraChunk chunk = IAuraChunk.getAuraChunk(oldLevel, spot);
            int amount = chunk.getDrainSpot(spot);
            if (amount <= 0)
                continue;
            int toMove = Math.min(amount, 300000);
            int drained = chunk.drainAura(spot, toMove, false, false);
            if (drained <= 0)
                continue;
            int toLose = Mth.ceil(drained / 250F);
            BlockPos newSpot = newPos.add(offset);
            IAuraChunk.getAuraChunk(newLevel, newSpot).storeAura(newSpot, drained - toLose, false, false);
        }
    }

    @Override
    public void onActivatorRailPass(int x, int y, int z, boolean receivingPower) {
        if (this.isActive != receivingPower) {
            this.isActive = receivingPower;

            BlockPos pos = this.getPosition();
            if (!this.isActive) {
                this.moveAura(this.level, this.lastPosition, this.level, pos);
                this.spotOffsets.clear();
                this.lastPosition = BlockPos.ZERO;
                return;
            }

            IAuraChunk.getSpotsInArea(this.level, pos, 25, (spot, amount) -> {
                if (amount > 0)
                    this.spotOffsets.add(spot.subtract(pos));
            });
            this.lastPosition = pos;
        }
    }

    @Override
    public void killMinecart(DamageSource source) {
        this.remove();
        if (this.level.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS))
            this.entityDropItem(new ItemStack(ModItems.MOVER_CART), 0);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compound = super.serializeNBT();
        compound.putBoolean("active", this.isActive);
        compound.putLong("last_pos", this.lastPosition.toLong());

        ListNBT list = new ListNBT();
        for (BlockPos offset : this.spotOffsets)
            list.add(LongNBT.valueOf(offset.toLong()));
        compound.put("offsets", list);
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundTag compound) {
        super.deserializeNBT(compound);
        this.isActive = compound.getBoolean("active");
        this.lastPosition = BlockPos.fromLong(compound.getLong("last_pos"));

        this.spotOffsets.clear();
        ListNBT list = compound.getList("offsets", Constants.NBT.TAG_LONG);
        for (INBT base : list)
            this.spotOffsets.add(BlockPos.fromLong(((LongNBT) base).getLong()));
    }

    @Nullable
    @Override
    public Entity changeDimension(ServerLevel destination, ITeleporter teleporter) {
        Entity entity = super.changeDimension(destination, teleporter);
        if (entity instanceof EntityMoverMinecart) {
            BlockPos pos = entity.getPosition();
            this.moveAura(this.level, this.lastPosition, entity.level, pos);
            ((EntityMoverMinecart) entity).lastPosition = pos;
        }
        return entity;
    }

    @Override
    public BlockState getDisplayTile() {
        return Blocks.STONE.getDefaultState();
    }

    @Override
    public Type getMinecartType() {
        return Type.RIDEABLE;
    }

    @Override
    public ItemStack getCartItem() {
        return new ItemStack(ModItems.MOVER_CART);
    }

    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        return new ItemStack(ModItems.MOVER_CART);
    }

    @Override
    public boolean canBeRidden() {
        return false;
    }

    @Override
    protected void applyDrag() {
        Vector3d motion = this.getMotion();
        this.setMotion(motion.x * 0.99F, 0, motion.z * 0.99F);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
