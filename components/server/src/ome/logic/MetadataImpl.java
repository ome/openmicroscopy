/*
 * ome.logic.MetadataImpl 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package ome.logic;

//Java imports
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries
import org.springframework.transaction.annotation.Transactional;

//Application-internal dependencies
import ome.annotations.NotNull;
import ome.annotations.RolesAllowed;
import ome.annotations.Validate;
import ome.api.IMetadata;
import ome.api.ServiceInterface;
import ome.model.IObject;
import ome.model.acquisition.Arc;
import ome.model.acquisition.Filament;
import ome.model.acquisition.Laser;
import ome.model.acquisition.LightEmittingDiode;
import ome.model.acquisition.LightSettings;
import ome.model.acquisition.LightSource;
import ome.model.core.LogicalChannel;
import ome.parameters.Parameters;


/** 
 * Implement the {@link IMetadata} I/F.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class MetadataImpl 
	extends AbstractLevel2Service 
	implements IMetadata
{

	/**
	 * Returns the Interface implemented by this class.
	 * 
	 * @return See above.
	 */
    public final Class<? extends ServiceInterface> getServiceInterface() {
        return IMetadata.class;
    }
    
    /**
     * Implemented as speficied by the {@link IMetadata} I/F
     * @see IMetadata#loadChannelAcquisitionData(Set)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public Set loadChannelAcquisitionData(@NotNull 
			@Validate(Long.class) Set<Long> ids)
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append("select channel from LogicalChannel as channel ");
		sb.append("left outer join fetch channel.detectorSettings as ds ");
        sb.append("left outer join fetch channel.lightSourceSettings as lss ");
        sb.append("left outer join fetch ds.detector as detector ");
        sb.append("left outer join fetch detector.type as dt ");
        sb.append("left outer join fetch ds.binning as binning ");
        sb.append("left outer join fetch lss.lightSource as light ");
        sb.append("left outer join fetch light.type as lt ");
        sb.append("where channel.id in (:ids)");
        List<LogicalChannel> list = iQuery.findAllByQuery(sb.toString(), 
        		new Parameters().addIds(ids));
        Iterator<LogicalChannel> i = list.iterator();
        LogicalChannel channel;
        LightSettings light;
        LightSource src;
        IObject object;
        while (i.hasNext()) {
        	channel = i.next();
			light = channel.getLightSourceSettings();
			if (light != null) {
				sb = new StringBuilder();
				src = light.getLightSource();
				if (src instanceof Laser) {
					sb.append("select laser from Laser as laser ");
					sb.append("left outer join fetch laser.type as type ");
					sb.append("left outer join fetch laser.laserMedium as " +
							"medium ");
					sb.append("left outer join fetch laser.pulse as pulse ");
			        sb.append("where laser.id = :id");
				} else if (src instanceof Filament) {
					sb.append("select filament from Filament as filament ");
					sb.append("left outer join fetch filament.type as type ");
			        sb.append("where filament.id = :id");
				} else if (src instanceof Arc) {
					sb.append("select arc from Arc as arc ");
					sb.append("left outer join fetch arc.type as type ");
			        sb.append("where arc.id = :id");
				} else if (src instanceof LightEmittingDiode) {
					sb = null;
				}
				if (sb != null) {
					object = iQuery.findByQuery(sb.toString(), 
			        		new Parameters().addId(src.getId()));
					light.setLightSource((LightSource) object);
				}
			}
		}
    	return new HashSet<LogicalChannel>(list);
    }

}
