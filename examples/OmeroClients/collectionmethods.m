import omero.model.*;

image = ImageI(1, true);
image.getDetails().setUpdateEvent( EventI(1, false) );

% On creation, all collections are
% initialized to empty, and can be added
% to.
assert(image.sizeOfDatasetLinks() == 0);
dataset = DatasetI(1, false);
link = image.linkDataset(dataset);
assert(image.sizeOfDatasetLinks() == 1);

% If you want to work with this collection,
% you'll need to get a copy.
links = image.copyDatasetLinks();

% When you are done working with it, you can
% unload the datasets, assuming the changes
% have been persisted to the server.
image.unloadDatasetLinks();
assert(image.sizeOfDatasetLinks() < 0);
try
    image.linkDataset( DatasetI() );
catch ME
    % Can't access an unloaded collection
end

% The reload...() method allows one instance
% to take over a collection from another, if it
% has been properly initialized on the server.
% sameImage will have it's collection unloaded.
sameImage = ImageI(1, true);
sameImage.getDetails().setUpdateEvent( EventI(1, false) ); 
sameImage.linkDataset( DatasetI(1, false) );
image.reloadDatasetLinks( sameImage );
assert(image.sizeOfDatasetLinks() == 1);
assert(sameImage.sizeOfDatasetLinks() < 0);

% If you would like to remove all the member
% elements from a collection, don't unload it
% but "clear" it.
image.clearDatasetLinks();
% Saving this to the database will remove
% all dataset links!

% Finally, all collections can be unloaded
% to use an instance as a single row in the db.
image.unloadCollections();

% Ordered collections have slightly different methods.
image = ImageI(1, true);
image.addPixels( PixelsI() );
image.getPixels(0);
image.getPrimaryPixels(); % Same thing
image.removePixels( image.getPixels(0) );
