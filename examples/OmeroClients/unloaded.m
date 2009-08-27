image = omero.model.ImageI();                 % A loaded object by default
assert(image.isLoaded());
image.unload();
assert( ~ image.isLoaded() );                 % can then be unloaded

image = omero.model.ImageI( 1, false );
assert( ~ image.isLoaded() );                 % Creates an unloaded "proxy"

image.getId();                                % Ok.
try
    image.getName();                          % No data access is allowed other than id
catch ME
    % OK
end
