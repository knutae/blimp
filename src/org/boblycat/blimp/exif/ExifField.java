package org.boblycat.blimp.exif;

import java.util.Vector;

/**
 * A single Exif field, which can hold several elements of the same type.
 * 
 * @author Knut Arild Erstad
 */
public class ExifField {
    private int tag;
    private ExifDataType type;
    private Vector<Object> values;
    private String stringValue;
    
    public ExifField(int tag, ExifDataType type) {
        this.tag = tag;
        this.type = type;
        if (type == ExifDataType.ASCII)
            stringValue = "";
        else
            values = new Vector<Object>();
    }
    
    public ExifField(int tag, String asciiValue) {
        this.tag = tag;
        this.type = ExifDataType.ASCII;
        this.stringValue = asciiValue;
    }
    
    public int getTag() {
        return tag;
    }
    
    public ExifDataType getType() {
        return type;
    }
    
    public int getCount() {
        if (type == ExifDataType.ASCII) {
            // length + zero byte (?)
            assert(stringValue != null);
            return stringValue.length() + 1;
        }
        else {
            assert(values != null);
            return values.size();
        }
    }
    
    public void setStringValue(String value) {
        assert(type == ExifDataType.ASCII);
        stringValue = value;
    }
    
    public void addValue(Object value) {
        assert(type != ExifDataType.ASCII);
        assert(values != null);
        values.add(value);
    }
    
    public Object getValue() {
        // TODO: is this function really needed?
        if (type == ExifDataType.ASCII)
            return stringValue;
        if (values.size() > 0)
            return values.get(0);
        return null;
    }
    
    public int getByteCount() {
        return getCount() * type.getByteCount();
    }
}
