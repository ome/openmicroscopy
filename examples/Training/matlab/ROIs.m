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
% GNU General Public License f4or more details.
%
% You should have received a copy of the GNU General Public License along
% with this program; if not, write to the Free Software Foundation, Inc.,
% 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
% ROIs

% To learn about the model see  http://www.ome-xml.org/wiki/ROI/2010-04.
% Note that annotation can be linked to ROI.
try
    % Initialize a client and a session using the ice.config file
    % See ConnectToOMERO for alternative ways to initialize a session
    [client, session] = loadOmero();
    p = parseOmeroProperties(client);
    eventContext = session.getAdminService().getEventContext();
    fprintf(1, 'Created connection to %s\n', p.hostname);
    msg = 'Created session for user %s (id: %g) using group %s (id: %g)\n';
    fprintf(1, msg, char(eventContext.userName), eventContext.userId,...
        char(eventContext.groupName), eventContext.groupId);
  
    % Information to edit
    imageId = p.imageid;
    
    % Load image
    image = getImages(session, imageId);
    assert(~isempty(image), 'OMERO:ROIs', 'Image id not valid');
    
    %Create ROI. In this example, we create an ROI with a rectangular shape and
    %attach it to an image.
    % First create a rectangular shape.
    disp('Create rectangular shape');
    rectangle = createRectangle(0, 0, 10, 20);
    % indicate on which plane to attach the shape
    rectangle = setShapeCoordinates(rectangle, 0, 0, 0);
    
    % Create an ellipse shape.
    disp('Create ellipsoidal shape');
    ellipse = createEllipse(10, 10, 10, 10);
    % indicate on which plane to attach the shape
    ellipse = setShapeCoordinates(ellipse, 0, 1, 0);
    
    % Create the roi.
    roi = omero.model.RoiI;
    % Attach the shape to the roi, several shapes can be added.
    roi.addShape(rectangle);
    roi.addShape(ellipse);
    % Link the roi and the image
    roi.setImage(omero.model.ImageI(imageId, false));
    % Save the ROI
    roi = session.getUpdateService().saveAndReturnObject(roi);
    fprintf(1, 'Created ROI %g\n', roi.getId().getValue());
    
    % Check that the shapes have been added.
    fprintf(1, 'Reading shapes attached to ROI %g\n', roi.getId().getValue());
    nShapes = roi.sizeOfShapes;
    fprintf(1, 'Found %g shapes\n', nShapes);
    for i = 1 : nShapes
        shape = roi.getShape(i - 1);
        if (isa(shape, 'omero.model.Rect'))
            %handle rectangle
            x = shape.getX().getValue();
            y = shape.getY().getValue();
            width = shape.getWidth().getValue();
            height = shape.getHeight().getValue();
            fprintf(1, '  Rectangle x: %g, y: %g, width: %g, height: %g\n',...
                x, y, width, height);
            
        elseif (isa(shape, 'omero.model.Ellipse'))
            cx = shape.getCx().getValue();
            cy = shape.getCy().getValue();
            rx = shape.getRx().getValue();
            ry = shape.getRy().getValue();
            fprintf(1, '  Ellipse x: %g, y: %g, rx: %g, ry: %g\n',...
                cx, cy, rx, ry);
        end
    end
    
    % Retrieve the roi linked to an image.
    fprintf(1, 'Reading ROIs attached to image %g\n', imageId);
    roiResult =  session.getRoiService().findByImage(imageId, []);
    rois = roiResult.rois;
    nRois = rois.size;
    fprintf(1, 'Found %g ROI(s)\n', nRois);
    for i = 1 : nRois
        roi = rois.get(i-1);
        fprintf(1, '  ROI %g\n', roi.getId().getValue());
        shapes = roi.copyShapes();
        
        % Remove the first shape
        if (shapes.size > 0)
            fprintf(1, '  Removing %g shapes\n', shapes.size);
            for j = 1 : shapes.size
                roi.removeShape(shapes.get(j-1));
            end
            roi = session.getUpdateService().saveAndReturnObject(roi);
        end
    end
    
    % Delete ROI
    deleteCommand = omero.cmd.Delete('/Roi', roi.getId().getValue(), []);
    doAll = omero.cmd.DoAll();
    doAll.requests = toJavaList(deleteCommand);
    session.submit(doAll);
    
    % Create a mask covering half of the image
    pixels = image.getPrimaryPixels();
    sizeX = pixels.getSizeX().getValue();
    sizeY = pixels.getSizeY().getValue();
    m = false(sizeY, sizeX / 2);
    m(end/4:3*end/4,end/2:end)=true;
    mask = createMask(m);
    setShapeCoordinates(mask, 0, 0, 0);
    roi = omero.model.RoiI();
    roi.addShape(mask);
    roi.setImage(omero.model.ImageI(imageId, false));
    roi = session.getUpdateService().saveAndReturnObject(roi);
    fprintf(1, 'Created ROI %g\n', roi.getId().getValue());
    
    % Create a mask shape at a position different from (0, 0)
    m = true(sizeY/8 - 3, sizeX/8 - 5);
    m(end/4 : 3 * end/4, end/4 : 3 * end/4) = false;
    mask = createMask(5 * sizeY / 8, 5 * sizeX/8, m);
    setShapeCoordinates(mask, 0, 0, 0);
    roi = omero.model.RoiI();
    roi.addShape(mask);
    roi.setImage(omero.model.ImageI(imageId, false));
    roi = session.getUpdateService().saveAndReturnObject(roi);
    fprintf(1, 'Created ROI %g\n', roi.getId().getValue());
    
catch err
    client.closeSession();
    throw(err);
end

% Close the session
client.closeSession();
