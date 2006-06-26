package omeis.providers.re.data;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import ome.api.IQuery;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.Color;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;
import ome.model.enums.Family;
import ome.model.enums.PixelsType;
import ome.model.enums.RenderingModel;
import ome.system.OmeroContext;

import omeis.providers.re.ColorsFactory;
import omeis.providers.re.Renderer;
import omeis.providers.re.quantum.QuantumFactory;

/**
 * 
 * Encapsulates access to the image raw data. 
 * Contains the logic to interpret a linear byte array as a 5D array. 
 * Knows how to extract a 2D-plane from the 5D array, but delegates to the 
 * specified 2D-Plane the retrieval of pixel values. 
 * 
 * @author Chris Allan <callan@blackcat.ca>
 *
 */
public class PlaneFactory
{
	/** Identifies the type used to store pixel values. */
    public static final int BYTE = 0;
    
    /** Identifies the type used to store pixel values. */
    public static final int SHORT = 1;
    
    /** Identifies the type used to store pixel values. */
    public static final int INT = 2;
    
    /** Identifies the type used to store pixel values. */
    public static final int LONG = 3;
    
    /** Identifies the type used to store pixel values. */
    public static final int FLOAT = 4;
    
    /** Identifies the type used to store pixel values. */
    public static final int DOUBLE = 5;
    
    
	/**
	 * A static helper method to check if a type is one of the elements in an
	 * array.
	 * 
	 * @param type A pixels type enumeration.
	 * @param strings The strings for which you want to check against.
	 * @return True on successful match and false on failure to match.
	 */
	public static boolean in(PixelsType type, String[] strings)
	{
		String typeAsString = type.getValue();
		for (int i = 0; i < strings.length; i++)
			if (typeAsString.equals(strings[i]))
				return true;
		return false;
	}
    
    /**
     * A static helper method to retrieve pixel byte widths.
     * 
     * @param type The pixels type for which you want to know the byte width.
     * @return The number of bytes per pixel value.
     */
    static int bytesPerPixel(PixelsType type)
    {
        if (in(type, new String[] {"int8", "uint8" }))
            return 1;
        else if (in(type, new String[] { "int16", "uint16" }))
            return 2;
        else if (in(type, new String[] { "int32", "uint32", "float" }))
            return 4;
        else if (type.getValue().equals("double"))
            return 8;
        else
            throw new RuntimeException("Unknown pixel type: '"
                    + type.getValue() + "'");
    }

    /**
     * A static helper method to retrieve Java type mappings.
     * 
     * @param type The pixels type for which you wish to know the mapped Java
     * type.
     * @return The Java type as an enumerated integer.
     */
    static int javaType(PixelsType type)
    {
    	if (in(type, new String[] {"int8", "uint8" }))
    		return BYTE;
    	else if (in(type, new String[] { "int16", "uint16" }))
    		return SHORT;
    	else if (in(type, new String[] { "int32", "uint32" }))
    		return INT;
    	else if (type.getValue().equals("float"))
    		return FLOAT;
    	else if (type.getValue().equals("double"))
    		return DOUBLE;
    	else
    		throw new RuntimeException("Unknown pixel type: '"
    				+ type.getValue() + "'");
    }
    
