import omero

image = omero.model.ImageI()
details = image.getDetails()
# Always available
p = details.getPermissions()
THIS WAS NULL IN THE OTHER BINDINGS!
assert p.isUserRead()
# Available when returned from server
# Possibly modifiable
details.getOwner()
details.setGroup(omero.model.ExperimenterGroupI(1L, False))
# Available when returned from server
# Not modifiable
details.getCreationEvent()
details.getUpdateEvent()
