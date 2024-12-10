package net.mokai.quicksandrehydrated.block.quicksands;

import net.mokai.quicksandrehydrated.block.quicksands.core.QuicksandBase;
import net.mokai.quicksandrehydrated.block.quicksands.core.QuicksandBehavior;
import net.mokai.quicksandrehydrated.util.EasingHandler;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

import static net.mokai.quicksandrehydrated.util.DepthCurve.Vec2;
import static org.joml.Math.clamp;

public class TestQuicksand extends QuicksandBase {
    public TestQuicksand(Properties pProperties, QuicksandBehavior QuicksandBehavior) {
        super(pProperties, QuicksandBehavior);
    }


    public double getVertSpeed(double depthRaw) {
        ArrayList<Vector2d> pts = new ArrayList<>(List.of(
                Vec2(0.3, 0.0),
                Vec2(0.1, 0.35/2.0),
                Vec2(0.05, 1.0/2.0)
        ));
        return EasingHandler.vector2dArrayInterpolate(depthRaw/2, pts);
    }

    public double getSinkSpeed(double depthRaw) {
        double ratio = getVertSpeed(0.0) / getVertSpeed(depthRaw);

        ArrayList<Vector2d> pts = new ArrayList<>(List.of(
                Vec2(0.002, 0.0),
                Vec2(0.0014, 0.3/2.0),
                Vec2(0.0003, 0.4/2.0),
                Vec2(0.0003, 1.0/2.0)
        ));
        return EasingHandler.vector2dArrayInterpolate(depthRaw/2, pts)*ratio;

    }

    public double getWalkSpeed(double depthRaw) {
        ArrayList<Vector2d> pts = new ArrayList<>(List.of(
                Vec2(1.0, 0.0),
                Vec2(0.9, 0.35/2.0),
                Vec2(0.1, 0.5/2.0),
                Vec2(0.0, 1.0/2.0)
        ));
        return EasingHandler.vector2dArrayInterpolate(depthRaw/2, pts);
    }

//    public double getWalkSpeed(double depthRaw) { return EasingHandler.doubleListInterpolate(depthRaw/2, new double[]{1.00d,   .9d, .75d, .250d,   .000d,   .000d, .000d, .000d,   .000d,  }); }





    public double getWobbleTugHorizontal(double depth) { return EasingHandler.doubleListInterpolate(depth/2, new double[]{0.0,   0.1, 0.12, 0.15, 0.2,     0.2,  0.2, 0.2, 0.2, 0.2,    0.2}); }
    public double getWobbleTugVertical(double depth) {   return EasingHandler.doubleListInterpolate(depth/2, new double[]{0.0,   0.1, 0.12, 0.15, 0.2,     0.2,  0.2, 0.2, 0.2, 0.2,    0.2}); }


    public double getWobbleMove(double depthRaw) {
        return 0.9d;
    }


    public double getWobbleDecay(double depthRaw) {
        return 0.94d;
    }

    public double getWobbleRebound(double depthRaw) {
        return 0.4d/20.0d;
    }

    public double getWobbleApply(double depthRaw) {
        return -0.5d/20.0d;
    }

}
