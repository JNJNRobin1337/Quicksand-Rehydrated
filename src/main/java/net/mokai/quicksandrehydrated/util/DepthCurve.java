package net.mokai.quicksandrehydrated.util;
import org.joml.Vector2d;

import java.util.ArrayList;

import static org.joml.Math.clamp;

public class DepthCurve {

    /**
     * This class is a helper class that stores a curve, designed to be instantiated to define quicksand behavior.
     */

    public InterpType interptype;
    public double start;
    public double end;
    public double exp;
    public double[] array;
    public ArrayList<Vector2d> points;



    public static Vector2d Vec2(double xx, double yy) {
        return new Vector2d(xx, yy);
    }



    public DepthCurve (double constant) {
        interptype = InterpType.CONSTANT;
        start = constant;
    }

    public DepthCurve (double[] array) {
        interptype = InterpType.ARRAY;
        this.array = array;
        start = array[0];
        end = array[array.length-1];
    }

    public DepthCurve (ArrayList<Vector2d> points) {
        interptype = InterpType.CUSTOM;
        this.points = points;
        start = points.get(0).y;
        end = points.get(points.size()-1).y;
    }

    public DepthCurve (InterpType type, double start, double end, double exponent) {
        if (type == InterpType.CUSTOM || type == InterpType.ARRAY) {
            throw new IllegalArgumentException("DepthCurve(Interptype, double, double, double) CANNOT be CUSTOM or ARRAY.");
        } else {
            interptype = type;
        }
            this.start = start;
            this.end = end;
            this.exp = exponent;
    }

    public DepthCurve(double start, double end) {
        interptype = InterpType.LINEAR;
        this.start = start;
        this.end = end;
    }


    public double getAt(double pos) {

        pos = Math.max(0, Math.min(pos, 1));

        switch (interptype) {
            case CONSTANT:  return start;
            case LINEAR:    return ease(start, end, pos);
            case POW_IN:    return ease(start, end, Math.pow(pos, exp));
            case POW_OUT:   return ease(start, end, 1-Math.pow(1-pos, exp));
            case POW_INOUT: return pos < .5 ? ease(start, end, Math.pow(pos*2, exp)/2) : ease(start, end, 1-Math.pow(2+pos*-2, exp)/2);
            case ARRAY:

                int len = array.length;
                if (array.length == 0) {
                    throw new IndexOutOfBoundsException("Cannot interpolate into an empty list. What would the correct default value be?");
                } else if (len == 1 || pos == 0) {
                    return array[0];
                } else if (pos == 1.0) {
                    return array[len-1];
                }

                int indexMaximum = len - 1;
                double scaledDouble = pos * indexMaximum;
                int leftIndex = (int) Math.floor(scaledDouble);
                int rightIndex = leftIndex + 1;

                double percent = rightIndex - scaledDouble;
                double val = ease(array[rightIndex], array[leftIndex], percent);

                return val;

            case CUSTOM:

                len = points.size();
                if (points.size() == 0) {
                    throw new IndexOutOfBoundsException("Cannot interpolate into an empty list. What would the correct default value be?");
                } else if (len == 1 || pos == 0) {
                    return points.get(0).x;
                } else if (pos == 1.0) {
                    return points.get(len-1).x;
                } else if (pos > points.get(len-1).y) {
                    return points.get(len-1).x;
                }

                int startIndex = 0;
                int endIndex = len-1;

                while (endIndex-startIndex > 1) {

                    int middle = (startIndex+endIndex) / 2;

                    if (pos < points.get(middle).y()) {
                        endIndex = middle;
                    }
                    else if (pos > points.get(middle).y()) {
                        startIndex = middle;
                    }

                }

                return ease(points.get(startIndex), points.get(endIndex), pos);

            default: return 0d;
        }
    }

    private double ease(double start, double end, double pos) {
        return (pos*(end-start))+start;
    }

    private double ease(Vector2d start, Vector2d end, double pos) {

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


    public enum InterpType {
        CONSTANT,
        LINEAR,
        POW_IN,
        POW_OUT,
        POW_INOUT,
        ARRAY,
        CUSTOM

    }

}
