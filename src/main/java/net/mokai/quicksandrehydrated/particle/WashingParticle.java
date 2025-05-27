package net.mokai.quicksandrehydrated.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Particle effect for washing off quicksand coverage
 */
@OnlyIn(Dist.CLIENT)
public class WashingParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected WashingParticle(ClientLevel level, double x, double y, double z, 
                             double xSpeed, double ySpeed, double zSpeed, SpriteSet spriteSet) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        
        this.friction = 0.8F;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.quadSize *= 0.85F;
        this.lifetime = 20 + this.random.nextInt(10);
        this.sprites = spriteSet;
        
        // Set particle color (brownish for quicksand)
        this.rCol = 0.76F;
        this.gCol = 0.69F;
        this.bCol = 0.5F;
        
        this.setSpriteFromAge(spriteSet);
        this.hasPhysics = true;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        
        // Fade out as the particle ages
        if (this.age > this.lifetime / 2) {
            this.alpha = 1.0F - ((float)this.age - (float)(this.lifetime / 2)) / (float)this.lifetime;
        }
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType particleType, @NotNull ClientLevel level,
                                      double x, double y, double z,
                                      double xSpeed, double ySpeed, double zSpeed) {
            return new WashingParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}