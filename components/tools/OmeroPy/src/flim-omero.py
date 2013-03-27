#!/usr/bin/env python
# -*- coding: utf-8 -*-
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
<a href="mailto:p@schofield.dundee.ac.uk">p.schofield@dundee.ac.uk</a>
@author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.1
 
""" 

from mpi4py import MPI as mpi
import warnings as wrn

import matplotlib as mpl
mpl.use('Agg')

import time as tim
import resource as res
import sys as sys
import getopt as gop
import os as os
import itertools as itr

import numpy as npy

from matplotlib import pylab as plt
from matplotlib import colors as col
from scipy import optimize as opz
from scipy import ndimage as ndi
from scipy import cluster as clt

import mpfit as mpf

# OMERO Imports 
import omero.clients
import omero.util.pixelstypetopython as pixelstypetopython
from omero.rtypes import *
import omero_ext.uuid as uuid # see ticket:3774

# Script Utility helper methods.
import omero.util.script_utils as script_utils

# Image saving and loading Imports 
from struct import *

NAMESPACE = omero.constants.analysis.flim.NSFLIM;

try:
    from PIL import Image, ImageDraw # see ticket:2597
except ImportError:
    import Image, ImageDraw # see ticket:2597

# eps (epsilon) is a magic small number to avoid getting division by zero warnings from the mpfit calls
eps=0.000000001

# mycmdata1 and cycm1 are custom colormaps for generating plots in matplotlib of the numpy arrays
# of the spatial parameter distributions (the colours are chosen to make nice inverted images for presentations
# rather than nice look nice in their uninverted form.
mycmdata1 = {'red' : ((0., 1., 1.), (0.75, 0.5, 0.5), (1., 0.25, 0.)),
	                  'green': ((0., 1., 1.), (0.5, 0.5, 0.5), (1., 0., 0.)),
	                  'blue' : ((0., 1., 1.), (0.25, 0.5, 0.5),  (1., 0.25, 0.))}
cycm1 = col.LinearSegmentedColormap('mycm', mycmdata1)

# strings for writing parameter names to the tex file
paranames=['$\\alpha_{1}$','$\\tau_{1}$','$b$','$\\alpha_{2}$','$\\tau_{2}$']

# A functions to locate the index of the maximum value in an array 
argmax = lambda a: max(itr.izip(a, xrange(len(a))))[1]

# The following functions are the single exponential and the double exponential functions
# and the wrapper functions for the mpfit program.

# Single exponential decay function convolved with irf
cexp_1= lambda p,x, i: npy.convolve(p[0]*npy.e**(-x/(p[1]+eps))+p[2],i[::-1],mode=2)
    
# Single exponential decay function wrapped for calling by mpfit
def err_1(p,fjac=None,x=None,y=None,err=None,i=None,sh=None,sm=None,lf=None):
    err=None
    model = cexp_1(p,x,i)
    status = 0
    if err==None:
        return [status, (y[sm:lf]-model[sm+sh:sh+lf])]
    else:
        return [status, (y[sm:lf]-model[sm+sh:sh+lf])/err[sm:lf]]

# Double exponential decay function convolved with irf
cexp_2 = lambda p,x,i: npy.convolve(p[0]*npy.e**(-x/(p[1]+eps))+p[3]*npy.e**(-x/(p[4]+eps))+p[2],i[::-1],mode=2)

# Double exponential decay function wrapped for calling by mpfit
def err_2(p,fjac=None,x=None,y=None,err=None,i=None,sh=None,sm=None,lf=None):
    err=None
    model = cexp_2(p,x,i)
    status = 0
    if err==None:
        return [status, (y[sm:lf]-model[sm+sh:sh+lf])]
    else:
        return [status, (y[sm:lf]-model[sm+sh:sh+lf])/err[sm:lf]]

# This function fits a numpy array s representing the signal and e respresenting the variance against a model
# given by mode over time points given by a_tp. It returns a large number of values so that the global average
# fit can be plotted and incorporated into the tex
def procFitIt(s,e,a_tp,mode,d_args,par=None):
    # Setup arrays to pass to fit function
    i_slice=len(a_tp)
    # create 2n long arrays for convolution of model with instrument response fucntion
    sig=npy.zeros(i_slice*2)
    err=npy.ones(i_slice*2)
    se=npy.zeros(i_slice*2)
    tp=npy.zeros(i_slice*2)
    irf=npy.zeros(i_slice*2)
    # fill in the values from the signal and time points from irf
    sig[0:i_slice]=s[0:i_slice]
    tp[0:i_slice]=a_tp[0:i_slice,0]
    err[0:i_slice]=e[0:i_slice]
    irf[int(i_slice*0.5):int(i_slice*1.5)] = a_tp[0:i_slice,1]
    # Fill up time points missing time points
    textra=npy.array(range(i_slice),npy.float)
    textra+=1.0
    textra*=float(tp[2]-tp[1])
    textra+=tp[i_slice-1]
    tp[i_slice:]=textra
    # Select which model to fit
    if mode>1:
        # fit double exponential (FRET condition fit) if there are no initial parameters generate them 
        # THESE ARE MAGIC NUMBER DEFAULTS FOR INITAL VALUES
        # At this point for average initial fit calculate the shift to align the peak of the convolved model signal
        # with the peak of the actual signal
        if par==None:
            par = npy.array([1.0,  d_args['n']/2.0, 0.0, 1.0,  d_args['n']],'d')
            cf=cexp_2(par,tp,irf)
            smax=argmax(sig)
            cmax=argmax(cf)
            shift=cmax-smax
        else:
            smax=par[-2]
            shift=par[-1]
        # set up parameters and constriants for mpfit
        params = [{'value':par[0], 'fixed':0, 'limited':[1,0], 'limits':[0.,0.]},
			  {'value':par[1], 'fixed':0, 'limited':[1,0], 'limits':[0.,0.]},
			  {'value':par[2], 'fixed':0, 'limited':[1,0], 'limits':[0.,0.]},
			  {'value':par[3], 'fixed':0, 'limited':[1,0], 'limits':[0.,0.]},
			  {'value':par[4], 'fixed':1, 'limited':[1,0], 'limits':[0.,0.]}]
        # create dictionary of values to send to fit function
        fa={'x':tp, 'y':sig, 'i':irf, 'sh':shift, 'sm':smax,'err':se,'lf':i_slice}
        #  DO THE FIT
        fit = mpf.mpfit(err_2,parinfo=params,functkw=fa,quiet=True)
        cf1=cexp_2(fit.params,tp,irf)
    else:
        # Single exponential (NO FRET) fit
        # THESE ARE MAGIC NUMBER DEFAULTS FOR INITAL VALUES
        # At this point for average initial fit calculate the shift to align the peak of the convolved model signal
        # with the peak of the actual signal
        if par==None:
            par = npy.array([1.0, d_args['n'], 0.0],'d')
            cf=cexp_1(par,tp,irf)
            smax=argmax(sig)
            cmax=argmax(cf)
            shift=cmax-smax
        else:
            smax=par[-2]
            shift=par[-1]
        # set up parameters and constraints for mpfit
        params = [{'value':par[0], 'fixed':0, 'limited':[1,0], 'limits':[0.,0.]},
			  {'value':par[1], 'fixed':0, 'limited':[1,0], 'limits':[0.,0.]},
			  {'value':par[2], 'fixed':0, 'limited':[1,0], 'limits':[0.,0.]}]
        # create dictionary of values to send to fit function
        fa={'x':tp, 'y':sig, 'i':irf, 'sh':shift, 'sm':smax,'err':err,'lf':i_slice}
        #  DO THE FIT
        fit = mpf.mpfit(err_1,parinfo=params,functkw=fa,quiet=True)
        cf1=cexp_1(fit.params,tp,irf)
    # create fitted parameter array to pass back
    # Here a lot of values are returned so that higher level calls can lot the fit
    fpar=[]
    for i in range(len(fit.params)):
      fpar.append(fit.params[i])  
    fpar.append(smax)
    fpar.append(shift)
    return [fit,tp[0:i_slice],cf1[shift:shift+i_slice],sig[0:i_slice],se[0:i_slice],npy.array(fpar)]

# This routine performs a k-means segmentation of the average projection of the data to identify the focal pixels
# it is passed the whole file as rawdata and the parameter dictionary so it can know the structure of the data
# spatial dimensions , time points.
# it also makes an initial fit of the global averaged data for focal pixels and hence needs to know the model to use
def procSEG(rawdata,a_tp,d_args):
    i_res=d_args['s']
    i_slice=d_args['t']
    i_mode=d_args['m']
    shape=(i_slice,i_res*i_res)
    ndata=npy.zeros(i_res*i_res*i_slice,dtype=npy.float).reshape(shape)
    # find cell boundary use stdev and average projections for K-means thresholding on normalized image stacks
    nave=clt.vq.whiten(npy.average(rawdata,axis=0))
    tabck,tacel=npy.sort(clt.vq.kmeans(nave,2)[0])
    th1=1.75*tabck+tacel*0.25
    th2=1.25*tabck+tacel*0.75
    # Whole cell Thresholding 
    obck=npy.where(nave<th1,1,0)
    ocel=npy.where(nave<th1,0,1)
    ncel=len(ocel)
    # At this point i would be possible to segment the images further with call scipy watershedding and labeling
    # to generate separate regions of interest for each cell with in the image. However this would have knock on 
    # effect on distribution of pixels in mpi. Also watershedding might need manual tuning to prevent shattering 
    # cells 
    # Calculate average focal pixel intensities and variance
    zabck=npy.zeros(i_slice,dtype=npy.float)
    s1bck=npy.zeros(i_slice,dtype=npy.float)
    for i in range(i_slice):
        s1bck[i]=ndi.variance(rawdata[i],obck)
        ndata[i]=rawdata[i]-ndi.mean(rawdata[i],obck)
    #
    # Cell 
    #
    zacel=npy.zeros(i_slice,dtype=npy.float)
    s1cel=npy.zeros(i_slice,dtype=npy.float)
    for i in range(i_slice):
        zacel[i]=ndi.mean(ndata[i],ocel)
        s1cel[i]=npy.sqrt(ndi.variance(ndata[i],ocel)+s1bck[i])
    # initialize signal, error and time points
    # Fit cell average to get ball park figures
    initfit,tp,cf1,sig,se,fpar=procFitIt(zacel,s1cel,a_tp,i_mode,d_args,None)
    return [ocel.reshape((i_res,i_res)),initfit,tp,cf1,sig,se,fpar]

# This function reads the whole file of pixels in for each node in mpi cluster and returns a numpy array
def procRead(rawPixelsStore, pixels):
    id = pixels.getId().getValue();
    sizeC = pixels.getSizeC().getValue();
    sizeX = pixels.getSizeX().getValue();
    sizeY = pixels.getSizeY().getValue();
    
    imageCXY = script_utils.readFlimImageFile(rawPixelsStore, pixels);
    imageCD = imageCXY.reshape(sizeC,sizeX*sizeY);
    return imageCD
    #fd=open(d_args['f']+'.raw','rb')
    #shape=(d_args['t'],d_args['s']*d_args['s'])
    #rawdata = npy.fromfile(file=fd, dtype=npy.uint16).byteswap().reshape(shape)
    #return rawdata

# This function reads in the irf file and return 
def procTP(rawFileStore, queryService, instrumentResponseFileId):
    return script_utils.readFileAsArray(rawFileStore, queryService, instrumentResponseFileId, 256, 2, separator = ' ');

def createSession(host, user, password):
    client = omero.client(host);
    session = client.createSession(user, password);
    return client, session;

def pixelsInDataset(containerService, datasetId):
    Images = containerService.getImages('DatasetI',[datasetId],None,None)
    pixels = [];
    for image in Images:
        pixels.append(image.getPixels(0));
    return pixels;

def imagesInDataset(containerService, datasetId):
    images = containerService.getImages('DatasetI',[datasetId],None,None)
    return images;

# This is the main mpi script 

def getParentProject(containerService, datasetId):
    projectList = containerService.findContainerHierarchies('Project',[datasetId],None,None);
    return projectList[0];
 
def getDataset(containerService, datasetId): 
    datasetList =  containerService.loadContainerHierarchy('Dataset',[datasetId],None,None)
    return datasetList[0];

def getProject(containerService, projectId): 
    projectList =  containerService.loadContainerHierarchy('Project',[projectId],None,None)
    return projectList[0];

def getImage(gateway, imageId): 
    return gateway.getImage(imageId)

def attachFileToDataset(containerService, queryService, updateService, rawFileStore, datasetID, localName):
    dataset = getDataset(containerService, datasetID);
    return script_utils.uploadAndAttachFile(queryService, updateService, rawFileStore, dataset, localName, 'PDF', description=None, namespace=None, origFilePathName=None);
  
def attachFileToProject(containerService, queryService, updateService, rawFileStore, projectId, localName):
    project = getProject(containerService, projectId);
    return script_utils.uploadAndAttachFile(queryService, updateService, rawFileStore, project, localName, 'PDF', description=None, namespace=None, origFilePathName=None);
 
def attachFileToImage(gateway, queryService, updateService, rawFileStore, imageID, localName):
    image = getImage(gateway, imageID);
    return script_utils.uploadAndAttachFile(queryService, updateService, rawFileStore, image, localName, 'CSV', description=None, namespace=NAMESPACE, origFilePathName=None);
   
def getImageIdFromPixels(gateway, pixelId):
    return gateway.getPixels(pixelsId).getImage().getId().getValue();

def mpi_run(argv):
    texfilename = 'test_n.tex';
    # set up mpi world recover number of nodes and rank of each process
    # Process of rank=0 acts as co-ordinating process and does the latex generation
    comm = mpi.COMM_WORLD
    size = comm.Get_size()
    rank = comm.Get_rank()
    name= mpi.Get_processor_name()
    # get the command line arguments 
    try:
        opts, args = gop.getopt(argv, "h:u:p:c:e:r:o", ["host","user","password","control","experiment", "rfid", "outputfilename"])
    except gop.error, msg:
        print >>sys.stderr, msg
        return 2
    s_dir = str(uuid.uuid1())+'/';
    d_args={}
    d_args['d'] = s_dir;
    for opt, arg in opts:
        if opt in ("-h", "--host"):
            host=arg.strip()
            d_args['host']=host
        if opt in ("-u", "--user"):
            user=arg.strip()
            d_args['user']=user
        if opt in ("-p", "--password"):
            password=arg.strip()
            d_args['password']=password
        if opt in ("-c", "--control"):
            control=arg.strip()
            d_args['control']=control
        if opt in ("-e", "--experiment"):
            experiment=arg.strip()
            d_args['experiment']=experiment
        if opt in ("-r","--rfid"):
            rfid=arg.strip()
            d_args['rfid']=rfid

            #a_tp=procTP(arg.strip())
            #i_slice=len(a_tp)
            #d_args['t']=i_slice
        #if opt in ("-r","--resolution"):
        #    i_sres=int(arg.strip())
        #    d_args['s']=i_sres
        
    client, session = createSession(d_args['host'], d_args['user'], d_args['password']);
    updateService = session.getUpdateService();
    queryService = session.getQueryService();
    rawFileStore = session.createRawFileStore();
    rawPixelsStore = session.createRawPixelsStore();
    iPixels = session.getPixelsService();
    gateway = session.createGateway();
    containerService = session.getContainerService();
    a_rawfiles=[] 
    # for the NO FRET control and FRET positive subdirectories get the names of all the files
    #a_dir=[s_dir+'negative/',s_dir+'positive/']
    #for i in range(2):
    #    a_rawfiles.append([])
    #    d_flist=os.listdir(a_dir[i])
    #    for j in range(len(d_flist)):
    #        s_filestr = d_flist[j]
    #        s_fsplit= s_filestr.split('.')
    #        if len(s_fsplit)>1:
    #            if s_fsplit[1]=="raw" : 
    #                a_rawfiles[i].append(a_dir[i]+s_fsplit[0])
    a_rawfiles.append(imagesInDataset(containerService, int(d_args['control'])));
    a_rawfiles.append(imagesInDataset(containerService, int(d_args['experiment'])));
    a_tp = procTP(rawFileStore, queryService, int(d_args['rfid']));
    i_sres = a_rawfiles[0][0].getPixels(0).getSizeX().getValue();
    d_args['s'] = i_sres;
    d_args['t'] = a_rawfiles[0][0].getPixels(0).getSizeC().getValue();
    i_slice = d_args['t'];
    
    # Some output stuff to generate latex doc
    if rank==0:
        os.system('mkdir ' + s_dir);
        os.system('mkdir ' + s_dir+'/out');
        tex=open(s_dir+texfilename,'w')
        tex.write('\\documentclass{/usr/local/share/texmf/proposal} \n')
        tex.write('\\usepackage{graphics} \n')
        tex.write('\\usepackage{rotating} \n')
        tex.write('\\usepackage{ifpdf} \n')
        tex.write('\\setlength{\\oddsidemargin}{-1cm} \n')
        tex.write('\\setlength{\\topmargin}{-1cm} \n')
        tex.write('\\setlength{\\textwidth}{17cm} \n')
        tex.write('\\running{FLIM-mpi Results} \n')
        tex.write('\\runningsub{} \n')
        tex.write('\\begin{document} \n')
        tex.write('\\section*{No FRET Files} \n')
    # The serious stuff starts here
    done=False
    group=0
    fcount=0
    cummean=0
    d_args['n']=1.0
    totalfitcalls=0
    # Loop until all files in both directories are processed
    while not done:
        # make up name for output files for particular file being processed 
        # this could use input file name but this is a shorter consistant form
        s_id=a_rawfiles[group][fcount].getId().getValue();
        # set up meta parameter dictionary that contains useful parameters for subroutines
        # m model type 1 or 2
        # f file name 
        d_args['i']=s_id
        i_mode=group+1
        d_args['m']=i_mode
        d_args['p']=0
        d_args['f']=a_rawfiles[group][fcount].getPixels(0);
        # every process gets a copy of the whole rawdata at for current file at this point
        # this would better if only process of rank 0 did this and then once pixels have been assigned
        # scattered to each process they got just the pixels they need but this works for now
        localraw=procRead(rawPixelsStore, d_args['f'])
        localpix=[]		
        # This is where process rank=0 segments the image and performs global average fit 
        # then generates pixel lists that are to be scattered to each of 
        # the processed so each process knows which pixels it is working on
        if rank==0:
            tex.write("\\verb=%s= \\\\ \n"%d_args['i'])
            start=mpi.Wtime()
            celpix, initfit,tp,cf1,sig,se,ipar=procSEG(localraw,a_tp,d_args)
            tex.write("Status: %d Chisq: %10.5f \\\\ \n"%(initfit.status,initfit.fnorm))
            for i in range(len(ipar)-2):
                tex.write('%s=%10.6f \\\\ \n'%(paranames[i],ipar[i]))
            fig=plt.figure()
            ax=fig.add_subplot(111)
            ax.errorbar(tp,sig,yerr=se,fmt='go')
            ax.plot(tp,cf1,'b-')
            fig.savefig(d_args['d']+'out/fit_'+str(d_args['i'])+'.pdf')
            pixarray=npy.transpose(npy.nonzero(celpix))
            for r in range(size):
                pix=[ r + size*i for i in range(int(float(len(pixarray))/size)+1) if r + size*i < len(pixarray) ]
                localpix.append([])
                for p in pix:
                    localpix[r].append(pixarray[p])
        else:
            ipar=None			
        # scatter list of pixels for processing to each node
        localpixels=comm.scatter(localpix,root=0)
        # broadcast to all process the parameters from the initial averaged fit 
        localparams=comm.bcast(ipar,root=0)
        # Make the local grids for the processes to fill in the parameter values they calculate
        # Again here each process has full size grid but only fill in the pixels it has been assigned
        g=npy.zeros((i_sres,i_sres),npy.float)
        a1=npy.zeros((i_sres,i_sres),npy.float)
        k1=npy.zeros((i_sres,i_sres),npy.float)
        f=npy.zeros((i_sres,i_sres),npy.float)
        b=npy.zeros((i_sres,i_sres),npy.float)
        c=npy.zeros((i_sres,i_sres),npy.float)
        if i_mode>1:
            a2=npy.zeros((i_sres,i_sres),npy.float)
            k2=npy.zeros((i_sres,i_sres),npy.float)
        # for each pixel process has been assigne fo the fit
        for idx in localpixels:
            # PUT FITTING CALL HERE  only fit none edge pixels
            if idx[0]>0 and idx[0]<i_sres and idx[1]>0 and idx[1]<i_sres:
                s=localraw.reshape((i_slice,i_sres,i_sres))[:,idx[0]-1:idx[0]+1,idx[1]-1:idx[1]+1].mean(axis=1).mean(axis=1)
                e=localraw.reshape((i_slice,i_sres,i_sres))[:,idx[0]-1:idx[0]+1,idx[1]-1:idx[1]+1].std(axis=1).std(axis=1)
                e=npy.zeros(len(s))+1
                fit,tp,cf1,sig,se,fpar=procFitIt(s,e,a_tp,i_mode,d_args,localparams)
                totalfitcalls+=1
                c[idx[0],idx[1]]=fit.fnorm
                g[idx[0],idx[1]]=fit.status
                if i_mode >1:
                    a1[idx[0],idx[1]]=fit.params[0]/(fit.params[0]+fit.params[3])
                    k1[idx[0],idx[1]]=fit.params[1]
                    b[idx[0],idx[1]]=fit.params[2]
                    a2[idx[0],idx[1]]=fit.params[3]/(fit.params[0]+fit.params[3])
                    k2[idx[0],idx[1]]=fit.params[4]
                    f[idx[0],idx[1]]=(1.0-k1[idx[0],idx[1]]/k2[idx[0],idx[1]])
                else:
                    a1[idx[0],idx[1]]=fit.params[0]
                    k1[idx[0],idx[1]]=fit.params[1]
                    b[idx[0],idx[1]]=fit.params[2]
        # Gather the results from all nodes
        rg=comm.gather(g,root=0)
        ra1=comm.gather(a1,root=0)
        rk1=comm.gather(k1,root=0)
        rb=comm.gather(b,root=0)
        rc=comm.gather(c,root=0)
        rf=comm.gather(f,root=0)
        if i_mode>1:
            ra2=comm.gather(a2,root=0)
            rk2=comm.gather(k2,root=0)
        # Once process rank=0 has gathered all the grids of pixels from the other processes collate them 
        # to produce one grid with all the values in
        if rank == 0:
            gg=npy.zeros((i_sres,i_sres),npy.float)
            ga1=npy.zeros((i_sres,i_sres),npy.float)
            gk1=npy.zeros((i_sres,i_sres),npy.float)
            gf=npy.zeros((i_sres,i_sres),npy.float)
            gb=npy.zeros((i_sres,i_sres),npy.float)
            gc=npy.zeros((i_sres,i_sres),npy.float)
            if i_mode>1:
                ga2=npy.zeros((i_sres,i_sres),npy.float)
                gk2=npy.zeros((i_sres,i_sres),npy.float)
            for i in range(len(rg)):
                gg+=npy.array(rg[i])
                ga1+=npy.array(ra1[i])
                gk1+=npy.array(rk1[i])
                gf+=npy.array(rf[i])
                gb+=npy.array(rb[i])
                gc+=npy.array(rc[i])
                if i_mode>1:
                    ga2+=npy.array(ra2[i])
                    gk2+=npy.array(rk2[i])
            # AT THIS POINT NEED TO CALCULATE DISTRIBUTION AND SELECT NO-FRET LIFE TIME FOR GROUP1
            # Calculate no fret decay from pixel-by-pixel fit histogram for k1
            # generate histogram magic number 1000 bins				
            k1h=npy.histogram(gk1[npy.where(gk1>0)], bins=1000)
            # find midpoint of bins
            mp=0.5*(k1h[1][0:-1]+k1h[1][1:])
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
            tex.write('Mean %10.5f\\\\ \n'%nmean)
            end=mpi.Wtime()
            tex.write('Time %9.5f\\\\ \n'%((end-start)))
            cummean+=nmean
            # THIS BIT IS WHERE PROCESS RANK=0 generates output for use in the tex document
            # output pictures
            fig=plt.figure()
            n,bins,patches=plt.hist(gk1[npy.where(gk1>0)],1000,normed=1,histtype='bar')
            fig.savefig(s_dir+'out/gk1h_'+str(s_id)+'.pdf')
            fig.clf()
            if i_mode>1:
                fig=plt.figure()
                n,bins,patches=plt.hist(gk2[npy.where(gk2>0)],1000,normed=1,histtype='bar')
                fig.savefig(s_dir+'out/gk2h_'+str(s_id)+'.pdf')
                fig.clf()
            fig=plt.figure()
            plt.matshow(ga1,False,cmap=cycm1)
            fig.savefig(s_dir+'out/ga1_'+str(s_id)+'.png')
            fig.clf()
            fig=plt.figure()
            plt.matshow(gk1,False,cmap=cycm1)
            fig.savefig(s_dir+'out/gk1_'+str(s_id)+'.png')
            fig.clf()
            fig=plt.figure()
            plt.matshow(gb,False,cmap=cycm1)
            fig.savefig(s_dir+'out/gb_'+str(s_id)+'.png')
            fig.clf()
            fig=plt.figure()
            plt.matshow(gc,False,cmap=cycm1)
            fig.savefig(s_dir+'out/gc_'+str(s_id)+'.png')
            fig.clf()
            fig=plt.figure()
            plt.matshow(gf,False,cmap=cycm1)
            fig.savefig(s_dir+'out/gf_'+str(s_id)+'.png')
            fig.clf()
            fig=plt.figure()
            plt.matshow(gg,False,cmap=cycm1)
            fig.savefig(s_dir+'out/gg_'+str(s_id)+'.png')
            fig.clf()
            # This is where process rank=0 saves the csv files with the parameters
            
            npy.savetxt(s_dir+'out/gg_'+str(s_id)+'.txt',gg,fmt='%10.5f',delimiter=',')
            npy.savetxt(s_dir+'out/ga1_'+str(s_id)+'.txt',ga1,fmt='%10.5f',delimiter=',')
            npy.savetxt(s_dir+'out/gk1_'+str(s_id)+'.txt',gk1,fmt='%10.5f',delimiter=',')
            npy.savetxt(s_dir+'out/gf_'+str(s_id)+'.txt',gf,fmt='%10.5f',delimiter=',')
            npy.savetxt(s_dir+'out/gb_'+str(s_id)+'.txt',gb,fmt='%10.5f',delimiter=',')
            npy.savetxt(s_dir+'out/gc_'+str(s_id)+'.txt',gc,fmt='%10.5f',delimiter=',')
            attachFileToImage(gateway, queryService,  updateService, rawFileStore, s_id, s_dir+'out/gg_'+str(s_id)+'.txt');
            attachFileToImage(gateway, queryService,  updateService, rawFileStore, s_id, s_dir+'out/ga1_'+str(s_id)+'.txt');
            attachFileToImage(gateway, queryService,  updateService, rawFileStore, s_id, s_dir+'out/gk1_'+str(s_id)+'.txt');
            attachFileToImage(gateway, queryService,  updateService, rawFileStore, s_id, s_dir+'out/gf_'+str(s_id)+'.txt');
            attachFileToImage(gateway, queryService,  updateService, rawFileStore, s_id, s_dir+'out/gb_'+str(s_id)+'.txt');
            attachFileToImage(gateway, queryService,  updateService, rawFileStore, s_id, s_dir+'out/gc_'+str(s_id)+'.txt');
            if i_mode >1:
                fig=plt.figure()
                plt.matshow(ga2,False,cmap=cycm1)
                fig.savefig(s_dir+'out/ga2_'+str(s_id)+'.png')
                fig.clf()
                fig=plt.figure()
                plt.matshow(gk2,False,cmap=cycm1)
                fig.savefig(s_dir+'out/gk2_'+str(s_id)+'.png')
                fig.clf()
                npy.savetxt(s_dir+'out/ga2_'+str(s_id)+'.txt',ga2,fmt='%10.5f',delimiter=',')
                npy.savetxt(s_dir+'out/gk2_'+str(s_id)+'.txt',gk2,fmt='%10.5f',delimiter=',')
                attachFileToImage(gateway, queryService,  updateService, rawFileStore, s_id, s_dir+'out/ga2_'+str(s_id)+'.txt');
                attachFileToImage(gateway, queryService,  updateService, rawFileStore, s_id, s_dir+'out/gk2_'+str(s_id)+'.txt');
                
            # all stuff for generating the tex document
            tex.write("\\begin{figure}[h!] \n")
            tex.write("\\centering \n")
            tex.write("\\begin{tabular}{cc} \n")
            tex.write("\\includegraphics[scale=0.3]{%sout/gk1h_%s.pdf}& \n"%(s_dir,str(s_id)))
            tex.write("\\includegraphics[scale=0.3]{%sout/fit_%s.pdf}\\\\ \n"%(s_dir,str(s_id)))
            tex.write('$tau_{1}$ distribution& Global fit\\\\')
            tex.write("\\end{tabular} \n")
            tex.write("\\end{figure} \n")
            tex.write("\\begin{figure}[h!] \n")
            tex.write("\\centering \n")            
            tex.write("\\begin{tabular}{ccc} \n")
            tex.write("\\includegraphics[scale=0.2]{%sout/ga1_%s.png}& \n"%(s_dir,str(s_id)))
            tex.write("\\includegraphics[scale=0.2]{%sout/gk1_%s.png}& \n"%(s_dir,str(s_id)))
            tex.write("\\includegraphics[scale=0.2]{%sout/gb_%s.png}\\\\ \n"%(s_dir,str(s_id)))
            tex.write('$\\alpha_{1}$&$\\tau_{1}$&$b$\\\\ \n')
            if i_mode>1:
                tex.write("\\includegraphics[scale=0.2]{%sout/ga2_%s.png}& \n"%(s_dir,str(s_id)))
                tex.write("\\includegraphics[scale=0.2]{%sout/gk2_%s.png}& \n"%(s_dir,str(s_id)))
                tex.write("\\includegraphics[scale=0.2]{%sout/gf_%s.png}\\\\ \n"%(s_dir,str(s_id)))
                tex.write('$\\alpha_{2}$&$\\tau_{2}$&$E_{f}$\\\\ \n')
            tex.write("\\includegraphics[scale=0.2]{%sout/gc_%s.png}& \n"%(s_dir,str(s_id)))
            tex.write("\\includegraphics[scale=0.2]{%sout/gg_%s.png}\\\\ \n"%(s_dir,str(s_id)))
            tex.write('$\\chi^{2}$&$fit$\\\\ \n')
            tex.write("\\end{tabular} \n")
            tex.write("\\end{figure} \n")
        
        ncalls=npy.asarray(0,'i')
        # This line syncs the processes so they wait for process rank=0 to do its collating and output before getting next file
        comm.Allreduce([npy.asarray(totalfitcalls,'i'),mpi.INT], [ncalls,mpi.INT],op=mpi.SUM)
        if rank==0:
            tex.write('Total pixels fitted=%d\\\\ \n'%(ncalls))
            tex.flush()
        fcount+=1
        # Here the cumlative mean no-fret life time is calculated and the group is changed once all the no-fret files have been processed
        if fcount>=len(a_rawfiles[group]):
            group+=1
            if group>1:
                if rank==0:
                    cummean/=float(fcount)
                    tex.write('Cumlative mean fret %10.5f \\\\ \n'%cummean)
                    d_args['n']=cummean
                    cummean=0
                done=True
            else:
                if rank==0:
                    cummean/=float(fcount)
                    tex.write('Cumlative mean no-fret %10.5f \\\\ \n'%cummean)
                    tex.write('\\section*{FRET conditions} \n')
                    d_args['n']=cummean
                    cummean=0
                fcount=0
        if rank==0:
            tex.write('\\newpage\n')            
            tex.write("\\noindent")
    if rank==0:
        tex.write('\\end{document}\n')
        tex.close()
	os.system('pdflatex -output-directory='+s_dir+' '+s_dir+texfilename);
        pdfFile = texfilename.rsplit('.tex')[0]+'.pdf';
        attachFileToProject(containerService, queryService,  updateService, rawFileStore, getParentProject(containerService, int(d_args['experiment'])).getId().getValue(),  s_dir+pdfFile);
	#-------------------------

if __name__ == "__main__":
    sys.exit(mpi_run(sys.argv[1:]))

