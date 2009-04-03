/*
 * org.openmicroscopy.ds.st.NullPixels
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.ds.st;


//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.dto.Attribute;
import org.openmicroscopy.ds.dto.Dataset;
import org.openmicroscopy.ds.dto.Feature;
import org.openmicroscopy.ds.dto.Image;
import org.openmicroscopy.ds.dto.ModuleExecution;
import org.openmicroscopy.ds.dto.SemanticType;

/** 
 * No-op implementation.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class NullPixels
        implements Pixels
{

    public Integer getSizeX() { return null; }
    public void setSizeX(Integer value) {}
    public Integer getSizeY() { return null; }
    public void setSizeY(Integer value) {}
    public Integer getSizeZ() { return null; }
    public void setSizeZ(Integer value) {}
    public Integer getSizeC() { return null; }
    public void setSizeC(Integer value) {}
    public Integer getSizeT() { return null; }
    public void setSizeT(Integer value) {}
    public String getPixelType() { return null; }
    public void setPixelType(String value) {}

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.st.Pixels#getFileSHA1()
     */
    public String getFileSHA1()
    {
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.st.Pixels#setFileSHA1(java.lang.String)
     */
    public void setFileSHA1(String value)
    {
        

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.st.Pixels#getRepository()
     */
    public Repository getRepository()
    {
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.st.Pixels#setRepository(org.openmicroscopy.ds.st.Repository)
     */
    public void setRepository(Repository value)
    {
        

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.st.Pixels#getImageServerID()
     */
    public Long getImageServerID()
    {
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.st.Pixels#setImageServerID(java.lang.Long)
     */
    public void setImageServerID(Long value)
    {
        

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.st.Pixels#getDisplayOptionses()
     */
    public List getDisplayOptionses()
    {
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.st.Pixels#countDisplayOptionses()
     */
    public int countDisplayOptionses()
    {
        
        return 0;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.st.Pixels#getPixelChannelComponents()
     */
    public List getPixelChannelComponents()
    {
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.st.Pixels#countPixelChannelComponents()
     */
    public int countPixelChannelComponents()
    {
        
        return 0;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.DataInterface#getDTOTypeName()
     */
    public String getDTOTypeName()
    {
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.DataInterface#getDTOType()
     */
    public Class getDTOType()
    {
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#getID()
     */
    public int getID()
    {
        
        return 0;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#getSemanticType()
     */
    public SemanticType getSemanticType()
    {
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#getDataset()
     */
    public Dataset getDataset()
    {
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#setDataset(org.openmicroscopy.ds.dto.Dataset)
     */
    public void setDataset(Dataset dataset)
    {
        

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#getImage()
     */
    public Image getImage()
    {
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#setImage(org.openmicroscopy.ds.dto.Image)
     */
    public void setImage(Image image)
    {
        

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#getFeature()
     */
    public Feature getFeature()
    {
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#setFeature(org.openmicroscopy.ds.dto.Feature)
     */
    public void setFeature(Feature feature)
    {
        

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#getModuleExecution()
     */
    public ModuleExecution getModuleExecution()
    {
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#setModuleExecution(org.openmicroscopy.ds.dto.ModuleExecution)
     */
    public void setModuleExecution(ModuleExecution mex)
    {
        

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#verifySemanticType(org.openmicroscopy.ds.dto.SemanticType)
     */
    public void verifySemanticType(SemanticType type)
    {
        

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#verifySemanticType(java.lang.String)
     */
    public void verifySemanticType(String typeName)
    {
        

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#getBooleanElement(java.lang.String)
     */
    public Boolean getBooleanElement(String element)
    {
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#setBooleanElement(java.lang.String, java.lang.Boolean)
     */
    public void setBooleanElement(String element, Boolean value)
    {
        

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#getShortElement(java.lang.String)
     */
    public Short getShortElement(String element)
    {
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#setShortElement(java.lang.String, java.lang.Short)
     */
    public void setShortElement(String element, Short value)
    {
        

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#getIntegerElement(java.lang.String)
     */
    public Integer getIntegerElement(String element)
    {
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#setIntegerElement(java.lang.String, java.lang.Integer)
     */
    public void setIntegerElement(String element, Integer value)
    {
        

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#getLongElement(java.lang.String)
     */
    public Long getLongElement(String element)
    {
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#setLongElement(java.lang.String, java.lang.Long)
     */
    public void setLongElement(String element, Long value)
    {
        

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#getFloatElement(java.lang.String)
     */
    public Float getFloatElement(String element)
    {
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#setFloatElement(java.lang.String, java.lang.Float)
     */
    public void setFloatElement(String element, Float value)
    {
        

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#getDoubleElement(java.lang.String)
     */
    public Double getDoubleElement(String element)
    {
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#setDoubleElement(java.lang.String, java.lang.Double)
     */
    public void setDoubleElement(String element, Double value)
    {
        

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#getStringElement(java.lang.String)
     */
    public String getStringElement(String element)
    {
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#setStringElement(java.lang.String, java.lang.String)
     */
    public void setStringElement(String element, String value)
    {
        

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#getAttributeElement(java.lang.String)
     */
    public Attribute getAttributeElement(String element)
    {
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.dto.Attribute#setAttributeElement(java.lang.String, org.openmicroscopy.ds.dto.Attribute)
     */
    public void setAttributeElement(String element, Attribute value)
    {
        

    }
    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.st.Pixels#getDisplayOptionsList()
     */
    public List getDisplayOptionsList()
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.st.Pixels#countDisplayOptionsList()
     */
    public int countDisplayOptionsList()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.st.Pixels#getPixelChannelComponentList()
     */
    public List getPixelChannelComponentList()
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.openmicroscopy.ds.st.Pixels#countPixelChannelComponentList()
     */
    public int countPixelChannelComponentList()
    {
        // TODO Auto-generated method stub
        return 0;
    }

}
