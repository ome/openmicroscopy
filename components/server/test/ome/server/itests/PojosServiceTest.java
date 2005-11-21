/*
 * ome.server.itests.PojosServiceTest
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
package ome.server.itests;

//Java imports
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies
import ome.model.Dataset;
import ome.model.ImagePixel;
import ome.model.Project;
import ome.model.Repository;
import ome.security.Utils;
import ome.testing.AbstractPojosServiceTest;
import ome.util.ContextFilter;
import ome.util.Filterable;
import ome.util.builders.PojoOptions;

/** 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 2.0
 */
public class PojosServiceTest
        extends
            AbstractPojosServiceTest {

    protected static Log log = LogFactory.getLog(PojosServiceTest.class); // TODO modify to getLog() abstract
    
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {

        return ConfigHelper.getConfigLocations(); 
    }

    @Override
    protected void onSetUp() throws Exception {
    	super.onSetUp();
    	Utils.setUserAuth();
    }
    
    public void testRepositoryNotNull(){
    	checkForRepository(psrv.getUserImages(new PojoOptions().exp(1).map()));
    	checkForRepository(psrv.getImages(Dataset.class,Collections.singleton(1),null));
    	checkForRepository(psrv.loadContainerHierarchy(Project.class,Collections.singleton(1),null));
    }
    
    protected void checkForRepository(Object o){
    	Scanner scanner = new Scanner(ImagePixel.REPOSITORY);
    	List<Repository> l = scanner.scan(o);
    	for (Repository r : l){
    		assertTrue("No server url",r.getImageServerUrl()!=null);
    	}    	
    }
    
}

// TODO refactor out. 
class Scanner extends ContextFilter {
	
	protected String _field;
	
	protected List _hits = new ArrayList();
	
	public Scanner(String field){
		this._field = field;
	}
	
	public List scan(Object target) {
		super.filter(null,target);
		return _hits;
	}
	
	@Override
	public Filterable filter(String fieldId, Filterable f) {
		addIfHit(fieldId, f); // TODO here's where we could use a single call back for each filter method. (onFilter) also (onFilterX)
		return super.filter(fieldId,f);
	}
	
	@Override
	public Collection filter(String fieldId, Collection c) {
		addIfHit(fieldId, c); // TODO here's where we could use a single call back for each filter method. (onFilter) also (onFilterX)
		return super.filter(fieldId,c);
	}

	@Override
	public Map filter(String fieldId, Map m) {
		addIfHit(fieldId, m); // TODO here's where we could use a single call back for each filter method. (onFilter) also (onFilterX)
		return super.filter(fieldId,m);
	}

	@Override
	public Object filter(String fieldId, Object o) {
		addIfHit(fieldId, o); // TODO here's where we could use a single call back for each filter method. (onFilter) also (onFilterX)
		return super.filter(fieldId,o);
	}
	
	public void addIfHit(String fieldId, Object o){
		if (fieldId==null) {
			if (_field==null){
				_hits.add(o);
			}
		} else {
			if (fieldId.equals(_field)){
				_hits.add(o);
			}
		}
	}
	
}