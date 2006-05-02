package ome.services.dao;

import ome.model.containers.Project;

import junit.framework.TestCase;

public class Test extends TestCase {

	Query q;
	
	@Override
	protected void setUp() throws Exception {
		q = new Query();
	}
	
	public void test_1() throws Exception {
		Dao<Project> d = q.getDao();
		Project e = d.findEntity();
	
	}
	
}
