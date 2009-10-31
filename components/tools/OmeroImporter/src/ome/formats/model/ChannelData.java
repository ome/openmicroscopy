/*
 * ome.formats.model.ChannelData
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

package ome.formats.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ome.formats.OMEROMetadataStoreClient;
import ome.util.LSID;
import omero.metadatastore.IObjectContainer;
import omero.model.Channel;
import omero.model.Filter;
import omero.model.FilterSet;
import omero.model.IObject;
import omero.model.LightSettings;
import omero.model.LightSource;
import omero.model.LogicalChannel;

/**
 * Represents all the metadata required to make accurate decisions about
 * channel colour and name.
 * 
 * @author Chris Allan <callan at blackcat dot ca>
 * @author Jean-Marie <jburel at dundee dot ac dot uk>
 */
public class ChannelData
{
	/** Base channel data. */
	private Channel channel;
	
	/** Index of the channel for the image. */
	private Integer channelIndex;
	
	/** Channel --> LogicalChannel */
	private LogicalChannel logicalChannel;
	
	/** ... LogicalChannel --> Filterset */
	private FilterSet filterSet;
	
	/** ... LogicalChannel --> FilterSet --> Filter (Em) */
	private Filter filterSetEmFilter;

	/** ... LogicalChannel --> FilterSet --> Filter (Ex) */
	private Filter filterSetExFilter;
	
	/** ... LogicalChannel --> Filter (SecondaryEm) */
	private Filter secondaryEmFilter;
	
	/** ... LogicalChannel --> Filter (SecondaryEx) */
	private Filter secondaryExFilter;
	
	/** ... LogicalChannel --> LightSettings */
	private LightSettings lightSourceSettings;
	
	/** ... LogicalChannel --> LightSettings --> LightSource */
	private LightSource lightSource;
	
	/**
	 * Retrieves channel data from an object container store.
	 * @param store Store to retrieve the channel data from.
	 * @param imageIndex Index of the image to retrieve channel data for.
	 * @param channelIndex Index of the channel to retrieve channel data for.
	 * @return Populated channel data for the specified channel.
	 */
	public static ChannelData fromObjectContainerStore(
			IObjectContainerStore store, int imageIndex, int channelIndex)
	{
		Map<LSID, List<LSID>> referenceCache = store.getReferenceCache();
		ChannelData data = new ChannelData();
		
		// Channel
		data.channel = (Channel) store.getSourceObject(
				new LSID(Channel.class, imageIndex, channelIndex));
		data.channelIndex = channelIndex;
		if (data.channel == null)
		{
			// Channel is missing, create it.
			LinkedHashMap<String, Integer> indexes = 
				new LinkedHashMap<String, Integer>();
			indexes.put("imageIndex", imageIndex);
			indexes.put("logicalChannelIndex", channelIndex);
			IObjectContainer container =
				store.getIObjectContainer(Channel.class, indexes);
			data.channel = (Channel) container.sourceObject;
		}
		// Channel --> LogicalChannel
		LSID logicalChannelLSID = 
			new LSID(LogicalChannel.class, imageIndex, channelIndex); 
		data.logicalChannel = 
			(LogicalChannel) store.getSourceObject(logicalChannelLSID);
		if (data.logicalChannel == null)
		{
			// Channel is missing, create it.
			LinkedHashMap<String, Integer> indexes = 
				new LinkedHashMap<String, Integer>();
			indexes.put("imageIndex", imageIndex);
			indexes.put("logicalChannelIndex", channelIndex);
			IObjectContainer container =
				store.getIObjectContainer(LogicalChannel.class, indexes);
			data.logicalChannel = (LogicalChannel) container.sourceObject;
		}
		// ... LogicalChannel --> FilterSet
		IObjectContainer filterSetContainer = getFirstReferencedContainer(
				referenceCache, store, logicalChannelLSID, FilterSet.class);
		if (filterSetContainer != null)
		{
			data.filterSet = (FilterSet) filterSetContainer.sourceObject;
			// ... LogicalChannel --> FilterSet --> Filter (Em) AND
			// ... LogicalChannel --> FilterSet --> Filter (Ex)
			LSID filterSetLSID = new LSID(FilterSet.class, 
					filterSetContainer.indexes.get("instrumentIndex"),
					filterSetContainer.indexes.get("filterSetIndex"));
			List<IObjectContainer> filterContainers = getReferencedContainers(
					referenceCache, store, filterSetLSID, Filter.class);
			List<LSID> references = referenceCache.get(filterSetLSID);
			for (IObjectContainer container : filterContainers)
			{
				if (references.contains(new LSID(container.LSID + 
						OMEROMetadataStoreClient.OMERO_EMISSION_FILTER_SUFFIX)))
				{
					data.filterSetEmFilter = (Filter) container.sourceObject;
				}
				if (references.contains(new LSID(container.LSID + 
						OMEROMetadataStoreClient.OMERO_EXCITATION_FILTER_SUFFIX)))
				{
					data.filterSetExFilter = (Filter) container.sourceObject;
				}
			}
		}
		// ... LogicalChannel --> Filter (SecondaryEm)
		List<IObjectContainer> filterContainers = getReferencedContainers(
				referenceCache, store, logicalChannelLSID, Filter.class);
		List<LSID> references = referenceCache.get(logicalChannelLSID);
		for (IObjectContainer container : filterContainers)
		{
			if (references.contains(new LSID(container.LSID + 
					OMEROMetadataStoreClient.OMERO_EMISSION_FILTER_SUFFIX)))
			{
				data.secondaryEmFilter = (Filter) container.sourceObject;
			}
			if (references.contains(new LSID(container.LSID + 
					OMEROMetadataStoreClient.OMERO_EXCITATION_FILTER_SUFFIX)))
			{
				data.secondaryExFilter = (Filter) container.sourceObject;
			}
		}
		// ... LogicalChannel --> LightSettings
		LSID lightSettingsLSID = 
			new LSID(LightSettings.class, imageIndex, channelIndex);
		data.lightSourceSettings = 
			(LightSettings) store.getSourceObject(lightSettingsLSID);
		if (data.lightSourceSettings != null)
		{
			// ... LogicalChannel --> LightSettings --> LightSource
			IObjectContainer lightSourceContainer = 
				getFirstReferencedContainer(
						referenceCache, store,
						lightSettingsLSID, LightSource.class);
			if (lightSourceContainer != null)
			{
				data.lightSource =
					(LightSource) lightSourceContainer.sourceObject;
			}
		}
		return data;
	}
	
