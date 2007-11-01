package ome.io.nio.utests.deltavision;

import ome.model.core.OriginalFile;

class DeltaVisionOriginalFile extends OriginalFile
{
	private String path = "/Users/callan/testimages/01_R3D.dv";
	
	public DeltaVisionOriginalFile() { }
	
	public DeltaVisionOriginalFile(String path)
	{
		this.path = path;
	}
	
	public String getPath()
	{
		return path;
	}
}
