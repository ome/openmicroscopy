package ome.services.dao;

public class Query {

	<T extends Entity> Dao<T> getDao() { return null; }
	<T extends Package> T getTypedDao(Class<T> c) {
		return null; 
	}
}
