package org.boblycat.blimp.layers;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.geometry.Rotate180;
import net.sourceforge.jiu.geometry.Rotate90Left;
import net.sourceforge.jiu.geometry.Rotate90Right;

import org.boblycat.blimp.Bitmap;

public class OrientationLayer extends AdjustmentLayer {
    public enum Rotation {
        None,
        Rotate90Left,
        Rotate90Right,
        Rotate180,
    }
    
    private Rotation rotation;
    
    public OrientationLayer() {
        rotation = Rotation.None;
    }
    
    @Override
    public Bitmap applyLayer(Bitmap source) {
        PixelImage image = source.getImage();
        switch (rotation) {
        case None:
            break;
        case Rotate90Left:
            image = applyJiuOperation(image, new Rotate90Left());
            break;
        case Rotate90Right:
            image = applyJiuOperation(image, new Rotate90Right());
            break;
        case Rotate180:
            image = applyJiuOperation(image, new Rotate180());
            break;
        }
        return new Bitmap(image);
    }

    @Override
    public String getDescription() {
        return "Image Orientation";
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }

    public Rotation getRotation() {
        return rotation;
    }

}
