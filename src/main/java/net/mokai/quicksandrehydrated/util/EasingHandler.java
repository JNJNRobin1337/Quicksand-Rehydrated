package net.mokai.quicksandrehydrated.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.mokai.quicksandrehydrated.block.quicksands.core.QuicksandBase;
import org.joml.Vector2d;

import java.util.ArrayList;

import static org.joml.Math.clamp;

public class EasingHandler {







    public static double lerp(double start, double end, double position) {
        position = Math.max(0, Math.min(position, 1)); // limits `position` to [0,1]
        return ease(start, end, position);
    }

    public static double ease_pow(double start, double end, double position, double exponent) {
        position = Math.max(0, Math.min(position, 1));
        return ease(Math.pow(position,exponent), start, end);
    }

    public static double ease_pow_inv(double start, double end, double position, double exponent) {
        position = Math.max(0, Math.min(position, 1));
        return Math.pow(position-1,exponent)*(start-end)+end;
    }

    public static double ease_inout(double start, double end, double position, double exponent) {
        if(position>.5){
            return ease_pow(start, end, position, exponent);
        } else {
            return ease_pow_inv(start, end, position, exponent);
        }
    }

    public static double reverse_interp(double start, double end, double position) {
        double b = end-start;
        return (position-start)/b;
    }


    public static double getDepth(Entity pEntity, Level pLevel, BlockPos pPos, double offset) {
        // Ottimizzazione: Usa direttamente la posizione Y dell'entità invece di getPosition(1)
        double playerY = pEntity.getY();
        double playerHeight = pEntity.getBbHeight();
        
        // Limita la ricerca verso l'alto per evitare loop infiniti
        BlockPos playercube = pPos;
        int maxHeight = pPos.getY() + 10; // Limita a 10 blocchi sopra la posizione attuale
        
        for (int y = pPos.getY() + 1; y <= maxHeight; y++) {
            BlockPos check = new BlockPos(pPos.getX(), y, pPos.getZ());
            if (!(pLevel.getBlockState(check).getBlock() instanceof QuicksandBase)) {
                playercube = check;
                break;
            }
            playercube = check;
        }
        
        // Calcola la profondità standard (dai piedi)
        double standardDepth = playercube.getY() - playerY - offset;
        
        // Calcola anche la profondità dalla testa
        double headY = playerY + playerHeight;
        double headDepth = playercube.getY() - headY - offset;
        
        // Se la testa è sotto la superficie, usa la profondità standard
        // Altrimenti, usa una formula che considera sia la posizione dei piedi che della testa
        if (headDepth > 0) {
            // La testa è sotto la superficie, usa la profondità standard
            return standardDepth;
        } else {
            // La testa è sopra la superficie, calcola una profondità proporzionale
            // alla parte del corpo che è effettivamente immersa
            double immersedPortion = (playerHeight + headDepth) / playerHeight;
            return standardDepth * immersedPortion;
        }
    }

    public static double getDepthPos(Vec3 worldPos, Level pLevel, BlockPos pPos, double offset) {
        double playerY = worldPos.y();
        
        // Limita la ricerca verso l'alto per evitare loop infiniti
        BlockPos playercube = pPos;
        int maxHeight = pPos.getY() + 10; // Limita a 10 blocchi sopra la posizione attuale
        
        for (int y = pPos.getY() + 1; y <= maxHeight; y++) {
            BlockPos check = new BlockPos(pPos.getX(), y, pPos.getZ());
            if (!(pLevel.getBlockState(check).getBlock() instanceof QuicksandBase)) {
                playercube = check;
                break;
            }
            playercube = check;
        }
        
        return playercube.getY() - playerY - offset;
    }



    public static double doubleListInterpolate(double val, double[] listOfDoubles) {

        // val should be scaled 0 to 1, to be mapped to either end of list
        // listOfDoubles is just a list of vals
        if (listOfDoubles.length == 0) {
            throw new IndexOutOfBoundsException("cannot interpolate into an empty list. What would the correct default value be?");
        }
        else if (listOfDoubles.length == 1) {
            return listOfDoubles[0];
        }

        if (val >= 1.0) {
            return listOfDoubles[listOfDoubles.length-1];
        }
        else if (val <= 0.0) {
            return listOfDoubles[0];
        }

        int indexMaximum = (listOfDoubles.length)-1;
        double scaledDouble = val * indexMaximum;

        int leftIndex = (int) Math.floor(scaledDouble);
        int rightIndex = leftIndex + 1;

        double percent = rightIndex - scaledDouble;

        double leftNumber = listOfDoubles[leftIndex];
        double rightNumber = listOfDoubles[rightIndex];

        return ease(leftNumber, rightNumber, 1-percent);

    }

    public static double vector2dArrayInterpolate(double val, ArrayList<Vector2d> points) {

        int len = points.size();
        if (points.size() == 0) {
            throw new IndexOutOfBoundsException("Cannot interpolate into an empty list. What would the correct default value be?");
        } else if (len == 1 || val == 0) {
            return points.get(0).x;
        } else if (val == 1.0) {
            return points.get(len-1).x;
        } else if (val > points.get(len-1).y) {
            return points.get(len-1).x;
        }

        int startIndex = 0;
        int endIndex = len-1;

        while (endIndex-startIndex > 1) {

            int middle = (startIndex+endIndex) / 2;

            if (val < points.get(middle).y()) {
                endIndex = middle;
            }
            else if (val > points.get(middle).y()) {
                startIndex = middle;
            }

        }

        return ease(points.get(startIndex), points.get(endIndex), val);

    }

    private static double ease(double start, double end, double pos) {
        return start + ((end-start) * pos);
    }

    private static double ease(Vector2d start, Vector2d end, double pos) {

        if (start.y() == end.y()) {
            return 1.0d;
        }

        double posPos = clamp(start.y(), end.y(), pos) - start.y();
        double posEnd = end.y() - start.y();
        // pos Start can be treated as 0

        // then, scale posEnd to 1 (move posPos accordingly)
        posPos = (1.0/posEnd) * posPos;
        posEnd = 1.0;

        double val = ease(start.x(), end.x(), posPos);
        return val;

    }

}
