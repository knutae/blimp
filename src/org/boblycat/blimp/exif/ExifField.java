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
        if (useStringValue())
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
            addValue(value);
        else if (value instanceof String)
            stringValue = (String) value;
        else
            throw new ValidationError("Internal type error, expected a string");
        validate();
    }

    private static Object coerceValue(Class<?> expectedType, Object value)
    throws ValidationError {
        if (expectedType.isInstance(value))
            return value;
        if (expectedType == Rational.class) {
            if (value instanceof Integer)
                return new Rational((Integer) value, 1);
        }
        throw new ValidationError("Failed to coerce "
                + value.getClass().getSimpleName()
                + " to " + expectedType.getSimpleName());
    }

    private boolean useStringValue() {
        switch (type) {
        case ASCII:
        case UNDEFINED:
        case BYTE:
        case SBYTE:
            return true;
        default:
            return false;
        }
    }

    private Class<?> valueClass() {
        switch (type) {
        case ASCII:
        case UNDEFINED:
        case BYTE:
        case SBYTE:
            // not really in use
            return String.class;
        case SHORT:
        case SSHORT:
        case LONG:
        case SLONG:
            return Integer.class;
        case RATIONAL:
        case SRATIONAL:
            return Rational.class;
        }
        return null;
    }

    private void validateValueTypes(Class<?> expectedType) throws ValidationError {
        for (Object value: values) {
            if (!expectedType.isInstance(value)) {
                throw new ValidationError("Internal type error, expected "
                        + expectedType.getCanonicalName() + " but got "
                        + value.getClass().getCanonicalName());
            }
        }
    }

    private void validate() throws ValidationError {
        ExifTag exifTag = ExifTag.fromTag(tag);
        if (exifTag == null)
            throw new ValidationError("Unknown tag " + tag);
        if (!exifTag.supportsType(type))
            throw new ValidationError(exifTag.name() +
                    " does not support the type " + type.name());
        if (useStringValue()) {
            if (values != null || stringValue == null)
                throw new ValidationError("Internal type error, expected a string value");
        }
        else {
            validateValueTypes(valueClass());
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
        case BYTE:
        case SBYTE:
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
        assert(useStringValue());
        stringValue = value;
    }

    public String getStringValue() {
        assert(useStringValue());
        return stringValue;
    }

    public void addValue(Object value) {
        assert(!useStringValue());
        assert(values != null);
        try {
            value = coerceValue(valueClass(), value);
        }
        catch (ValidationError e) {
            // ignore the error here (maybe change this?)
        }
        values.add(value);
    }

    public Object valueAt(int index) {
        if (index < 0 || index >= getCount())
            return null;
        switch (type) {
        case ASCII:
            if (index == stringValue.length())
                return '\0';
            // note, no break here by purpose!
        case UNDEFINED:
            return stringValue.charAt(index);
        case BYTE:
            return (int) stringValue.charAt(index);
        case SBYTE:
            // this one is neat
            return (int) (byte) stringValue.charAt(index);
        default:
            return values.get(index);
        }
    }

    private static String escape(String str) {
        StringBuilder buf = new StringBuilder();
        for (int i=0; i<str.length(); ++i) {
            char c = str.charAt(i);
            if (c == '\\')
                buf.append("\\\\");
            else if (Character.isISOControl(c) || (c >= 128))
                buf.append(String.format("[0x%02x]", (int) c));
            else
                buf.append(c);
        }
        return buf.toString();
    }

    private static String printable(String str) {
        boolean isPrintable = true;
        for (int i=0; i<str.length(); ++i) {
            char c = str.charAt(i);
            if (Character.isISOControl(c) || c >= 128) {
                isPrintable = false;
                break;
            }
        }
        if (isPrintable)
            return str;
        else {
            if (str.length() <= 1024)
                return escape(str);
            else
                return "<binary data, " + str.length() + " bytes>";
        }
    }

    public String toString() {
        if (stringValue != null) {
            return printable(stringValue);
        }
        StringBuilder str = new StringBuilder();
        for (Object val: values) {
            str.append(val.toString());
            str.append(' ');
        }
        return str.toString();
    }

    public Vector<Object> getValues() {
        return values;
    }

    public int getByteCount() {
        return getCount() * type.getByteCount();
    }
}
