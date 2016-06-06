% Copyright (C) 2011-2014 University of Dundee & Open Microscopy Environment.
% All rights reserved.
%
% This program is free software; you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Foundation; either version 2 of the License, or
% (at your option) any later version.
%
% This program is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU General Public License for more details.
%
% You should have received a copy of the GNU General Public License along
% with this program; if not, write to the Free Software Foundation, Inc.,
% 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

% Delete Data

% It is possible to delete Projects, datasets, images, ROIs etc and objects
% linked to them depending on the specified options
try
%%
% start-code
%%

    % Initialize a client and a session using the ice.config file
    % See ConnectToOMERO for alternative ways to initialize a session
    [client, session] = loadOmero();
    p = parseOmeroProperties(client);
    eventContext = session.getAdminService().getEventContext();
    fprintf(1, 'Created connection to %s\n', p.hostname);
    msg = 'Created session for user %s (id: %g) using group %s (id: %g)\n';
    fprintf(1, msg, char(eventContext.userName), eventContext.userId,...
        char(eventContext.groupName), eventContext.groupId);
 
% Delete Image
% ============
   
    % Delete Image. In the following example, we create an image and delete it.
    % First create the image.
    image = omero.model.ImageI;
    image.setName(rstring('image name'))
    image = session.getUpdateService().saveAndReturnObject(image);
    imageId = image.getId().getValue();
    
    % Check the image has been created
    image = getImages(session, imageId);
    assert(~isempty(image), 'OMERO:LoadMetadataAdvanced', 'Image Id not valid');
    fprintf(1, 'Image %g created\n', imageId);
    
    %Create ROI. In this example, we create an ROI with a rectangular shape and
    %attach it to an image to then test deleting the ROIs.
    
    % First create a rectangular shape
    disp('Create rectangular and point shape');
    rectangle = createRectangle(0, 0, 10, 20);
    rectangle = setShapeCoordinates(rectangle, 0, 0, 0);
    point = createPoint(0, 0);
    point = setShapeCoordinates(point, 0, 0, 0);
    
    % Create the roi.
    roi = omero.model.RoiI;
    % Attach the shape to the roi, several shapes can be added.
    roi.addShape(rectangle);
    roi.addShape(point);
    % Link the roi and the image
    roi.setImage(omero.model.ImageI(imageId, false));
    % Save the ROI
    roi = session.getUpdateService().saveAndReturnObject(roi);
    fprintf(1, 'Created ROI %g\n', roi.getId().getValue());
    
    % Retrieve the roi linked to an image.
    fprintf(1, 'Reading ROIs attached to image %g\n', imageId);
    roiResult =  session.getRoiService().findByImage(imageId, []);
    rois = toMatlabList(roiResult.rois);
    fprintf(1, 'Found %g ROI(s)\n', numel(rois));
    
    % Remove all the shapes from the first ROI
    roi = rois(1);
    shapes = roi.copyShapes();
    if (shapes.size > 0)
        fprintf(1, '  Removing %g shapes\n', shapes.size);
        for j = 1 : shapes.size
            roi.removeShape(shapes.get(j-1));
        end
        roi = session.getUpdateService().saveAndReturnObject(roi);
    end
        
    % Delete ROI
    fprintf(1, 'Deleting ROI %g\n', roi.getId().getValue());
    targetObj = java.util.Hashtable;
    targetObj.put('ROI', toJavaList(roi.getId().getValue(), 'java.lang.Long'));
    deleteCommand = omero.cmd.Delete2(targetObj, [], false);
    session.submit(deleteCommand);

    % Delete the image. You can delete more than one image at a time.
    fprintf(1, 'Deleting image %g\n', imageId);
    deleteImages(session, imageId);
    
    % Check the image has been deleted
    pause(5)
    image = getImages(session, imageId);
    assert(isempty(image), 'OMERO:LoadMetadataAdvanced', 'Image not deleted');
    fprintf(1, 'Image %g deleted\n', imageId);
 
%%
% end-code
%%

catch err
    client.closeSession();
    throw(err);
end

% Close the session
client.closeSession();
