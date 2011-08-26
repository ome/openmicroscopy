/*
 * integration.ScriptServiceTest 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package integration;


//Java imports
import java.util.Iterator;
import java.util.List;

//Third-party libraries
import org.testng.annotations.Test;

//Application-internal dependencies
import omero.api.IScriptPrx;
import omero.grid.JobParams;
import omero.model.OriginalFile;

/** 
 * Collections of tests for the <code>Script</code> service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
@Test(groups = { "client", "integration", "blitz" })
public class ScriptServiceTest 
	extends AbstractTest
{

    /**
     * Tests the retrieval of the scripts using the <code>getScripts</code>
     * method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testGetScripts() 
    	throws Exception 
    {
    	IScriptPrx svc = factory.getScriptService();
    	List<OriginalFile> scripts = svc.getScripts();
    	assertNotNull(scripts);
    	assertNotNull(scripts.size() > 0);
    	Iterator<OriginalFile> i = scripts.iterator();
    	while (i.hasNext()) {
			assertNotNull(i.next());
		}
    }
    
    /**
     * Tests the retrieval of the parameters associated to a script 
     * using the <code>getParams</code> method.
     * @throws Exception Thrown if an error occurred.
     * @see #testGetScripts() 
     */
    @Test(enabled = true)
    public void testGetParams() 
    	throws Exception 
    {
    	IScriptPrx svc = factory.getScriptService();
    	List<OriginalFile> scripts = svc.getScripts();
    	Iterator<OriginalFile> i = scripts.iterator();
    	OriginalFile f;
    	JobParams params;
    	while (i.hasNext()) {
			f = i.next();
			params = svc.getParams(f.getId().getValue());
			assertNotNull(params);
		}
    }
    
    /**
     * Tests to upload an official script by a user who is not an administrator,
     * this method uses the <code>uploadOfficialScript</code>.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = true)
    public void testUploadOfficialScript()
    	throws Exception
    {
    	StringBuffer buf = new StringBuffer("");
    	String[] values = {"a", "b", "c"};
    	for (int i = 0; i < values.length; i++) {
			buf.append(values[i].charAt(0));
		}
    	String folder = "officialTestFolder";
    	IScriptPrx svc = factory.getScriptService();
    	int n = svc.getScripts().size();
    	try {
    		svc.uploadOfficialScript(folder, buf.toString());
    		fail("Only administrators can upload official script.");
		} catch (Exception e) {
		}
    	assertTrue(svc.getScripts().size() == n);
    }
    
    /**
     * Tests to upload a script, this method uses the <code>uploadScript</code>.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = true)
    public void testUploadScript()
    	throws Exception
    {
    	StringBuffer buf = new StringBuffer("");
    	String[] values = {"a", "b", "c"};
    	for (int i = 0; i < values.length; i++) {
			buf.append(values[i].charAt(0));
		}
    	String folder = "scriptTestFolder";
    	IScriptPrx svc = factory.getScriptService();
    	long id = svc.uploadScript(folder, buf.toString());
    	assertTrue(id > 0);
    }
    
}
