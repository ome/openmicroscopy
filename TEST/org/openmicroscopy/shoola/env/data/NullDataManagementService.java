/*
 * org.openmicroscopy.shoola.env.data.NullDataManagementService
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

//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.AnalysisChainData;
import org.openmicroscopy.shoola.env.data.model.AnalysisLinkData;
import org.openmicroscopy.shoola.env.data.model.AnalysisNodeData;
import org.openmicroscopy.shoola.env.data.model.ChainExecutionData;
import org.openmicroscopy.shoola.env.data.model.ChannelData;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.FormalInputData;
import org.openmicroscopy.shoola.env.data.model.FormalOutputData;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ModuleCategoryData;
import org.openmicroscopy.shoola.env.data.model.ModuleData;
import org.openmicroscopy.shoola.env.data.model.ModuleExecutionData;
import org.openmicroscopy.shoola.env.data.model.NodeExecutionData;
import org.openmicroscopy.shoola.env.data.model.PixelsDescription;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;
import org.openmicroscopy.shoola.env.data.model.SemanticTypeData;
import org.openmicroscopy.shoola.env.data.model.UserDetails;
import org.openmicroscopy.shoola.env.rnd.defs.RenderingDef;

/** 
 * 
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
public class NullDataManagementService
        implements DataManagementService
{


    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#getUserDetails()
     */
    public UserDetails getUserDetails()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#getSessionKey()
     */
    public String getSessionKey()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveUserProjects(org.openmicroscopy.shoola.env.data.model.ProjectSummary, org.openmicroscopy.shoola.env.data.model.DatasetSummary)
     */
    public List retrieveUserProjects(ProjectSummary pProto,
            DatasetSummary dProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveUserProjects()
     */
    public List retrieveUserProjects()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveUserProjectsWithDatasetData(org.openmicroscopy.shoola.env.data.model.ProjectSummary, org.openmicroscopy.shoola.env.data.model.DatasetData)
     */
    public List retrieveUserProjectsWithDatasetData(ProjectSummary pProto,
            DatasetData dProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveUserProjectsWithDatasetData()
     */
    public List retrieveUserProjectsWithDatasetData()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveUserDatasets(org.openmicroscopy.shoola.env.data.model.DatasetSummary)
     */
    public List retrieveUserDatasets(DatasetSummary dProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveUserDatasets()
     */
    public List retrieveUserDatasets()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveUserDatasets(org.openmicroscopy.shoola.env.data.model.DatasetData, org.openmicroscopy.shoola.env.data.model.ImageSummary)
     */
    public List retrieveUserDatasets(DatasetData dProto, ImageSummary iProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveUserImages(org.openmicroscopy.shoola.env.data.model.ImageSummary)
     */
    public List retrieveUserImages(ImageSummary iProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveUserImages()
     */
    public List retrieveUserImages()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveProject(int, org.openmicroscopy.shoola.env.data.model.ProjectData)
     */
    public ProjectData retrieveProject(int projectID, ProjectData retVal)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveProject(int)
     */
    public ProjectData retrieveProject(int projectID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveDataset(int, org.openmicroscopy.shoola.env.data.model.DatasetData)
     */
    public DatasetData retrieveDataset(int datasetID, DatasetData retVal)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveDataset(int)
     */
    public DatasetData retrieveDataset(int datasetID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImage(int, org.openmicroscopy.shoola.env.data.model.ImageData)
     */
    public ImageData retrieveImage(int id, ImageData retVal)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImage(int)
     */
    public ImageData retrieveImage(int id)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImages(int)
     */
    public List retrieveImages(int datasetID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveImages(int, org.openmicroscopy.shoola.env.data.model.ImageSummary)
     */
    public List retrieveImages(int datasetID, ImageSummary iProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrievePixels(int, int)
     */
    public PixelsDescription retrievePixels(int pixelsID, int imageID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveModules(org.openmicroscopy.shoola.env.data.model.ModuleData, org.openmicroscopy.shoola.env.data.model.ModuleCategoryData, org.openmicroscopy.shoola.env.data.model.FormalInputData, org.openmicroscopy.shoola.env.data.model.FormalOutputData, org.openmicroscopy.shoola.env.data.model.SemanticTypeData)
     */
    public List retrieveModules(ModuleData mProto, ModuleCategoryData mcProto,
            FormalInputData finProto, FormalOutputData foutProto,
            SemanticTypeData stProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveModuleCategories()
     */
    public List retrieveModuleCategories()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveModuleCategories(org.openmicroscopy.shoola.env.data.model.ModuleCategoryData, org.openmicroscopy.shoola.env.data.model.ModuleData)
     */
    public List retrieveModuleCategories(ModuleCategoryData mcProto,
            ModuleData mProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveModules()
     */
    public List retrieveModules()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveChains(org.openmicroscopy.shoola.env.data.model.AnalysisChainData, org.openmicroscopy.shoola.env.data.model.AnalysisLinkData, org.openmicroscopy.shoola.env.data.model.AnalysisNodeData, org.openmicroscopy.shoola.env.data.model.ModuleData, org.openmicroscopy.shoola.env.data.model.FormalInputData, org.openmicroscopy.shoola.env.data.model.FormalOutputData, org.openmicroscopy.shoola.env.data.model.SemanticTypeData)
     */
    public List retrieveChains(AnalysisChainData acProto,
            AnalysisLinkData alProto, AnalysisNodeData anProto,
            ModuleData modProto, FormalInputData finProto,
            FormalOutputData foutProto, SemanticTypeData stProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveChains()
     */
    public List retrieveChains()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveChainExecutions(org.openmicroscopy.shoola.env.data.model.ChainExecutionData, org.openmicroscopy.shoola.env.data.model.DatasetData, org.openmicroscopy.shoola.env.data.model.AnalysisChainData, org.openmicroscopy.shoola.env.data.model.NodeExecutionData, org.openmicroscopy.shoola.env.data.model.AnalysisNodeData, org.openmicroscopy.shoola.env.data.model.ModuleData, org.openmicroscopy.shoola.env.data.model.ModuleExecutionData)
     */
    public List retrieveChainExecutions(ChainExecutionData ceProto,
            DatasetData dsProto, AnalysisChainData acProto,
            NodeExecutionData neProto, AnalysisNodeData anProto,
            ModuleData mProto, ModuleExecutionData meProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveChainExecutions()
     */
    public List retrieveChainExecutions()
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#createProject(org.openmicroscopy.shoola.env.data.model.ProjectData, org.openmicroscopy.shoola.env.data.model.ProjectSummary)
     */
    public ProjectSummary createProject(ProjectData retVal,
            ProjectSummary pProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#createDataset(java.util.List, java.util.List, org.openmicroscopy.shoola.env.data.model.DatasetData, org.openmicroscopy.shoola.env.data.model.DatasetSummary)
     */
    public DatasetSummary createDataset(List projectSummaries,
            List imageSummaries, DatasetData retVal, DatasetSummary dProto)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#createProject(org.openmicroscopy.shoola.env.data.model.ProjectData)
     */
    public ProjectSummary createProject(ProjectData retVal)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#createDataset(java.util.List, java.util.List, org.openmicroscopy.shoola.env.data.model.DatasetData)
     */
    public DatasetSummary createDataset(List projectSummaries,
            List imageSummaries, DatasetData retVal)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#createAnalysisChain(org.openmicroscopy.shoola.env.data.model.AnalysisChainData)
     */
    public void createAnalysisChain(AnalysisChainData retVal)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#updateProject(org.openmicroscopy.shoola.env.data.model.ProjectData, java.util.List, java.util.List)
     */
    public void updateProject(ProjectData retVal, List dsToRemove, List dsToAdd)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#updateDataset(org.openmicroscopy.shoola.env.data.model.DatasetData, java.util.List, java.util.List)
     */
    public void updateDataset(DatasetData retVal, List isToRemove, List isToAdd)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#updateImage(org.openmicroscopy.shoola.env.data.model.DataObject)
     */
    public void updateImage(DataObject retVal)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#getChannelData(int)
     */
    public ChannelData[] getChannelData(int imageID)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#updateChannelData(org.openmicroscopy.shoola.env.data.model.ChannelData)
     */
    public void updateChannelData(ChannelData retVal)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#retrieveRenderingSettings(int, int, int)
     */
    public RenderingDef retrieveRenderingSettings(int pixelsID, int imageID,
            int pixelType)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.data.DataManagementService#saveRenderingSettings(int, int, org.openmicroscopy.shoola.env.rnd.defs.RenderingDef)
     */
    public void saveRenderingSettings(int pixelsID, int imageID,
            RenderingDef rDef)
            throws DSOutOfServiceException, DSAccessException
    {
        // TODO Auto-generated method stub

    }
     
    public AnalysisChainData retrieveChain(int id)
        throws DSOutOfServiceException, DSAccessException
    {
    		return null;
    }

    public AnalysisChainData retrieveChain(int id, AnalysisChainData acProto,
            AnalysisLinkData alProto, AnalysisNodeData anProto,
            ModuleData modProto, FormalInputData finProto, 
            FormalOutputData foutProto, SemanticTypeData stProto)
        throws DSOutOfServiceException, DSAccessException
    {
    		return null;
    }
 
}
