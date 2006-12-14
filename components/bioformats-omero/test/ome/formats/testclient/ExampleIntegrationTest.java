/*
 * ome.formats.testclient.ExampleIntegrationTest
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
 *    This library is free software; you can redistribute it and/or
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

package ome.formats.testclient;

import java.io.File;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ResourceUtils;
import org.testng.annotations.Test;

import ome.formats.OMEROMetadataStore;
import ome.formats.importer.ImportFixture;
import ome.formats.importer.ImportLibrary;
import ome.model.containers.Dataset;
import ome.system.Login;
import ome.system.ServiceFactory;

import junit.framework.TestCase;

@Test( groups = "integration" )
public class ExampleIntegrationTest extends TestCase {

	private final static Log log = LogFactory.getLog(ExampleIntegrationTest.class);
	
	protected ImportFixture fixture;
	
	public void testUseHardCoded() throws Exception {
		ServiceFactory sf = new ServiceFactory(new Login("root","ome"));
		
		Dataset d = new Dataset();
		d.setName(UUID.randomUUID().toString());
		d = sf.getUpdateService().saveAndReturnObject(d);
		
		OMEROMetadataStore store = new OMEROMetadataStore(sf);
		
		String  file 	 = "tinyTest.d3d.dv";
		File 	tinyTest = ResourceUtils.getFile("classpath:"+file);
		
		fixture = new ImportFixture(store);
		fixture.put(tinyTest,d);
		
		fixture.setUp();
		fixture.doImport(new ImportLibrary.Step(){
			@Override
			public void step(int n) {
				log.debug("Wrote plane:"+n);
			}
		});
		fixture.tearDown();
		
	}
	
}
