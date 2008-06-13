/*
 * Copyright (C) 2007 Knut Arild Erstad
 *
 * This file is part of Blimp, a layered photo editor.
 *
 * Blimp is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Blimp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
        if (type == ExifDataType.ASCII || type == ExifDataType.UNDEFINED)
            stringValue = "";
        else
            values = new Vector<Object>();
    }

    public ExifField(int tag, String asciiValue) {
        this.tag = tag;
        this.type = ExifDataType.ASCII;
        this.stringValue = asciiValue;
    }

    public ExifField(ExifTag tag) {
        this(tag.getTag(), tag.getDefaultType());
    }

    public ExifField(ExifTag tag, Object value) throws ValidationError {
        this(tag);
        if (values != null)
            values.add(value);
        else if (value instanceof String)
            stringValue = (String) value;
        else
            throw new ValidationError("Internal type error, expected a string");
        validate();
    }

    private void validateValueTypes(Class<?> expectedType) throws ValidationError {
        for (Object value: values) {
            if (!expectedType.isInstance(value))
                throw new ValidationError("Internal type error, expected "
                        + expectedType.getCanonicalName() + " but got "
                        + value.getClass().getCanonicalName());
        }
    }

    private void validate() throws ValidationError {
        ExifTag exifTag = ExifTag.fromTag(tag);
        if (exifTag == null)
            throw new ValidationError("Unknown tag " + tag);
        if (!exifTag.supportsType(type))
            throw new ValidationError(exifTag.name() +
                    " does not support the type " + type.name());
        switch (type) {
        case ASCII:
        case UNDEFINED:
            if (values != null && stringValue == null)
                throw new ValidationError("Internal type error, expected a string value");
            break;
        case BYTE:
            validateValueTypes(Byte.class);
            break;
        case LONG:
        case SLONG:
        case SHORT:
            validateValueTypes(Integer.class);
            break;
        case RATIONAL:
        case SRATIONAL:
            validateValueTypes(Rational.class);
            break;
        }
        if (!exifTag.supportsCount(getCount()))
            throw new ValidationError(exifTag.name() +
                    " does not support a count of " + getCount());

    }

    public int getTag() {
        return tag;
    }

    public ExifDataType getType() {
        return type;
    }

    public int getCount() {
        switch (type) {
        case ASCII:
            assert(stringValue != null);
            return stringValue.length() + 1;

        case UNDEFINED:
            if (stringValue == null)
                return 0;
            else
                return stringValue.length();

        default:
            assert(values != null);
            return values.size();
        }
    }

    public void setStringValue(String value) {
        assert(type == ExifDataType.ASCII || type == ExifDataType.UNDEFINED);
        stringValue = value;
    }

    public String getStringValue() {
        assert(type == ExifDataType.ASCII || type == ExifDataType.UNDEFINED);
        return stringValue;
    }

    public void addValue(Object value) {
        assert(type != ExifDataType.ASCII && type != ExifDataType.UNDEFINED);
        assert(values != null);
        values.add(value);
    }

    public Object valueAt(int index) {
        if (index < 0 || index >= getCount())
            return null;
        if (stringValue != null) {
            if (index == stringValue.length())
                return '\0';
            return stringValue.charAt(index);
        }
        return values.get(index);
    }

    public String toString() {
        if (stringValue != null)
            return stringValue;
        StringBuilder str = new StringBuilder();
        str.append("[ ");
        for (Object val: values) {
            str.append(val.toString());
            str.append(' ');
        }
        str.append(']');
        return str.toString();
    }

    public Vector<Object> getValues() {
        return values;
    }

    public int getByteCount() {
        return getCount() * type.getByteCount();
    }
}
