package de.ellpeck.naturesaura.data;

import de.ellpeck.naturesaura.NaturesAura;
import de.ellpeck.naturesaura.blocks.ModBlocks;
import de.ellpeck.naturesaura.reg.ModRegistry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class BlockTagProvider extends BlockTagsProvider {

    public static final TagKey<Block> ALTAR_WOOD = BlockTags.create(new ResourceLocation(NaturesAura.MOD_ID, "altar_wood"));
    public static final TagKey<Block> ALTAR_STONE = BlockTags.create(new ResourceLocation(NaturesAura.MOD_ID, "altar_stone"));
    public static final TagKey<Block> NETHER_ALTAR_WOOD = BlockTags.create(new ResourceLocation(NaturesAura.MOD_ID, "nether_altar_wood"));
    public static final TagKey<Block> NETHER_ALTAR_STONE = BlockTags.create(new ResourceLocation(NaturesAura.MOD_ID, "nether_altar_stone"));

    public BlockTagProvider(DataGenerator gen, @Nullable ExistingFileHelper existingFileHelper) {
        super(gen, NaturesAura.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        this.tag(BlockTags.LOGS).add(ModBlocks.ANCIENT_LOG, ModBlocks.ANCIENT_BARK);
        this.tag(BlockTags.PLANKS).add(ModBlocks.ANCIENT_PLANKS);
        this.tag(BlockTags.STAIRS).add(ModBlocks.ANCIENT_STAIRS, ModBlocks.INFUSED_BRICK_STAIRS, ModBlocks.INFUSED_STAIRS);
        this.tag(BlockTags.LEAVES).add(ModBlocks.GOLDEN_LEAVES, ModBlocks.ANCIENT_LEAVES, ModBlocks.DECAYED_LEAVES);
        this.tag(BlockTags.RAILS).add(ModBlocks.DIMENSION_RAIL_END, ModBlocks.DIMENSION_RAIL_NETHER, ModBlocks.DIMENSION_RAIL_OVERWORLD);
        this.tag(BlockTags.SLABS).add(ModBlocks.ANCIENT_SLAB, ModBlocks.INFUSED_SLAB, ModBlocks.INFUSED_BRICK_SLAB);
        this.tag(BlockTags.DIRT).add(ModBlocks.NETHER_GRASS);
        this.tag(BlockTags.SMALL_FLOWERS).add(ModBlocks.END_FLOWER, ModBlocks.AURA_BLOOM);
        this.tag(BlockTagProvider.ALTAR_WOOD).addTag(BlockTags.PLANKS);
        this.tag(BlockTagProvider.ALTAR_STONE).addTag(BlockTags.STONE_BRICKS);
        this.tag(BlockTagProvider.NETHER_ALTAR_WOOD).add(Blocks.CRIMSON_PLANKS, Blocks.WARPED_PLANKS);
        this.tag(BlockTagProvider.NETHER_ALTAR_STONE).add(Blocks.NETHER_BRICKS);

        for (var item : ModRegistry.ALL_ITEMS) {
            if (!(item instanceof Block b))
                continue;
            var state = b.defaultBlockState();
            if (!state.requiresCorrectToolForDrops())
                continue;
            var material = state.getMaterial();
            if (material == Material.STONE) {
                this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(b);
            } else if (material == Material.WOOD) {
                this.tag(BlockTags.MINEABLE_WITH_AXE).add(b);
            }
        }
    }
}
