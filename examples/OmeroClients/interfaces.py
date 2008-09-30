from omero_model_EventI import EventI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_GroupExperimenterMapI import GroupExperimenterMapI

assert ! EventI().isMutable()
assert ExperimenterI().isMutable()
assert ExperimenterI().isGlobal()
assert ExperimenterI().isAnnotated()
assert GroupExperimenterMAp().isLink()
