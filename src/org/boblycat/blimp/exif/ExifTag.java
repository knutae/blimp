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

import java.util.EnumSet;
import java.util.HashMap;
import static org.boblycat.blimp.exif.ExifDataType.*;

/**
 * An enumeration of Exif 2.2 tags.
 * This class provides a list of tags with limited type information and a
 * lookup table based upon tag ID, but no interpretation of what the tags
 * mean.
 *
 * See the <a href="http://www.exif.org/specifications.html">
 * Exif specifications</a> for the meaning of the different tags.
 *
 * @author Knut Arild Erstad
 */
public enum ExifTag {
    // 4.6.3 Exif-specific IFD
    Exif_IFD_Pointer (34665, Category.Pointer, LONG, 1),
    GPS_IFD_Pointer (34853, Category.Pointer, LONG, 1),
    Interoperability_IFD_Pointer (40965, Category.Pointer, LONG, 1),

    // 4.6.4 TIFF Rev. 6.0 Attribute Information
    // A. Tags relating to image data structure
    ImageWidth (256, Category.Tiff, EnumSet.of(SHORT, LONG), 1),
    ImageLength (257, Category.Tiff, EnumSet.of(SHORT, LONG), 1),
    BitsPerSample (258, Category.Tiff, SHORT, 3),
    Compression (259, Category.Tiff, SHORT, 1),
    PhotometricInterpretation (262, Category.Tiff, SHORT, 1),
    Orientation (274, Category.Tiff, SHORT, 1),
    SamplesPerPixel (277, Category.Tiff, SHORT, 1),
    PlanarConfiguation (284, Category.Tiff, SHORT, 1),
    YCbCrSubSampling (530, Category.Tiff, SHORT, 2),
    YCbCrPositioning (531, Category.Tiff, SHORT, 1),
    XResolution (282, Category.Tiff, RATIONAL, 1),
    YResolution (283, Category.Tiff, RATIONAL, 1),
    ResolutionUnit (296, Category.Tiff, SHORT, 1),
    // B. Tags relating to recording offset
    StripOffsets (273, Category.Tiff, EnumSet.of(SHORT, LONG), -1),
    RowsPerStrip (278, Category.Tiff, EnumSet.of(SHORT, LONG), 1),
    StripByteCounts (279, Category.Tiff, EnumSet.of(SHORT, LONG), -1),
    JPEGInterchangeFormat (513, Category.Tiff, LONG, -1),
    JPEGInterchangeFormatLength (514, Category.Tiff, LONG, -1),
    // C. Tags relating to image data characteristics
    TransferFunction (301, Category.Tiff, SHORT, 3 * 256),
    WhitePoint (318, Category.Tiff, RATIONAL, 2),
    PrimaryChromaticities (319, Category.Tiff, RATIONAL, 6),
    YCbCrCoefficients (529, Category.Tiff, RATIONAL, 3),
    ReferenceBlackWhite (532, Category.Tiff, RATIONAL, 6),
    // D. Other tags
    DateTime (306, Category.Tiff, ASCII, 20),
    ImageDescription (270, Category.Tiff, ASCII, -1),
    Make (271, Category.Tiff, ASCII, -1),
    Model (272, Category.Tiff, ASCII, -1),
    Software (305, Category.Tiff, ASCII, -1),
    Artist (315, Category.Tiff, ASCII, -1),
    Copyright (33432, Category.Tiff, ASCII, -1),

