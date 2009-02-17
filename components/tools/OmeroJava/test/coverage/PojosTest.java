/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package coverage;

import static omero.rtypes.rstring;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import omero.api.IContainerPrx;
import omero.model.CommentAnnotationI;
import omero.model.ExperimenterI;
import omero.model.IObject;
import omero.model.ImageI;

import org.testng.annotations.Test;

public class PojosTest extends IceTest {

    @Test
    public void testGetCollectionCount() throws Exception {

        IContainerPrx p = ice.getSession().getContainerService();
        p.getCollectionCount(ExperimenterI.class.getName(),
                ExperimenterI.GROUPEXPERIMENTERMAP, Arrays.asList(0L), null);
    }

    @Test(enabled = false)
    public void testFindAnnotations() throws Exception {

    	/*
        ImageI i = new ImageI();
        i.setName(rstring("findAnnotationsTest"));

        CommentAnnotationI a = new CommentAnnotationI();
        a.setTextValue(rstring("an annotation"));
        i.linkAnnotation(a);

        i = (ImageI) ice.getSession().getUpdateService().saveAndReturnObject(i);
        a = (CommentAnnotationI) i.linkedAnnotationList().get(0);

        Map<Long, List<IObject>> retVal = ice.getSession().getContainerService()
                .findAnnotations("Image", Arrays.asList(i.getId().getValue()), null,
                        null);
                        */
    }
}
