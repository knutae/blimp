/*
 * Copyright (C) 2007, 2008, 2009 Knut Arild Erstad
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
package org.boblycat.blimp;

import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Helper class for iterating over DOM nodes.
 */
public class DOMNodeIterator implements Iterable<Node> {
    private Node currentChild;
    private boolean skipWhiteSpaceNodes;

    /**
     * Utility function to determine if a node consists of white space only.
     *
     * @param node
     *            The node to check.
     * @return True if the node is a text node which is either empty or
     *         consisting solely of whitespace characters.
     */
    public static boolean isWhiteSpaceNode(Node node) {
        if (!(node instanceof Text))
            return false;
        return (node.getNodeValue().trim().length() == 0);
    }

    public DOMNodeIterator(Element parent, boolean skipWhiteSpaceNodes) {
        this.skipWhiteSpaceNodes = skipWhiteSpaceNodes;
        currentChild = parent.getFirstChild();
        currentChildChanged();
    }

    public DOMNodeIterator(Element parent) {
        this(parent, false);
    }

    void currentChildChanged() {
        if (!skipWhiteSpaceNodes)
            return;
        while ((currentChild != null) && isWhiteSpaceNode(currentChild))
            currentChild = currentChild.getNextSibling();
    }

    public Iterator<Node> iterator() {
        return new Iterator<Node>() {
            public boolean hasNext() {
                return (currentChild != null);
            }

            public Node next() {
                Node child = currentChild;
                currentChild = currentChild.getNextSibling();
                currentChildChanged();
                return child;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
