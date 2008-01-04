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
import omero.model.ImageI;
import omero.model.TextAnnotationI;

import org.testng.annotations.Test;

public class PojosTest extends IceTest {

    @Test
    public void testFindAnnotations() throws Exception {

        ImageI i = new ImageI();
        i.setName("findAnnotationsTest");

        TextAnnotationI a = new TextAnnotationI();
        a.setTextValue("an annotation");
        i.linkAnnotation(a);

        i = (ImageI) ice.getUpdateService(null).saveAndReturnObject(i);
        a = (TextAnnotationI) i.iterateAnnotationLinks().next();

        Map<Long, List<IObject>> retVal = ice.getPojosService(null)
                .findAnnotations("Image", Arrays.asList(i.id.val), null, null);
    }
}
