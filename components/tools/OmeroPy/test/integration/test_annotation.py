#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test for adding annotations to Project. 
"""
import unittest, time
import test.integration.library as lib
import omero
import omero.scripts
from omero.rtypes import *
import random
from datetime import datetime

class TestFigureExportScripts(lib.ITest):

    def testAddAnnotations(self):

        print "testAddAnnotations"
        
        # root session is root.sf
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        admin = self.root.sf.getAdminService()

        session = self.root.sf
        client = self.root
        updateService = session.getUpdateService()
        queryService = session.getQueryService()

        # create project
        parent = omero.model.ProjectI()
        parent.name = rstring("Annotations Test")
        parent = updateService.saveAndReturnObject(parent)
        
        xml = "<testXml><testElement><annotation>Text</annotation></testElement></testXml>"
        doubleVal = random.random()
        timeVal = datetime.now().microsecond
        addTag(updateService, parent, "Test-Tag", "test/omero/tag/ns", description=None)
        addComment(updateService, parent, "Test-Comment", ns="test/omero/comment/ns", description=None)
        addXmlAnnotation(updateService, parent, xml, ns="test/omero/xml/ns", description="Test xml annotation description")
        addBooleanAnnotation(updateService, parent, True, ns="test/omero/boolean/ns", description="True if True, otherwise False")
        addDoubleAnnotation(updateService, parent, doubleVal, ns="test/omero/double/ns", description="Random number!")
        addLongAnnotation(updateService, parent, 123456, ns="test/omero/long/ns", description=None)
        addTermAnnotation(updateService, parent, "Metaphase", ns="test/omero/term/ns", description="Metaphase is part of mitosis")
        addTimestampAnnotation(updateService, parent, timeVal, ns="test/omero/timestamp/ns", description=None)
        
        annValues = {"test/omero/tag/ns":["Test-Tag", "getTextValue"],
                    "test/omero/comment/ns":["Test-Comment", "getTextValue"],
                    "test/omero/xml/ns":[xml, "getTextValue"],
                    "test/omero/boolean/ns":[True, "getBoolValue"],
                    "test/omero/double/ns":[doubleVal, "getDoubleValue"],
                    "test/omero/long/ns":[123456, "getLongValue"],
                    "test/omero/term/ns":["Metaphase", "getTermValue"],
                    "test/omero/timestamp/ns":[timeVal, "getTimeValue"]}
        
        # retrieve the annotations and check values. 
        p = omero.sys.Parameters()
        p.map = {}
        p.map["pid"] = parent.getId()     
        query = "select l from ProjectAnnotationLink as l join fetch l.child as a where l.parent.id=:pid and a.ns=:ns"
        for ns, values in annValues.items():
            p.map["ns"] = rstring(ns)
            link = queryService.findByQuery(query, p)     # only 1 of each namespace
            valueMethod = getattr(link.child, values[1])
            self.assertEqual(values[0], valueMethod().getValue(), 
                    "Annotation %s value %s not equal to set value %s" % (link.child.__class__, valueMethod().getValue(), values[0]) )
            
            
def saveAndLinkAnnotation(updateService, parent, annotation, ns=None, description=None):
    """ Saves the Annotation and Links it to a Project, Dataset or Image """
    
    if ns:
        annotation.setNs(rstring(ns))
    if description:
        annotation.setDescription(rstring(description))
    annotation = updateService.saveAndReturnObject(annotation)
    if type(parent) == omero.model.DatasetI:
        l = omero.model.DatasetAnnotationLinkI()
    elif type(parent) == omero.model.ProjectI:
        l = omero.model.ProjectAnnotationLinkI()
    elif type(parent) == omero.model.ImageI:
        l = omero.model.ImageAnnotationLinkI()
    else:
        return
    parent = parent.__class__(parent.id.val, False)
    l.setParent(parent)
    l.setChild(annotation)
    return updateService.saveAndReturnObject(l)
    
# Text Annotations
def addTag(updateService, parent, text, ns=None, description=None):
    """ Adds a Tag. """
    child = omero.model.TagAnnotationI()
    child.setTextValue(rstring(text))
    saveAndLinkAnnotation(updateService, parent, child, ns, description)
       
def addComment(updateService, parent, text, ns=None, description=None):
    """ Adds a Comment. """
    child = omero.model.CommentAnnotationI()
    child.setTextValue(rstring(text))
    saveAndLinkAnnotation(updateService, parent, child, ns, description)

def addXmlAnnotation(updateService, parent, xmlText, ns=None, description=None):
    """ Adds XML Annotation. """
    child = omero.model.XmlAnnotationI()
    child.setTextValue(rstring(xmlText))
    saveAndLinkAnnotation(updateService, parent, child, ns, description)

# Basic Annotations      
def addBooleanAnnotation(updateService, parent, boolean, ns=None, description=None):
    """ Adds a Boolean Annotation. """
    child = omero.model.BooleanAnnotationI()
    child.setBoolValue(rbool(boolean))
    saveAndLinkAnnotation(updateService, parent, child, ns, description)
    
def addDoubleAnnotation(updateService, parent, double, ns=None, description=None):
    """ Adds a Double Annotation. """
    child = omero.model.DoubleAnnotationI()
    child.setDoubleValue(rdouble(double))
    saveAndLinkAnnotation(updateService, parent, child, ns, description)
    
def addLongAnnotation(updateService, parent, longValue, ns=None, description=None):
    """ Adds a Long Annotation. """
    child = omero.model.LongAnnotationI()
    child.setLongValue(rlong(longValue))
    saveAndLinkAnnotation(updateService, parent, child, ns, description)
    
def addTermAnnotation(updateService, parent, term, ns=None, description=None):
    """ Adds a Term Annotation. """
    child = omero.model.TermAnnotationI()
    child.setTermValue(rstring(term))
    saveAndLinkAnnotation(updateService, parent, child, ns, description)

def addTimestampAnnotation(updateService, parent, timeValue, ns=None, description=None):
    """ Adds a Timestamp Annotation. """
    child = omero.model.TimestampAnnotationI()
    child.setTimeValue(rtime(timeValue))
    saveAndLinkAnnotation(updateService, parent, child, ns, description)

if __name__ == '__main__':
    unittest.main()
