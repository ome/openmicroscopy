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
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.model.ActualInputData;
import org.openmicroscopy.shoola.env.data.model.AnalysisChainData;
import org.openmicroscopy.shoola.env.data.model.AnalysisLinkData;
import org.openmicroscopy.shoola.env.data.model.AnalysisNodeData;
import org.openmicroscopy.shoola.env.data.model.ChainExecutionData;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.DatasetSummaryLinked;
import org.openmicroscopy.shoola.env.data.model.FormalInputData;
import org.openmicroscopy.shoola.env.data.model.FormalOutputData;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ModuleCategoryData;
import org.openmicroscopy.shoola.env.data.model.ModuleData;
import org.openmicroscopy.shoola.env.data.model.ModuleExecutionData;
import org.openmicroscopy.shoola.env.data.model.NodeExecutionData;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;
import org.openmicroscopy.shoola.env.data.model.SemanticTypeData;
import org.openmicroscopy.shoola.env.data.model.UserDetails;

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

    public UserDetails getUserDetails()
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public String getSessionKey()
    {
        return null;
    }

    public List retrieveUserProjects(ProjectSummary pProto,
            DatasetSummary dProto)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveUserProjects()
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveUserProjectsWithDAnnotations()
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveUserProjectsWithDAnnotations(ProjectSummary pProto,
                                                    DatasetSummary dProto)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveUserProjectsWithDatasetData(ProjectSummary pProto,
                                                    DatasetData dProto)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveUserProjectsWithDatasetData()
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveUserDatasets(DatasetSummary dProto)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveUserDatasets()
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List fullRetrieveUserDatasets(DatasetData dProto,
                                        ImageSummary iProto)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveUserImages(ImageSummary iProto)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveUserImages()
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }
    
    public List retrieveImagesInUserGroup()
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImagesInUserGroup(ImageSummary iProto)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImagesInUserDatasets(List datasetIDs)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImagesInUserDatasets(List datasetIDs,
                                            ImageSummary iProto)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImagesInUserDatasets()
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }
    
    public List retrieveImagesInSystem()
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImagesInSystem(ImageSummary iProto)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public ProjectData retrieveProject(int projectID, ProjectData retVal)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public ProjectData retrieveProject(int projectID)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public DatasetData retrieveDataset(int datasetID, DatasetData retVal)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public DatasetData retrieveDataset(int datasetID)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public ImageData retrieveImage(int id, ImageData retVal)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public ImageData retrieveImage(int id)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImages(int datasetID)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImages(int datasetID, ImageSummary iProto)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImagesWithAnnotations(int datasetID)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImagesWithAnnotations(int datasetID,
                                            ImageSummary iProto)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveModules(ModuleData mProto, ModuleCategoryData mcProto,
                                FormalInputData finProto,
                                FormalOutputData foutProto,
                                SemanticTypeData stProto)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveModuleCategories()
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveModuleCategories(ModuleCategoryData mcProto)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }
    
    public List retrieveModules()
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveChains(AnalysisChainData acProto,
                        AnalysisLinkData alProto, AnalysisNodeData anProto,
                        ModuleData modProto, FormalInputData finProto,
                        FormalOutputData foutProto, SemanticTypeData stProto)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveChains()
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
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

    public List retrieveChainExecutions(ChainExecutionData ceProto,
                            DatasetData dsProto, AnalysisChainData acProto,
                            NodeExecutionData neProto, ModuleData mProto,
                            ModuleExecutionData meProto)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveChainExecutions()
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }
    
    public ProjectSummary createProject(ProjectData retVal,
                                        ProjectSummary pProto)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public DatasetSummary createDataset(List projectSummaries,
                                List imageSummaries, DatasetData retVal,
                                DatasetSummary dProto)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public ProjectSummary createProject(ProjectData retVal)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public DatasetSummary createDataset(List projectSummaries,
                                List imageSummaries, DatasetData retVal)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public void createAnalysisChain(AnalysisChainData retVal)
            throws DSOutOfServiceException, DSAccessException {}
    

    public void updateProject(ProjectData retVal, List dsToRemove,
                              List dsToAdd)
            throws DSOutOfServiceException, DSAccessException {}

    public void updateDataset(DatasetData retVal, List isToRemove, List isToAdd)
            throws DSOutOfServiceException, DSAccessException {}

    public void updateImage(DataObject retVal)
            throws DSOutOfServiceException, DSAccessException {}

    public List getMexExecutionHistory(int mexID, ModuleExecutionData mexData,
    			                    ModuleData modData, ActualInputData inpData, 
    			                    FormalInputData finData,
    			                    FormalOutputData foutData,
                                    SemanticTypeData stData) 
		throws DSOutOfServiceException, DSAccessException
	{
        return null;	
	}
    
    public List getMexExecutionHistory(int mexID) 
		throws DSOutOfServiceException, DSAccessException
	{
	   return null;	
	}

    public List getChainExecutionHistory(int mexID, ModuleExecutionData mexData,
    			                    ModuleData modData, ActualInputData inpData, 
    			                    FormalInputData finData,
    			                    FormalOutputData foutData,
                                    SemanticTypeData stData) 
		throws DSOutOfServiceException, DSAccessException
	{
	   return null;	
	}    

    public List getChainExecutionHistory(int mexID) 
		throws DSOutOfServiceException, DSAccessException 
	{
	   return null;	
	}

    public List retrieveUserImages(ImageSummary iProto, Map filters,
                                Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveUserImages(Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImagesInUserGroup(Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImagesInUserGroup(ImageSummary iProto, Map filters,
                                        Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImagesInUserDatasets(List datasetIDs,
                        ImageSummary iProto, Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImagesInUserDatasets(List datasetIDs, Map filters,
                                            Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImagesInUserDatasets(Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImagesInSystem(ImageSummary iProto, Map filters,
                                        Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveImagesInSystem(Map filters, Map complexFilters)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveProjectsTree(List projectIDs, boolean annotated)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public ProjectSummary retrieveProjectTree(int projectID, boolean annotated)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrieveDatasetsTree(List datasetIDs, boolean annotated)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public DatasetSummaryLinked retrieveDatasetTree(int datasetID, 
                                                    boolean annotated)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }

    public List retrievePDIHierarchy(List imageSummaries, boolean annotated)
            throws DSOutOfServiceException, DSAccessException
    {
        return null;
    }    
		
    
}
