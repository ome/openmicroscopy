/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014 University of Dundee. All rights reserved.
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
package omero.gateway.model;

import java.util.Iterator;
import java.util.List;

import omero.model.MapAnnotation;
import omero.model.MapAnnotationI;
import omero.model.NamedValue;

@SuppressWarnings("unchecked")
/**
 * The data that makes up an <i>OME</i> MapAnnotation
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class MapAnnotationData extends AnnotationData {

	/**
	 * The name space used to identify MapAnnotations created be the user
	 */
	public static final String NS_CLIENT_CREATED = omero.constants.metadata.NSCLIENTMAPANNOTATION.value;

	public MapAnnotationData(MapAnnotation value) {
		super(value);
	}

	public MapAnnotationData() {
		super(MapAnnotationI.class);
	}

	@Override
	public void setContent(Object content) {
		MapAnnotation anno = (MapAnnotation) asAnnotation();
		anno.setMapValue((List<NamedValue>) content);
		setDirty(true);
	}

	@Override
	public Object getContent() {
		MapAnnotation anno = (MapAnnotation) asAnnotation();
		return anno.getMapValue();
	}

	@Override
	public String getContentAsString() {
		List<NamedValue> data = (List<NamedValue>) getContent();
		StringBuilder sb = new StringBuilder();
		Iterator<NamedValue> it = data.iterator();
		while (it.hasNext()) {
			NamedValue next = it.next();
			sb.append(next.name + "=" + next.value);
			if (it.hasNext())
				sb.append(';');
		}
		return sb.toString();
	}

}
