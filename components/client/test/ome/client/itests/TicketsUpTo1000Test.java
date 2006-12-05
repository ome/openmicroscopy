package ome.client.itests;

import org.testng.annotations.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import junit.framework.TestCase;

import ome.api.IAdmin;
import ome.api.IPojos;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.api.RawFileStore;
import ome.model.IObject;
import ome.model.annotations.DatasetAnnotation;
import ome.model.annotations.ImageAnnotation;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.OriginalFile;
import ome.model.core.Pixels;
import ome.model.core.PlaneInfo;
import ome.model.enums.Format;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.parameters.Parameters;
import ome.system.EventContext;
import ome.system.Login;
import ome.system.ServiceFactory;
import ome.testing.ObjectFactory;
import ome.util.builders.PojoOptions;
import pojos.ImageData;

@Test( 
	groups = {"client","integration"} 
)
public class TicketsUpTo1000Test extends TestCase
{

    ServiceFactory sf = new ServiceFactory("ome.client.test");
    IUpdate iUpdate   = sf.getUpdateService();
    IQuery iQuery     = sf.getQueryService();
    IAdmin iAdmin     = sf.getAdminService();

    // ~ Ticket 509
    // =========================================================================
    
    @Test( groups = "ticket:509")
    public void test_connectionsShouldBeFreedOnClose() throws Exception
    {
    	OriginalFile of = makeFile();
    	for (int i = 0; i < 50; i++) {
	    	RawFileStore rfs = sf.createRawFileStore();
	    	rfs.setFileId(of.getId());
	    	rfs.close();
		}

    }

    @Test( groups = "ticket:509")
    public void test_connectionsShouldBeFreedOnTimeout() throws Exception
    {
    	OriginalFile of = makeFile();
    	int count = 0;
    	for (int i = 0; i < 50; i++) {
    		try {
    			RawFileStore rfs = sf.createRawFileStore();
    			rfs.setFileId(of.getId());
    		} catch (Exception e) {
    			count++;
    		}
    	}
    	assertTrue(count + " fails!",count==0);

    }

    @Test( groups = "ticket:509")
    public void test_simplestPossibleFail() throws Exception
    {
    	// The TxInterceptor throws an exception which makes the id for this of
    	// invalid
    	OriginalFile of = makeFile();
    	
    	// must restart the app server to prime this method.
    	// but probably only when it occurs in the same thread.
    	RawFileStore rfs = sf.createRawFileStore();
    	rfs.setFileId(of.getId().longValue());
    	rfs.close();
    	    	
    	sf.getQueryService().get(OriginalFile.class, of.getId());
    
//    	 The TxInterceptor throws an exception which makes the id for this of
    	// invalid
    	of = makeFile();
    	
    	// must restart the app server to prime this method.
    	// but probably only when it occurs in the same thread.
    	rfs = sf.createRawFileStore();
    	rfs.setFileId(of.getId());
    	rfs.close();
    	    	
    	sf.getQueryService().get(OriginalFile.class, of.getId());
    
    }
    
    // ~ Ticket 530
    // =========================================================================
    
    @Test( groups = "ticket:530")
    public void test_hierarchyInversionShouldWork() throws Exception
    {
        Dataset d = new Dataset();
        d.setName("d");
        Image i = new Image();
        i.setName("i");
        Category c = new Category();
        c.setName("c");
        d.linkImage(i);
        c.linkImage(i);
        i = sf.getUpdateService().saveAndReturnObject(i);

        d = (Dataset) i.linkedDatasetList().get(0);
        c = (Category) i.linkedCategoryList().get(0);

        Set s = 
        	sf.getPojosService().findContainerHierarchies
        	(Project.class,Collections.singleton(
        		i.getId()),new PojoOptions().leaves().map());
        s = sf.getPojosService().findContainerHierarchies
            (CategoryGroup.class,Collections.singleton(
            	i.getId()),new PojoOptions().leaves().map());
     
        assertTrue(s.size()>0);
    }
    
    // ~ Helpers
	// =========================================================================


	private OriginalFile makeFile() {
		OriginalFile of = new OriginalFile();
    	of.setSha1("ticket:509");
    	of.setSize(0);
    	of.setName("ticket:509");
    	of.setPath("/dev/null");
    	of.setFormat(new Format(1L,false));
    	of = sf.getUpdateService().saveAndReturnObject(of);
		return of;
	}

}
