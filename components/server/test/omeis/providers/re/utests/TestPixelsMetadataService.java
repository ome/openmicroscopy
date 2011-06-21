/*
 *   Copyright (C) 2009-2011 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omeis.providers.re.utests;

import java.util.ArrayList;
import java.util.List;

import ome.api.IPixels;
import ome.model.IObject;
import ome.model.core.Pixels;
import ome.model.display.RenderingDef;
import ome.model.enums.Family;
import ome.model.enums.PixelsType;
import ome.model.enums.RenderingModel;

public class TestPixelsMetadataService implements IPixels {
	
	private static final String[] FAMILIES =
		new String[] { "linear", "polynomial", "exponential", "logarithmic" };
	
	private static final String[] MODELS =
		new String[] { "rgb", "greyscale" };

	public Long copyAndResizeImage(long arg0, Integer arg1, Integer arg2,
			Integer arg3, Integer arg4, List<Integer> arg5, String arg6,
			boolean arg7) {
		// TODO Auto-generated method stub
		return null;
	}

	public Long copyAndResizePixels(long arg0, Integer arg1, Integer arg2,
			Integer arg3, Integer arg4, List<Integer> arg5, String arg6,
			boolean arg7) {
		// TODO Auto-generated method stub
		return null;
	}

	public Long createImage(int arg0, int arg1, int arg2, int arg3,
			List<Integer> arg4, PixelsType arg5, String arg6, String arg7) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends IObject> List<T> getAllEnumerations(Class<T> klass)
	{
		if (klass.equals(Family.class))
		{
			List<T> list = new ArrayList<T>();
			for (String value : FAMILIES)
			{
				Family o = new Family();
				o.setValue(value);
				list.add((T) o);
			}
			return list;
		}
		else if (klass.equals(RenderingModel.class))
		{
			List<T> list = new ArrayList<T>();
			for (String value : MODELS)
			{
				RenderingModel o = new RenderingModel();
				o.setValue(value);
				list.add((T) o);
			}
			return list;
		}
		throw new RuntimeException("Unhandled class: " + klass);
	}

	public int getBitDepth(PixelsType arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public <T extends IObject> T getEnumeration(Class<T> arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public RenderingDef loadRndSettings(long arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<IObject> retrieveAllRndSettings(long arg0, long arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Pixels retrievePixDescription(long arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public RenderingDef retrieveRndSettings(long arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public RenderingDef retrieveRndSettingsFor(long arg0, long arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public void saveRndSettings(RenderingDef arg0) {
		// TODO Auto-generated method stub

	}

	public void setChannelGlobalMinMax(long arg0, int arg1, double arg2,
			double arg3) {
		// TODO Auto-generated method stub

	}

}
