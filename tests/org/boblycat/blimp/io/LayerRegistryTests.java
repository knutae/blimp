/*
 * Copyright (C) 2007, 2008, 2009, 2010 Knut Arild Erstad
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
package org.boblycat.blimp.io;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.boblycat.blimp.gui.helper.LayerRegistry;
import org.boblycat.blimp.layers.Layer;
import org.boblycat.blimp.layers.PrintLayer;
import org.boblycat.blimp.layers.TestInput;
import org.boblycat.blimp.layers.TestLayer;
import org.boblycat.blimp.layers.ViewResizeLayer;
import org.junit.Test;

import static org.junit.Assert.*;

public class LayerRegistryTests {
    private static <T> List<Class<? extends T>> getClasses(String packageName,
            Class<T> baseClass) throws Exception {
        ArrayList<Class<? extends T>> classes = new ArrayList<Class<? extends T>>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = loader.getResources(path);
        assertTrue("at least one package resource (" + path + ") should exist", resources.hasMoreElements());
        while (resources.hasMoreElements()) {
        	URL resource = resources.nextElement();
            assertNotNull("package resource (" + path + ") should not be null", resource);
            File packageDir = new File(resource.toURI());
            assertTrue(packageDir.exists());
            for (String fname: packageDir.list()) {
                if (!fname.endsWith(".class"))
                    continue;
                File file = new File(fname);
                String shortClassName = file.getName().substring(0, file.getName().length() - 6);
                String fullClassName = packageName + '.' + shortClassName;
                //System.out.println("Detected class: " + fullClassName);
                Class<?> klass = Class.forName(fullClassName);
                if (!baseClass.isAssignableFrom(klass)) {
                    //System.out.println("Skipping (wrong type): " + fullClassName);
                    continue;
                }
                classes.add(klass.asSubclass(baseClass));
            }
        }
        return classes;
    }
    
    private static void checkLayerClass(SerializationRegistry registry, Class<? extends Layer> layerClass) {
        //System.out.println("Checking " + layerClass.getName());
        String name = registry.getTypeId(layerClass);
        if (Modifier.isAbstract(layerClass.getModifiers())) {
            assertNull("Abstract class " + layerClass.getSimpleName() +
                    " should not have a serializable type ID (" + name + ")",
                    name);
        }
        else {
            assertNotNull("Non-abstract class " + layerClass.getSimpleName() +
                    " should have a serializable type ID",
                    name);
        }
    }
    
    @Test
    public void testSerializationOfClassesInLayersPackage() throws Exception {
        List<Class<? extends Layer>> layerClasses = getClasses("org.boblycat.blimp.layers", Layer.class);
        assertTrue(layerClasses.size() > 10); // did we manage to load any classes?
        SerializationRegistry registry = SerializationRegistry.createDefaultRegistry();
        for (Class<? extends Layer> layerClass: layerClasses) {
            if (layerClass == PrintLayer.class || layerClass == ViewResizeLayer.class ||
                    layerClass == TestLayer.class || layerClass == TestInput.class) {
                // Skip these classes since they are not serialized
                continue;
            }
            checkLayerClass(registry, layerClass);
        }
    }
    
    @Test
    public void testSerializationOfLayerRegistryClasses() {
        LayerRegistry lRegistry = LayerRegistry.createDefaultRegistry();
        SerializationRegistry sRegistry = SerializationRegistry.createDefaultRegistry();
        for (LayerRegistry.Category cat: lRegistry) {
            for (LayerRegistry.LayerInfo info: cat) {
                String name = sRegistry.getTypeId(info.layerClass);
                assertNotNull("Registered class " + info.layerClass.getSimpleName() +
                        " should have a serializable type ID",
                        name);
            }
        }
    }
}
