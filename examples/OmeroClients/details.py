import omero

image = omero.model.ImageI()
details = image.getDetails()
# Always available
p = details.getPermissions()
assert p.isUserRead()
# Available when returned from server
# Possibly modifiable
details.getOwner()
details.setGroup(ExperimenterGroupI(1L, false))
# Available when returned from server
# Not modifiable
details.getCreationEvent()
details.getUpdateEvent()
