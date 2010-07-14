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

This script performs FRET analysis on FLIM images calculating Decay curve differences
between control and experimental datasets. 

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

import sys
import os as OS
import time
import numpy as NP
import itertools as IT
import resource
import platform
import omero.util.mpfit as mpfit
# OMERO Imports 
import omero.clients
import omero.scripts as scripts
import omero_api_Gateway_ice
import omero_api_IScript_ice
import omero.util.pixelstypetopython as pixelstypetopython
from omero.rtypes import *
import omero_Constants_ice
from omero.util.OmeroPopo import EllipseData as EllipseData
from omero.util.OmeroPopo import RectData as RectData
from omero.util.OmeroPopo import MaskData as MaskData
from omero.util.OmeroPopo import WorkflowData as WorkflowData
from omero.util.OmeroPopo import ROIData as ROIData
from omero.util.OmeroPopo import ROICoordinate as ROICoordinate


# Script Utility helper methods.
import omero.util.script_utils as script_utils

# Image saving and loading Imports 
from struct import *

try:
    from PIL import Image, ImageDraw # see ticket:2597
except ImportError:
    import Image, ImageDraw # see ticket:2597

CELL = "Cell";
NAMESPACE = "openmicroscopy.org.uk/ROI/FLIM";
BACKGROUND = "Background";


# A functions to locate the index of the maximum value in an array 
argmax = lambda a: max(IT.izip(a, xrange(len(a))))[1]

# Single exponential decay function convolved with irf
def mod1exp(p,x,i):
    y=NP.convolve(p[0]*NP.e**(-(x/p[1]))+p[2],i[::-1],mode=2)
    return y
    
# Single exponential decay function wrapped for calling by mpfit
def exp1(p,fjac=None,x=None,y=None,i=None,sh=None,sm=None,err=None,weights=None):
    subSizeC = 240
    model = mod1exp(p,x,i)
    status = 0
    return [status, y[sm:subSizeC]-model[sh+sm:sh+subSizeC]]
    
# Double exponential decay function convolved with irf
def mod2exp(p,x,i):
    y=NP.convolve(p[0]*NP.e**(-(x/p[2]))+p[1]*NP.e**(-(x/p[3]))+p[4],i[::-1],mode=2)
    return y

# Double exponential decay function wrapped for calling by mpfit
def exp2(p,fjac=None,x=None,y=None,i=None,sh=None,sm=None,err=None,weights=None):
    subSizeC = 240;
    model = mod2exp(p,x,i)
    status = 0
    return [status, y[sm:subSizeC]-model[sh+sm:sh+subSizeC]]

#### Change to fitpixary to accommodate calling either exp1 (No FRET) or exp2 (FRET)
#    parameter fittype added expect 1 or 2 

# Call to mpfit wrapped for calling in loop It might be possible to generate threads here as nothing is returned it depends if parameters are by reference (addresses) or by value (copies)
def fitpixary(sigf,t,i,shift,sigmax,paras,pxy,pxx,a1,a2,k1,k2,b,chi,s1,fittype):
    sizeC = len(sigf);
    maxv=float(max(sigf))
    maxv=1
    s=NP.zeros(sizeC*2)
    s[0:sizeC]=sigf[0:sizeC]/maxv

    # Setup arrays of values to send to fit function (zero pad this too use convolve) and calculate shift
    # create dictionary of values to send to fit function
    fa={'x':t, 'y':s, 'i':i, 'sh':shift, 'sm':sigmax}

    # Call either single decay or double decay
    if fittype==1:
        mpf=mpfit.mpfit(exp1,parinfo=paras,functkw=fa,quiet=True)
        s1[pxy,pxx]=mpf.status
        if mpf.status>0 and mpf.status<5:
            a1[pxy,pxx]=mpf.params[0]
            k1[pxy,pxx]=mpf.params[1]
            b[pxy,pxx]=mpf.params[2]
            chi[pxy,pxx]=mpf.fnorm
        else:
            a1[pxy,pxx]=0
            k1[pxy,pxx]=0
            b[pxy,pxx]=0
            chi[pxy,pxx]=0
    else:
        mpf = mpfit.mpfit(exp2,parinfo=paras,functkw=fa,quiet=True)
        s1[pxy,pxx]=mpf.status
        if mpf.status>0 and mpf.status<5:
            a1[pxy,pxx]=mpf.params[0]
            a2[pxy,pxx]=mpf.params[1]
            k1[pxy,pxx]=mpf.params[2]
            k2[pxy,pxx]=mpf.params[3]
            b[pxy,pxx]=mpf.params[4]
            chi[pxy,pxx]=mpf.fnorm
        else:
            a1[pxy,pxx]=0
            a2[pxy,pxx]=0
            k1[pxy,pxx]=0
            k2[pxy,pxx]=0
            chi[pxy,pxx]=0

