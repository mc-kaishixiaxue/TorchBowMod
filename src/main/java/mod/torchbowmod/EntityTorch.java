package mod.torchbowmod;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;

import static mod.torchbowmod.TorchBowMod.*;
import static net.minecraft.core.Direction.DOWN;
import static net.minecraft.core.Direction.UP;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class EntityTorch extends AbstractArrow {
    enum EntityTorchMode{
        TORCH_STATE,
        ARROW_STATE
    }
    private EntityTorchMode state;

    public EntityTorch(PlayMessages.SpawnEntity packet, Level worldIn) {
        super(entityTorch.get(), worldIn);
    }

    public EntityTorch(EntityType<? extends EntityTorch> p_i50172_1_, Level p_i50172_2_) {
        super(p_i50172_1_, p_i50172_2_);
    }

    public EntityTorch(Level worldIn, double x, double y, double z) {
        super(entityTorch.get(), x, y, z, worldIn);
    }

    public EntityTorch(Level worldIn, LivingEntity shooter,EntityTorchMode mode) {
        super(entityTorch.get(), shooter, worldIn);
        state = mode;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityRayTraceResult) {
        super.onHitEntity(entityRayTraceResult);
        Entity entity = entityRayTraceResult.getEntity();
        entity.setRemainingFireTicks(100);
    }

    @Override
    protected void onHitBlock(BlockHitResult raytraceResultIn) {
        super.onHitBlock(raytraceResultIn);
        HitResult.Type raytraceresult$type = raytraceResultIn.getType();
        if (raytraceresult$type == HitResult.Type.BLOCK) {
            setTorch(raytraceResultIn, raytraceResultIn);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void setTorch(BlockHitResult blockraytraceresult, HitResult raytraceResultIn) {
        BlockPos blockpos = blockraytraceresult.getBlockPos();
        if (!this.level().getBlockState(blockpos).isAir()) {
            if (!level().isClientSide) {
                Direction face = ((BlockHitResult) raytraceResultIn).getDirection();
                BlockState torch_state = Blocks.WALL_TORCH.defaultBlockState();
                BlockPos setBlockPos = getPosOfFace(blockpos, face);
                if (isBlockAIR(setBlockPos)) {
                    if (face == UP) {
                        torch_state = Blocks.TORCH.defaultBlockState();
                        level().setBlock(setBlockPos,torch_state,3);
                        this.remove(RemovalReason.KILLED);
                    } else if (face == DOWN && CeilingTorch != null) {
                        BlockState ceiling_torch = CeilingTorch.defaultBlockState();
                        level().setBlock(setBlockPos, ceiling_torch,3);
                        this.remove(RemovalReason.KILLED);
                    } else if (face != DOWN) {
                        level().setBlock(setBlockPos, torch_state.setValue(HORIZONTAL_FACING, face), 3);
                        this.remove(RemovalReason.KILLED);
                    }
                }
            }
        }
    }

    private BlockPos getPosOfFace(BlockPos blockPos, Direction face) {
        return switch (face) {
            case UP -> blockPos.above();
            case EAST -> blockPos.east();
            case WEST -> blockPos.west();
            case SOUTH -> blockPos.south();
            case NORTH -> blockPos.north();
            case DOWN -> blockPos.below();
        };
    }

    private boolean isBlockAIR(BlockPos pos) {
        Block getBlock = this.level().getBlockState(pos).getBlock();
        if (getBlock instanceof BushBlock) return true;
        Block[] a = {Blocks.CAVE_AIR, Blocks.AIR, Blocks.SNOW, Blocks.VINE};//空気だとみなすブロックリスト
        for (Block traget : a) {
            if (getBlock == traget) return true;
        }
        return false;
    }

    @Override
    protected ItemStack getPickupItem() {
        if(state == EntityTorchMode.TORCH_STATE){
            return new ItemStack(Blocks.TORCH);
        }
        return new ItemStack(torchArrow.get());
    }
}