# The script is executed just once by each worker
# process and defines the TestRunner class. The Grinder creates an
# instance of TestRunner for each worker thread, and repeatedly calls
# the instance for each run of that thread.

from java.lang import Throwable
from net.grinder.script.Grinder import grinder
from net.grinder.script import Test
from net.grinder.statistics import ExpressionView, StatisticsIndexMap, StatisticsView
from java.util import Set
from java.util import HashSet
from org.openmicroscopy.omero.itests import ComparisonUtils as Cmp
from org.openmicroscopy.omero.tests import OMEPerformanceData as Data
from org.openmicroscopy.omero.itests import OmeroGrinderTest as Omero
from org.openmicroscopy.omero.util  import Utils
from org.openmicroscopy.shoola.env.data.t import ShoolaGrinderTest as Shoola
from org.springframework.beans.factory.config import AutowireCapableBeanFactory as Autowire
from java.lang import Boolean as Bool


#################################################################
# SETUP
#
dummy = Omero("Data to be added").init() # To get context
ds = dummy.appContext.getBean("dataSource")
methods=Utils.getObjectVoidMethods(Omero) # Assumes omeromethods=shoolamethods

# Create a Test with a test number and a description. The test will be
# automatically registered with The Grinder console if you are using
# it.
oTests = [Test(i, "omero."+name+"()") for name, i in zip(methods, range(len(methods)))]
sTests = [Test(i+len(methods), "shoola."+name+"()") for name, i in zip(methods, range(len(methods)))]

# A shorter alias for the grinder.logger.output() method and
# statistics
log = grinder.logger.output

# Our own statistics
szIndex = StatisticsIndexMap.getInstance().getLongIndex("userLong1")
szDetailView = StatisticsView()
szDetailView.add(ExpressionView("Size", "statistic.size", "userLong1"))
grinder.registerDetailStatisticsView(szDetailView)
grinder.registerSummaryStatisticsView(szDetailView) ## TODO THIS IS NOT ACCURATE!
#
#################################################################

# A TestRunner instance is created for each thread.
# It can be used to store thread-specific data.
class TestRunner:
   
    def __init__(self):
	self.percent = 0.0333
	self.increase = 0.05
	self.run = 0
	self.omero = None
	self.shoola = None
	
    def getData(self):
	#self.percent += self.increase * self.run 
	self.run += 1
	data = Data(self.percent)
	data.setDataSource(ds)
	data.init()
	log(" ")
	log("========================================")
        log(str(data))
	log("========================================")
	log(" ")
	print(str(self.percent))#+":"+str(data.imgsPDI))
	
	return data
            
    # This method is called for every run.
    def __call__(self):

	data = self.getData()
	_omero = Omero(data).init()
	_omero.setData(data) # Eeek.
	_shoola = Shoola(data)
        
	grinder.statistics.delayReports = 1
	for o,s in zip(oTests,sTests):
		self.omero = o.wrap(_omero)
		self.shoola = s.wrap(_shoola)
		a = self.doIt( o.description )
		b = self.doIt( s.description )

		if not Cmp.compare(a,b):
			log("xxxxxxxxxxxxxxxxxxxxxxxxxxx")
			log(o.description + " and " + s.description + "differ.")
			log("o="+str(a))
			log("s="+str(b))
			log("xxxxxxxxxxxxxxxxxxxxxxxxxxx")

    def doIt(self,method):
	success=1
	result=None
	try:
		result=eval("self."+method)
		sz = Utils.structureSize(result)
		Utils.writeXmlToFile(result,"log/"+method[:-2]+".xml")
		grinder.statistics.setValue(szIndex, sz)        
		log("Return from method "+method+" : ( "+str(sz)+")")
		log(str(result))
		log(" ")
	except Throwable, inst:
		result="Error occurred in "+method
		log("------------------------------------------")
		log(result+" :")
		t = inst
		while (t != None):
			log(str(t))
			next = inst.cause
			if t != next:
				t = next
			else:
				t = None		
		log(" ")
		log("Stack Trace:")
		for s in inst.stackTrace:
			log(str(s))
		log("------------------------------------------")
		success=0
	grinder.statistics.setSuccess(success)
	grinder.statistics.report()
	return result