### --------------------------------------------

def runfret(session, commandArgs):
    iROI = session.getRoiService();
    iQuery = session.getQueryService();
    iUpdate = session.getUpdateService();
    rawFileStore = session.createRawFileStore();
    rawPixelsStore = session.createRawPixelsStore();
    iPixels = session.getPixelsService();
    roiService = session.getRoiService();
        
    noFretPixelsId = commandArgs['Image_ID_No_Fret'];
    fretPixelsId = commandArgs['Image_ID_Fret'];
   
    instrumentResponseFileId = commandArgs['IRF_ID'];
    
    noFretPixels = iPixels.retrievePixDescription(noFretPixelsId);
    fretPixels = iPixels.retrievePixDescription(fretPixelsId);
    fretImage = fretPixels.getImage().getId().getValue();
    noFretImage = noFretPixels.getImage().getId().getValue();
    annotateImage = fretPixels.getImage();
       
    noFretROI = script_utils.findROIByImage(roiService, noFretImage, NAMESPACE);
    fretROI = script_utils.findROIByImage(roiService, fretImage, NAMESPACE);
    if(len(fretROI)==0 or len(noFretROI)==0):
          raise Exception("No ROI on Images of type " + NAMESPACE);
 
    fretPoints = {};
    noFretPoints = {};
    fretBackgroundPoints = {};
    noFretBackgroundPoints = {};
    
    noFretKeywords = {};
    fretKeywords = {};
    print roi
    for roi in noFretROI:
        shapeList = roi.getShapes(0,0);
        for shape in shapeList:
            noFretKeywords[roi.getKeywords()]=True;
            points = shape.containsPoints();
            if(roi.getKeywords()==BACKGROUND):
                for point in points:
                    noFretBackgroundPoints[point] = roi.getKeywords();
            else:
                for point in points:
                    noFretPoints[point] = roi.getKeywords();
    
    for roi in fretROI:
        shapeList = roi.getShapes(0,0);
        for shape in shapeList:
            fretKeywords[roi.getKeywords()]=True;
            points = shape.containsPoints();
            if(roi.getKeywords()==BACKGROUND):
                for point in points:
                    fretBackgroundPoints[point] = roi.getKeywords();
            else:
                for point in points:
                    fretPoints[point] = roi.getKeywords();
    
    if( not fretKeywords.has_key(BACKGROUND)):
        raise Exception("No ROI on Fret Images with keywords : " +BACKGROUND);
    if( not fretKeywords.has_key(CELL)):
        raise Exception("No ROI on Fret Images with keywords : " +CELL);
    if( not noFretKeywords.has_key(BACKGROUND)):
        raise Exception("No ROI on noFret Images with keywords : " +BACKGROUND);
    if( not noFretKeywords.has_key(CELL)):
        raise Exception("No ROI on noFret Images with keywords : " +CELL);
        
    sizeX = noFretPixels.getSizeX().getValue();
    sizeY = noFretPixels.getSizeY().getValue();
    sizeC = noFretPixels.getSizeC().getValue();
    sizeC_half = sizeC/2;
    sizeC_double = sizeC*2;

    irf = script_utils.readFileAsArray(rawFileStore, iQuery, instrumentResponseFileId, sizeC, 2,separator=' ');
        
    irfmax=argmax(irf[:,1])
    tirf=irf[:,0]
    dt=tirf[2]-tirf[1]
    t=NP.zeros(sizeC_double)
    for pos in range(sizeC_double):
        t[pos]=pos*dt

    # read no fret file
    nofret = script_utils.readFlimImageFile(rawPixelsStore, noFretPixels);
    sigf=NP.sum(nofret.reshape(sizeC,sizeX*sizeY),axis=1)
    maxs=float(NP.max(sigf))
    sumsqr = pow(sum(sigf*sigf),0.5)
    sigf=sigf/sumsqr
    print sigf
    sigmax=argmax(sigf)
    s=NP.zeros(sizeC_double)
    s[0:sizeC]=sigf[0:sizeC]
    # Setup arrays of values to send to fit function (zero pad this too use convolve) and calculate shift
    a1_0=1
    t1_0=1
    b1_0=0.01
    p0 = NP.array([a1_0, t1_0, b1_0])
    params = [{'value':p0[0], 'fixed':0, 'limited':[1,0], 'limits':[0.,0.]},
                {'value':p0[1], 'fixed':0, 'limited':[1,0], 'limits':[0.,0.]},
                {'value':p0[2], 'fixed':1, 'limited':[1,0], 'limits':[0.,0.]}]
    i=NP.zeros(sizeC_double)
    i[sizeC_half:sizeC_half+sizeC] = irf[0:sizeC,1]/float(NP.sum(irf[:,1]))
    cf=mod1exp(p0,t,i)
    cf=cf/cf.max()
    cmax=argmax(cf)
    shift=cmax-sigmax
    # create dictionary of values to send to fit function
    fa={'x':t, 'y':s, 'i':i, 'sh':shift, 'sm':sigmax}
    #  DO THE FIT
    mpf = mpfit.mpfit(exp1,parinfo=params,functkw=fa,quiet=True)
    # Output stuff for checking
    nflt=mpf.params[1]
    cf=mod1exp(mpf.params,t,i)

