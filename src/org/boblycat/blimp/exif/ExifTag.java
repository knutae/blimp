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
    ImageWidth (256, Category.TIFF, EnumSet.of(SHORT, LONG), 1),
    ImageLength (257, Category.TIFF, EnumSet.of(SHORT, LONG), 1),
    BitsPerSample (258, Category.TIFF, SHORT, 3),
    Compression (259, Category.TIFF, SHORT, 1),
    PhotometricInterpretation (262, Category.TIFF, SHORT, 1),
    Orientation (274, Category.TIFF, SHORT, 1),
    SamplesPerPixel (277, Category.TIFF, SHORT, 1),
    PlanarConfiguation (284, Category.TIFF, SHORT, 1),
    YCbCrSubSampling (530, Category.TIFF, SHORT, 2),
    YCbCrPositioning (531, Category.TIFF, SHORT, 1),
    XResolution (282, Category.TIFF, RATIONAL, 1),
    YResolution (283, Category.TIFF, RATIONAL, 1),
    ResolutionUnit (296, Category.TIFF, SHORT, 1),
    // B. Tags relating to recording offset
    StripOffsets (273, Category.TIFF, EnumSet.of(SHORT, LONG), -1),
    RowsPerStrip (278, Category.TIFF, EnumSet.of(SHORT, LONG), 1),
    StripByteCounts (279, Category.TIFF, EnumSet.of(SHORT, LONG), -1),
    JPEGInterchangeFormat (513, Category.TIFF, LONG, -1),
    JPEGInterchangeFormatLength (514, Category.TIFF, LONG, -1),
    // C. Tags relating to image data characteristics
    TransferFunction (301, Category.TIFF, SHORT, 3 * 256),
    WhitePoint (318, Category.TIFF, RATIONAL, 2),
    PrimaryChromaticities (319, Category.TIFF, RATIONAL, 6),
    YCbCrCoefficients (529, Category.TIFF, RATIONAL, 3),
    ReferenceBlackWhite (532, Category.TIFF, RATIONAL, 6),
    // D. Other tags
    DateTime (306, Category.TIFF, ASCII, 20),
    ImageDescription (270, Category.TIFF, ASCII, -1),
    Make (271, Category.TIFF, ASCII, -1),
    Model (272, Category.TIFF, ASCII, -1),
    Software (305, Category.TIFF, ASCII, -1),
    Artist (315, Category.TIFF, ASCII, -1),
    Copyright (33432, Category.TIFF, ASCII, -1),

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

    // BaseLine TIFF 6.0 tags NOT used by Exif
    NewSubfileType (254, Category.TIFF, LONG, 1),
    SubfileType (255, Category.TIFF, SHORT, 1),
    Threshholding (263, Category.TIFF, SHORT, 1),
    CellWidth (264, Category.TIFF, SHORT, 1),
    CellLength (265, Category.TIFF, SHORT, 1),
    FillOrder (266, Category.TIFF, SHORT, 1),
    MinSampleValue (280, Category.TIFF, SHORT, -1),
    MaxSampleValue (281, Category.TIFF, SHORT, -1),
    FreeOffsets (288, Category.TIFF, LONG, -1),
    FreeByteCounts (289, Category.TIFF, LONG, -1),
    GrayResponseUnit (290, Category.TIFF, SHORT, 1),
    GrayResponseCurve (291, Category.TIFF, SHORT, -1),
    HostComputer (316, Category.TIFF, ASCII, -1),
    ColorMap (320, Category.TIFF, SHORT, -1),
    ExtraSamples (338, Category.TIFF, SHORT, -1),

    // Extension TIFF tags
    TileWidth (322, Category.TIFF, EnumSet.of(SHORT, LONG), 1),
    TileLength (323, Category.TIFF, EnumSet.of(SHORT, LONG), 1),
    TileOffsets (324, Category.TIFF, LONG, -1),
    TileByteCount (325, Category.TIFF, EnumSet.of(SHORT, LONG), -1),
    XMP (700, Category.TIFF, BYTE, -1),

    // TIFF/EP tags (see http://en.wikipedia.org/wiki/TIFF/EP)
    SubIFDs (330, Category.TIFF, LONG, -1),
    JPEGTables (347, Category.TIFF, UNDEFINED, -1),
    CFARepeatPatternDim (33421, Category.TIFF, SHORT, 2),
    BatteryLevel (33423, Category.TIFF, EnumSet.of(RATIONAL, ASCII), -1),
    IPTC_NAA (33723, Category.TIFF, EnumSet.of(LONG, ASCII), -1),
    InterColorProfile (34675, Category.TIFF, UNDEFINED, -1),
    Interlace (34857, Category.TIFF, SHORT, 1),
    TimeZoneOffset (34858, Category.TIFF, SSHORT, -1), // 1 or 2
    SelfTimerMode (34859, Category.TIFF, SHORT, 1),
    Noise (37389, Category.TIFF, UNDEFINED, -1),
    ImageNumber (37393, Category.TIFF, LONG, 1),
    SecurityClassification (37394, Category.TIFF, ASCII, -1),
    ImageHistory (37395, Category.TIFF, ASCII, -1),
    TIFF_EPStandardID (37398, Category.TIFF, BYTE, 4),
    // TIFF/EP tags which are also defined in Exif (what, me confused?)
    EP_CFAPattern (33422, Category.TIFF, BYTE, -1),
    EP_FlashEnergy (37387, Category.TIFF, RATIONAL, -1), // 1 or 2
    EP_SpatialFrequencyResponse (37388, Category.TIFF, RATIONAL, -1), // 1 or 2
    EP_FocalPlaneXResolution (37390, Category.TIFF, RATIONAL, 1),
    EP_FocalPlaneYResolution (37391, Category.TIFF, RATIONAL, 1),
    EP_SubjectLocation (37396, Category.TIFF, SHORT, -1), // 2, 3 or 4
    EP_ExposureIndex (37397, Category.TIFF, RATIONAL, -1), // 1 or 2
    EP_SensingMethod (37399, Category.TIFF, SHORT, 1),

    // DNG 1.2.0 format tags (Adobe's Digital Negative format)
    DNGVersion (50706, Category.DNG, BYTE, 4),
    DNGBackwardVersion (50707, Category.DNG, BYTE, 4),
    UniqueCameraModel (50708, Category.DNG, ASCII, -1),
    LocalizedCameraModel (50709, Category.DNG, EnumSet.of(ASCII, BYTE), -1),
    CFAPlaneColor (50710, Category.DNG, BYTE, -1),
    CFALayout (50711, Category.DNG, SHORT, 1),
    LinearizationTable (50712, Category.DNG, SHORT, -1),
    BlackLevelRepeatDim (50713, Category.DNG, SHORT, 2),
    BlackLevel (50714, Category.DNG, EnumSet.of(SHORT, LONG, RATIONAL), -1),
    BlackLevelDeltaH (50715, Category.DNG, SRATIONAL, -1),
    BlackLevelDeltaV (50716, Category.DNG, SRATIONAL, -1),
    WhiteLevel (50717, Category.DNG, EnumSet.of(SHORT, LONG), -1),
    DefaultScale (50718, Category.DNG, RATIONAL, 2),
    BestQualityScale (50780, Category.DNG, RATIONAL, 1),
    DefaultCropOrigin (50719, Category.DNG, EnumSet.of(SHORT, LONG, RATIONAL), 2),
    DefaultCropSize (50720, Category.DNG, EnumSet.of(SHORT, LONG, RATIONAL), 2),
    CalibrationIlluminant1 (50778, Category.DNG, SHORT, 1),
    CalibrationIlluminant2 (50779, Category.DNG, SHORT, 1),
    ColorMatrix1 (50721, Category.DNG, SRATIONAL, -1),
    ColorMatrix2 (50722, Category.DNG, SRATIONAL, -1),
    CameraCalibration1 (50723, Category.DNG, SRATIONAL, -1),
    CameraCalibration2 (50724, Category.DNG, SRATIONAL, -1),
    ReductionMatrix1 (50725, Category.DNG, SRATIONAL, -1),
    ReductionMatrix2 (50726, Category.DNG, SRATIONAL, -1),
    AnalogBalance (50727, Category.DNG, RATIONAL, -1),
    AsShotNeutral (50728, Category.DNG, EnumSet.of(SHORT, RATIONAL), -1),
    AsShotWhiteXY (50729, Category.DNG, RATIONAL, 2),
    BaselineExposure (50730, Category.DNG, SRATIONAL, 1),
    BaselineNoise (50731, Category.DNG, RATIONAL, 1),
    BaselineSharpness (50732, Category.DNG, RATIONAL, 1),
    BayerGreenSplit (50733, Category.DNG, LONG, 1),
    LinearResponseLimit (50734, Category.DNG, RATIONAL, 1),
    CameraSerialNumber (50735, Category.DNG, ASCII, -1),
    LensInfo (50736, Category.DNG, RATIONAL, 4),
    ChromaBlurRadius (50737, Category.DNG, RATIONAL, 1),
    AntiAliasStrength (50738, Category.DNG, RATIONAL, 1),
    ShadowScale (50739, Category.DNG, RATIONAL, 1),
    DNGPrivateData (50740, Category.DNG, BYTE, -1),
    MarkerNoteSafety (50741, Category.DNG, SHORT, 1),
    RawDataUniqueID (50781, Category.DNG, BYTE, 16),
    OriginalRawFileName (50827, Category.DNG, EnumSet.of(ASCII, BYTE), -1),
    OriginalRawFileData (50828, Category.DNG, UNDEFINED, -1),
    ActiveArea (50829, Category.DNG, EnumSet.of(SHORT, LONG), 4),
    MaskedAreas (50830, Category.DNG, EnumSet.of(SHORT, LONG), -1),
    AsShotICCProfile (50831, Category.DNG, UNDEFINED, -1),
    AsShotPreProfileMatrix (50832, Category.DNG, SRATIONAL, -1),
    CurrentICCProfile (50833, Category.DNG, UNDEFINED, -1),
    CurrentPreProfileMatrix (50834, Category.DNG, SRATIONAL, -1),
    // Tags new in DNG 1.2.0.0
    ColorimetricReference (50879, Category.DNG, SHORT, 1),
    CameraCalibrationSignature (50931, Category.DNG, EnumSet.of(ASCII, BYTE), -1),
    ProfileCalibrationSignature (50932, Category.DNG, EnumSet.of(ASCII, BYTE), -1),
    ExtraCameraProfiles (50933, Category.DNG, LONG, -1),
    AsShotProfileName (50934, Category.DNG, EnumSet.of(ASCII, BYTE), -1),
    NoiseReductionApplied (50935, Category.DNG, RATIONAL, 1),
    ProfileName (50936, Category.DNG, EnumSet.of(ASCII, BYTE), -1),
    ProfileHueSatMapDims (50937, Category.DNG, LONG, 3),
    // floats not supported yet
    //ProfileHueSatMapData1 (50938, Category.DNG, FLOAT, -1),
    //ProfileHueSatMapData2 (50939, Category.DNG, FLOAT, -1),
    //ProfileToneCurve (50940, Category.DNG, FLOAT, -1),
    ProfileEmbedPolicy (50941, Category.DNG, LONG, 1),
    ProfileCopyright (50942, Category.DNG, EnumSet.of(ASCII, BYTE), -1),
    ForwardMatrix1 (50964, Category.DNG, SRATIONAL, -1),
    ForwardMatrix2 (50965, Category.DNG, SRATIONAL, -1),
    PreviewApplicationName (50966, Category.DNG, EnumSet.of(ASCII, BYTE), -1),
    PreviewApplicationVersion (50967, Category.DNG, EnumSet.of(ASCII, BYTE), -1),
    PreviewSettingName (50968, Category.DNG, EnumSet.of(ASCII, BYTE), -1),
    PreviewSettingDigest (50969, Category.DNG, BYTE, 16),
    PreviewColorSpace (50970, Category.DNG, LONG, 1),
    PreviewDateTime (50971, Category.DNG, ASCII, -1),
    RawImageDigest (50972, Category.DNG, BYTE, 16),
    OriginalRawImageDigest (50973, Category.DNG, BYTE, 16),
    SubTileBlockSize (50974, Category.DNG, EnumSet.of(SHORT, LONG), 2),
    RowInterleaveFactor (50975, Category.DNG, EnumSet.of(SHORT, LONG), 1),
    ProfileLookTableDims (50981, Category.DNG, LONG, 3),
    //ProfileLookTableData (50982, Category.DNG, FLOAT, -1),
    ;

    /**
     * Tags are categorized according to the TIFF and Exif specs.
     * The category decides where in the binary data each tag is
     * stored.
     */
    public enum Category {
        Pointer,
        TIFF,
        Exif,
        DNG,
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
