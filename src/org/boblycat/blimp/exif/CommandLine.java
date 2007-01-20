package org.boblycat.blimp.exif;

import java.io.File;
import java.io.UnsupportedEncodingException;

import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.boblycat.blimp.BitmapUtil;
import org.boblycat.blimp.DOMNodeIterator;
import org.boblycat.blimp.Util;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

enum ExifType {
    BYTE {
        int getByteCount() { return 1; }
    },
    ASCII {
        int getByteCount() { return 1; }
    },
    SHORT {
        int getByteCount() { return 2; }
    },
    LONG {
        int getByteCount() { return 4; }
    },
    RATIONAL {
        int getByteCount() { return 8; }
    },
    UNDEFINED {
        int getByteCount() { return 1; }
    },
    SLONG {
        int getByteCount() { return 4; }
    },
    SRATIONAL {
        int getByteCount() { return 8; }
    };
    
    abstract int getByteCount();
    
    static ExifType fromTypeTag(int type) {
        switch (type) {
        case 1:
            return BYTE;
        case 2:
            return ASCII;
        case 3:
            return SHORT;
        case 4:
            return LONG;
        case 5:
            return RATIONAL;
        case 7:
            return UNDEFINED;
        case 9:
            return SLONG;
        case 10:
            return SRATIONAL;
        }
        return null;
    }
}

public class CommandLine {
    private static void fatal(String message) {
        System.err.println(message);
        System.exit(1);
    }
    
    private static IIOMetadataNode findUnknownNodeType(Node tree, int tag) {
        if (tree == null || !(tree instanceof Element))
            return null;
        Element element = (Element) tree;
        if (element.getNodeName().equals("unknown")
                && element.getAttribute("MarkerTag").equals(Integer.toString(tag)))
            return (IIOMetadataNode) element;
        for (Node child: new DOMNodeIterator(element)) {
            IIOMetadataNode result = findUnknownNodeType(child, tag);
            if (result != null)
                return result;
        }
        return null;
    }
    
    private static IIOMetadataNode findExifNode(Node tree) {
        return findUnknownNodeType(tree, 225);
    }
    
    private static void printMetaData(IIOMetadata metadata) {
        //System.out.println(metadata.toString());
        String[] formats = metadata.getMetadataFormatNames();
        for (String format: formats) {
            //System.out.println("*** Format: " + format);
            IIOMetadataNode tree = (IIOMetadataNode) metadata.getAsTree(format);
            IIOMetadataNode exifNode = findExifNode(tree);
            if (exifNode != null) {
                System.out.println("Found Exif node in format " + format);
                byte[] rawExifData = (byte[]) exifNode.getUserObject();
                System.out.println("Bytes: " + rawExifData.length);
                //System.out.println(new String(rawExifData));
                printIFDs(rawExifData);
            }
        }
    }
    
    private static long bytesToLong(byte[] data, int offset, int byteCount,
            boolean bigEndian) {
        assert(byteCount <= 8);
        if (offset + byteCount > data.length)
            fatal("premature end of byte data");
        long result = 0;
        if (bigEndian) {
            for (int i = offset; i < offset+byteCount; i++) {
                int byteValue = data[i] & 0xff;
                //System.out.println("   byte val " + i + " : " + byteValue);
                result = (result << 8) | byteValue;
            }
        }
        else {
            for (int i = offset+byteCount-1; i >= offset; i--) {
                int byteValue = data[i] & 0xff;
                //System.out.println("   byte val " + i + " : " + byteValue);
                result = (result << 8) | byteValue;
            }
        }
        return result;
    }
    
    private static int bytesToInt(byte[] data, int offset, int byteCount,
            boolean bigEndian) {
        long result = bytesToLong(data, offset, byteCount, bigEndian);
        if (result > Integer.MAX_VALUE)
            fatal("too large integer");
        return (int) result;
    }
    
    private static String bytesToAscii(byte[] data, int offset, int length) {
        try {
            return new String(data, offset, length, "US-ASCII");
        }
        catch (UnsupportedEncodingException e) {
            fatal("ascii encoding not supported");
            return null;
        }
    }
    
    static final int BASE_EXIF_OFFSET = 6;
    
    private static String getAsciiValue(byte[] exifData, int offset, int length) {
        return bytesToAscii(exifData, BASE_EXIF_OFFSET + offset, length);
    }

    private static void printIFDs(byte[] exifData) {
        String exifId = bytesToAscii(exifData, 0, 6);
        if (!exifId.equals("Exif\0\0"))
            fatal("no 'Exif' header found");
        String byteOrderDef = bytesToAscii(exifData, 6, 2);
        boolean bigEndian = true;
        if (byteOrderDef.equals("MM"))
            bigEndian = true;
        else if (byteOrderDef.equals("II"))
            bigEndian = false;
        else
            fatal("Failed to parse byte order definition");
        System.out.println("Big endian: " + bigEndian);
        long magic = bytesToLong(exifData, 8, 2, bigEndian);
        if (magic != 42)
            fatal("42 is the answer, not " + magic);
        int firstIFDOffset = bytesToInt(exifData, 10, 4, bigEndian);
        System.out.println("Offset of first IFD: " + firstIFDOffset);
        int offset = BASE_EXIF_OFFSET + firstIFDOffset;
        int ifdCount = 0;
        while (offset < exifData.length) {
            ifdCount++;
            int fieldCount = bytesToInt(exifData, offset, 2, bigEndian);
            System.out.println("-----------------");
            System.out.println("field count is " + fieldCount);
            offset += 2;
            for (int i=0; i<fieldCount; i++) {
                // 12-byte "interopability array":
                int tag = bytesToInt(exifData, offset, 2, bigEndian);
                int type = bytesToInt(exifData, offset+2, 2, bigEndian);
                int count = bytesToInt(exifData, offset+4, 4, bigEndian);
                int valueOffset = bytesToInt(exifData, offset+8, 4, bigEndian);
                offset += 12;
                
                ExifType exifType = ExifType.fromTypeTag(type);
                if (exifType == null)
                    fatal("Unknown Exif type: " + type);
                System.out.println("field " + i);
                System.out.println("  tag   : " + tag);
                System.out.println("  type  : " + exifType.toString());
                System.out.println("  count : " + count);
                System.out.println("  value : " + valueOffset);
                if (exifType == ExifType.ASCII)
                    System.out.println("  ascii : "
                            + getAsciiValue(exifData, valueOffset, count));
            }

            // offset of next IFD:
            int nextIFDOffset = bytesToInt(exifData, offset, 4, bigEndian);
            System.out.println("next offset at " + nextIFDOffset);
            if (nextIFDOffset == 0)
                break;
            offset = BASE_EXIF_OFFSET + nextIFDOffset;
        }
        System.out.println("Total number of IFDs: " + ifdCount);
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            fatal("missing file name");
        
        String filename = args[0];
        String format = Util.getFileExtension(filename);
        ImageReader reader = BitmapUtil.getImageReader(format);
        if (reader == null)
            fatal("unknown format " + format);
        ImageInputStream input = new FileImageInputStream(new File(filename));
        reader.setInput(input);
        IIOMetadata metadata = reader.getImageMetadata(0);
        printMetaData(metadata);
    }
}