### NEW pixel-by-pixel fit for NO FRET data to calculate average decay rather than decay of average

    # need these data structures now rather where they were previously
    s1=NP.zeros((sizeX, sizeY))
    chi2=NP.zeros((sizeX, sizeY),NP.float)
    a1=NP.zeros((sizeX, sizeY),NP.float)
    a2=NP.zeros((sizeX, sizeY),NP.float)
    k1=NP.zeros((sizeX, sizeY),NP.float)
    k2=NP.zeros((sizeX, sizeY),NP.float)
    b=NP.zeros((sizeX, sizeY),NP.float)
    chi=NP.zeros((sizeX, sizeY),NP.float)

    # loop through all pixels (ignoring edge pixels)
    for point in noFretPoints:
            fxy = point[1];
            fxx = point[0];
            pxx=fxx+1
            pxy=fxy+1
            
            sigf=nofret[:,pxy,pxx]
            #  5000 is threshold pixel count not to fit below
            if NP.sum(sigf)>5000 :
                # while nstarted < nthread: pass 
                # thread.start_new_thread(fitpixary,(sigf,t,i,shift,sigmax,params,pxy,pxx))
                sumsqr = pow(sum(sigf*sigf),0.5)
                sigf=sigf/sumsqr
                fitpixary(sigf,t,i,shift,sigmax,params,pxy,pxx,a1,a2,k1,k2,b,chi,s1,1)
                # take 9 pixel neighbourhood bin for each pixel
            else :
                # if not enough photons in pixel agregate surounding 8 pixels and retry
                subary=nofret[:,pxy-1:pxy+2,pxx-1:pxx+2]
                if subary.size == sizeC*9:
                    sigf=NP.sum(subary.reshape(sizeC,9),axis=1)
                    if NP.sum(sigf)>5000 :
                        # while nstarted < nthread: pass 
                        # thread.start_new_thread(fitpixary,(sigf,t,i,shift,sigmax,params,pxy,pxx))
                        sigf=sigf/9.0
                        sumsqr = pow(sum(sigf*sigf),0.5)
                        sigf=sigf/sumsqr
                        fitpixary(sigf,t,i,shift,sigmax,params,pxy,pxx,a1,a2,k1,k2,b,chi,s1,1)

    # Calculate no fret decay from pixel-by-pixel fit histogram for k1
    # generate histogram magic number 1000 bins
    k1h=NP.histogram(k1, bins=1000)
    # find midpoint of bins
    mp=0.5*k1h[1][0:-1]+k1h[1][1:]
    # get mode
    md=argmax(k1h[0][1:])
    # calc cumlative sum for lower half and find 50 centile for this range
    csl=100*k1h[0][1:md].cumsum()/k1h[0][1:md].sum()
    csmn=100-(csl-50)*(csl-50)
    csmin=argmax(csmn)
    # calc cumlative sum for upper half and find 50 centile for this range
    csu=100*k1h[0][md:].cumsum()/k1h[0][md:].sum()
    csmx=100-(csu-50)*(csu-50)
    csmax=argmax(csmx)
    # calculate the mean of this new range
    nmean=(k1h[0][csmin:csmax+md]*mp[csmin:csmax+md]).sum()/k1h[0][csmin:csmax+md].sum()
    k1Max = k1.max()

