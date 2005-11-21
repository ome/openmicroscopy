/*
 * ome.api.Write
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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

package ome.api;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Provides writing methods
 *
 * @author  <br>Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">
 * 					josh.moore@gmx.de</a>
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since 1.0
 */
public interface Write
{

//	public void createExperimenter(
//			String name, 
//			String firstname, 
//			String lastname,
//			String email,
//			String password,
//			Integer groupId,
//			String dataDir,
//			);
//	 attribute_id        | integer                | not null default nextval('attribute_seq'::text)
//	 ome_name            | character varying(30)  | 
//	 email               | character varying(50)  | 
//	 firstname           | character varying(30)  | 
//	 password            | character varying(64)  | 
//	 group_id            | integer                | 
//	 data_dir            | character varying(256) | 
//	 module_execution_id | integer                | not null
//	 lastname            | character varying(30)  | 
//	 institution         | character varying(256) | 

	/*
	 attribute_id        | integer | not null default nextval('attribute_seq'::text)
	 content             | text    | 
	 module_execution_id | integer | not null
	 dataset_id          | integer | not null
	 valid               | boolean | 
	 */
	public void createDatasetAnnotation(Integer datasetId, String content);

}
