"""
 components/tools/OmeroPy/scripts/omero/analysis_scripts/FLIM.py

-----------------------------------------------------------------------------
  Copyright (C) 2006-2010 University of Dundee. All rights reserved.


  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along
  with this program; if not, write to the Free Software Foundation, Inc.,
  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

------------------------------------------------------------------------------

Initialises the namespace and keywords for the FLIM script, /scripts/omero/analysis_scripts/FLIM.py

@author  Pieta Schofield &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:p@schofield.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.1
 
"""

# OMERO Imports 
import omero.clients
import omero.scripts as scripts
import omero.util.pixelstypetopython as pixelstypetopython
from omero.rtypes import *
from omero.util.OmeroPopo import EllipseData as EllipseData
from omero.util.OmeroPopo import RectData as RectData
from omero.util.OmeroPopo import MaskData as MaskData
from omero.util.OmeroPopo import WorkflowData as WorkflowData
from omero.util.OmeroPopo import ROIData as ROIData
from omero.util.OmeroPopo import ROICoordinate as ROICoordinate


# Script Utility helper methods.
import omero.util.script_utils as script_utils

CELL = omero.constants.analysis.flim.KEYWORDFLIMCELL;
NAMESPACE = omero.constants.analysis.flim.NSFLIM;
BACKGROUND = omero.constants.analysis.flim.KEYWORDFLIMBACKGROUND;

def initialise(session):
    iQuery = session.getQueryService();
    iUpdate = session.getUpdateService();
    
    #keywords = BACKGROUND+","+CELL;
    keywords = CELL;
    script_utils.registerNamespace(iQuery, iUpdate, NAMESPACE, keywords);

def runAsScript():
    client = scripts.client('FLIM_initialise.py', """Sets up the namespace and keywords for the FLIM script.""",
    version = "4.2.0",
    authors = ["Donald MacDonald", "OME Team"],
    institutions = ["University of Dundee"],
    contact = "ome-users@lists.openmicroscopy.org.uk",)
    try:
        session = client.getSession();
        initialise(session)
    finally:
        client.closeSession()

if __name__ == '__main__':
    runAsScript();
