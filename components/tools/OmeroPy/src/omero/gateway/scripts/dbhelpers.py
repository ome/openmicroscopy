import sys
sys.path.append('.')

import omero
from omero.rtypes import *
import os
import subprocess
import re
import time

BASEPATH = os.path.dirname(os.path.abspath(__file__))

Gateway = omero.gateway.BlitzGateway

def loginAsRoot ():
    return login(ROOT)

def login (alias):
    if isinstance(alias, UserEntry):
        return alias.login()
    return USERS[alias].login()

#def addGroupToUser (client, groupname):
#    a = client.getAdminService()
#    if not 'system' in [x.name.val for x in a.containedGroups(client._userid)]:
#        admin = loginAsRoot()
#        a = admin.getAdminService()
#    else:
#        admin = client
#    try:
#        g = a.lookupGroup(groupname)
#    except:
#        g = omero.model.ExperimenterGroupI()
#        g.setName(rstring(groupname))
#        a.createGroup(g)
#        g = a.lookupGroup(groupname)
#    a.addGroups(a.getExperimenter(client._userid), (g,))

#def setGroupForSession (client, groupname):
#    ssuid = client._sessionUuid
#    ss = client.getSessionService()
#    sess = ss.getSession(ssuid)
#    if sess.getDetails().getGroup().getName().val == groupname:
#        # Already correct
#        return
#    a = client.getAdminService()
#    if not groupname in [x.name.val for x in a.containedGroups(client._userid)]:
#        UserEntry.addGroupToUser(client, groupname)
#    g = a.lookupGroup(groupname)
#    sess.getDetails().setGroup(g)
#    ss.updateSession(sess)

class UserEntry (object):
    def __init__ (self, name, passwd, firstname='', lastname='', groupname=None, admin=False):
        self.name = name
        self.passwd = passwd
        self.firstname = firstname
        self.lastname = lastname
        self.admin = admin
        self.groupname = groupname

    def fullname (self):
        return '%s %s' % (self.firstname, self.lastname)

    def login (self):
        client = Gateway(self.name, self.passwd, group=self.admin and 'system' or None)
        if not client.connect():
            print "Can not connect" 
            return None
        return client

    def create (self, client):
        a = client.getAdminService()
        try:
            a.lookupExperimenter(self.name)
            #print "Already exists: %s" % self.name
            return False
        except:
            #print "Creating: %s" % self.name
            pass
        if self.groupname is None:
            self.groupname = self.name + '_group'
        try:
            g = a.lookupGroup(self.groupname)
        except:
            g = omero.model.ExperimenterGroupI()
            g.setName(rstring(self.groupname))
            a.createGroup(g)
            g = a.lookupGroup(self.groupname)
        u = omero.model.ExperimenterI()
        u.setOmeName(rstring(self.name))
        u.setFirstName(rstring(self.firstname))
        u.setLastName(rstring(self.lastname))
        a.createUser(u, g.getName().val)
        if self.admin:
            u =a.lookupExperimenter(self.name)
            a.addGroups(u,(a.lookupGroup("system"),))
        a.changeUserPassword(u.getOmeName().val, rstring(self.passwd))
        return True

    @staticmethod
    def addGroupToUser (client, groupname):
        a = client.getAdminService()
        if not 'system' in [x.name.val for x in a.containedGroups(client._userid)]:
            admin = loginAsRoot()
            a = admin.getAdminService()
        else:
            admin = client
        try:
            g = a.lookupGroup(groupname)
        except:
            g = omero.model.ExperimenterGroupI()
            g.setName(rstring(groupname))
            a.createGroup(g)
            g = a.lookupGroup(groupname)
        a.addGroups(a.getExperimenter(client._userid), (g,))

    @staticmethod
    def setGroupForSession (client, groupname):
        sess = client._session
        if sess.getDetails().getGroup().getName().val == groupname:
            # Already correct
            return
        a = client.getAdminService()
        if not groupname in [x.name.val for x in a.containedGroups(client._userid)]:
            UserEntry.addGroupToUser(client, groupname)
        g = a.lookupGroup(groupname)
        sess.getDetails().setGroup(g)
        client.getSessionService().updateSession(sess)


class ObjectEntry (object):
    @staticmethod
    def chGroup (client, obj, groupname):
        a = client.getAdminService()
        if not 'system' in [x.name.val for x in a.containedGroups(client._userid)]:
            admin = loginAsRoot()
            a = admin.getAdminService()
        else:
            admin = client
        try:
            g = a.lookupGroup(groupname)
        except:
            g = omero.model.ExperimenterGroupI()
            g.setName(rstring(groupname))
            a.createGroup(g)
            g = a.lookupGroup(groupname)
        ao = admin.getQueryService().find(obj.OMERO_CLASS, obj.getId())
        ao.getDetails().setGroup(g)
        admin.getUpdateService().saveObject(ao)
        

class ProjectEntry (ObjectEntry):
    def __init__ (self, name, owner, create_group=False):
        self.name = name
        self.owner = owner
        self.create_group = create_group

    def get (self, client, fromCreate=False):
        for p in client.listProjects():
            if p.getName() == self.name:
                p.__loadedHotSwap__()
                return p
        return None

    def create (self):
        client = USERS[self.owner].login()
        p = self.get(client)
        if p is not None:
            #print ".. -> project already exists: %s" % self.name
            return p
        #print ".. -> create new project: %s" % self.name
        p = omero.model.ProjectI(loaded=True)
        p.setName(rstring(self.name))
        p.setDescription(rstring(self.name))
        p = omero.gateway.ProjectWrapper(client, client.getUpdateService().saveAndReturnObject(p))
        if self.create_group:
            #print "creating group"
            groupname = 'project_%d' % p.getId()
            UserEntry.addGroupToUser (client, groupname)
            self.chGroup(client, p, groupname)
        return self.get(client, True)

