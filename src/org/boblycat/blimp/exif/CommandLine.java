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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class CommandLine {
    private static void fatal(String message) {
        System.err.println(message);
        System.exit(1);
    }

    private static void indentLine(int level) {
        for (int i=0; i<level; ++i)
            System.out.print("    ");
    }

    private static IIOMetadataNode findUnknownNodeType(Node tree, int tag,
            int level, boolean verbose) {
        if (tree == null || !(tree instanceof Element))
            return null;
        Element element = (Element) tree;
        if (verbose) {
            indentLine(level);
            System.out.println("Element: " + element.getNodeName());
            NamedNodeMap attributes = element.getAttributes();
            for (int i=0; i<attributes.getLength(); ++i) {
                indentLine(level + 1);
                Node attr = attributes.item(i);
                System.out.println("[" + attr.getNodeName() + ": " + attr.getNodeValue() + "]");
            }
        }

        IIOMetadataNode returnValue = null;
        if (element.getNodeName().equals("unknown")
                && element.getAttribute("MarkerTag").equals(Integer.toString(tag)))
            return (IIOMetadataNode) element;
        for (Node child: new DOMNodeIterator(element)) {
            IIOMetadataNode result = findUnknownNodeType(child, tag, level+1, verbose);
            if (result != null) {
                //return result;
                if (returnValue != null)
                    System.out.println("Warning: more that one result found");
                returnValue = result;
            }
        }
        return returnValue;
    }

    private static IIOMetadataNode findExifNode(Node tree) {
        return findUnknownNodeType(tree, 225, 0, true);
    }

    static void printMetaData(IIOMetadata metadata) {
        System.out.println(metadata.toString());
        System.out.println(metadata.getClass().getName());
        String[] formats = metadata.getMetadataFormatNames();
        for (String format: formats) {
            System.out.println("*** Format: " + format);
            IIOMetadataNode tree = null;
            try {
                 tree = (IIOMetadataNode) metadata.getAsTree(format);
            }
            catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                continue;
            }
            //System.out.println(Serializer.domToXml(tree));
            IIOMetadataNode exifNode = findExifNode(tree);
            if (exifNode != null) {
                System.out.println("Found Exif node in format " + format);
                byte[] rawExifData = (byte[]) exifNode.getUserObject();
                if (rawExifData == null || rawExifData.length == 0) {
                    System.out.println("raw data is null");
                }
                else {
                    System.out.println("Bytes: " + rawExifData.length);
                    //System.out.println(new String(rawExifData));
                    printIFDs(rawExifData);
                }
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
                    int tag = field.getTag();
                    ExifTag exifTag = ExifTag.fromTag(tag);
                    if (exifTag == null)
                        System.out.println("+++ Unknown tag " + tag);
                    else
                        System.out.println("+++ " + exifTag.toString());
                    System.out.println("  tag      : " + Integer.toString(field.getTag())
                            + " (0x" + Integer.toHexString(field.getTag()) + ")");
                    System.out.println("  type     : " + field.getType());
                    if (field.getCount() != 1)
                        System.out.println("  count    : " + field.getCount());
                    System.out.println("  value(s) : " + field.toString());
                }
            }
        }
        catch (ReaderError e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            fatal("missing file name");

        String filename = args[0];
        System.out.println("Reading file " + filename);
        String format = Util.getFileExtension(filename);
        ImageReader reader = BitmapUtil.getImageReader(format);
        if (reader == null)
            fatal("unknown format " + format);
        ImageInputStream input = new FileImageInputStream(new File(filename));
        reader.setInput(input);
        System.out.println("Finding metadata...");
        IIOMetadata metadata = reader.getImageMetadata(0);
        printMetaData(metadata);
        System.out.println("Done.");
    }
}
