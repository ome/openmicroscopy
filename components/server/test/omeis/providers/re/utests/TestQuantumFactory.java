/*
 * omeis.providers.re.quantum.QuantumFactory
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re.utests;

// Java imports

// Third-party libraries

// Application-internal dependencies
import ome.model.core.Pixels;
import ome.model.display.QuantumDef;
import omeis.providers.re.quantum.Quantization_8_16_bit;
import omeis.providers.re.quantum.QuantumFactory;
import omeis.providers.re.quantum.QuantumStrategy;

public class TestQuantumFactory extends QuantumFactory {

	private QuantumStrategy strategy;
	
    public TestQuantumFactory()
    {
    	super(null);
    }
    
    public void setStrategy(QuantumStrategy strategy)
    {
    	this.strategy = strategy;
    }

    public QuantumStrategy getStrategy(QuantumDef qd, Pixels pixels)
    {
    	if (strategy == null)
    	{
    		strategy = new Quantization_8_16_bit(qd, pixels);
    	}
    	return strategy;
    }

}
