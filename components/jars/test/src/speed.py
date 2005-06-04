# The script is executed just once by each worker
# process and defines the TestRunner class. The Grinder creates an
# instance of TestRunner for each worker thread, and repeatedly calls
# the instance for each run of that thread.

from net.grinder.script.Grinder import grinder
from net.grinder.script import Test
from net.grinder.statistics import ExpressionView, StatisticsIndexMap, StatisticsView
from java.util import Set
from java.util import HashSet
from org.openmicroscopy.omero.tests.client import OMEPerformanceData
from org.openmicroscopy.omero.tests.client import GrinderTest as Omero
from org.openmicroscopy.omero.tests.client import Utils
from org.openmicroscopy.shoola.env.data.t import GrinderTest as Shoola

# A shorter alias for the grinder.logger.output() method.
log = grinder.logger.output

# Create a Test with a test number and a description. The test will be
# automatically registered with The Grinder console if you are using
# it.

data = OMEPerformanceData()
omero = Omero(data)
shoola = Shoola(data)

methods=Utils.getObjectVoidMethods(Omero) # Assumes omeromethods=shoolamethods
oTests = [Test(i, "omero."+name+"()") for name, i in zip(methods, range(len(methods)))]
sTests = [Test(i, "shoola."+name+"()") for name, i in zip(methods, range(len(methods)))]

# Our own statistics
szIndex = StatisticsIndexMap.getInstance().getLongIndex("userLong1")
szDetailView = StatisticsView()
szDetailView.add(ExpressionView("Size", "statistic.size", "userLong1"))
grinder.registerDetailStatisticsView(szDetailView)
grinder.registerSummaryStatisticsView(szDetailView) ## TODO THIS IS NOT ACCURATE!

def grok(method):
    result=eval(method)
    log(str(result))
    sz = Utils.structureSize(result)
    grinder.statistics.setValue(szIndex, sz)        
    grinder.statistics.setSuccess(1)

# A TestRunner instance is created for each thread.
# It can be used to store thread-specific data.
class TestRunner:
    
    # This method is called for every run.
    def __call__(self):

        grinder.statistics.delayReports = 1
        for o,s in zip(oTests,sTests):
		proxy = o.wrap(grok)
        	proxy( o.description )
		proxy = s.wrap(grok)
		proxy( p.description )
            