### -------------------------------------------------------------------------------
    
    fret = script_utils.readFlimImageFile(rawPixelsStore, fretPixels);
    print '3'
    sigf=NP.sum(fret.reshape(sizeC,sizeX*sizeY),axis=1)
    maxs=float(NP.max(sigf))
    sigf=sigf/maxs
    sumsqr = pow(sum(sigf*sigf),0.5)
    sigf=sigf/sumsqr
    sigmax=argmax(sigf)
    s=NP.zeros(sizeC_double)
    s[0:sizeC]=sigf[0:sizeC]
### set initial values to new mean (nmean) not k1
    p0 = NP.array([mpf.params[0], mpf.params[0], nmean, nmean, mpf.params[2]])
    params = [{'value':p0[0], 'fixed':0, 'limited':[1,0], 'limits':[0.,2.]},
                {'value':p0[1], 'fixed':0, 'limited':[1,0], 'limits':[0.,2.]},
                {'value':p0[2], 'fixed':1, 'limited':[1,1], 'limits':[p0[2],p0[2]]},
                {'value':p0[3], 'fixed':0, 'limited':[1,1], 'limits':[0.,nmean]},
                {'value':p0[4], 'fixed':1, 'limited':[1,1], 'limits':[0.,1.]}]
    # Setup arrays of values to send to fit function (zero pad this too use convolve) and calculate shift
    cf=mod2exp(p0,t,i)
    cf=cf/cf.max()
    cmax=argmax(cf)
    shift=cmax-sigmax
    # create dictionary of values to send to fit function
    fa={'x':t, 'y':s, 'i':i, 'sh':shift, 'sm':sigmax}
    # Do a global fit of aggregated pixels
    mpf = mpfit.mpfit(exp2,parinfo=params,functkw=fa,quiet=True)
    # This is what is meant to happen DO fits for all pixels where the photon count over bins is greater than a threshold
    cf=mod2exp(mpf.params,t,i)
    p0 = NP.array([mpf.params[0], mpf.params[1], mpf.params[2], mpf.params[3], mpf.params[4]])
    params = [{'value':p0[0], 'fixed':0, 'limited':[1,1], 'limits':[0.,2.]},
                {'value':p0[1], 'fixed':0, 'limited':[1,1], 'limits':[0.,2.]},
                {'value':p0[2], 'fixed':1, 'limited':[1,1], 'limits':[p0[2],p0[2]]},
                {'value':p0[3], 'fixed':0, 'limited':[1,1], 'limits':[0.,p0[2]]},
                {'value':p0[4], 'fixed':1, 'limited':[1,1], 'limits':[0.,1.]}]
    s1=NP.zeros((sizeC,sizeC))
    chi2=NP.zeros((sizeC,sizeC),NP.float)
    a1=NP.zeros((sizeC,sizeC),NP.float)
    a2=NP.zeros((sizeC,sizeC),NP.float)
    k1=NP.zeros((sizeC,sizeC),NP.float)
    k2=NP.zeros((sizeC,sizeC),NP.float)
    chi=NP.zeros((sizeC,sizeC),NP.float)
    print '4'
    # loop through all pixels (ignoring edge pixels)
    for point in fretPoints:
            fxy = point[1];
            fxx = point[0];
            pxx=fxx+1
            pxy=fxy+1
            sigf=fret[:,pxy,pxx]
            #  5000 is threshold pixel count not to fit below
            if NP.sum(sigf)>5000 :
                # while nstarted < nthread: pass 
                # thread.start_new_thread(fitpixary,(sigf,t,i,shift,sigmax,params,pxy,pxx))
                sumsqr = pow(sum(sigf*sigf),0.5)
                sigf=sigf/sumsqr
