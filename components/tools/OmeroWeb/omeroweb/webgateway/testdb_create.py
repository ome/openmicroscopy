#
# webgateway/testdb_create - helper functions to create and interact with the test database fixture
# 
# Copyright (c) 2008, 2009 Glencoe Software, Inc. All rights reserved.
# 
# This software is distributed under the terms described by the LICENCE file
# you can find at the root of the distribution bundle, which states you are
# free to use it only for non commercial purposes.
# If the file is missing please request a copy by contacting
# jason@glencoesoftware.com.
#
# Author: Carlos Neves <carlos(at)glencoesoftware.com>

TESTIMG_NS = 'weblitz.testimage'

ROOT=('root','ome')
GUEST=('weblitz_test_user','foobar')
GUEST_NAME=('User', 'Weblitz')
AUTHOR=('weblitz_test_author','foobar')
AUTHOR_NAME=('Author', 'Weblitz')

PUBLIC_PREFIX='weblitz_test_pub'
PRIVATE_PREFIX='weblitz_test_priv'

TESTIMG_NAME='weblitz_test_image'
TESTPROJ_NAME='weblitz_test_project'
TESTDS_NAME='weblitz_test_dataset'

import sys
sys.path.append('.')

from blitz_config import *

import omero
from omero.rtypes import *
import os

if os.path.exists('etc/ice.config'):
    omero.gateway.BlitzGateway.ICE_CONFIG='etc/ice.config'

def loginAsRoot ():
    return login(*ROOT)
    
def login (user, passwd, trysuper=False):
    global client
    global query
    global update
    global search

    client = omero.client_wrapper(user, passwd, group=trysuper and 'system' or None)
    client.allow_thread_timeout = False
    if not client.connect():
        print "Can not connect" 
        return False
    print "client:",client
    query = client.getQueryService()
    print "query:",query
    update = client.getUpdateService()
    print "update:",update
    search = client.createSearchService()
    print "search:",search
    return True

def getClient ():
    global client
    return client

def createGroup (name):
    a = client.getAdminService()
    g = omero.model.ExperimenterGroupI()
    g.setName(rstring(name))
    a.createGroup(g)
    return a.lookupGroup(name)

def createUser (omename, firstname, lastname, passwd, groupname):
    a = client.getAdminService()
    try:
        a.lookupExperimenter(omename)
        print "Already exists: %s" % omename
        return
    except:
        pass
    try:
        g = a.lookupGroup(groupname)
    except:
        g = omero.model.ExperimenterGroupI()
        g.setName(rstring(groupname))
        a.createGroup(g)
        g = a.lookupGroup(groupname)
    u = omero.model.ExperimenterI()
    u.setOmeName(rstring(omename))
    u.setFirstName(rstring(firstname))
    u.setLastName(rstring(lastname))
    a.createUser(u, g.getName().val)
    a.changeUserPassword(u.getOmeName().val, rstring(passwd))

def deleteGroup (groupname):
    g = a.lookupGroup(groupname)
    a.deleteGroup(g)

def deleteUser (omename, groupname):
    a = client.getAdminService()
    u = a.lookupExperimenter(omename)
    a.deleteExperimenter(u)
    deleteGroup(groupname)

def testProjectName (public=False):
    return (public and PUBLIC_PREFIX or PRIVATE_PREFIX) + '_project'

def testDatasetName (public=False):
    return (public and PUBLIC_PREFIX or PRIVATE_PREFIX) + '_dataset'

def testImageName (public=False):
    return (public and PUBLIC_PREFIX or PRIVATE_PREFIX) + '_image'

def testImageDescription (public=False):
    return testImageName(public) + ' by ' + ' '.join(AUTHOR_NAME)

def getTestProject (client, public=False):
    name = testProjectName(public)
    for p in client.listProjects():
        if p.getName() == name:
            return p
    return None

def getTestDataset (client, project=None, public=False):
    name = testDatasetName(public)
    if project is None:
        project = getTestProject(client, public=public)
    for d in project.listChildren():
        if d.getName() == name:
            return d
    return None

def _getTestImage (client, name, dataset=None, public=False):
    if dataset is None:
        dataset = getTestDataset(client, public=public)
    for i in dataset.listChildren():
        if i.getName() == name:
            return i
    return None

def getTestImage (client, dataset=None, public=False):
    return _getTestImage(client, testImageName(public), dataset, public)

