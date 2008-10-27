/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package coverage;

import static omero.rtypes.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import omero.api.IPojosPrx;
import omero.model.ExperimenterI;
import omero.model.IObject;
import omero.model.ImageI;
import omero.model.TextAnnotationI;

import org.testng.annotations.Test;

public class PojosTest extends IceTest {

    @Test
    public void testGetCollectionCount() throws Exception {

        IPojosPrx p = ice.getSession().getPojosService();
        p.getCollectionCount(ExperimenterI.class.getName(),
                ExperimenterI.GROUPEXPERIMENTERMAP, Arrays.asList(0L), null);
    }

    @Test(enabled = false)
    public void testFindAnnotations() throws Exception {

        ImageI i = new ImageI();
        i.setName(rstring("findAnnotationsTest"));

        TextAnnotationI a = new TextAnnotationI();
        a.setTextValue(rstring("an annotation"));
        i.linkAnnotation(a);

        i = (ImageI) ice.getSession().getUpdateService().saveAndReturnObject(i);
        a = (TextAnnotationI) i.linkedAnnotationList().get(0);

        Map<Long, List<IObject>> retVal = ice.getSession().getPojosService()
                .findAnnotations("Image", Arrays.asList(i.getId().getValue()), null,
                        null);
    }
}