    // 4.6.5 Exif IFD Attribute Information
    // A. Tags relating to version
    ExifVersion (36864, Category.Exif, UNDEFINED, 4),
    FlashpixVersion (40960, Category.Exif, UNDEFINED, 4),
    // B. Tag relating to color space
    ColorSpace (40961, Category.Exif, SHORT, 1),
    // C. Tags relating to image configuration
    PixelXDimension (40962, Category.Exif, EnumSet.of(SHORT, LONG), 1),
    PixelYDimension (40963, Category.Exif, EnumSet.of(SHORT, LONG), 1),
    ComponentsConfiguration (37121, Category.Exif, UNDEFINED, 4),
    CompressedBitsPerPixel (37122, Category.Exif, RATIONAL, 1),
    // D. Tags related to user information
    MakerNote (37500, Category.Exif, UNDEFINED, -1),
    UserComment (37510, Category.Exif, UNDEFINED, -1),
    // E. Tag relating to related file (sic)
    RelatedSoundFile (40964, Category.Exif, ASCII, 13),
    // F. Tags related to date and time
    DateTimeOriginal (36867, Category.Exif, ASCII, 20),
    DateTimeDigitized (36868, Category.Exif, ASCII, 20),
    SubsecTime (37520, Category.Exif, ASCII, -1),
    SubsecTimeOriginal (37521, Category.Exif, ASCII, -1),
    SubsecTimeDigitized (37522, Category.Exif, ASCII, -1),
    // G. Tags relating to picture-taking conditions
    ExposureTime (33434, Category.Exif, RATIONAL, 1),
    FNumber (33437, Category.Exif, RATIONAL, 1),
    ExposureProgram (34850, Category.Exif, SHORT, 1),
    SpectralSensitivity (34852, Category.Exif, ASCII, -1),
    ISOSpeedRatings (34855, Category.Exif, SHORT, -1),
    OECF (34856, Category.Exif, UNDEFINED, -1),
    ShutterSpeedValue (37377, Category.Exif, SRATIONAL, 1),
    ApertureValue (37378, Category.Exif, RATIONAL, 1),
    BrightnessValue (37379, Category.Exif, SRATIONAL, 1),
    ExposureBiasValue (37380, Category.Exif, SRATIONAL, 1),
    MaxApertureValue (37381, Category.Exif, RATIONAL, 1),
    SubjectDistance (37382, Category.Exif, RATIONAL, 1),
    MeteringMode (37383, Category.Exif, SHORT, 1),
    LightSource (37384, Category.Exif, SHORT, 1),
    Flash (37385, Category.Exif, SHORT, 1),
    SubjectArea (37396, Category.Exif, SHORT, -1), // count = 2, 3 or 4
    FocalLength (37386, Category.Exif, RATIONAL, 1),
    FlashEnergy (41483, Category.Exif, RATIONAL, 1),
    SpatialFrequencyResponse (41484, Category.Exif, UNDEFINED, -1),
    FocalPlaneXResolution (41486, Category.Exif, RATIONAL, 1),
    FocalPlaneYResolution (41487, Category.Exif, RATIONAL, 1),
    FocalPlaneResolutionUnit (41488, Category.Exif, SHORT, 1),
    SubjectLocation (41492, Category.Exif, SHORT, 2),
    ExposureIndex (41493, Category.Exif, RATIONAL, 1),
    SensingMethod (41495, Category.Exif, SHORT, 1),
    FileSource (41728, Category.Exif, UNDEFINED, 1),
    SceneType (41729, Category.Exif, UNDEFINED, 1),
    CFAPattern (41730, Category.Exif, UNDEFINED, -1),
    CustomRendered (41985, Category.Exif, SHORT, 1),
    ExposureMode (41986, Category.Exif, SHORT, 1),
    WhiteBalance (41987, Category.Exif, SHORT, 1),
    DigitalZoomRatio (41988, Category.Exif, RATIONAL, 1),
    FocalLengthIn35mmFilm (41989, Category.Exif, SHORT, 1),
    SceneCaptureType (41990, Category.Exif, SHORT, 1),
    GainControl (41991, Category.Exif, SHORT, 1),
    Contrast (41992, Category.Exif, SHORT, 1),
    Saturation (41993, Category.Exif, SHORT, 1),
    Sharpness (41994, Category.Exif, SHORT, 1),
    DeviceSettingDescription (41995, Category.Exif, UNDEFINED, -1),
    SubjectDistanceRange (41996, Category.Exif, SHORT, 1),
    // H. Other tags
    ImageUniqueID (42016, Category.Exif, ASCII, 33),

    // 4.6.6 GPS Attribute Information: not yet
    ;

    /**
     * Tags are categorized according to the TIFF and Exif specs.
     * The category decides where in the binary data each tag is
     * stored.
     */
    public enum Category {
        Pointer,
        Tiff,
        Exif,
    }

    private int tag;
    private Category category;
    private EnumSet<ExifDataType> supportedTypes;
    private int count;

    private ExifTag(int tag, Category category, ExifDataType type, int count) {
        this(tag, category, EnumSet.of(type), count);
    }

    private ExifTag(int tag, Category category, EnumSet<ExifDataType> types, int count) {
        this.tag = tag;
        this.category = category;
        this.supportedTypes = types;
        this.count = count;
    }

    public int getTag() {
        return tag;
    }

    public Category getCategory() {
        return category;
    }

    public ExifDataType getDefaultType() {
        assert(supportedTypes.size() > 0);
        if (supportedTypes.size() == 1)
            return supportedTypes.iterator().next();
        // prefer SHORT over LONG...?
        if (supportedTypes.contains(SHORT))
            return SHORT;
        return supportedTypes.iterator().next();
    }

    public boolean supportsType(ExifDataType type) {
        return supportedTypes.contains(type);
    }

    public boolean supportsCount(int testCount) {
        if (count < 0)
            return true;
        return testCount == count;
    }

    private static HashMap<Integer, ExifTag> map;

    static {
        map = new HashMap<Integer, ExifTag>();
        for (ExifTag enumElement: ExifTag.values()) {
            map.put(enumElement.tag, enumElement);
        }
    }

    public static ExifTag fromTag(int tag) {
        return map.get(tag);
    }
}
