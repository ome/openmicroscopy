package ome.formats.importer;

import ome.model.acquisition.LightSource;


/**
 * Since the LightSource class is abstract, MetaLightSource is used 
 * as a place holder for any sets  from bio-formats before we know 
 * specifically what kind of light source we are dealing with 
 * (laser, arc, or filament). Once any of the concrete methods are 
 * called (for example, setLaserWavelength()), any values stored in
 * this temporary object will be copied into the new concrete object.
 * 
 * @author Brian W. Loranger
 *
 */
public class MetaLightSource extends ome.model.acquisition.LightSource
{
   
    public void copyData(LightSource lightSource)
    {
        lightSource.setManufacturer(manufacturer);
        lightSource.setModel(model);
        lightSource.setPower(power);
        lightSource.setSerialNumber(serialNumber);
    }
    
}
