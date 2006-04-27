package ome.services.dao;

import junit.framework.TestCase;

public class Test extends TestCase {

	Query q;
	
	@Override
	protected void setUp() throws Exception {
		q = new Query();
	}
	
	public void test_1() throws Exception {
		Dao<Entity> d = q.getDao();
		// Nope. Dao<Package> d = q.getDao();
		Entity e = d.findEntity();
	
		Package p = q.getTypedDao(Package.class);
		// Dao dao = q.getTypedDao(Dao.class);
	}
	
}
