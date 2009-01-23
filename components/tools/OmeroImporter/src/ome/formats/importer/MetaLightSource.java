package ome.formats.importer;

import Ice.Current;
import omero.RFloat;
import omero.RInt;
import omero.RLong;
import omero.RString;
import omero.model.Details;
import omero.model.IObject;
import omero.model.Instrument;
import omero.model.LightSource;


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
public class MetaLightSource extends LightSource
{
   
    public void copyData(LightSource lightSource)
    {
        lightSource.setManufacturer(manufacturer);
        lightSource.setModel(model);
        lightSource.setPower(power);
        lightSource.setSerialNumber(serialNumber);
    }

    public Instrument getInstrument(Current arg0)
    {
        return instrument;
    }

    public RString getManufacturer(Current arg0)
    {
        return manufacturer;
    }

    public RString getModel(Current arg0)
    {
        return model;
    }

    public RFloat getPower(Current arg0)
    {
        return power;
    }

    public RString getSerialNumber(Current arg0)
    {
        return serialNumber;
    }

    public RInt getVersion(Current arg0)
    {
        return version;
    }

    public void setInstrument(Instrument arg0, Current arg1)
    {
        instrument = arg0;
    }

    public void setManufacturer(RString arg0, Current arg1)
    {
        manufacturer = arg0;
    }

    public void setModel(RString arg0, Current arg1)
    {
        model = arg0;
    }

    public void setPower(RFloat arg0, Current arg1)
    {
        power = arg0;
    }

    public void setSerialNumber(RString arg0, Current arg1)
    {
        serialNumber = arg0;
    }

    public void setVersion(RInt arg0, Current arg1)
    {
        version = arg0;
    }

    public Details getDetails(Current arg0)
    {
        return details;
    }

    public RLong getId(Current arg0)
    {
        return id;
    }

    public boolean isAnnotated(Current arg0)
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean isGlobal(Current arg0)
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean isLink(Current arg0)
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean isLoaded(Current arg0)
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean isMutable(Current arg0)
    {
        // TODO Auto-generated method stub
        //return false;
        throw new RuntimeException("Not implemented yet.");
    }

    public IObject proxy(Current arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public void setId(RLong arg0, Current arg1)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public IObject shallowCopy(Current arg0)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public void unload(Current arg0)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void unloadCollections(Current arg0)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void unloadDetails(Current arg0)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }
    
}