    /**
     * Factory method to fetch plane data and create an object to access it.
     * 
     * @param planeDef Defines the plane to be retrieved. Must not be null.
     * @param channel The wavelength at which data is to be fetched.
     * @param pixels The pixels from which the data is to be fetched.
     * @param buffer The pixels buffer from which the data is to be fetched.
     * @return A plane 2D object that encapsulates the actual plane pixels.
     */
    public static Plane2D createPlane(PlaneDef planeDef, int channel,
    		                          Pixels pixels, PixelBuffer buffer) 
    {
        if (planeDef == null)
            throw new NullPointerException("Expecting not null planeDef");
        else if (pixels == null)
            throw new NullPointerException("Expecting not null pixels");
        else if (buffer == null)
            throw new NullPointerException("Expecting not null buffer");
        
        Integer z = Integer.valueOf(planeDef.getZ());
        Integer c = Integer.valueOf(channel);
        Integer t = Integer.valueOf(planeDef.getT());
        
        try
        {
        	switch (planeDef.getSlice())
        	{
        	case PlaneDef.XY:
        		return new XYPlane(planeDef, pixels, buffer.getPlane(z, c, t));
        	case PlaneDef.XZ:
        		return new XZPlane(planeDef, pixels, buffer.getStack(c, t));
        	case PlaneDef.ZY:
        		return  new ZYPlane(planeDef, pixels, buffer.getStack(c, t));
        	}
        }
        catch (IOException e)
        {
        	throw new RuntimeException(e);
        }
        catch (DimensionsOutOfBoundsException e)
        {
        	throw new RuntimeException(e);
        }
        
        return null;
    }
    
    /**
     * Helper method to retrieve a RenderingModel enumeration from the database.
     * 
     * @param value The enumeration value.
     * @return A rendering model enumeration object.
     */
    public static RenderingModel getRenderingModel(String value)
    {
        OmeroContext ctx = OmeroContext.getManagedServerContext();
        IQuery iQuery = (IQuery) ctx.getBean("queryService");
    	return (RenderingModel)
    		iQuery.findByString(RenderingModel.class, "value", value);
    }
    
    /** 
     * Helper method to create the default settings if none is available.
     * In this case we use a grayscale model to map the first wavelength in
     * the pixels file.  The mapping is linear and the intervals are selected
     * according to a "best-guess" statistical approach.
     * 
     * @param stats For each wavelength, it contains the global minimum and
     *              maximum of the wavelength stack across time.
     * @return  The default rendering settings.
     */
    public static RenderingDef createDefaultRenderingDef(Pixels metadata)
    {
        int c_size = metadata.getSizeC().intValue();
        int z_size = metadata.getSizeZ().intValue();
        
        QuantumDef qDef = new QuantumDef();
        qDef.setBitResolution(new Integer(QuantumFactory.DEPTH_8BIT));
        qDef.setCdStart(new Integer(0));
        qDef.setCdEnd(new Integer(QuantumFactory.DEPTH_8BIT));
        
        ChannelBinding[] waves = new ChannelBinding[c_size];
        boolean active = false;
        RenderingModel model = getRenderingModel(Renderer.MODEL_GREYSCALE);
        
        List channels = metadata.getChannels();
        int w = 0;
        for (Iterator i = channels.iterator(); i.hasNext(); )
        {
        	Channel channel = (Channel) i.next();
        	double gMin = channel.getStatsInfo().getGlobalMin().doubleValue();
        	double gMax = channel.getStatsInfo().getGlobalMax().doubleValue();
            Color color = ColorsFactory.getColor(w, channel);
            if (metadata.getAcquisitionContext().
            	getPhotometricInterpretation().getValue() == Renderer.PHOTOMETRIC_RGB)
            { 
                active = true;
                model = getRenderingModel(Renderer.MODEL_RGB);
            }
            waves[w] = new ChannelBinding();
            // FIXME: These downcasts should probably be dealt with. The input
            // start and input end are floating point whereas the statistical
            // global min and global max values stored in the database are
            // double precision.
            waves[w].setInputStart((float) gMin);
            waves[w].setInputEnd((float) gMax);
            waves[w].setColor(color);
            waves[w].setActive(active);
            waves[w].setFamily(QuantumFactory.getFamily(QuantumFactory.LINEAR));
            waves[w].setCoefficient(1.0);
        }
        waves[0].setActive(true);  //NOTE: dims object enforces 1 < sizeC.
        RenderingDef newRD = new RenderingDef();
        int defaultZ = z_size/2+z_size%2-1;
        newRD.setDefaultZ(Integer.valueOf(defaultZ));
        newRD.setDefaultT(Integer.valueOf(0));
        newRD.setModel(model);
        newRD.setQuantization(qDef);
        newRD.setWaveRendering(Arrays.asList(waves));
        return newRD;
    }
}
