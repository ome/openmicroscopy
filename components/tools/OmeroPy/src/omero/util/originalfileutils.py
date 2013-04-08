#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#------------------------------------------------------------------------------
#  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
#
#
# 	This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#  
#  You should have received a copy of the GNU General Public License along
#  with this program; if not, write to the Free Software Foundation, Inc.,
#  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#
#----------------------------------------------------------------------

###
#
# Original file utilities
# This file supplies some utilities to deal with OriginalFile Objects.
#
#
# @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
# 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
# @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
# 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
# @version 3.0
# <small>
# (<b>Internal version:</b> $Revision: $Date: $)
# </small>
# @since 3.0-Beta4
#/
import mimetypes;

importerMap = {'.dcm':'Dicom',
'.dicom':'Dicom',
'.pic':'BioRad',
'.ipl':'IPLab',
'.iplm':'IPLab',
'.dv':'Deltavision',
'.r3d':'Deltavision',
'.mrc':'MRC',
'.dm3':'Gatan',
'.ims':'Imaris',
'.raw':'OpenlabRaw',
'.ome':'OMEXML',
'.lif':'LIF',
'.sdt':'SDT',
'.sld':'Slidebook',
'.al3d':'Alicona',
'.mng':'MNG',
'.nrrd':'NNRD',
'.xv':'Khoros',
'.xys':'Visitech',
'.lim':'LIM',
'.xdce':'InCell',
'.ics':'ICS',
'.2':'PerkinElmer',
'.3':'PerkinElmer',
'.4':'PerkinElmer',
'.5':'PerkinElmer',
'.6':'PerkinElmer',
'.7':'PerkinElmer',
'.8':'PerkinElmer',
'.9':'PerkinElmer',
'.zvi':'ZeissZVI',
'.ipw':'IPW',
'.nef':'LegecyND2',
'.nd2':'ND2',
'.cxd':'PCI',
'.stk':'Metamorph',
'.lsm':'ZeissLSM',
'.seq':'SEQ',
'.gel':'Gel',
'.flex':'Flex',
'.svs':'SVS',
'.lei':'Leica',
'.oib':'Fluoview',
'.oif':'Fluoview',
'.ome.tif':'OMETiff',
'.liff':'Openlab'};

formatMap = {'.avi':'AVI',
'.qt':'QT',
'.pic':'Pict',
'.eps':'EPS',
'.psd':'PSD',
'.jp2':'video/jpeg2000',
'.tif':'image/tiff',
'.tiff':'image/tiff'};

UNKNOWN = 'type/unknown';
KNOWNMIMETYPE = 'type/known';
IMPORTER = 'application/importer';

def getExtension(filename):
    if(filename==None):
        return filename;
    str = filename.split('.');
    if(len(str)<2):
        return None;
    return str[len(str)-1];

def getFormat(filename):
    if(getExtension(filename)==None):
        return (UNKNOWN, UNKNOWN);
    if(getExtension(filename) in importerMap):
        return (IMPORTER, importerMap[filename]);
    if(getExtension(filename) in formatMap):
        return (KNOWNMIMETYPE, formatMap[filename]);
    if(mimetypes.guess_type(filename) != (None, None)):
        return (KNOWNMIMETYPE, mimetypes.guess_type(filename)[0]);
    else:
        return (UNKNOWN, UNKNOWN);
