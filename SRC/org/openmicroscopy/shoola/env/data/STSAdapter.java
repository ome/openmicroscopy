/*
 * org.openmicroscopy.shoola.env.data.STSAdapter
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

package org.openmicroscopy.shoola.env.data;

import java.util.List;

import org.openmicroscopy.ds.DataFactory;
import org.openmicroscopy.ds.dto.Attribute;
import org.openmicroscopy.ds.dto.SemanticType;
import org.openmicroscopy.shoola.env.config.Registry;

//Java imports

//Third-party libraries

//Application-internal dependencies


/** 
 * NB: Temporary. DON'T code against it!
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @author <br>Jeff Mellen &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:jeffm@alum.mit.edu">
 *                  jeffm@alum.mit.edu</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class STSAdapter
	implements SemanticTypesService
{
    private DataFactory proxy;
    private Registry context;
    
    public STSAdapter(DataFactory proxy, Registry context)
    {
        if(proxy == null || context == null)
        {
            throw new IllegalArgumentException("INTERNAL ERROR: parameters" +
                "cannot be null in STSAdapter constructor.");
        }
        
        this.proxy = proxy;
        this.context = context;
    }
    
    /**
     * TODO write documentation
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#getAvailableGlobalTypes()
     */
    public List getAvailableGlobalTypes()
        throws DSOutOfServiceException, DSAccessException
    {
        // TODO flesh out this method
        return null;
    }
    
    /**
     * TODO write documentation
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#getAvailableProjectTypes()
     */
    public List getAvailableProjectTypes()
        throws DSOutOfServiceException, DSAccessException
    {
        // TODO flesh out this method
        return null;
    }
    
    /**
     * TODO write documentation
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#getAvailableDatasetTypes()
     */
    public List getAvailableDatasetTypes()
        throws DSOutOfServiceException, DSAccessException
    {
        // TODO flesh out this method
        return null;
    }
    
    /**
     * TODO write documentation
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#getAvailableImageTypes()
     */
    public List getAvailableImageTypes()
        throws DSOutOfServiceException, DSAccessException
    {
        // TODO flesh out this method
        return null;
    }
    
    /**
     * TODO write documentation
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#getAvailableFeatureTypes()
     */
    public List getAvailableFeatureTypes()
        throws DSOutOfServiceException, DSAccessException
    {
        // TODO flesh out this method
        return null;
    }

    /**
     * TODO write documentation
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#countProjectAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public int countProjectAttributes(SemanticType type, int projectID)
        throws DSOutOfServiceException, DSAccessException
    {
        // TODO flesh out this method
        return 0;
    }
    
    /**
     * TODO write documentation
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#countDatasetAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public int countDatasetAttributes(SemanticType type, int datasetID)
        throws DSOutOfServiceException, DSAccessException
    {
        // TODO flesh out this method
        return 0;
    }
    
    /**
     * TODO write documentation
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#countImageAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public int countImageAttributes(SemanticType type, int imageID)
        throws DSOutOfServiceException, DSAccessException
    {
        // TODO flesh out this method
        return 0;
    }
    
    /**
     * TODO write documentation
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#countFeatureAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public int countFeatureAttributes(SemanticType type, int featureID)
        throws DSOutOfServiceException, DSAccessException
    {
        // TODO flesh out this method
        return 0;
    }
    
    /**
     * TODO write documentation
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveProjectAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public List retrieveProjectAttributes(SemanticType type, int projectID)
        throws DSOutOfServiceException, DSAccessException
    {
        // TODO flesh out this method
        return null;
    }
    
    /**
     * TODO write documentation
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveDatasetAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public List retrieveDatasetAttributes(SemanticType type, int datasetID)
        throws DSOutOfServiceException, DSAccessException
    {
        // TODO flesh out this method
        return null;
    }
    
    /**
     * TODO write documentation
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveImageAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public List retrieveImageAttributes(SemanticType type, int imageID)
        throws DSOutOfServiceException, DSAccessException
    {
        // TODO flesh out this method
        return null;
    }
    
    /**
     * TODO write documentation
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveFeatureAttributes(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public List retrieveFeatureAttributes(SemanticType type, int featureID)
        throws DSOutOfServiceException, DSAccessException
    {
        // TODO flesh out this method
        return null;
    }

    /**
     * TODO write documentation
     * @see org.openmicroscopy.shoola.env.data.SemanticTypesService#retrieveAttribute(org.openmicroscopy.ds.dto.SemanticType, int)
     */
    public Attribute retrieveAttribute(SemanticType type, int attributeID)
        throws DSOutOfServiceException, DSAccessException
    {
        // TODO flesh out this method
        return null;
    }
}
