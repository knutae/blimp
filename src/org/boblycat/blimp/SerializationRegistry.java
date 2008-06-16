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
package org.boblycat.blimp;

import java.util.HashMap;
import org.boblycat.blimp.layers.*;

/**
 * A BlimpBean registry used for serialization purposes.
 *
 * @author Knut Arild Erstad
 */
public class SerializationRegistry {
    HashMap<String, Class<? extends BlimpBean>> typeIdClassMap;
    HashMap<Class<? extends BlimpBean>, String> classTypeIdMap;

    private SerializationRegistry() {
        typeIdClassMap = new HashMap<String, Class<? extends BlimpBean>>();
        classTypeIdMap = new HashMap<Class<? extends BlimpBean>, String>();
    }

    /**
     * Clear the registry.
     */
    public void clear() {
        typeIdClassMap.clear();
        classTypeIdMap.clear();
    }

    /**
     * Register a type identifier for the given bean class.
     *
     * The type identifier must be a unique string and is used in the XML/DOM
     * represantation of beans as a "type" attribute. If a bean has no type ID
     * registered, a "class" attribute specifying the class name, including the
     * package, is used instead.
     *
     * The rationale for this is to make it possible to rename layer classes
     * or move them to different packages in the future without breaking the
     * serialization.  Also, it makes the XML format easier to read and less
     * Java-specific.
     *
     * @param beanClass a bean (usually layer) class.
     * @param typeId a unique identifier for the bean class, which should
     *   preferrably be short and concise.
     */
    public void registerTypeId(Class<? extends BlimpBean> beanClass,
            String typeId) {
        assert(beanClass != null);
        assert(typeId != null);
        classTypeIdMap.put(beanClass, typeId);
        typeIdClassMap.put(typeId, beanClass);
    }

    public Class<? extends BlimpBean> getBeanClass(String typeId) {
        return typeIdClassMap.get(typeId);
    }

    public String getTypeId(Class<? extends BlimpBean> beanClass) {
        return classTypeIdMap.get(beanClass);
    }

    /**
     * Create a default registry with the built-in layers registered.
     *
     * @return a new serialization registry.
     */
    public static SerializationRegistry createDefaultRegistry() {
        // Changing layer type IDs will break backwards compatibility,
        // so be careful!
        SerializationRegistry reg = new SerializationRegistry();
        reg.registerTypeId(InvertLayer.class, "Invert");
        reg.registerTypeId(BrightnessContrastLayer.class, "BrightnessContrast");
        reg.registerTypeId(CurvesLayer.class, "Curves");
        reg.registerTypeId(SaturationLayer.class, "HueSaturationLightness");
        reg.registerTypeId(Color16BitLayer.class, "Promote16Bit");
        reg.registerTypeId(GammaLayer.class, "Gamma");
        reg.registerTypeId(GrayscaleMixerLayer.class, "GrayscaleMixer");
        reg.registerTypeId(ResizeLayer.class, "Resize");
        reg.registerTypeId(UnsharpMaskLayer.class, "UnsharpMask");
        reg.registerTypeId(LocalContrastLayer.class, "LocalContrast");
        reg.registerTypeId(CropLayer.class, "Crop");
        reg.registerTypeId(OrientationLayer.class, "Orientation");
        reg.registerTypeId(LevelsLayer.class, "Levels");
        reg.registerTypeId(SolidColorBorderLayer.class, "Border");
        reg.registerTypeId(RawFileInputLayer.class, "RawInput");
        reg.registerTypeId(SimpleFileInputLayer.class, "FileInput");
        return reg;
    }
}
