/*
 * ome.formats.importer.gui.GuiCommonElements
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package ome.formats;

import java.util.HashMap;
import java.util.Map;

import loci.formats.meta.MetadataStore;

/**
 * Stores all Image names consumed by the interface.
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class ImageNameMetadataStore implements MetadataStore
{
    /**
     * The Map of Image index vs. Image name. This is map because of the
     * potential out of order population via metadata store usage.
     */ 
    private Map<Integer, String> imageNames = new HashMap<Integer, String>();

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageName(java.lang.String, int)
     */
    public void setImageName(String imageName, int imageIndex)
    {
        imageNames.put(imageIndex, imageName);
    }

    /**
     * Retrieves the current map of Image names held.
     * @return Map of Image index vs. Image name.
     */
    public Map<Integer, String> getImageNames()
    {
        return imageNames;
    }

    //
    // MetadataStore stubs follow
    //

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#createRoot()
     */
    public void createRoot()
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#getRoot()
     */
    public Object getRoot()
    {
        // Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setArcType(java.lang.String, int, int)
     */
    public void setArcType(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelComponentColorDomain(java.lang.String, int, int, int)
     */
    public void setChannelComponentColorDomain(String arg0, int arg1, int arg2,
            int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelComponentIndex(java.lang.Integer, int, int, int)
     */
    public void setChannelComponentIndex(Integer arg0, int arg1, int arg2,
            int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelComponentPixels(java.lang.String, int, int, int)
     */
    public void setChannelComponentPixels(String arg0, int arg1, int arg2,
            int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setCircleCx(java.lang.String, int, int, int)
     */
    public void setCircleCx(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setCircleCy(java.lang.String, int, int, int)
     */
    public void setCircleCy(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setCircleID(java.lang.String, int, int, int)
     */
    public void setCircleID(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setCircleR(java.lang.String, int, int, int)
     */
    public void setCircleR(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setCircleTransform(java.lang.String, int, int, int)
     */
    public void setCircleTransform(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setContactExperimenter(java.lang.String, int)
     */
    public void setContactExperimenter(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDatasetDescription(java.lang.String, int)
     */
    public void setDatasetDescription(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDatasetExperimenterRef(java.lang.String, int)
     */
    public void setDatasetExperimenterRef(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDatasetGroupRef(java.lang.String, int)
     */
    public void setDatasetGroupRef(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDatasetID(java.lang.String, int)
     */
    public void setDatasetID(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDatasetLocked(java.lang.Boolean, int)
     */
    public void setDatasetLocked(Boolean arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDatasetName(java.lang.String, int)
     */
    public void setDatasetName(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDatasetRefID(java.lang.String, int, int)
     */
    public void setDatasetRefID(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorAmplificationGain(java.lang.Double, int, int)
     */
    public void setDetectorAmplificationGain(Double arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorGain(java.lang.Double, int, int)
     */
    public void setDetectorGain(Double arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorID(java.lang.String, int, int)
     */
    public void setDetectorID(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorManufacturer(java.lang.String, int, int)
     */
    public void setDetectorManufacturer(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorModel(java.lang.String, int, int)
     */
    public void setDetectorModel(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorOffset(java.lang.Double, int, int)
     */
    public void setDetectorOffset(Double arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorSerialNumber(java.lang.String, int, int)
     */
    public void setDetectorSerialNumber(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorSettingsBinning(java.lang.String, int, int)
     */
    public void setDetectorSettingsBinning(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorSettingsDetector(java.lang.String, int, int)
     */
    public void setDetectorSettingsDetector(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorSettingsGain(java.lang.Double, int, int)
     */
    public void setDetectorSettingsGain(Double arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorSettingsOffset(java.lang.Double, int, int)
     */
    public void setDetectorSettingsOffset(Double arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorSettingsReadOutRate(java.lang.Double, int, int)
     */
    public void setDetectorSettingsReadOutRate(Double arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorSettingsVoltage(java.lang.Double, int, int)
     */
    public void setDetectorSettingsVoltage(Double arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorType(java.lang.String, int, int)
     */
    public void setDetectorType(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorVoltage(java.lang.Double, int, int)
     */
    public void setDetectorVoltage(Double arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorZoom(java.lang.Double, int, int)
     */
    public void setDetectorZoom(Double arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDichroicID(java.lang.String, int, int)
     */
    public void setDichroicID(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDichroicLotNumber(java.lang.String, int, int)
     */
    public void setDichroicLotNumber(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDichroicManufacturer(java.lang.String, int, int)
     */
    public void setDichroicManufacturer(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDichroicModel(java.lang.String, int, int)
     */
    public void setDichroicModel(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDimensionsPhysicalSizeX(java.lang.Double, int, int)
     */
    public void setDimensionsPhysicalSizeX(Double arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDimensionsPhysicalSizeY(java.lang.Double, int, int)
     */
    public void setDimensionsPhysicalSizeY(Double arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDimensionsPhysicalSizeZ(java.lang.Double, int, int)
     */
    public void setDimensionsPhysicalSizeZ(Double arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDimensionsTimeIncrement(java.lang.Double, int, int)
     */
    public void setDimensionsTimeIncrement(Double arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDimensionsWaveIncrement(java.lang.Integer, int, int)
     */
    public void setDimensionsWaveIncrement(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDimensionsWaveStart(java.lang.Integer, int, int)
     */
    public void setDimensionsWaveStart(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDisplayOptionsDisplay(java.lang.String, int)
     */
    public void setDisplayOptionsDisplay(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDisplayOptionsID(java.lang.String, int)
     */
    public void setDisplayOptionsID(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDisplayOptionsZoom(java.lang.Double, int)
     */
    public void setDisplayOptionsZoom(Double arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseCx(java.lang.String, int, int, int)
     */
    public void setEllipseCx(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseCy(java.lang.String, int, int, int)
     */
    public void setEllipseCy(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseID(java.lang.String, int, int, int)
     */
    public void setEllipseID(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseRx(java.lang.String, int, int, int)
     */
    public void setEllipseRx(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseRy(java.lang.String, int, int, int)
     */
    public void setEllipseRy(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseTransform(java.lang.String, int, int, int)
     */
    public void setEllipseTransform(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEmFilterLotNumber(java.lang.String, int, int)
     */
    public void setEmFilterLotNumber(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEmFilterManufacturer(java.lang.String, int, int)
     */
    public void setEmFilterManufacturer(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEmFilterModel(java.lang.String, int, int)
     */
    public void setEmFilterModel(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEmFilterType(java.lang.String, int, int)
     */
    public void setEmFilterType(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExFilterLotNumber(java.lang.String, int, int)
     */
    public void setExFilterLotNumber(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExFilterManufacturer(java.lang.String, int, int)
     */
    public void setExFilterManufacturer(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExFilterModel(java.lang.String, int, int)
     */
    public void setExFilterModel(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExFilterType(java.lang.String, int, int)
     */
    public void setExFilterType(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimentDescription(java.lang.String, int)
     */
    public void setExperimentDescription(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimentExperimenterRef(java.lang.String, int)
     */
    public void setExperimentExperimenterRef(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimentID(java.lang.String, int)
     */
    public void setExperimentID(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimentType(java.lang.String, int)
     */
    public void setExperimentType(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterEmail(java.lang.String, int)
     */
    public void setExperimenterEmail(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterFirstName(java.lang.String, int)
     */
    public void setExperimenterFirstName(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterID(java.lang.String, int)
     */
    public void setExperimenterID(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterInstitution(java.lang.String, int)
     */
    public void setExperimenterInstitution(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterLastName(java.lang.String, int)
     */
    public void setExperimenterLastName(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterMembershipGroup(java.lang.String, int, int)
     */
    public void setExperimenterMembershipGroup(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterOMEName(java.lang.String, int)
     */
    public void setExperimenterOMEName(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilamentType(java.lang.String, int, int)
     */
    public void setFilamentType(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterFilterWheel(java.lang.String, int, int)
     */
    public void setFilterFilterWheel(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterID(java.lang.String, int, int)
     */
    public void setFilterID(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterLotNumber(java.lang.String, int, int)
     */
    public void setFilterLotNumber(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterManufacturer(java.lang.String, int, int)
     */
    public void setFilterManufacturer(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterModel(java.lang.String, int, int)
     */
    public void setFilterModel(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSetDichroic(java.lang.String, int, int)
     */
    public void setFilterSetDichroic(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSetEmFilter(java.lang.String, int, int)
     */
    public void setFilterSetEmFilter(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSetExFilter(java.lang.String, int, int)
     */
    public void setFilterSetExFilter(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSetID(java.lang.String, int, int)
     */
    public void setFilterSetID(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSetLotNumber(java.lang.String, int, int)
     */
    public void setFilterSetLotNumber(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSetManufacturer(java.lang.String, int, int)
     */
    public void setFilterSetManufacturer(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSetModel(java.lang.String, int, int)
     */
    public void setFilterSetModel(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterType(java.lang.String, int, int)
     */
    public void setFilterType(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setGroupID(java.lang.String, int)
     */
    public void setGroupID(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setGroupName(java.lang.String, int)
     */
    public void setGroupName(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageAcquiredPixels(java.lang.String, int)
     */
    public void setImageAcquiredPixels(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageCreationDate(java.lang.String, int)
     */
    public void setImageCreationDate(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageDefaultPixels(java.lang.String, int)
     */
    public void setImageDefaultPixels(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageDescription(java.lang.String, int)
     */
    public void setImageDescription(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageExperimentRef(java.lang.String, int)
     */
    public void setImageExperimentRef(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageExperimenterRef(java.lang.String, int)
     */
    public void setImageExperimenterRef(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageGroupRef(java.lang.String, int)
     */
    public void setImageGroupRef(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageID(java.lang.String, int)
     */
    public void setImageID(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageInstrumentRef(java.lang.String, int)
     */
    public void setImageInstrumentRef(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImagingEnvironmentAirPressure(java.lang.Double, int)
     */
    public void setImagingEnvironmentAirPressure(Double arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImagingEnvironmentCO2Percent(java.lang.Double, int)
     */
    public void setImagingEnvironmentCO2Percent(Double arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImagingEnvironmentHumidity(java.lang.Double, int)
     */
    public void setImagingEnvironmentHumidity(Double arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImagingEnvironmentTemperature(java.lang.Double, int)
     */
    public void setImagingEnvironmentTemperature(Double arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setInstrumentID(java.lang.String, int)
     */
    public void setInstrumentID(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserFrequencyMultiplication(java.lang.Integer, int, int)
     */
    public void setLaserFrequencyMultiplication(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserLaserMedium(java.lang.String, int, int)
     */
    public void setLaserLaserMedium(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserPockelCell(java.lang.Boolean, int, int)
     */
    public void setLaserPockelCell(Boolean arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserPulse(java.lang.String, int, int)
     */
    public void setLaserPulse(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserRepetitionRate(java.lang.Double, int, int)
     */
    public void setLaserRepetitionRate(Double arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserTuneable(java.lang.Boolean, int, int)
     */
    public void setLaserTuneable(Boolean arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserType(java.lang.String, int, int)
     */
    public void setLaserType(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserWavelength(java.lang.Integer, int, int)
     */
    public void setLaserWavelength(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightSourceID(java.lang.String, int, int)
     */
    public void setLightSourceID(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightSourceManufacturer(java.lang.String, int, int)
     */
    public void setLightSourceManufacturer(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightSourceModel(java.lang.String, int, int)
     */
    public void setLightSourceModel(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightSourcePower(java.lang.Double, int, int)
     */
    public void setLightSourcePower(Double arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightSourceRefAttenuation(java.lang.Double, int, int, int)
     */
    public void setLightSourceRefAttenuation(Double arg0, int arg1, int arg2,
            int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightSourceRefLightSource(java.lang.String, int, int, int)
     */
    public void setLightSourceRefLightSource(String arg0, int arg1, int arg2,
            int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightSourceRefWavelength(java.lang.Integer, int, int, int)
     */
    public void setLightSourceRefWavelength(Integer arg0, int arg1, int arg2,
            int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightSourceSerialNumber(java.lang.String, int, int)
     */
    public void setLightSourceSerialNumber(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightSourceSettingsAttenuation(java.lang.Double, int, int)
     */
    public void setLightSourceSettingsAttenuation(Double arg0, int arg1,
            int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightSourceSettingsLightSource(java.lang.String, int, int)
     */
    public void setLightSourceSettingsLightSource(String arg0, int arg1,
            int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightSourceSettingsWavelength(java.lang.Integer, int, int)
     */
    public void setLightSourceSettingsWavelength(Integer arg0, int arg1,
            int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineID(java.lang.String, int, int, int)
     */
    public void setLineID(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineTransform(java.lang.String, int, int, int)
     */
    public void setLineTransform(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineX1(java.lang.String, int, int, int)
     */
    public void setLineX1(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineX2(java.lang.String, int, int, int)
     */
    public void setLineX2(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineY1(java.lang.String, int, int, int)
     */
    public void setLineY1(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineY2(java.lang.String, int, int, int)
     */
    public void setLineY2(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLogicalChannelContrastMethod(java.lang.String, int, int)
     */
    public void setLogicalChannelContrastMethod(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLogicalChannelDetector(java.lang.String, int, int)
     */
    public void setLogicalChannelDetector(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLogicalChannelEmWave(java.lang.Integer, int, int)
     */
    public void setLogicalChannelEmWave(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLogicalChannelExWave(java.lang.Integer, int, int)
     */
    public void setLogicalChannelExWave(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLogicalChannelFilterSet(java.lang.String, int, int)
     */
    public void setLogicalChannelFilterSet(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLogicalChannelFluor(java.lang.String, int, int)
     */
    public void setLogicalChannelFluor(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLogicalChannelID(java.lang.String, int, int)
     */
    public void setLogicalChannelID(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLogicalChannelIlluminationType(java.lang.String, int, int)
     */
    public void setLogicalChannelIlluminationType(String arg0, int arg1,
            int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLogicalChannelLightSource(java.lang.String, int, int)
     */
    public void setLogicalChannelLightSource(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLogicalChannelMode(java.lang.String, int, int)
     */
    public void setLogicalChannelMode(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLogicalChannelName(java.lang.String, int, int)
     */
    public void setLogicalChannelName(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLogicalChannelNdFilter(java.lang.Double, int, int)
     */
    public void setLogicalChannelNdFilter(Double arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLogicalChannelOTF(java.lang.String, int, int)
     */
    public void setLogicalChannelOTF(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLogicalChannelPhotometricInterpretation(java.lang.String, int, int)
     */
    public void setLogicalChannelPhotometricInterpretation(String arg0,
            int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLogicalChannelPinholeSize(java.lang.Double, int, int)
     */
    public void setLogicalChannelPinholeSize(Double arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLogicalChannelPockelCellSetting(java.lang.Integer, int, int)
     */
    public void setLogicalChannelPockelCellSetting(Integer arg0, int arg1,
            int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLogicalChannelSamplesPerPixel(java.lang.Integer, int, int)
     */
    public void setLogicalChannelSamplesPerPixel(Integer arg0, int arg1,
            int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLogicalChannelSecondaryEmissionFilter(java.lang.String, int, int)
     */
    public void setLogicalChannelSecondaryEmissionFilter(String arg0, int arg1,
            int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLogicalChannelSecondaryExcitationFilter(java.lang.String, int, int)
     */
    public void setLogicalChannelSecondaryExcitationFilter(String arg0,
            int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskHeight(java.lang.String, int, int, int)
     */
    public void setMaskHeight(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskID(java.lang.String, int, int, int)
     */
    public void setMaskID(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskPixelsBigEndian(java.lang.Boolean, int, int, int)
     */
    public void setMaskPixelsBigEndian(Boolean arg0, int arg1, int arg2,
            int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskPixelsBinData(byte[], int, int, int)
     */
    public void setMaskPixelsBinData(byte[] arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskPixelsExtendedPixelType(java.lang.String, int, int, int)
     */
    public void setMaskPixelsExtendedPixelType(String arg0, int arg1, int arg2,
            int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskPixelsID(java.lang.String, int, int, int)
     */
    public void setMaskPixelsID(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskPixelsSizeX(java.lang.Integer, int, int, int)
     */
    public void setMaskPixelsSizeX(Integer arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskPixelsSizeY(java.lang.Integer, int, int, int)
     */
    public void setMaskPixelsSizeY(Integer arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskTransform(java.lang.String, int, int, int)
     */
    public void setMaskTransform(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskWidth(java.lang.String, int, int, int)
     */
    public void setMaskWidth(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskX(java.lang.String, int, int, int)
     */
    public void setMaskX(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskY(java.lang.String, int, int, int)
     */
    public void setMaskY(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicrobeamManipulationExperimenterRef(java.lang.String, int, int)
     */
    public void setMicrobeamManipulationExperimenterRef(String arg0, int arg1,
            int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicrobeamManipulationID(java.lang.String, int, int)
     */
    public void setMicrobeamManipulationID(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicrobeamManipulationRefID(java.lang.String, int, int)
     */
    public void setMicrobeamManipulationRefID(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicrobeamManipulationType(java.lang.String, int, int)
     */
    public void setMicrobeamManipulationType(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicroscopeID(java.lang.String, int)
     */
    public void setMicroscopeID(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicroscopeManufacturer(java.lang.String, int)
     */
    public void setMicroscopeManufacturer(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicroscopeModel(java.lang.String, int)
     */
    public void setMicroscopeModel(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicroscopeSerialNumber(java.lang.String, int)
     */
    public void setMicroscopeSerialNumber(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicroscopeType(java.lang.String, int)
     */
    public void setMicroscopeType(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFBinaryFile(java.lang.String, int, int)
     */
    public void setOTFBinaryFile(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFID(java.lang.String, int, int)
     */
    public void setOTFID(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFObjective(java.lang.String, int, int)
     */
    public void setOTFObjective(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFOpticalAxisAveraged(java.lang.Boolean, int, int)
     */
    public void setOTFOpticalAxisAveraged(Boolean arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFPixelType(java.lang.String, int, int)
     */
    public void setOTFPixelType(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFSizeX(java.lang.Integer, int, int)
     */
    public void setOTFSizeX(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFSizeY(java.lang.Integer, int, int)
     */
    public void setOTFSizeY(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveCalibratedMagnification(java.lang.Double, int, int)
     */
    public void setObjectiveCalibratedMagnification(Double arg0, int arg1,
            int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveCorrection(java.lang.String, int, int)
     */
    public void setObjectiveCorrection(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveID(java.lang.String, int, int)
     */
    public void setObjectiveID(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveImmersion(java.lang.String, int, int)
     */
    public void setObjectiveImmersion(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveIris(java.lang.Boolean, int, int)
     */
    public void setObjectiveIris(Boolean arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveLensNA(java.lang.Double, int, int)
     */
    public void setObjectiveLensNA(Double arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveManufacturer(java.lang.String, int, int)
     */
    public void setObjectiveManufacturer(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveModel(java.lang.String, int, int)
     */
    public void setObjectiveModel(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveNominalMagnification(java.lang.Integer, int, int)
     */
    public void setObjectiveNominalMagnification(Integer arg0, int arg1,
            int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveSerialNumber(java.lang.String, int, int)
     */
    public void setObjectiveSerialNumber(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveSettingsCorrectionCollar(java.lang.Double, int)
     */
    public void setObjectiveSettingsCorrectionCollar(Double arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveSettingsMedium(java.lang.String, int)
     */
    public void setObjectiveSettingsMedium(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveSettingsObjective(java.lang.String, int)
     */
    public void setObjectiveSettingsObjective(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveSettingsRefractiveIndex(java.lang.Double, int)
     */
    public void setObjectiveSettingsRefractiveIndex(Double arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveWorkingDistance(java.lang.Double, int, int)
     */
    public void setObjectiveWorkingDistance(Double arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathD(java.lang.String, int, int, int)
     */
    public void setPathD(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathID(java.lang.String, int, int, int)
     */
    public void setPathID(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsBigEndian(java.lang.Boolean, int, int)
     */
    public void setPixelsBigEndian(Boolean arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsDimensionOrder(java.lang.String, int, int)
     */
    public void setPixelsDimensionOrder(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsID(java.lang.String, int, int)
     */
    public void setPixelsID(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsPixelType(java.lang.String, int, int)
     */
    public void setPixelsPixelType(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsSizeC(java.lang.Integer, int, int)
     */
    public void setPixelsSizeC(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsSizeT(java.lang.Integer, int, int)
     */
    public void setPixelsSizeT(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsSizeX(java.lang.Integer, int, int)
     */
    public void setPixelsSizeX(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsSizeY(java.lang.Integer, int, int)
     */
    public void setPixelsSizeY(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsSizeZ(java.lang.Integer, int, int)
     */
    public void setPixelsSizeZ(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlaneHashSHA1(java.lang.String, int, int, int)
     */
    public void setPlaneHashSHA1(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlaneID(java.lang.String, int, int, int)
     */
    public void setPlaneID(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlaneTheC(java.lang.Integer, int, int, int)
     */
    public void setPlaneTheC(Integer arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlaneTheT(java.lang.Integer, int, int, int)
     */
    public void setPlaneTheT(Integer arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlaneTheZ(java.lang.Integer, int, int, int)
     */
    public void setPlaneTheZ(Integer arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlaneTimingDeltaT(java.lang.Double, int, int, int)
     */
    public void setPlaneTimingDeltaT(Double arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlaneTimingExposureTime(java.lang.Double, int, int, int)
     */
    public void setPlaneTimingExposureTime(Double arg0, int arg1, int arg2,
            int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateColumnNamingConvention(java.lang.String, int)
     */
    public void setPlateColumnNamingConvention(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateDescription(java.lang.String, int)
     */
    public void setPlateDescription(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateExternalIdentifier(java.lang.String, int)
     */
    public void setPlateExternalIdentifier(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateID(java.lang.String, int)
     */
    public void setPlateID(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateName(java.lang.String, int)
     */
    public void setPlateName(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateRefID(java.lang.String, int, int)
     */
    public void setPlateRefID(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateRefSample(java.lang.Integer, int, int)
     */
    public void setPlateRefSample(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateRefWell(java.lang.String, int, int)
     */
    public void setPlateRefWell(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateRowNamingConvention(java.lang.String, int)
     */
    public void setPlateRowNamingConvention(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateStatus(java.lang.String, int)
     */
    public void setPlateStatus(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateWellOriginX(java.lang.Double, int)
     */
    public void setPlateWellOriginX(Double arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateWellOriginY(java.lang.Double, int)
     */
    public void setPlateWellOriginY(Double arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointCx(java.lang.String, int, int, int)
     */
    public void setPointCx(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointCy(java.lang.String, int, int, int)
     */
    public void setPointCy(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointID(java.lang.String, int, int, int)
     */
    public void setPointID(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointR(java.lang.String, int, int, int)
     */
    public void setPointR(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointTransform(java.lang.String, int, int, int)
     */
    public void setPointTransform(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolygonID(java.lang.String, int, int, int)
     */
    public void setPolygonID(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolygonPoints(java.lang.String, int, int, int)
     */
    public void setPolygonPoints(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolygonTransform(java.lang.String, int, int, int)
     */
    public void setPolygonTransform(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineID(java.lang.String, int, int, int)
     */
    public void setPolylineID(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylinePoints(java.lang.String, int, int, int)
     */
    public void setPolylinePoints(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineTransform(java.lang.String, int, int, int)
     */
    public void setPolylineTransform(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setProjectDescription(java.lang.String, int)
     */
    public void setProjectDescription(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setProjectExperimenterRef(java.lang.String, int)
     */
    public void setProjectExperimenterRef(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setProjectGroupRef(java.lang.String, int)
     */
    public void setProjectGroupRef(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setProjectID(java.lang.String, int)
     */
    public void setProjectID(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setProjectName(java.lang.String, int)
     */
    public void setProjectName(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setProjectRefID(java.lang.String, int, int)
     */
    public void setProjectRefID(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPumpLightSource(java.lang.String, int, int)
     */
    public void setPumpLightSource(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setROIID(java.lang.String, int, int)
     */
    public void setROIID(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setROIRefID(java.lang.String, int, int, int)
     */
    public void setROIRefID(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setROIT0(java.lang.Integer, int, int)
     */
    public void setROIT0(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setROIT1(java.lang.Integer, int, int)
     */
    public void setROIT1(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setROIX0(java.lang.Integer, int, int)
     */
    public void setROIX0(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setROIX1(java.lang.Integer, int, int)
     */
    public void setROIX1(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setROIY0(java.lang.Integer, int, int)
     */
    public void setROIY0(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setROIY1(java.lang.Integer, int, int)
     */
    public void setROIY1(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setROIZ0(java.lang.Integer, int, int)
     */
    public void setROIZ0(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setROIZ1(java.lang.Integer, int, int)
     */
    public void setROIZ1(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setReagentDescription(java.lang.String, int, int)
     */
    public void setReagentDescription(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setReagentID(java.lang.String, int, int)
     */
    public void setReagentID(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setReagentName(java.lang.String, int, int)
     */
    public void setReagentName(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setReagentReagentIdentifier(java.lang.String, int, int)
     */
    public void setReagentReagentIdentifier(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectHeight(java.lang.String, int, int, int)
     */
    public void setRectHeight(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectID(java.lang.String, int, int, int)
     */
    public void setRectID(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectTransform(java.lang.String, int, int, int)
     */
    public void setRectTransform(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectWidth(java.lang.String, int, int, int)
     */
    public void setRectWidth(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectX(java.lang.String, int, int, int)
     */
    public void setRectX(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectY(java.lang.String, int, int, int)
     */
    public void setRectY(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRegionID(java.lang.String, int, int)
     */
    public void setRegionID(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRegionName(java.lang.String, int, int)
     */
    public void setRegionName(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRegionTag(java.lang.String, int, int)
     */
    public void setRegionTag(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRoiLinkDirection(java.lang.String, int, int, int)
     */
    public void setRoiLinkDirection(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRoiLinkName(java.lang.String, int, int, int)
     */
    public void setRoiLinkName(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRoiLinkRef(java.lang.String, int, int, int)
     */
    public void setRoiLinkRef(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRoot(java.lang.Object)
     */
    public void setRoot(Object arg0)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenAcquisitionEndTime(java.lang.String, int, int)
     */
    public void setScreenAcquisitionEndTime(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenAcquisitionID(java.lang.String, int, int)
     */
    public void setScreenAcquisitionID(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenAcquisitionStartTime(java.lang.String, int, int)
     */
    public void setScreenAcquisitionStartTime(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenDescription(java.lang.String, int)
     */
    public void setScreenDescription(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenExtern(java.lang.String, int)
     */
    public void setScreenExtern(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenID(java.lang.String, int)
     */
    public void setScreenID(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenName(java.lang.String, int)
     */
    public void setScreenName(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenProtocolDescription(java.lang.String, int)
     */
    public void setScreenProtocolDescription(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenProtocolIdentifier(java.lang.String, int)
     */
    public void setScreenProtocolIdentifier(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenReagentSetDescription(java.lang.String, int)
     */
    public void setScreenReagentSetDescription(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenReagentSetIdentifier(java.lang.String, int)
     */
    public void setScreenReagentSetIdentifier(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenRefID(java.lang.String, int, int)
     */
    public void setScreenRefID(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenType(java.lang.String, int)
     */
    public void setScreenType(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeBaselineShift(java.lang.String, int, int, int)
     */
    public void setShapeBaselineShift(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeDirection(java.lang.String, int, int, int)
     */
    public void setShapeDirection(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeFillColor(java.lang.String, int, int, int)
     */
    public void setShapeFillColor(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeFillOpacity(java.lang.String, int, int, int)
     */
    public void setShapeFillOpacity(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeFillRule(java.lang.String, int, int, int)
     */
    public void setShapeFillRule(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeFontFamily(java.lang.String, int, int, int)
     */
    public void setShapeFontFamily(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeFontSize(java.lang.Integer, int, int, int)
     */
    public void setShapeFontSize(Integer arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeFontStretch(java.lang.String, int, int, int)
     */
    public void setShapeFontStretch(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeFontStyle(java.lang.String, int, int, int)
     */
    public void setShapeFontStyle(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeFontVariant(java.lang.String, int, int, int)
     */
    public void setShapeFontVariant(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeFontWeight(java.lang.String, int, int, int)
     */
    public void setShapeFontWeight(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeG(java.lang.String, int, int, int)
     */
    public void setShapeG(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeGlyphOrientationVertical(java.lang.Integer, int, int, int)
     */
    public void setShapeGlyphOrientationVertical(Integer arg0, int arg1,
            int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeID(java.lang.String, int, int, int)
     */
    public void setShapeID(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeLocked(java.lang.Boolean, int, int, int)
     */
    public void setShapeLocked(Boolean arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeStrokeAttribute(java.lang.String, int, int, int)
     */
    public void setShapeStrokeAttribute(String arg0, int arg1, int arg2,
            int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeStrokeColor(java.lang.String, int, int, int)
     */
    public void setShapeStrokeColor(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeStrokeDashArray(java.lang.String, int, int, int)
     */
    public void setShapeStrokeDashArray(String arg0, int arg1, int arg2,
            int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeStrokeLineCap(java.lang.String, int, int, int)
     */
    public void setShapeStrokeLineCap(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeStrokeLineJoin(java.lang.String, int, int, int)
     */
    public void setShapeStrokeLineJoin(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeStrokeMiterLimit(java.lang.Integer, int, int, int)
     */
    public void setShapeStrokeMiterLimit(Integer arg0, int arg1, int arg2,
            int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeStrokeOpacity(java.lang.Double, int, int, int)
     */
    public void setShapeStrokeOpacity(Double arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeStrokeWidth(java.lang.Integer, int, int, int)
     */
    public void setShapeStrokeWidth(Integer arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeText(java.lang.String, int, int, int)
     */
    public void setShapeText(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeTextAnchor(java.lang.String, int, int, int)
     */
    public void setShapeTextAnchor(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeTextDecoration(java.lang.String, int, int, int)
     */
    public void setShapeTextDecoration(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeTextFill(java.lang.String, int, int, int)
     */
    public void setShapeTextFill(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeTextStroke(java.lang.String, int, int, int)
     */
    public void setShapeTextStroke(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeTheT(java.lang.Integer, int, int, int)
     */
    public void setShapeTheT(Integer arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeTheZ(java.lang.Integer, int, int, int)
     */
    public void setShapeTheZ(Integer arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeVectorEffect(java.lang.String, int, int, int)
     */
    public void setShapeVectorEffect(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeVisibility(java.lang.Boolean, int, int, int)
     */
    public void setShapeVisibility(Boolean arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setShapeWritingMode(java.lang.String, int, int, int)
     */
    public void setShapeWritingMode(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setStageLabelName(java.lang.String, int)
     */
    public void setStageLabelName(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setStageLabelX(java.lang.Double, int)
     */
    public void setStageLabelX(Double arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setStageLabelY(java.lang.Double, int)
     */
    public void setStageLabelY(Double arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setStageLabelZ(java.lang.Double, int)
     */
    public void setStageLabelZ(Double arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setStagePositionPositionX(java.lang.Double, int, int, int)
     */
    public void setStagePositionPositionX(Double arg0, int arg1, int arg2,
            int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setStagePositionPositionY(java.lang.Double, int, int, int)
     */
    public void setStagePositionPositionY(Double arg0, int arg1, int arg2,
            int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setStagePositionPositionZ(java.lang.Double, int, int, int)
     */
    public void setStagePositionPositionZ(Double arg0, int arg1, int arg2,
            int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setThumbnailHref(java.lang.String, int)
     */
    public void setThumbnailHref(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setThumbnailID(java.lang.String, int)
     */
    public void setThumbnailID(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setThumbnailMIMEtype(java.lang.String, int)
     */
    public void setThumbnailMIMEtype(String arg0, int arg1)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTiffDataFileName(java.lang.String, int, int, int)
     */
    public void setTiffDataFileName(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTiffDataFirstC(java.lang.Integer, int, int, int)
     */
    public void setTiffDataFirstC(Integer arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTiffDataFirstT(java.lang.Integer, int, int, int)
     */
    public void setTiffDataFirstT(Integer arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTiffDataFirstZ(java.lang.Integer, int, int, int)
     */
    public void setTiffDataFirstZ(Integer arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTiffDataIFD(java.lang.Integer, int, int, int)
     */
    public void setTiffDataIFD(Integer arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTiffDataNumPlanes(java.lang.Integer, int, int, int)
     */
    public void setTiffDataNumPlanes(Integer arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTiffDataUUID(java.lang.String, int, int, int)
     */
    public void setTiffDataUUID(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTransmittanceRangeCutIn(java.lang.Integer, int, int)
     */
    public void setTransmittanceRangeCutIn(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTransmittanceRangeCutInTolerance(java.lang.Integer, int, int)
     */
    public void setTransmittanceRangeCutInTolerance(Integer arg0, int arg1,
            int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTransmittanceRangeCutOut(java.lang.Integer, int, int)
     */
    public void setTransmittanceRangeCutOut(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTransmittanceRangeCutOutTolerance(java.lang.Integer, int, int)
     */
    public void setTransmittanceRangeCutOutTolerance(Integer arg0, int arg1,
            int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTransmittanceRangeTransmittance(java.lang.Integer, int, int)
     */
    public void setTransmittanceRangeTransmittance(Integer arg0, int arg1,
            int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setUUID(java.lang.String)
     */
    public void setUUID(String arg0)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellColumn(java.lang.Integer, int, int)
     */
    public void setWellColumn(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellExternalDescription(java.lang.String, int, int)
     */
    public void setWellExternalDescription(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellExternalIdentifier(java.lang.String, int, int)
     */
    public void setWellExternalIdentifier(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellID(java.lang.String, int, int)
     */
    public void setWellID(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellReagent(java.lang.String, int, int)
     */
    public void setWellReagent(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellRow(java.lang.Integer, int, int)
     */
    public void setWellRow(Integer arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellSampleID(java.lang.String, int, int, int)
     */
    public void setWellSampleID(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellSampleImageRef(java.lang.String, int, int, int)
     */
    public void setWellSampleImageRef(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellSampleIndex(java.lang.Integer, int, int, int)
     */
    public void setWellSampleIndex(Integer arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellSamplePosX(java.lang.Double, int, int, int)
     */
    public void setWellSamplePosX(Double arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellSamplePosY(java.lang.Double, int, int, int)
     */
    public void setWellSamplePosY(Double arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellSampleRefID(java.lang.String, int, int, int)
     */
    public void setWellSampleRefID(String arg0, int arg1, int arg2, int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellSampleTimepoint(java.lang.Integer, int, int, int)
     */
    public void setWellSampleTimepoint(Integer arg0, int arg1, int arg2,
            int arg3)
    {
        // Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellType(java.lang.String, int, int)
     */
    public void setWellType(String arg0, int arg1, int arg2)
    {
        // Auto-generated method stub
        
    }

}
