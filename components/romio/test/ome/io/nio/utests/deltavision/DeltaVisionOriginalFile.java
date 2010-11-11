package ome.io.nio.utests.deltavision;

import ome.model.core.OriginalFile;

class DeltaVisionOriginalFile extends OriginalFile
{
    private static final long serialVersionUID = -1206483994172071229L;

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