class DatasetEntry (ObjectEntry):
    def __init__ (self, name, project):
        self.name = name
        self.project = project

    def get (self, client, forceproj=None):
        if forceproj is None:
            project = PROJECTS[self.project].get(client)
        else:
            project = forceproj
        for d in project.listChildren():
            if d.getName() == self.name:
                d.__loadedHotSwap__()
                return d
        return None

    def create (self):
        project = PROJECTS[self.project]
        user = USERS[project.owner]
        client = user.login()
        project = project.get(client)
        d = self.get(client, project)
        if d is not None:
            #print ".. -> dataset already exists: %s" % self.name
            return d
        #print ".. -> create new dataset: %s" % self.name
        UserEntry.setGroupForSession(client, project.getDetails().getGroup().getName())
        d = omero.model.DatasetI(loaded=True)
        d.setName(rstring(self.name))
        d.setDescription(rstring(self.name))
        project.linkDataset(d)
        project.save()
        return self.get(client, project)

class ImageEntry (ObjectEntry):
    def __init__ (self, name, filename, dataset):
        self.name = name
        self.filename = filename # If False will create image without pixels
        self.dataset = dataset

    def get (self, client, forceds=None):
        if forceds is None:
            dataset = DATASETS[self.dataset].get(client)
        else:
            dataset = forceds
        for i in dataset.listChildren():
            if i.getName() == self.name:
                return i
        return None

    def create (self):
        dataset = DATASETS[self.dataset]
        project = PROJECTS[dataset.project]
        client = USERS[project.owner].login()
        dataset = dataset.get(client)
        i = self.get(client, dataset)
        if i is not None:
            #print ".. -> image already exists: %s" % self.name
            return i
        #print ".. -> create new image: %s" % self.name
        if self.filename is False:
            UserEntry.setGroupForSession(client, dataset.getDetails().getGroup().getName())
            self._createWithoutPixels(client, dataset)
            return self.get(client, dataset)
        fpath = os.path.join(BASEPATH, self.filename)
        if not os.path.exists(fpath):
            raise IOError('No such file %s' % fpath)
        host = dataset._conn.c.ic.getProperties().getProperty('omero.host') or 'localhost'
        port = dataset._conn.c.ic.getProperties().getProperty('omero.port') or '4063'
        if os.path.exists('../bin/omero'):
            exe = '../bin/omero'
        else:
            exe = 'omero'
        newconn = dataset._conn.clone()
        newconn.connect()
        UserEntry.setGroupForSession(newconn, dataset.getDetails().getGroup().getName())
        session = newconn._sessionUuid
        #print session
        exe += ' import -s %s -k %s -d %i -p %s -n %s %s' % (host, session, dataset.getId(), port, self.name, fpath)
        #print exe
        try:
            p = subprocess.Popen(exe.split(),  shell=False, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        except OSError:
            print "!!Please make sure the 'omero' executable is in PATH"
            return None
        pid = re.search('Saving pixels id: (\d*)', p.communicate()[0]).group(1)
        img = omero.gateway.ImageWrapper(dataset._conn, dataset._conn.getQueryService().find('Pixels', long(pid)).image)
        #print "imgid = %i" % img.getId()
        img.setName(self.name)
        img.save()
        return img

    def _createWithoutPixels (self, client, dataset):
        img = omero.model.ImageI()
        img.setName(rstring(self.name))
        img.setAcquisitionDate(rtime(0))
        if not dataset.imageLinksLoaded:
            print ".!."
            dataset._obj._imageLinksSeq = []
            dataset._obj._imageLinksLoaded = True;
        dataset.linkImage(img)
        dataset.save()

def getProject (client, alias):
    return PROJECTS[alias].get(client)

def assertCommentAnnotation (object, ns, value):
    ann = object.getAnnotation(ns)
    if ann is None or ann.getValue() != value:
        ann = CommentAnnotationWrapper()
        ann.setNs(ns)
        ann.setValue(value)
        object.linkAnnotation(ann)
    return ann

def getDataset (client, alias, forceproj=None):
    return DATASETS[alias].get(client, forceproj)

def getImage (client, alias, forceds=None):
    return IMAGES[alias].get(client, forceds)

def bootstrap ():
    # Create users
    client = loginAsRoot()
    for k, u in USERS.items():
        u.create(client)
    for k, p in PROJECTS.items():
        p.create()
        #print p.get(client).getDetails().getPermissions().isUserWrite()
    for k, d in DATASETS.items():
        d.create()
    for k, i in IMAGES.items():
        i.create()

def cleanup ():
    client = loginAsRoot()
    for k, p in PROJECTS.items():
        p = p.get(client)
        if p is not None:
            update = client.getUpdateService()
            delete = client.getDeleteService()
            for d in p.listChildren():
                delete.deleteImagesByDataset(d.getId(), True)
                update.deleteObject(d._obj)
            #print ".. -> removing project %s" % p.getName()
            update.deleteObject(p._obj)
    # What about users?


ROOT=UserEntry('root','ome',admin=True)

USERS = {
    #'alias': UserEntry entry,
    }

PROJECTS = {
    #'alias': ProjectEntry entry,
    }

DATASETS = {
    #'alias': DatasetEntry entry,
    }

IMAGES = {
    #'alias': ImageEntry entry,
}

