import omero
from omero_model_EventI import EventI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupExperimenterLinkI import ExperimenterGroupExperimenterLinkI

assert ( not EventI().isMutable() )
assert ExperimenterI().isMutable()
assert ExperimenterI().isGlobal()
assert ExperimenterI().isAnnotated()
assert ExperimenterGroupExperimenterLinkI().isLink()
