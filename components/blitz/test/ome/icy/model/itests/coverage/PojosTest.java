/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.itests.coverage;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ome.icy.model.itests.IceTest;
import omero.model.IObject;
import omero.model.ImageAnnotationI;
import omero.model.ImageI;

import org.testng.annotations.Test;

public class PojosTest extends IceTest {

    @Test
    public void testFindAnnotations() throws Exception {

        ImageI i = new ImageI();
        i.setName("findAnnotationsTest");

        ImageAnnotationI a = new ImageAnnotationI();
        a.setContent("an annotation");
        a.setImage(i);

        a = (ImageAnnotationI) ice.getUpdateService(null)
                .saveAndReturnObject(a);

        Map<Long, List<IObject>> retVal = ice.getPojosService(null)
                .findAnnotations("Image", Arrays.asList(a.getImage().id.val),
                        null, null);
    }
}