	/**
	 * Returns the all the referenced containers of a certain type for a given
	 * target LSID. 
	 * @param referenceCache Reference cache to pull references from.
	 * @param store Container store which is holding all object containers.
	 * @param target LSID of the target object.
	 * @param referencedClass Type filter for the retrieved containers.
	 * @see getFirstReferencedContainer()
	 * @return A list of the referenced containers.
	 * <code>list.size() == 0</code> if no containers could be found.
	 * <b>Note:</b> This is different than the
	 * {@link getFirstReferencedContainer()} return value logic.
	 */
	private static List<IObjectContainer> getReferencedContainers(
			Map<LSID, List<LSID>> referenceCache, IObjectContainerStore store,
			LSID target, Class<? extends IObject> referencedClass)
	{
		List<IObjectContainer> toReturn = new ArrayList<IObjectContainer>();
		if (referenceCache.containsKey(target))
		{
			List<IObjectContainer> containers = 
				store.getIObjectContainers(referencedClass);
			List<LSID> references = referenceCache.get(target);
			for (IObjectContainer container : containers)
			{
				if (references.contains(new LSID(container.LSID))
					|| references.contains(new LSID(container.LSID + 
						OMEROMetadataStoreClient.OMERO_EMISSION_FILTER_SUFFIX))
					|| references.contains(new LSID(container.LSID + 
						OMEROMetadataStoreClient.OMERO_EXCITATION_FILTER_SUFFIX)))
				{
					toReturn.add(container);
				}
			}
		}
		return toReturn;
	}
	
	/**
	 * Returns the the first referenced container of a certain type for a given
	 * target LSID. 
	 * @param referenceCache Reference cache to pull references from.
	 * @param store Container store which is holding all object containers.
	 * @param target LSID of the target object.
	 * @param referencedClass Type filter for the retrieved containers.
	 * @see getFirstReferencedContainer()
	 * @return A referenced container or <code>null</code> if no container 
	 * could be found. <b>Note:</b> This is different than the
	 * {@link getReferencedContainers()} return value logic.
	 * @throws ModelException If the number of containers that match
	 * <code>target</code> and <code>referencedClass</code> is > 1.
	 */
	private static IObjectContainer getFirstReferencedContainer(
			Map<LSID, List<LSID>> referenceCache, IObjectContainerStore store,
			LSID target, Class<? extends IObject> referencedClass)
	{
		List<IObjectContainer> containers = getReferencedContainers(
				referenceCache, store, target, referencedClass);
		if (containers.size() == 0)
		{
			return null;
		}
		if (containers.size() != 1)
		{
			throw new ModelException(String.format(
					"Container count of references for %s of type %s > 1: %d.",
					target, referencedClass, containers.size()));
		}
		return containers.get(0);
	}
	
	/**
	 * Returns the channel this channel data is for.
	 * @return See above.
	 */
	public Channel getChannel()
	{
		return channel;
	}
	
	/**
	 * Returns the index of the channel this channel data is for.
	 * @return See above.
	 */
	public int getChannelIndex()
	{
		return channelIndex;
	}
	
	/**
	 * Returns the logical channel for this channel data.
	 * @return See above.
	 */
	public LogicalChannel getLogicalChannel()
	{
		return logicalChannel;
	}
	
	/**
	 * Returns the filter set for the logical channel of this channel data.
	 * @return See above.
	 */
	public FilterSet getFilterSet()
	{
		return filterSet;
	}
	
	/**
	 * Returns the filter set's emission filter for the logical channel of 
	 * this channel data.
	 * @return See above.
	 */
	public Filter getFilterSetEmissionFilter()
	{
		return filterSetEmFilter;
	}
	
	/**
	 * Returns the filter set's excitation filter for the logical channel of 
	 * this channel data.
	 * @return See above.
	 */
	public Filter getFilterSetExcitationFilter()
	{
		return filterSetExFilter;
	}
	
	/**
	 * Returns the logical channel's secondary emission filter of this channel
	 * data. 
	 * @return See above.
	 */
	public Filter getSecondaryEmissionFilter()
	{
		return secondaryEmFilter;
	}
	
	/**
	 * Returns the logical channel's secondary excitation filter of this
	 * channel data. 
	 * @return See above.
	 */
	public Filter getSecondaryExcitationFilter()
	{
		return secondaryExFilter;
	}
	
	/**
	 * Returns the light source settings for the logical channel of this
	 * channel data.
	 * @return See above.
	 */
	public LightSettings getLightSourceSettings()
	{
		return lightSourceSettings;
	}
	
	/**
	 * Returns the light source for the light source settings of this channel
	 * data.
	 * @return See above.
	 */
	public LightSource getLightSource()
	{
		return lightSource;
	}
}
