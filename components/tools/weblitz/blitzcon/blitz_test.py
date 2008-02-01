#/bin/env python -i

#
# blitz_test.py - testing ground for ICE communication with blitz
# 
# Copyright (c) 2007, 2008 Glencoe Software, Inc. All rights reserved.
# 
# This software is distributed under the terms described by the LICENCE file
# you can find at the root of the distribution bundle, which states you are
# free to use it only for non commercial purposes.
# If the file is missing please request a copy by contacting
# jason@glencoesoftware.com.

# Set up the python include paths
import sys
import time
sys.path.append('/opt/IcePy/python')
sys.path.append('lib')
import os

import omero
import omero_model_ImageI
import Ice
import omero_RTypes_ice


iid = Ice.InitializationData()
props = Ice.createProperties()
props.setProperty("Ice.Default.Router","OMEROGlacier2/router:tcp -p 9998 -h envy.glencoesoftware.com")
iid.properties = props
c = omero.client(['--Ice.Config=etc/ice.config'], iid)

# The properties we are setting through the interface
props = {omero.constants.USERNAME: 'demo1', omero.constants.PASSWORD: '1omed'}
for k,v in props.items():
    c.getProperties().setProperty(k,v)

print "Creating session:",
c.createSession()
print "ok"

#i = omero.model.ImageI()
#i.name = omero.RString("hi")
#c.sf.getUpdateService().saveObject(i)

session = c.getSession() # same as c.sf
print "'session' = getSession()"
query = session.getQueryService()
print "'query' = session.getQueryService()"
# Logged in Experimenter

param = omero.sys.Parameters()
alterego = query.findAllByQuery("from Experimenter as e where e.omeName='%s'" % c.getProperty(omero.constants.USERNAME), param)[0]
print "'alterego' = our experimenter object (name='%s')" % c.getProperty(omero.constants.USERNAME)


# Experimenters
#el = query.findAll("Experimenter", None)
#print "Experimenters:"
#for e in el:
#    print "->", e.getOmeName().val

#pl = query.findAll("Project", None)
pl = query.findAllByQuery("from Project as p where p.details.owner.id=%i" % alterego.id.val, param)

update = session.getUpdateService()

ds = query.get("Dataset", 1383)
img = query.get("Image", 2291)

## Using Pojos to find annotations for a dataset (omero_API_ice)
#p = c.sf.getPojosService()
#an = p.findAnnotations("Dataset", [1], None, None)
#
## Delete an annotation
##**wrong**c.getSession().delete(an[1][0])
#
## Update an annotation
#an[1][0].content = omero.RString("Something changed at %s" % time.ctime())
#update.saveObject(an[1][0])
#
## Create a new annotation
#new_an = omero.model.DatasetAnnotationI()
#new_an.content = omero.RString("Something created at %s" % time.ctime())
#update.saveObject(new_an)

# Update the description on an object
ds.description = omero.RString("Something created at %s" % time.ctime())
update.saveObject(ds)


param = omero.sys.Parameters()
pixels = query.findAllByQuery("from Pixels as p where p.image.id=8", param)[0]

# Attempt at images
re = c.sf.createRenderingEngine()
pd = omero.romio.PlaneDef(0)

#?re.resetDefaults()
re.lookupPixels(long(pixels.id.val))
re.lookupRenderingDef(long(pixels.id.val))
re.load()
pixels = re.getPixels() # pixels is fully loaded now
channels = len(pixels.channels)

tb = session.createThumbnailStore()
tb.setPixelsId(pixels.getId().val)
jpeg = tb.getThumbnail(64,64)


#print
#print "%i channels:" % channels
#print
#for i in range(channels):
#    print "* Channel %i" % i
#    print "  -> Family: %s" % re.getChannelFamily(i).value.val
#    print "  -> Active: %s" % str(re.isActive(i))
#
#print "Trying to turn channel 1 active..."
#re.setActive(1, True)
#for i in range(channels):
#    print "* Channel %i" % i
#    print "  -> Family: %s" % re.getChannelFamily(i).value.val
#    print "  -> Active: %s" % str(re.isActive(i))
#
#
#models = re.getAvailableModels()
#
#channel = pixels.channels[0]
#lc = channel.getLogicalChannel()
#emWave = lc.getEmissionWave();
#explicitColor = channel.getColorComponent();
#pi = lc.getPhotometricInterpretation()
#if not pi.loaded:
#    pi = query.get(pi.__class__.__name__, pi.id.val)

## Dump the rendered images resulting of the iteration of the ZT axis into files
#
#def dumpImages(pd, re, pixels, prefix):
#    pZ = pixels.sizeZ
#    pT = pixels.sizeT
#    for z in range(pZ.val):
#        pd.z = z
#        for t in range(pT.val):
#            pd.t = t
#            f = file('%sZ%i_T%i.jpg' % (prefix, z, t), 'wb')
#            f.write(re.renderCompressed(pd))
#            f.close()
#


#for model in re.getAvailableModels():
#    print "Model: %s" % model.value.val
#    outdir = 'test_output/pixels_%i/%s' % (pixels.id.val, model.value.val)
#    re.setModel(model)
#
#    # Each channel?
#    realoutdir = os.path.join(outdir, 'ch_all/' % c)
#    os.system('mkdir -p ' + realoutdir)



#    for c in range(channels):
#        print "Channel %i active:%s" % (c, str(re.isActive(c)))
        #print "Channel %i (%s) active:%s" % (c, re.getChannelFamily(c), str(re.getActive(c)))
#        re.setActive(c, True)
#
#
#    outdir = 'test_output/pixels_%i/%s' % (pixels.id.val, model.value.val)
#    print "Rendering model %s..." % model.value.val
#    re.setModel(model)
#    realoutdir = os.path.join(outdir, 'ch_all/' % c)
#    os.system('mkdir -p ' + realoutdir)
#    # Assuming all channels start active
#    dumpImages(pd, re, pixels, realoutdir)
#
#    #for c in range(channels):
#    #    re.setActive(c, False)
#    #    
#    #for c in range(channels):
#    #    re.setActive(c, True)
#    #    realoutdir = os.path.join(outdir, 'ch_%i/' % c)
#    #    os.system('mkdir -p ' + realoutdir)
#    #    dumpImages(pd, re, pixels, realoutdir)
#    #    re.setActive(c, False)
#
