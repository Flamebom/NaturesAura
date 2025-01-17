package de.ellpeck.naturesaura.items;

import de.ellpeck.naturesaura.api.NaturesAuraAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemNetheriteFinder extends ItemImpl {

    public ItemNetheriteFinder() {
        super("netherite_finder", new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level levelIn, Player playerIn, InteractionHand handIn) {
        var stack = playerIn.getItemInHand(handIn);
        var inst = NaturesAuraAPI.instance();
        if (!inst.extractAuraFromPlayer(playerIn, 200000, false))
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        if (levelIn.isClientSide) {
            inst.setParticleDepth(false);
            inst.setParticleSpawnRange(64);
            inst.setParticleCulling(false);

            var pos = playerIn.blockPosition();
            var range = 12;
            for (var x = -range; x <= range; x++) {
                for (var y = 0; y <= 128; y++) {
                    for (var z = -range; z <= range; z++) {
                        var offset = new BlockPos(pos.getX() + x, y, pos.getZ() + z);
                        var state = levelIn.getBlockState(offset);
                        if (state.getBlock() == Blocks.ANCIENT_DEBRIS || ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString().contains("netherite")) {
                            inst.spawnMagicParticle(
                                    offset.getX() + 0.5F, offset.getY() + 0.5F, offset.getZ() + 0.5F,
                                    0F, 0F, 0F, 0xab4d38, 6F, 20 * 60, 0F, false, true);
                        }
                    }
                }
            }

            inst.setParticleDepth(true);
            inst.setParticleSpawnRange(32);
            inst.setParticleCulling(true);
            playerIn.swing(handIn);
        }
        playerIn.getCooldowns().addCooldown(this, 20 * 60);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }
}
