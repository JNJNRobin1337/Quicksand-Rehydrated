package net.mokai.quicksandrehydrated.mixins;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.mokai.quicksandrehydrated.util.ModTags.Blocks.QUICKSAND_AO_OVERRIDE;

@Mixin(BlockState.class)
public abstract class CanOccludeMixin extends BlockBehaviour.BlockStateBase {

    protected CanOccludeMixin(Block pOwner, ImmutableMap<Property<?>, Comparable<?>> pValues,
                              MapCodec<BlockState> pPropertiesCodec) {
        super(pOwner, pValues, pPropertiesCodec);
    }

    @Override
    public boolean canOcclude() {
        BlockBehaviour.BlockStateBase bsb = (BlockBehaviour.BlockStateBase)(Object) this;
        if (bsb.is(QUICKSAND_AO_OVERRIDE)) {
            return true;
        }
        return super.canOcclude();
    }

    // this one is important due to changes that assumes this one works!
    @Override
    public boolean isCollisionShapeFullBlock(BlockGetter p_60839_, BlockPos p_60840_) {
        BlockBehaviour.BlockStateBase bsb = (BlockBehaviour.BlockStateBase)(Object) this;
        if (bsb.is(QUICKSAND_AO_OVERRIDE)) {
            return true;
        }
        return super.isCollisionShapeFullBlock(p_60839_, p_60840_);
    }

//    @Override
//    public boolean useShapeForLightOcclusion(BlockGetter p_60839_, BlockPos p_60840_) {
//        BlockBehaviour.BlockStateBase bsb = (BlockBehaviour.BlockStateBase)(Object) this;
//        if (bsb.is(QUICKSAND_AO_OVERRIDE)) {
//            return true;
//        }
//        return super.isCollisionShapeFullBlock(p_60839_, p_60840_);
//    }


}