def getTestImage2 (client, dataset=None, public=False):
    return _getTestImage(client, testImageName(public)+'2', dataset, public)

def getBadTestImage (client, dataset=None, public=False):
    return _getTestImage(client, testImageName(public) + '_bad', dataset, public)

def getTinyTestImage (client, dataset=None, public=False):
    return _getTestImage(client, testImageName(public) + '_tiny', dataset, public)

def assertTestGraph (client, public=False):
    p = getTestProject(client, public=public)
    if p is None:
        name = testProjectName(public)
        p = omero.model.ProjectI(loaded=True)
        p.setName(rstring(name))
        p.setDescription(rstring(name))
        p = omero.gateway.ProjectWrapper(client, update.saveAndReturnObject(p))
        print "created project #%i" % p.id
    print ".. have test project"
    d = getTestDataset(client, p, public=public)
    if d is None:
        name = testDatasetName(public)
        d = omero.model.DatasetI(loaded=True)
        d.setName(rstring(name))
        d.setDescription(rstring(name))
        #if not p.datasetLinksLoaded:
        #    p._toggleCollectionsLoaded(True)
        p.linkDataset(d)
        p.save()
        d = getTestDataset(client, p, public=public)
        print "created dataset #%i" % d.id
    print ".. have test dataset"
    return (p,d)

def _putTestImage (client, name, filename, dataset=None, public=False):
    if dataset is None:
        project, dataset = assertTestGraph(client, public)
    img = _getTestImage(client, name, dataset, public=public)
    if img is None:
        fpath = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'testimgs', filename)
        if not os.path.exists(fpath):
            raise IOError('No such file %s' % fpath)
        port = client.c.ic.getProperties().getProperty('omero.port') or '4063'
        exe = 'PATH=$PATH:../bin omero import -s localhost -u %s -w %s -d %i -p %s -n %s %s' % (AUTHOR[0], AUTHOR[1], dataset.getId(), port, name, fpath)
        print exe
        os.system(exe)
        img = _getTestImage(client, name, dataset, public=public)
        img.setDescription(rstring(testImageDescription(public)))
    return img

def putTestImage (client, dataset=None, public=False):
    _putTestImage(client, testImageName(public), 'CHOBI_d3d.dv', dataset, public)
    print ".. have test image"

def putTestImage2 (client, dataset=None, public=False):
    img = _putTestImage(client, testImageName(public)+'2', 'CHOBI_d3d.dv', dataset, public)
    img._loadPixels()
    p = img.getPrimaryPixels()
    if p._physicalSizeX is not None:
        p._physicalSizeX=None
        p._physicalSizeY=None
        p._physicalSizeZ=None
    img.save()
    print ".. have test image without pixel sizes"

def putTinyTestImage (client, dataset=None, public=False):
    _putTestImage(client, testImageName(public) + '_tiny', 'tinyTest.d3d.dv', dataset, public)
    print ".. have tiny test image"

def putBadTestImage (client, dataset=None, public=False):
    if dataset is None:
        project, dataset = assertTestGraph(client, public)
    img = getBadTestImage(client, dataset, public=public)
    if img is None:
        name = testImageName(public) + '_bad'
        img = omero.model.ImageI()
        img.setName(rstring(name))
        img.setAcquisitionDate(rtime(0))
        if not dataset.imageLinksLoaded:
            dataset._obj._imageLinksSeq = []
            dataset._obj._imageLinksLoaded = True;
        #    dataset._toggleCollectionsLoaded(True)
        dataset.linkImage(img)
        dataset.save()
        img = getBadTestImage(client, dataset, public=public)
        print "created bad image #%i" % img.id
    print ".. have bad test image"

if __name__ == '__main__':
    print "Bootstrapping..."
    loginAsRoot()
    print ".. logged in"
    createUser(GUEST[0], GUEST_NAME[0], GUEST_NAME[1], GUEST[1], '%s_group' % GUEST[0])
    createUser(AUTHOR[0], AUTHOR_NAME[0], AUTHOR_NAME[1], AUTHOR[1], '%s_group' % AUTHOR[0])
    print ".. users created"
    login(*AUTHOR)
    p,d = assertTestGraph(client, public=False)
    putBadTestImage(client, dataset=d, public=False)
    putTestImage(client, dataset=d, public=False)
    putTinyTestImage(client, dataset=d, public=False)
    putTestImage2(client, dataset=d, public=False)
