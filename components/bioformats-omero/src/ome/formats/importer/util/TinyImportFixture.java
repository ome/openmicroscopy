/*
 * ome.formats.importer.TinyImportFixture
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
GPL'd. See License attached to this project
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package ome.formats.importer.util;

// Java imports
import java.io.File;
import java.util.UUID;

import loci.formats.ChannelSeparator;
import loci.formats.ImageReader;
import ome.formats.OMEROMetadataStore;
import ome.formats.importer.ImportFixture;
import ome.formats.importer.ImportLibrary;
import ome.model.containers.Dataset;
import ome.system.ServiceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ResourceUtils;

/**
 * test fixture which uses a hard-coded file ("tinyTest.d3d.dv") from the 
 * classpath, and adds them to a new UUID-named dataset. 
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision: 1167 $, $Date: 2006-12-15 10:39:34 +0000 (Fri, 15 Dec 2006) $
 * @see OMEROMetadataStore
 * @see ExampleUnitTest
 * @since 3.0-M3
 */
// @RevisionDate("$Date: 2006-12-15 10:39:34 +0000 (Fri, 15 Dec 2006) $")
// @RevisionNumber("$Revision: 1167 $")
public class TinyImportFixture extends ImportFixture
{

	/** Hard-coded filename of the image to be imported */
	public final static String FILENAME = "tinyTest.d3d.dv";
	
    Log                        log = LogFactory.getLog(TinyImportFixture.class);

    private Dataset d;
    
    private ServiceFactory sf;
    
    public TinyImportFixture(ServiceFactory services) throws Exception
    {
        super( new OMEROMetadataStore(services),
               new ChannelSeparator(new ImageReader()) );
        this.sf = services;
    }

    /**
     * checks for the necessary fields and initializes the {@link ImportLibrary}
     * 
     * @throws Exception
     */
    public void setUp() throws Exception
    {
		d = new Dataset();
		d.setName(UUID.randomUUID().toString());
		d = sf.getUpdateService().saveAndReturnObject(d);
		
		File 	tinyTest = ResourceUtils.getFile("classpath:"+FILENAME);
		
		super.put( tinyTest, d );
    	super.setUp();
    }
    
    /** provides access to the created {@link Dataset} instance.
     */
    public Dataset getDataset()
    {
    	return d;
    }
}
