/*
 * org.openmicroscopy.shoola.agents.browser.tests.LocalTSMLoader
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser.tests;

import org.openmicroscopy.shoola.agents.browser.ThumbnailSourceModel;

/**
 * Loads the non-OME 96-image dataset into a ThumbnailSourceModel, using
 * the dummy LocalImageData objects to indicate their location on disk.
 * 
 * DELETE THIS CLASS WHEN INTEGRATION IS COMPLETE.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 * 
 */
public class LocalTSMLoader
{
    /*
    private static final String ROOT_PATH =
        "/Users/jeffmellen/OME/Shoola/SRC/images/";
        
    private static final String[][] imageNumbers =
    {{"358","359","360","361","365","366","367","777"},
     {"778","779","780","781","782","784","789","791"},
     {"793","795","796","799","803","804","806","807"},
     {"811","813","816","817","818","819","821","824"},
     {"826","827","828","829","830","833","835","841"},
     {"842","846","848","849","850","851","852","853"},
     {"855","857","858","859","861","862","863","864"},
     {"870","888","889","890","891","892","893","895"},
     {"896","897","898","899","900","905","906","908"},
     {"909","916","919","921","922","923","924","925"},
     {"926","927","929","931","933","937","938","939"},
     {"942","943","944","945","946","947","948","949"}};
    
    */
    private static final String[] columnNames =
    {"A","B","C","D","E","F","G","H"};
    
    private static final String ROOT_PATH =
        "/Users/jeff/OME/data/";
    
    private static final String[][] imageNumbers =
    {{"2","3","4","5","6","7","8","9"},
     {"10","11","12","13","14","15","16","17"},
     {"18","19","20","21","22","23","24","25"},
     {"26","27","28","29","30","31","32","33"},
     {"34","35","36","37","38","39","40","41"},
     {"42","43","44","45","46","47","48","49"},
     {"50","51","52","53","54","55","56","57"},
     {"58","59","60","61","62","63","64","65"},
     {"66","67","68","69","70","71","72","73"},
     {"74","75","76","77","78","79","80","81"},
     {"82","83","84","85","86","87","88","89"},
     {"90","91","92","93","94","95","96","97"}
    };
    
    private static final String SUFFIX = ".jpg";
    
    
    /**
     * Create a dummy TSMLoader.
     */
    public LocalTSMLoader()
    {
        // do nothing
    }
    
    /**
     * Gets a dummy (local) ThumbnailSourceModel using the embedded data.
     * @return
     */
    public ThumbnailSourceModel getModel()
    {
        ThumbnailSourceModel model = new ThumbnailSourceModel();
        for(int i=0;i<imageNumbers.length;i++)
        {
            String[] row = imageNumbers[i];
            for(int j=0;j<row.length;j++)
            {
                String fileNum = imageNumbers[i][j];
                String wellNum = columnNames[j]+String.valueOf(i);
                String fileName = ROOT_PATH + fileNum + SUFFIX;
                
                LocalImageData imageData =
                    new LocalImageData(fileName,(i*12+j),wellNum,
                                       null,null,null,1,"Jeff",
                                       "Mellen","jeffm@mit.edu",
                                       "MIT",2,null,null,null);
                model.putImageData(imageData);
            }
        }
        return model;
    }
}
