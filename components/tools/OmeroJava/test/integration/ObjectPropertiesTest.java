package integration;

import org.testng.Assert;
import org.testng.annotations.Test;

import omero.RType;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Pixels;
import omero.sys.ParametersI;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ObjectPropertiesTest extends AbstractServerTest {

    private String createName() throws Exception {
        final String name = UUID.randomUUID().toString();
        System.out.println(name);
        return name;
    }
    
    
    /**
     * Test to create an image and save it with long name
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testLongNameSaving() throws Exception {
        Image img = mmFactory.simpleImage();
        img.setName(omero.rtypes.rstring(createName()));
        Image sent = (Image) iUpdate.saveAndReturnObject(img);
        String savedName = sent.getName().getValue().toString();
        long id = sent.getId().getValue();
        System.out.println(id);
        System.out.println("savedName");
        System.out.println(savedName);
        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        final ParametersI params = new ParametersI().addId(id);
        final Image retrievedImage = (Image) iQuery.findByQuery(sb.toString(), params);
        // Assert.assertEquals(1, retrievedImage.size());
        final String retrievedName = retrievedImage.getName().getValue().toString();
        System.out.println("retrievedName");
        System.out.println(retrievedName);
        Assert.assertEquals(retrievedName, savedName);
    }
}
