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
 * TODO: type info should probably be added.
 * 
 * {@link http://www.exif.org/specifications.html}
 * 
 * @author Knut Arild Erstad
 */
public enum ExifTag {
    // 4.6.3 Exif-specific IFD
    Exif_IFD_Pointer (34665),
    GPS_IFD_Pointer (34853),
    Interoperability_IFD_Pointer (40965),
    
    // 4.6.4 TIFF Rev. 6.0 Attribute Information
    // A. Tags relating to image data structure
    ImageWidth (256),
    ImageLength (257),
    BitsPerSample (258),
    Compression (259),
    PhotometricInterpretation (262),
    Orientation (274),
    SamplesPerPixel (277),
    PlanarConfiguation (284),
    YCbCrSubSampling (530),
    YCbCrPositioning (531),
    XResolution (282),
    YResolution (283),
    ResolutionUnit (296),
    // B. Tags relating to recording offset
    StripOffsets (273),
    RowsPerStrip (278),
    StripByteCounts (279),
    JPEGInterchangeFormat (513),
    JPEGInterchangeFormatLength (514),
    // C. Tags relating to image data characteristics
    TransferFunction (301),
    WhitePoint (318),
    PrimaryChromaticities (319),
    YCbCrCoefficients (529),
    ReferenceBlackWhite (532),
    // D. Other tags
    DateTime (306),
    ImageDescription (270),
    Make (271),
    Model (272),
    Software (305),
    Artist (315),
    Copyright (33432),
    
    // 4.6.5 Exif IFD Attribute Information
    // A. Tags relating to version
    ExifVersion (36864),
    FlashpixVersion (40960),
    // B. Tag relating to color space
    ColorSpace (40961),
    // C. Tags relating to image configuration
    PixelXDimension (40962),
    PixelYDimension (40963),
    ComponentsConfiguration (37121),
    CompressedBitsPerPixel (37122),
    // D. Tags related to user information
    MakerNote (37500),
    UserComment (37510),
    // E. Tag relating to related file (sic)
    RelatedSoundFile (40964),
    // F. Tags related to date and time
    DateTimeOriginal (36867),
    DateTimeDigitized (36868),
    SubsecTime (37520),
    SubsecTimeOriginal (37521),
    SubsecTimeDigitized (37522),
    // G. Tags relating to picture-taking conditions
    ExposureTime (33434),
    FNumber (33437),
    ExposureProgram (34850),
    SpectralSensitivity (34852),
    ISOSpeedRatings (34855),
    OECF (34856),
    ShutterSpeedValue (37377),
    ApertureValue (37378),
    BrightnessValue (37379),
    ExposureBiasValue (37380),
    MaxApertureValue (37381),
    SubjectDistance (37382),
    MeteringMode (37383),
    LightSource (37384),
    Flash (37385),
    SubjectArea (37396),
    FocalLength (37386),
    FlashEnergy (41483),
    SpatialFrequencyResponse (41484),
    FocalPlaneXResolution (41486),
    FocalPlaneYResolution (41487),
    FocalPlaneResolutionUnit (41488),
    SubjectLocation (41492),
    ExposureIndex (41493),
    SensingMethod (41495),
    FileSource (41728),
    SceneType (41729),
    CFAPattern (41730),
    CustomRendered (41985),
    ExposureMode (41986),
    WhiteBalance (41987),
    DigitalZoomRatio (41988),
    FocalLengthIn35mmFilm (41989),
    SceneCaptureType (41990),
    GainControl (41991),
    Contrast (41992),
    Saturation (41993),
    Sharpness (41994),
    DeviceSettingDescription (41995),
    SubjectDistanceRange (41996),
    // H. Other tags
    ImageUniqueID (42016),
    
    // 4.6.6 GPS Attribute Information: not yet
    ;
    
    private int tag;
    
    private ExifTag(int tag) {
        this.tag = tag;
    }
    
    public int getTag() {
        return tag;
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
