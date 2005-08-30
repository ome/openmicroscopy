from java.lang import Throwable
from java.util import Set
from java.util import HashSet

from ome.testing import OMEPerformanceData as Data
from ome.itests import ComparisonUtils as Cmp
from ome.itests import OmeroGrinderTest as Omero
from ome.util  import Utils
from org.openmicroscopy.shoola.env.data.t import ShoolaGrinderTest as Shoola
import org.openmicroscopy.shoola.env.data.model as model

from org.springframework.beans.factory.config import AutowireCapableBeanFactory as Autowire
from java.lang import Boolean as Bool
from org.apache.commons.logging import Log
from org.apache.commons.logging import LogFactory


from pprint import pprint as pp

#################################################################
# SETUP
#
dummy = Omero("Data to be added").init() # To get context
ds = dummy.appContext.getBean("dataSource")
methods=Utils.getObjectVoidMethods(Omero) # Assumes omeromethods=shoolamethods
data = Data(0.02)
omero = Omero(data).init()
omero.setData(data) # Eek
shoola = Shoola(data)
log = LogFactory.getLog("jython.interactive") 
#
#################################################################

log.info("========================================")
log.info(str(data))
log.info("========================================")

#if not Cmp.compare(a,b):
#sz = Utils.structureSize(result)
#Utils.writeXmlToFile(result,"log/"+method[:-2]+".xml")

import java.lang.System as S
p=S.getProperties()
p.setProperty("omeds.url","http://localhost:8888/shoola")
p.setProperty("omeds.user","josh")
p.setProperty("omeds.pass","jmoore1")

def pdi(set):
    p=model.ProjectData
    ps=model.ProjectSummary
    d=model.DatasetData
    dsl=model.DatasetSummaryLinked
    projects=HashSet()
    datasets=HashSet()
    images=HashSet()
    for i in set.iterator():
        if i.class == p or i.class == ps:
            projects.add(i)
            datasets.addAll(i.datasets)
            images.addAll(pdi(i.datasets)[2])
        if i.class == d or i.class ==dsl:
            datasets.add(i)
            images.addAll(i.images)
    return projects,datasets,images

def getAB():
    a=omero.testFindPDIHierarchies()
    b=shoola.testFindPDIHierarchies()
    ia=pdi(a)[2]
    ib=pdi(b)[2]
    ias=[i.ID for i in ia.iterator()]
    ibs=[i.ID for i in ib.iterator()]
    ias.sort()
    ibs.sort()
    return a,b,ias,ibs

def Getters(klass):
    getters=[]
    for i in dir(klass):
        if i.startswith("get"):
            getters.append(i)
    return getters
   

def cmp(l1,l2):
    getters=Getters(l1.__class__)
    for g in getters:
        eval("l1."+g+"()")
        eval("l2."+g+"()")

def odir(obj):
    return dir(obj.__class__)

def osuper(obj):
    tmp = obj.getClass()
    res = []
    while (tmp != None):
        res.append(tmp)
        tmp = tmp.superclass
    return res

def adir(obj):
    classes = osuper(obj)
    attrs = []
    for c in classes:
        attrs.extend(dir(c))
    attrs.sort()
    return attrs

def pdir(pkg):
    pp(dir(pkg))

def set2tuple(set):
    tuple=[]
    for i in set.iterator():
        tuple.append(i)
    return tuple

if __name__=="__main__":
    a,b=getAB()[0:2]
    x,y,z=pdi(a)
    u,v,w=pdi(b)
    a=set2tuple(a)
    b=set2tuple(b)
