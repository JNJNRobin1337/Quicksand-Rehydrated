package net.mokai.quicksandrehydrated.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.UUID;

public class EntityTarGolem extends AbstractGolem implements NeutralMob {

    /**
     * TODO: We want to implement more complex behavior, probably through a TarGolemBrain class akin to Piglins.
     *
     * ALL STATES:
     *   - Has natural regeneration.
     *   - Physical attacks by and against the Golem grant a short Tarred status, reducing movement and swing speed, regardless of alignment.
     *
     * ATTACKS:
     *   - Slam: default attack, always starts with this.
     *       - use the full attack animation. The initial hit has negative knockback and pulls the player in closer,
     *         and then a following Slam attack (part of the same animation) attaches a Tar lead to the player.
     *   - Tar Drag: If it has a target leaded and it has a home base, uses this.
     *       - Drags the leaded target backwards towards its home base, moving past until it drags the target into the tar pit.
     *   - Absorb: If a target is leaded and it does NOT have a home base, use this.
     *       - Functions identical to slimes; attaches to the head and slowly sinks in.
     *
     * HOSTILE: Attacks on sight, and has a home block in a tar pit.
     *   - Touching the Tar Golem at any point will attach a Tar lead.
     *   - Once leaded, the Tar Golem has a 75-25% chance to use Tar Drag or absorb, rerolling this chance any time it takes a Melee attack.
     *   - If its home is covered or destroyed, always use Absorb.
     *
     * FRIENDLY:
     *   - The Tar Golem is friendly towards its creator, and passive towards other players and mobs. It will attack in retaliation,
     *     and attack anyone its controller hits, a la wolves.
     *   - Does not do pit-dragging behavior.
     *   - Can be mounted by creator by touching above floor height.
     *
     * MOUNTED:
     *   - Directly controlled like horses.
     *   - Climbs up 1-block steps like iron golems.
     *   - Jump has it use the Absorb attack on you; Shift automatically escapes and returns you to normal Mounted state.
     */


    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(EntityTarGolem.class, EntityDataSerializers.BOOLEAN);

    public AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;
    public AnimationState attackingAnimationState = new AnimationState();
    public int attackAnimationTimeout = 0;

    public EntityTarGolem(EntityType<? extends AbstractGolem> pEntityType, Level pLevel) {super(pEntityType, pLevel);}

    public static AttributeSupplier setAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2)
                .add(Attributes.MOVEMENT_SPEED, .25)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5f)
                .add(Attributes.ATTACK_DAMAGE, 2f)
                .build();
    }


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9D, 32.0F));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot pSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot pSlot, ItemStack pStack) {}

    @Override
    public HumanoidArm getMainArm() {
        return null;
    }
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.DROWNED_HURT;
    }
    protected SoundEvent getDeathSound() {
        return SoundEvents.DROWNED_DEATH;
    }


    @Override
    public int getRemainingPersistentAngerTime() {
        return 0;
    }

    @Override
    public void setRemainingPersistentAngerTime(int pRemainingPersistentAngerTime) {

    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return null;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID pPersistentAngerTarget) {

    }

    @Override
    public void startPersistentAngerTimer() {

    }


    @Override
    public void tick() {
        super.tick();
        setupAnimationStates();
    }

    private void setupAnimationStates() {
        if(this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = this.random.nextInt(40) + 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }

        if(this.isAttacking() && attackAnimationTimeout <= 0) {
            attackAnimationTimeout = 80; // Length in ticks of your animation
            attackingAnimationState.start(this.tickCount);
        } else {
            --this.attackAnimationTimeout;
        }

        if(!this.isAttacking()) {
            attackingAnimationState.stop();
        }
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    @Override
    protected void updateWalkAnimation(float pPartialTick) {
        float f;
        if(this.getPose() == Pose.STANDING) {
            f = Math.min(pPartialTick * 6F, 1f);
        } else {
            f = 0f;
        }

        this.walkAnimation.update(f, 0.2f);
    }


}
