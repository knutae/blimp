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
import java.io.IOException;

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
                    try {
                        printIFDs(new ExifBlobReader(rawExifData));
                    }
                    catch (ReaderError e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    static byte[] findRawExifData(IIOMetadata metadata) {
        // move this to MetaDataUtil
        String[] formats = metadata.getMetadataFormatNames();
        for (String format: formats) {
            IIOMetadataNode tree = null;
            try {
                 tree = (IIOMetadataNode) metadata.getAsTree(format);
            }
            catch (IllegalArgumentException e) {
                continue;
            }
            IIOMetadataNode exifNode = MetaDataUtil.findExifNode(tree, false);
            if (exifNode != null) {
                byte[] rawExifData = (byte[]) exifNode.getUserObject();
                if (rawExifData != null && rawExifData.length > 0)
                    return rawExifData;
            }
        }
        return null;
    }

    private static void printIndent(int indent, String str) {
        for (int i=0; i<indent; ++i)
            System.out.print("    ");
        System.out.println(str);
    }

    private static void printIFD(ImageFileDirectory ifd, int indent, String name) {
        printIndent(indent, "--- Begin IFD " + name + " ---");
        for (ExifField field: ifd) {
            int tag = field.getTag();
            ExifTag exifTag = ExifTag.fromTag(tag);
            if (exifTag == null)
                printIndent(indent,
                        "+ Unknown tag " + tag +
                        " (0x" + Integer.toHexString(tag) + ")");
            else {
                printIndent(indent,
                        "+ " + exifTag.toString() +
                        " [" + exifTag.getCategory() + "]");
            }
            printIndent(indent, "  type     : " + field.getType());
            if (field.getCount() != 1)
                printIndent(indent, "  count    : " + field.getCount());
            printIndent(indent, "  value(s) : " + field.toString());
        }
        int subIndex = 0;
        for (ImageFileDirectory sub: ifd.getSubIFDs()) {
            printIFD(sub, indent + 1, name + "." + subIndex);
            subIndex++;
        }
        printIndent(indent, "--- End IFD " + name + " ---");
        System.out.println();
    }

    private static void printIFDs(ExifBlobReader reader) {
        try {
            ExifTable table = reader.extractIFDTable();
            int index = 0;
            for (ImageFileDirectory ifd: table.getMainIFDs()) {
                printIFD(ifd, 0, Integer.toString(index));
                index++;
            }
            printIFD(table.getExifIFD(), 0, "Exif");
        }
        catch (ReaderError e) {
            e.printStackTrace();
        }
    }

    private static ExifBlobReader getExifReader(File filename)
    throws IOException, ReaderError {
        String format = Util.getFileExtension(filename);
        ImageReader reader = BitmapUtil.getImageReader(format);
        if (reader != null) {
            // metadata reader
            ImageInputStream input = new FileImageInputStream(filename);
            reader.setInput(input);
            //System.out.println("Finding metadata...");
            IIOMetadata metadata = reader.getImageMetadata(0);
            byte[] data = findRawExifData(metadata);
            if (data != null)
                return new ExifBlobReader(data);
        }
        // attempt to read Exif data directly from the file
        return new ExifBlobReader(filename);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            fatal("missing file name");

        ExifBlobReader reader = getExifReader(new File(args[0]));
        printIFDs(reader);
    }
}
