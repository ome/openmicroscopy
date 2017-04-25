/*
 *   Copyright 2006-2017 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re.quantum;

import ome.model.core.Pixels;
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
     * @param pixels The pixels
     */
    public BinaryMaskQuantizer(QuantumDef qd, Pixels pixels)
    {
        super(qd, pixels);
        PixelsType type = pixels.getPixelsType();
        if (!PixelsType.VALUE_BIT.equals(type.getValue()))
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
