package ome.testing;

public interface HierarchyBrowsingTests {

	/**
	 * @throws Exception*********************************/
	public abstract void testLoadPDIHierarchyProjectNoReturn() throws Exception;

	public abstract Object testLoadPDIHierarchyProject();

	/**
	 * @throws Exception*********************************/
	public abstract void testLoadPDIAnnotatedHierarchyProjectNoReturn()
			throws Exception;

	public abstract Object testLoadPDIAnnotatedHierarchyProject();

	/**
	 * @throws Exception*********************************/
	public abstract void testLoadPDIHierarchyDatasetNoReturn() throws Exception;

	public abstract Object testLoadPDIHierarchyDataset();

	/**
	 * @throws Exception*********************************/
	public abstract void testLoadPDIAnnotatedHierarchyDatasetNoReturn()
			throws Exception;

	public abstract Object testLoadPDIAnnotatedHierarchyDataset();

	/**
	 * @throws Exception*********************************/
	public abstract void testLoadCGCIHierarchyCategoryGroupNoReturn()
			throws Exception;

	public abstract Object testLoadCGCIHierarchyCategoryGroup();

	/**
	 * @throws Exception*********************************/
	public abstract void testLoadCGCIAnnotatedHierarchyCategoryGroupNoReturn()
			throws Exception;

	public abstract Object testLoadCGCIAnnotatedHierarchyCategoryGroup();

	/**
	 * @throws Exception*********************************/
	public abstract void testLoadCGCIHierarchyCategoryNoReturn()
			throws Exception;

	public abstract Object testLoadCGCIHierarchyCategory();

	/**
	 * @throws Exception*********************************/
	public abstract void testLoadCGCIAnnotatedHierarchyCategoryNoReturn()
			throws Exception;

	public abstract Object testLoadCGCIAnnotatedHierarchyCategory();

	/**
	 * @throws Exception*********************************/
	public abstract void testFindCGCIHierarchiesNoReturn() throws Exception;

	public abstract Object testFindCGCIHierarchies();

	/**
	 * @throws Exception*********************************/
	public abstract void testFindCGCPathsContainedNoReturn() throws Exception;

	public abstract Object testFindCGCPathsContained();

	/**
	 * @throws Exception*********************************/
	public abstract void testFindCGCPathsNotContainedNoReturn()
			throws Exception;

	public abstract Object testFindCGCPathsNotContained();

	/**
	 * @throws Exception*********************************/
	public abstract void testFindPDIHierarchiesNoReturn() throws Exception;

	public abstract Object testFindPDIHierarchies();

	/**
	 * @throws Exception*********************************/
	public abstract void testFindImageAnnotationsSetNoReturn() throws Exception;

	public abstract Object testFindImageAnnotationsSet();

	/**
	 * @throws Exception*********************************/
	public abstract void testFindImageAnnotationsSetForExperimenterNoReturn()
			throws Exception;

	public abstract Object testFindImageAnnotationsSetForExperimenter();

	/**
	 * @throws Exception*********************************/
	public abstract void testFindDatasetAnnotationsSetNoReturn()
			throws Exception;

	public abstract Object testFindDatasetAnnotationsSet();

	/**
	 * @throws Exception*********************************/
	public abstract void testFindDatasetAnnotationsSetForExperimenterNoReturn()
			throws Exception;

	public abstract Object testFindDatasetAnnotationsSetForExperimenter();

}