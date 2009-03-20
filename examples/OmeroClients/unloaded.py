import omero

image = ImageI()                # A loaded object by default
assert image.isLoaded()
image.unload()                  # can then be unloaded
assert ! image.isLoaded()

image = ImageI( 1L, false )     # Creates an unloaded "proxy"
assert ! image.isLoaded()

image.getId()                   # Ok
try:
    image.getName()             # No data access is allowed other than id.
except:
    pass
