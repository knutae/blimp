package org.boblycat.blimp.exif;

import java.io.File;
import java.util.Vector;

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
    
    private static void printIFDs(byte[] exifData) {
        try {
            ExifBlobReader reader = new ExifBlobReader(exifData);
            Vector<ImageFileDirectory> dirs = reader.extractIFDs();
            for (ImageFileDirectory ifd: dirs) {
                System.out.println("-------------------");
                for (ExifField field: ifd) {
                    System.out.println("  tag   : 0x" + Integer.toHexString(field.getTag()));
                    System.out.println("  type  : " + field.getType());
                    System.out.println("  count : " + field.getCount());
                    System.out.println("  value : " + field.getValue());
                    System.out.println("+++");
                }
            }
        }
        catch (ReaderError e) {
            e.printStackTrace();
            fatal(e.getMessage());
        }
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
