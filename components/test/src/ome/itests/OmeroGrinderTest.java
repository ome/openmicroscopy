/*
 * ome.itests.OmeroGrinderTest
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
package ome.itests;

// Java imports
import java.util.Map;
import java.util.Set;

// Third-party libraries

// Application-internal dependencies
import ome.model.Category;
import ome.model.CategoryGroup;
import ome.model.Dataset;
import ome.model.Project;
import ome.adapters.pojos.PojoAdapterUtils;
import ome.testing.AbstractOmeroHierarchyBrowserIntegrationTest;
import ome.testing.OMEData;
import ome.testing.OMEPerformanceData;

/**
 * used externally by Grinder to test not just client code but also conversion
 * by AdapterUtils to Shoola code.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
public class OmeroGrinderTest extends
		AbstractOmeroHierarchyBrowserIntegrationTest {

	/**
	 * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
	 */
	protected String[] getConfigLocations() {
		return new String[] { "ome/client/spring.xml",
				"ome/client/itests/data.xml",
				"ome/client/itests/test.xml" };
	}
//TODO check here are these methods sensible???
	public OmeroGrinderTest(String name) {
		super(name, new OMEPerformanceData());
	}

	public OmeroGrinderTest(OMEData data) {
		super("OmeroGrinderTest with Data", data);
	}

	@Override
	public Object testFindCGCIHierarchies() {
		return test(PojoAdapterUtils.adaptFoundCGCIHierarchies((Set) super
				.testFindCGCIHierarchies()));
	}

	@Override
	public Object testFindDatasetAnnotationsSet() {
		return test(PojoAdapterUtils.adaptFoundDatasetAnnotations((Map) super
				.testFindDatasetAnnotationsSet()));
	}

	@Override
	public Object testFindDatasetAnnotationsSetForExperimenter() {
		return test(PojoAdapterUtils.adaptFoundDatasetAnnotations((Map) super
				.testFindDatasetAnnotationsSetForExperimenter()));
	}

	@Override
	public Object testFindImageAnnotationsSet() {
		return test(PojoAdapterUtils.adaptFoundImageAnnotations((Map) super
				.testFindImageAnnotationsSet()));
	}

	@Override
	public Object testFindImageAnnotationsSetForExperimenter() {
		return test(PojoAdapterUtils.adaptFoundImageAnnotations((Map) super
				.testFindImageAnnotationsSetForExperimenter()));
	}

	@Override
	public Object testFindPDIHierarchies() {
		return test(PojoAdapterUtils.adaptFoundPDIHierarchies((Set) super
				.testFindPDIHierarchies()));
	}

	@Override
	public Object testLoadCGCIHierarchyCategory() {
		return test(PojoAdapterUtils.adaptLoadedCGCIHierarchy(Category.class, super
				.testLoadCGCIHierarchyCategory()));
	}

	@Override
	public Object testLoadCGCIAnnotatedHierarchyCategory() {
		return test(PojoAdapterUtils.adaptLoadedCGCIHierarchy(Category.class,
				super.testLoadCGCIAnnotatedHierarchyCategory()));
	}

	@Override
	public Object testLoadCGCIHierarchyCategoryGroup() {
		return test(PojoAdapterUtils.adaptLoadedCGCIHierarchy(CategoryGroup.class,
				super.testLoadCGCIHierarchyCategoryGroup()));
	}

	@Override
	public Object testLoadCGCIAnnotatedHierarchyCategoryGroup() {
		return test(PojoAdapterUtils.adaptLoadedCGCIHierarchy(CategoryGroup.class,
				super.testLoadCGCIAnnotatedHierarchyCategoryGroup()));
	}

	@Override
	public Object testLoadPDIHierarchyDataset() {
		return test(PojoAdapterUtils.adaptLoadedPDIHierarchy(Dataset.class, super
				.testLoadPDIHierarchyDataset()));
	}

	@Override
	public Object testLoadPDIAnnotatedHierarchyDataset() {
		return test(PojoAdapterUtils.adaptLoadedPDIHierarchy(Dataset.class, super
				.testLoadPDIAnnotatedHierarchyDataset()));
	}

	@Override
	public Object testLoadPDIHierarchyProject() {
		return test(PojoAdapterUtils.adaptLoadedPDIHierarchy(Project.class, super
				.testLoadPDIHierarchyProject()));

	}

	@Override
	public Object testLoadPDIAnnotatedHierarchyProject() {
		return test(PojoAdapterUtils.adaptLoadedPDIHierarchy(Project.class, super
				.testLoadPDIAnnotatedHierarchyProject()));
	}

	@Override
	public Object testFindCGCPathsContained() {
		return test(PojoAdapterUtils.adaptFoundCGCIHierarchies((Set) super
				.testFindCGCPathsContained()));
	}

	@Override
	public Object testFindCGCPathsNotContained() {
		return test(PojoAdapterUtils.adaptFoundCGCIHierarchies((Set) super
				.testFindCGCPathsNotContained()));
	}

	
	Object test(Object result) {
		
		if (true){
			return result;
		}
		
		if (null == result) {
			throw new RuntimeException("Result was null");
		} else if (result instanceof Set) {
			Set set = (Set) result;
			if (set.size() == 0) {
				throw new RuntimeException("Result was empty.");
			}
		} else if (result instanceof Map) {
			Map map = (Map) result;
			if (map.keySet().size() == 0) {
				throw new RuntimeException("Result has no keys");
			}
		}
		return result;
	}

}
