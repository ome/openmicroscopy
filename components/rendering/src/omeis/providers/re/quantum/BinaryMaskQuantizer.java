/*
 * omeis.providers.re.quantum.Quantization_8_16_bit
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re.quantum;

// Java imports

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Application-internal dependencies
import ome.model.display.QuantumDef;
import ome.model.enums.PixelsType;

/**
 * Quantization process for binary masks.
 * 
 * @author Chris Allan <callan at blackcat dot ca>
 */
public class BinaryMaskQuantizer extends QuantumStrategy
{
    /**
     * Creates a new strategy.
     * 
     * @param qd Quantum definition object, contained mapping data.
     * @param type The pixel type. Must be of type <code>bit</code>.
     */
    public BinaryMaskQuantizer(QuantumDef qd, PixelsType type)
    {
        super(qd, type);
        if (!type.getValue().equals("bit"))
        {
        	throw new IllegalArgumentException(
        			"The type " + type.getValue() + " != 'bit'.");
        }
    }

    /**
     * Implemented as specified in {@link QuantumStrategy}.
     * 
     * @see QuantumStrategy#quantize(double)
     */
    @Override
    public int quantize(double value) throws QuantizationException
    {
    	if (value == 0.0)
    	{
    		return 0;
    	}
    	if (value == 1.0)
    	{
    		return 255;
    	}
    	throw new QuantizationException(
    			"The value " + value + " is not 0.0 or 1.0.");
    }

	@Override
	protected void onWindowChange()
	{
		// No-op.
	}

}
