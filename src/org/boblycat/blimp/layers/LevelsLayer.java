package org.boblycat.blimp.layers;

import net.sourceforge.jiu.ops.LookupTableOperation;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.NaturalCubicSpline;
import org.boblycat.blimp.Util;

class LevelsOperation extends LookupTableOperation {
    void setValues(double blackLevel, double center, double whiteLevel,
            int bitDepth) {
        assert (bitDepth == 8 || bitDepth == 16);
        int size = 1 << bitDepth;
        int[] table = new int[size];

        // point 1: (black, 0)
        // point 2: (center, 0.5)
        // point 3: (white, 1)
        NaturalCubicSpline spline = new NaturalCubicSpline();
        spline.addPoint(blackLevel, 0);
        // ignore the center value if it is not between black and white
        if (center > blackLevel && center < whiteLevel)
            spline.addPoint(center, 0.5);
        spline.addPoint(whiteLevel, 1);
        
        for (int i = 0; i < size; i++) {
            double x = i / (double) (size - 1);
            double y = spline.getSplineValue(x);
            int iy = (int) (y * (size - 1));
            if (iy < 0)
                iy = 0;
            else if (iy >= size)
                iy = size - 1;
            assert (iy >= 0 && iy < size);
            // System.out.println("" + i + " --> " + intY);
            table[i] = iy;
        }
        setTables(table);
    }
}

public class LevelsLayer extends AdjustmentLayer {
    private double blackLevel;
    private double center;
    private double whiteLevel;
    
    public LevelsLayer() {
        blackLevel = 0;
        center = 0.5;
        whiteLevel = 1;
    }

    @Override
    public Bitmap applyLayer(Bitmap source) {
        LevelsOperation op = new LevelsOperation();
        op.setValues(blackLevel, center, whiteLevel,
                source.getChannelBitDepth());
        return new Bitmap(applyJiuOperation(source.getImage(), op));
    }

    @Override
    public String getDescription() {
        return "Levels";
    }

    public void setBlackLevel(double blackLevel) {
        this.blackLevel = Util.constrainedValue(blackLevel, 0, 1);
    }

    public double getBlackLevel() {
        return blackLevel;
    }

    public void setCenter(double center) {
        if (center <= 0 || center >= 1)
            return;
        this.center = center;
    }

    public double getCenter() {
        return center;
    }

    public void setWhiteLevel(double whiteLevel) {
        this.whiteLevel = Util.constrainedValue(whiteLevel, 0, 1);
    }

    public double getWhiteLevel() {
        return whiteLevel;
    }

}
