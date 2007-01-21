package org.boblycat.blimp.exif;

import java.util.Iterator;
import java.util.Vector;

/**
 * An image file directory (IFD) with Exif data, as defined in TIFF.
 * 
 * @author Knut Arild Erstad
 */
public class ImageFileDirectory implements Iterable<ExifField> {
    Vector<ExifField> fields;
    
    public ImageFileDirectory() {
        fields = new Vector<ExifField>();
    }
    
    public void addField(ExifField field) {
        fields.add(field);
    }
    
    public Iterator<ExifField> iterator() {
        return fields.iterator();
    }
}
