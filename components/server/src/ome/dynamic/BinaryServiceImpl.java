package ome.dynamic;

public class BinaryServiceImpl implements BinaryService {

	public byte[] getClass(String name) {
		CodeGeneration a = new CodeGeneration();
		return a.getClassFromDB(name);
	}

}
