/*
 * ome.server.utests.PojosConstraintsTest
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
package ome.server.utests;

//Java imports
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies
import ome.logic.PojosImpl;
import ome.model.CategoryGroup;
import ome.model.Dataset;
import ome.model.Image;
import ome.model.Project;
import ome.util.builders.PojoOptions;

/**
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since Omero 2.0
 */
public class PojosConstraintsTest extends TestCase {
    protected PojosImpl manager;
    
    protected void setUp() throws Exception {
        super.setUp();
        manager = new PojosImpl(null);
    }
    
    protected void tearDown() throws Exception {
        manager = null;
    }

	public void testFindAnnotations() {
		T t = new T(IllegalArgumentException.class){
			@Override
			public void doTest(Object[] arg) {
				manager.findAnnotations((Class) arg[0], (Set) arg[1], (Map) arg[2]);
			}
		};
		
		// param1: not null or wrong type
		t.blowup(true,null,new HashSet(),new HashMap());
		t.blowup(true,Project.class,new HashSet(),new HashMap());
		t.blowup(false,Image.class,new HashSet(),new HashMap()); // FIXME should check for empty sets.
		t.blowup(false,Dataset.class,new HashSet(),new HashMap());

		// param2: not null
		t.blowup(true,Dataset.class,null,new HashMap());
		
		// eek
		t.blowup(false,Dataset.class,new HashSet(),null);
			
	}

	public void testFindCGCPaths() {
		T t = new T(IllegalArgumentException.class){
			@Override
			public void doTest(Object[] arg) {
				manager.findCGCPaths((Set)arg[0],((Integer)arg[1]).intValue(), (Map)arg[2]);
			}
		};
		
		// param1: not null
		t.blowup(true,null,1,new HashMap());
		t.blowup(false,new HashSet(),1,new HashMap());
		
		// param2: 
		// TODO
		
	}

	public void testFindContainerHierarchies() {
		T t = new T(IllegalArgumentException.class){
			@Override
			public void doTest(Object[] arg) {
				manager.findContainerHierarchies((Class)arg[0],(Set)arg[1],(Map)arg[2]);
			}
		};
		
		// param1: not null or wrong type
		t.blowup(true,null,new HashSet(), new HashMap());
		t.blowup(true,Dataset.class,new HashSet(),new HashMap());
		t.blowup(true,Image.class,new HashSet(),new HashMap());
		t.blowup(false,Project.class,new HashSet(), new HashMap());
		t.blowup(false,CategoryGroup.class,new HashSet(), new HashMap());
		
		// param2: 
		t.blowup(true,Project.class,null, new HashMap());
		t.blowup(false,Project.class,new HashSet(),new HashMap());

	}

	public void testGetImages() {
		T t = new T(IllegalArgumentException.class){
			@Override
			public void doTest(Object[] arg) {
				manager.getImages((Class)arg[0],(Set)arg[1],(Map)arg[2]);
			}
		};
		
		// param1: not null
		t.blowup(true,null,new HashSet(),new HashMap());
		t.blowup(false,Dataset.class, new HashSet(),new HashMap());

	}

	public void testGetUserImages(Map options) {
		T t = new T(IllegalArgumentException.class){
			@Override
			public void doTest(Object[] arg) {
				manager.getUserImages((Map)arg[0]);
			}
		};
		
		t.blowup(true,new PojoOptions().allExps());
		t.blowup(false,new PojoOptions().exp(1));
		
	}

	public void testLoadContainerHierary() {
		Set ids;
		Map options;
		T t = new T(IllegalArgumentException.class){
			@Override
			public void doTest(Object[] arg) {
				manager.loadContainerHierarchy((Class)arg[0],(Set)arg[1],(Map)arg[2]);
			}
		};

		// param1: wrong or null class type
		ids=new HashSet<Integer>(Arrays.asList(1,2,3));
		options = new HashMap();
		t.blowup(true,null,ids,options);
		t.blowup(true,Image.class,ids,options);
		//FIXME do all blowup(false,...) belong in itests t.blowup(false,Project.class,new HashSet(),options);
		
		// param2: not null unless there's an experimenter 
		//FIXMEt.blowup(false,Project.class,null,new PojoOptions().exp(1).map());
		t.blowup(true,Project.class,null,new HashMap());
		//FIXMEt.blowup(false,Project.class,new HashSet(),new HashMap());//empty set is ok? TODO
		
		//param3: no constraints.
		
	}
    
	/** part of the testing framework. Allow imlementers to specifiy a method to be tested <
	 * <code>doTest</code> and then call it with an {@see #blowup(boolean, Object[]) blowup}.  
	 * Note: essentially a closure to make calling this thing easy.
	 */ 
	private static abstract class T {
		private Class t = null;
		public T(){}
		public T(Class thrown){t=thrown;}
		public abstract void doTest(Object[] arg);
		public void setException(Class type){t=type;}
		
		public void blowup(boolean exceptionExpected,Object... arg){
			try {
				doTest(arg);
				if (exceptionExpected) fail("Expected an exception here");
			} catch (Throwable e) {
				if (! exceptionExpected || (t != null && ! (t.isAssignableFrom(e.getClass())))) {
					throw new RuntimeException("Exception type "+e.getClass()+" not expected. Rethrowing",e);
				}
			}
		}
		
	}
    
}
