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

import java.util.HashMap;

/**
 * An enumeration of Exif 2.2 tags.
 * This class provides a list tags and a lookup table based upon tag ID,
 * but no interpretation of what the tags means.
 *
 * See the <a href="http://www.exif.org/specifications.html">
 * Exif specifications</a> for the meaning of the different tags.
 *
 * TODO: type info should probably be added.
 *
 * @author Knut Arild Erstad
 */
public enum ExifTag {
    // 4.6.3 Exif-specific IFD
    Exif_IFD_Pointer (34665, Category.Pointer),
    GPS_IFD_Pointer (34853, Category.Pointer),
    Interoperability_IFD_Pointer (40965, Category.Pointer),

    // 4.6.4 TIFF Rev. 6.0 Attribute Information
    // A. Tags relating to image data structure
    ImageWidth (256, Category.Tiff),
    ImageLength (257, Category.Tiff),
    BitsPerSample (258, Category.Tiff),
    Compression (259, Category.Tiff),
    PhotometricInterpretation (262, Category.Tiff),
    Orientation (274, Category.Tiff),
    SamplesPerPixel (277, Category.Tiff),
    PlanarConfiguation (284, Category.Tiff),
    YCbCrSubSampling (530, Category.Tiff),
    YCbCrPositioning (531, Category.Tiff),
    XResolution (282, Category.Tiff),
    YResolution (283, Category.Tiff),
    ResolutionUnit (296, Category.Tiff),
    // B. Tags relating to recording offset
    StripOffsets (273, Category.Tiff),
    RowsPerStrip (278, Category.Tiff),
    StripByteCounts (279, Category.Tiff),
    JPEGInterchangeFormat (513, Category.Tiff),
    JPEGInterchangeFormatLength (514, Category.Tiff),
    // C. Tags relating to image data characteristics
    TransferFunction (301, Category.Tiff),
    WhitePoint (318, Category.Tiff),
    PrimaryChromaticities (319, Category.Tiff),
    YCbCrCoefficients (529, Category.Tiff),
    ReferenceBlackWhite (532, Category.Tiff),
    // D. Other tags
    DateTime (306, Category.Tiff),
    ImageDescription (270, Category.Tiff),
    Make (271, Category.Tiff),
    Model (272, Category.Tiff),
    Software (305, Category.Tiff),
    Artist (315, Category.Tiff),
    Copyright (33432, Category.Tiff),

    // 4.6.5 Exif IFD Attribute Information
    // A. Tags relating to version
    ExifVersion (36864, Category.Exif),
    FlashpixVersion (40960, Category.Exif),
    // B. Tag relating to color space
    ColorSpace (40961, Category.Exif),
    // C. Tags relating to image configuration
    PixelXDimension (40962, Category.Exif),
    PixelYDimension (40963, Category.Exif),
    ComponentsConfiguration (37121, Category.Exif),
    CompressedBitsPerPixel (37122, Category.Exif),
    // D. Tags related to user information
    MakerNote (37500, Category.Exif),
    UserComment (37510, Category.Exif),
    // E. Tag relating to related file (sic)
    RelatedSoundFile (40964, Category.Exif),
    // F. Tags related to date and time
    DateTimeOriginal (36867, Category.Exif),
    DateTimeDigitized (36868, Category.Exif),
    SubsecTime (37520, Category.Exif),
    SubsecTimeOriginal (37521, Category.Exif),
    SubsecTimeDigitized (37522, Category.Exif),
    // G. Tags relating to picture-taking conditions
    ExposureTime (33434, Category.Exif),
    FNumber (33437, Category.Exif),
    ExposureProgram (34850, Category.Exif),
    SpectralSensitivity (34852, Category.Exif),
    ISOSpeedRatings (34855, Category.Exif),
    OECF (34856, Category.Exif),
    ShutterSpeedValue (37377, Category.Exif),
    ApertureValue (37378, Category.Exif),
    BrightnessValue (37379, Category.Exif),
    ExposureBiasValue (37380, Category.Exif),
    MaxApertureValue (37381, Category.Exif),
    SubjectDistance (37382, Category.Exif),
    MeteringMode (37383, Category.Exif),
    LightSource (37384, Category.Exif),
    Flash (37385, Category.Exif),
    SubjectArea (37396, Category.Exif),
    FocalLength (37386, Category.Exif),
    FlashEnergy (41483, Category.Exif),
    SpatialFrequencyResponse (41484, Category.Exif),
    FocalPlaneXResolution (41486, Category.Exif),
    FocalPlaneYResolution (41487, Category.Exif),
    FocalPlaneResolutionUnit (41488, Category.Exif),
    SubjectLocation (41492, Category.Exif),
    ExposureIndex (41493, Category.Exif),
    SensingMethod (41495, Category.Exif),
    FileSource (41728, Category.Exif),
    SceneType (41729, Category.Exif),
    CFAPattern (41730, Category.Exif),
    CustomRendered (41985, Category.Exif),
    ExposureMode (41986, Category.Exif),
    WhiteBalance (41987, Category.Exif),
    DigitalZoomRatio (41988, Category.Exif),
    FocalLengthIn35mmFilm (41989, Category.Exif),
    SceneCaptureType (41990, Category.Exif),
    GainControl (41991, Category.Exif),
    Contrast (41992, Category.Exif),
    Saturation (41993, Category.Exif),
    Sharpness (41994, Category.Exif),
    DeviceSettingDescription (41995, Category.Exif),
    SubjectDistanceRange (41996, Category.Exif),
    // H. Other tags
    ImageUniqueID (42016, Category.Exif),

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

    private ExifTag(int tag, Category category) {
        this.tag = tag;
        this.category = category;
    }

    public int getTag() {
        return tag;
    }

    public Category getCategory() {
        return category;
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