### -- Change to pass more params to fitpixary
                fitpixary(sigf,t,i,shift,sigmax,params,pxy,pxx,a1,a2,k1,k2,b,chi,s1,2)
                # take 9 pixel neighbourhood bin for each pixel
            else :
                # if not enough photons in pixel agregate surounding 8 pixels and retry
                subary=fret[:,pxy-1:pxy+2,pxx-1:pxx+2]
                if subary.size == sizeC*9:
                    sigf=NP.sum(subary.reshape(sizeC,9),axis=1)
                    if NP.sum(sigf)>5000 :
                        # while nstarted < nthread: pass 
                        # thread.start_new_thread(fitpixary,(sigf,t,i,shift,sigmax,params,pxy,pxx))
                        sigf=sigf/9.0
                        sumsqr = pow(sum(sigf*sigf),0.5)
                        sigf=sigf/sumsqr
### -- Change to pass more params to fitpixary
                        fitpixary(sigf,t,i,shift,sigmax,params,pxy,pxx,a1,a2,k1,k2,b,chi,s1,2)

    # write out the parameter arrays this point will be writing back an image to OMERO
    # while nstarted < 1: pass 
    script_utils.uploadArray(rawFileStore, iUpdate, iQuery, annotateImage, "a1.csv", a1);
    script_utils.uploadArray(rawFileStore, iUpdate, iQuery, annotateImage, "a2.csv", a2);
    script_utils.uploadArray(rawFileStore, iUpdate, iQuery, annotateImage, "k1.csv", k1);
    script_utils.uploadArray(rawFileStore, iUpdate, iQuery, annotateImage, "k2.csv", k2);
    script_utils.uploadArray(rawFileStore, iUpdate, iQuery, annotateImage, "chi.csv", chi);
    script_utils.uploadArray(rawFileStore, iUpdate, iQuery, annotateImage, "s1.csv", s1);
    

def runAsScript():
    client = scripts.client('FLIM.py', """Analysis a set of Lifetime images and perform FRET analysis betweeen control and sample images.
    See http://trac.openmicroscopy.org.uk/shoola/wiki/Analysis_Scripts#FLIM""", 
    scripts.Long("Image_ID_No_Fret", description="The image to analysis with no fret condition"),
    scripts.Long("Image_ID_Fret", description="The image to analysis under fret condition"),
    scripts.Long("IRF_ID", description="The file containing the instrument response function(IRF)"),
    version = "4.2.0",
    authors = ["Donald MacDonald", "OME Team"],
    institutions = ["University of Dundee"],
    contact = "ome-users@lists.openmicroscopy.org.uk",)
    try:
        session = client.getSession();
        commandArgs = {}
    
        # process the list of args above. 
        for key in client.getInputKeys():
            if client.getInput(key):
                commandArgs[key] = client.getInput(key).getValue()
    
        print commandArgs
        # call the main script, attaching resulting analysis files.. Returns the id of the originalFileLink child. (ID object, not value)
        fileAnnotation = runfret(session, commandArgs)
        # return this fileAnnotation to the client. 
        if fileAnnotation:
            client.setOutput("Message", rstring("FLIM Analysis complete."))
            client.setOutput("File_Annotation", robject(fileAnnotation))
    finally:
        client.closeSession()

if __name__ == '__main__':
    runAsScript();
