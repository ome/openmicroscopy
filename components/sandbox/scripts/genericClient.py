#Ice.loadSlice("--all slice/generic.ice")
import Ice
Ice.loadSlice("--all -I../common/target/generated-sources/ -I../common/target/generated-sources/slice slice/generic.ice")
from main.eis.generic import *

comm = Ice.initialize()

# Create a proxy for the invoice factory object.
proxy = comm.stringToProxy("server:tcp -p 10000")

# Narrow the proxy to the proper type.
generic = ServerPrx.checkedCast(proxy)

generic.query(None)

comm.shutdown()
