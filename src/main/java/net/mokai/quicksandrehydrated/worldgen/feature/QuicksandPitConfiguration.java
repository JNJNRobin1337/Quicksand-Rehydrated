package net.mokai.quicksandrehydrated.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.mokai.quicksandrehydrated.registry.QuicksandRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration for the QuicksandPit feature.
 * Allows customizing the block used for the pit and other parameters.
 */
public class QuicksandPitConfiguration implements FeatureConfiguration {
    // Default values
    public static final Block DEFAULT_BLOCK = QuicksandRegistry.QUICKSAND.get();
    public static final int DEFAULT_MIN_RADIUS = 4;
    public static final int DEFAULT_MAX_RADIUS = 8;
    public static final int DEFAULT_MIN_DEPTH = 2;
    public static final int DEFAULT_MAX_DEPTH = 3;
    public static final float DEFAULT_IRREGULARITY = 0.5f;
    public static final boolean DEFAULT_HAS_BORDER = true;
    public static final Block DEFAULT_BORDER_BLOCK = null; // null means use the same block as the pit
    public static final List<Block> DEFAULT_REPLACEABLE_BLOCKS = Arrays.asList(
            Blocks.SAND, Blocks.SANDSTONE
    );

    // Codec for serialization/deserialization
    public static final Codec<QuicksandPitConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    // Block to use for the pit (defaults to quicksand if not specified)
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").orElse(DEFAULT_BLOCK).forGetter(config -> config.block),
                    // Minimum radius (defaults to 4 if not specified)
                    Codec.INT.fieldOf("min_radius").orElse(DEFAULT_MIN_RADIUS).forGetter(config -> config.minRadius),
                    // Maximum radius (defaults to 8 if not specified)
                    Codec.INT.fieldOf("max_radius").orElse(DEFAULT_MAX_RADIUS).forGetter(config -> config.maxRadius),
                    // Minimum depth (defaults to 2 if not specified)
                    Codec.INT.fieldOf("min_depth").orElse(DEFAULT_MIN_DEPTH).forGetter(config -> config.minDepth),
                    // Maximum depth (defaults to 3 if not specified)
                    Codec.INT.fieldOf("max_depth").orElse(DEFAULT_MAX_DEPTH).forGetter(config -> config.maxDepth),
                    // Irregularity factor (0.0 = more circular, 1.0 = more irregular)
                    Codec.FLOAT.fieldOf("irregularity").orElse(DEFAULT_IRREGULARITY).forGetter(config -> config.irregularity),
                    // Whether to generate a border around the pit
                    Codec.BOOL.fieldOf("has_border").orElse(DEFAULT_HAS_BORDER).forGetter(config -> config.hasBorder),
                    // Block to use for the border (defaults to same as pit if not specified)
                    BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("border_block").forGetter(config -> config.borderBlock != null ? java.util.Optional.of(config.borderBlock) : java.util.Optional.empty()),
                    // List of blocks that can be replaced by quicksand
                    BuiltInRegistries.BLOCK.byNameCodec().listOf().optionalFieldOf("replaceable_blocks").forGetter(config -> java.util.Optional.of(config.replaceableBlocks))
            ).apply(instance, QuicksandPitConfiguration::new));

    public final Block block;
    public final int minRadius;
    public final int maxRadius;
    public final int minDepth;
    public final int maxDepth;
    public final float irregularity;
    public final boolean hasBorder;
    public final Block borderBlock;
    public final List<Block> replaceableBlocks;

    public QuicksandPitConfiguration(Block block, int minRadius, int maxRadius, int minDepth, int maxDepth, 
                                    float irregularity, boolean hasBorder, java.util.Optional<Block> borderBlock,
                                    java.util.Optional<List<Block>> replaceableBlocks) {
        this.block = block;
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
        this.irregularity = irregularity;
        this.hasBorder = hasBorder;
        this.borderBlock = borderBlock.orElse(null);
        this.replaceableBlocks = replaceableBlocks.orElse(DEFAULT_REPLACEABLE_BLOCKS);
    }
}