/*
 * Copyright (C) 2007, 2008 Knut Arild Erstad
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

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import org.boblycat.blimp.DOMNodeIterator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Metadata (ImageIO/JPEG) utilities.
 *
 * @author Knut Arild Erstad
 */
public class MetaDataUtil {
    public static final String JPEG_10_FORMAT_STRING = "javax_imageio_jpeg_image_1.0";

    private static final int EXIF_TAG = 225;
    private static final String MARKER_TAG = "MarkerTag";
    private static final String MARKER_SEQUENCE_ELEMENT = "markerSequence";
    private static final String UNKNOWN_ELEMENT = "unknown";

    public static IIOMetadataNode findMarkerSequence(Node tree) {
        if (tree == null || !(tree instanceof Element))
            return null;
        for (Node child: new DOMNodeIterator((Element) tree)) {
            if (!(child instanceof IIOMetadataNode))
                continue;
            if (child.getNodeName().equals(MARKER_SEQUENCE_ELEMENT))
                return (IIOMetadataNode) child;
        }
        return null;
    }

    public static IIOMetadataNode findExifNode(Node tree, boolean create) {
        Element markerSequence = findMarkerSequence(tree);
        if (markerSequence == null) {
            // maybe handle this case if create is true?
            return null;
        }
        String exifTag = Integer.toString(EXIF_TAG);
        for (Node child: new DOMNodeIterator(markerSequence)) {
            if (!(child instanceof IIOMetadataNode))
                continue;
            IIOMetadataNode el = (IIOMetadataNode) child;
            if (el.getNodeName().equals(UNKNOWN_ELEMENT)
                    && exifTag.equals(el.getAttribute(MARKER_TAG)))
                return el;
        }
        if (create) {
            IIOMetadataNode el = new IIOMetadataNode(UNKNOWN_ELEMENT);
            el.setAttribute(MARKER_TAG, exifTag);
            markerSequence.appendChild(el);
            return el;
        }
        return null;
    }

    public static byte[] findRawExifData(IIOMetadata metadata) {
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

    public static IIOMetadata generateExifMetaData(
            ImageWriter writer, ImageTypeSpecifier imageType,
            ExifTable table) {

        ImageWriteParam iwp = writer.getDefaultWriteParam();
        IIOMetadata metadata = writer.getDefaultImageMetadata(imageType, iwp);
        if (metadata == null)
            return null;

        Element root = (Element) metadata.getAsTree(JPEG_10_FORMAT_STRING);
        IIOMetadataNode exifNode = findExifNode(root, true);
        if (exifNode == null)
            return null;

        exifNode.setUserObject(BlobCreator.dataFromExifTable(table));
        try {
            metadata.setFromTree(JPEG_10_FORMAT_STRING, root);
        } catch (IIOInvalidTreeException e) {
            return null;
        }

        return metadata;
    }
}
