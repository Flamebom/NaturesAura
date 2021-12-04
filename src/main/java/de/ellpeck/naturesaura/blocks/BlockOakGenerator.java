package de.ellpeck.naturesaura.blocks;

import de.ellpeck.naturesaura.Helper;
import de.ellpeck.naturesaura.api.NaturesAuraAPI;
import de.ellpeck.naturesaura.api.aura.type.IAuraType;
import de.ellpeck.naturesaura.api.render.IVisualizable;
import de.ellpeck.naturesaura.blocks.tiles.BlockEntityOakGenerator;
import de.ellpeck.naturesaura.data.BlockStateGenerator;
import de.ellpeck.naturesaura.reg.ICustomBlockState;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.level.ILevel;
import net.minecraft.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.SaplingGrowTreeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;

public class BlockOakGenerator extends BlockContainerImpl implements IVisualizable, ICustomBlockState {

    public BlockOakGenerator() {
        super("oak_generator", BlockEntityOakGenerator::new, Properties.create(Material.WOOD).hardnessAndResistance(2F).sound(SoundType.WOOD));

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTreeGrow(SaplingGrowTreeEvent event) {
        ILevel level = event.getLevel();
        BlockPos pos = event.getPos();
        if (level instanceof Level && !level.isClientSide() && IAuraType.forLevel(level).isSimilar(NaturesAuraAPI.TYPE_OVERWORLD)
                && level.getBlockState(pos).getBlock() instanceof SaplingBlock) {
            Helper.getBlockEntitiesInArea(level, pos, 10, tile -> {
                if (!(tile instanceof BlockEntityOakGenerator))
                    return false;

                Random rand = event.getRand();
                if (rand.nextInt(10) == 0)
                    ((BlockEntityOakGenerator) tile).scheduledBigTrees.add(pos);

                long seed;
                do {
                    seed = rand.nextLong();
                    rand.setSeed(seed);
                }
                while (rand.nextInt(10) == 0);
                rand.setSeed(seed);

                return true;
            });
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AxisAlignedBB getVisualizationBounds(Level level, BlockPos pos) {
        return new AxisAlignedBB(pos).grow(10);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getVisualizationColor(Level level, BlockPos pos) {
        return 0x2e7a11;
    }

    @Override
    public void generateCustomBlockState(BlockStateGenerator generator) {
        generator.simpleBlock(this, generator.models().cubeBottomTop(this.getBaseName(),
                generator.modLoc("block/" + this.getBaseName()),
                generator.modLoc("block/" + this.getBaseName() + "_bottom"),
                generator.modLoc("block/" + this.getBaseName() + "_top")));
    }
}
