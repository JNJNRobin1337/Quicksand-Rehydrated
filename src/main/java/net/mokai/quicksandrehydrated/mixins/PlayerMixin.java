package net.mokai.quicksandrehydrated.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.mokai.quicksandrehydrated.QuicksandRehydrated;
import net.mokai.quicksandrehydrated.block.quicksands.core.QuicksandBase;
import net.mokai.quicksandrehydrated.entity.coverage.CoverageSerializer;
import net.mokai.quicksandrehydrated.entity.coverage.PlayerCoverage;
import net.mokai.quicksandrehydrated.entity.entityQuicksandVar;
import net.mokai.quicksandrehydrated.entity.playerStruggling;
import net.mokai.quicksandrehydrated.networking.ModMessages;
import net.mokai.quicksandrehydrated.networking.packet.StruggleDownC2SPacket;
import net.mokai.quicksandrehydrated.networking.packet.StruggleReleaseC2SPacket;
import net.mokai.quicksandrehydrated.util.Keybinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(Player.class)
public class PlayerMixin implements playerStruggling {


    private static final UUID GRAVITY_MODIFIER_QUICKSAND_UUID = UUID.fromString("b8c5b4f6-8188-4466-8239-53c567b11b32");
    private static final AttributeModifier GRAVITY_MODIFIER_QUICKSAND = new AttributeModifier(GRAVITY_MODIFIER_QUICKSAND_UUID, "Quicksand Gravity Cancel", (double)-1.0F, AttributeModifier.Operation.MULTIPLY_BASE);

    int struggleHold = 0;
    boolean holdingStruggle = false;

    public PlayerCoverage coverage = new PlayerCoverage();

    public PlayerCoverage getCoverage() {
        return this.coverage;
    }





    String coverageTexture = "textures/entity/coverage/quicksand_coverage.png";

    public String getCoverageTexture() {return coverageTexture;}
    public void setCoverageTexture(String set) {this.coverageTexture = set;}





    @Override
    public boolean getHoldingStruggle() {return this.holdingStruggle;}

    @Override
    public void setHoldingStruggle(boolean set) {this.holdingStruggle = set;}

    @Override
    public int getStruggleHold() {
        return this.struggleHold;
    }
    @Override
    public void setStruggleHold(int set) {
        this.struggleHold = set;
    }
    @Override
    public void addStruggleHold(int add) {
        this.struggleHold = this.struggleHold + add;
    }

    @Override
    public void attemptStruggle() {

        Player player = (Player)(Object) this;
        entityQuicksandVar QuicksandVarEntity = (entityQuicksandVar) player;
        playerStruggling strugglingPlayer = (playerStruggling) player;
        System.out.println("attemptStruggle()");

        if (QuicksandVarEntity.getInQuicksand()) {

            // find the block the player is stuck in
            BlockPos bp = QuicksandVarEntity.getStuckBlock(player);

            if (bp != null && QuicksandVarEntity.stuckBlockValid(bp, player)) {

                // can only do things if it exists, of course.

                Level eLevel = player.level();
                BlockState bs = eLevel.getBlockState(bp);

                int ticks = strugglingPlayer.getStruggleHold();

                // TODO; right now this has a hard coded value of 20.
                // Ideally there should be a way to determine what the max is per quicksand
                double struggleAmount = ticks / 20.0;

                QuicksandBase qs = (QuicksandBase) bs.getBlock();
                qs.struggleAttempt(bs, player, struggleAmount);

            }

        }

        strugglingPlayer.setStruggleHold(0);

    }

    public Vec3 velPosition0 = new Vec3(0, 0, 0);
    public Vec3 getVelPos0() {
        return this.velPosition0;
    }
    public void setVelPos0(Vec3 set) {
        this.velPosition0 = set;
    }
    public Vec3 velPosition1 = new Vec3(0, 0, 0);
    public Vec3 getVelPos1() {
        return this.velPosition1;
    }
    public void setVelPos1(Vec3 set) {
        this.velPosition1 = set;
    }


    public Vec3 getVelocity() {
        return this.velPosition0.subtract(this.velPosition1);
    }









    /**
     * Save player data when the player is saved
     */
    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    public void savePlayerData(CompoundTag compound, CallbackInfo ci) {
        try {
            // Save coverage data
            CompoundTag coverageTag = CoverageSerializer.serializeCoverage(this.coverage);
            compound.put("qsrehydrated_coverage", coverageTag);
        } catch (Exception e) {
            // Log error but don't crash the game
            System.err.println("[QuicksandRehydrated] Error saving player coverage data: " + e.getMessage());
        }
    }
    
    /**
     * Load player data when the player is loaded
     */
    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    public void loadPlayerData(CompoundTag compound, CallbackInfo ci) {
        try {
            // Load coverage data if it exists
            if (compound.contains("qsrehydrated_coverage")) {
                CompoundTag coverageTag = compound.getCompound("qsrehydrated_coverage");
                this.coverage = CoverageSerializer.deserializeCoverage(coverageTag);
            }
        } catch (Exception e) {
            // Log error but don't crash the game
            System.err.println("[QuicksandRehydrated] Error loading player coverage data: " + e.getMessage());
            // Initialize a new coverage if loading fails
            this.coverage = new PlayerCoverage();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickStruggleCooldown(CallbackInfo ci)
    {

        Player player = (Player)(Object) this;
        //System.out.println("tickStruggleCooldown(CallbackInfo ci)");
        this.velPosition1 = this.velPosition0; // 1 = previous
        this.velPosition0 = player.position(); // 0 = "current"

        entityQuicksandVar QuicksandVarEntity = (entityQuicksandVar) player;
        playerStruggling strugglingPlayer = (playerStruggling) player;

        AttributeInstance gravity = player.getAttribute(net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY.get());

        boolean inQuicksand = QuicksandVarEntity.getInQuicksand();

        if (inQuicksand) {
            BlockPos stuckPos = QuicksandVarEntity.getStuckBlock(player);
            inQuicksand = (stuckPos != null && QuicksandVarEntity.stuckBlockValid(stuckPos, player));
        }

        if (inQuicksand) {

            if (gravity != null && !gravity.hasModifier(GRAVITY_MODIFIER_QUICKSAND)) {
                gravity.addTransientModifier(GRAVITY_MODIFIER_QUICKSAND);
            }
        }
        else {
            if (gravity != null && gravity.hasModifier(GRAVITY_MODIFIER_QUICKSAND)) {
                gravity.removeModifier(GRAVITY_MODIFIER_QUICKSAND);
            }
        }

        /*
        if (gravity != null && gravity.hasModifier(GRAVITY_MODIFIER_QUICKSAND)) {
            player.displayClientMessage(Component.literal("has quicksand grav modifier"), false);
        }*/

        if (player.level().isClientSide()) {

            boolean keyDown = Keybinding.STRUGGLE_KEY.isDown();
            boolean flagHolding = strugglingPlayer.getHoldingStruggle();

            if ( keyDown && !flagHolding) {
                // key IS down this tick, flagHolding is NOT
                ModMessages.sendToServer(new StruggleDownC2SPacket());
            }
            else if ( !keyDown && flagHolding) {
                // key is NOT DOWN this tick, just released
                ModMessages.sendToServer(new StruggleReleaseC2SPacket());
            }

            strugglingPlayer.setHoldingStruggle(keyDown);

        }

        // both server AND client
        if (strugglingPlayer.getHoldingStruggle()) {
            addStruggleHold(1);
        }
        else if (strugglingPlayer.getStruggleHold() > 0) {
            strugglingPlayer.attemptStruggle();
        }

    }








}
