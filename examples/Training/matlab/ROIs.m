% Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
% All rights reversed.
% Use is subject to license terms supplied in LICENSE.txt

% ROIs

% To learn about the model see  http://www.ome-xml.org/wiki/ROI/2010-04.
% Note that annotation can be linked to ROI.
% ROIs
try
    [client, session] = loadOmero();
    fprintf(1, 'Created connection to %s\n', char(client.getProperty('omero.host')));
    fprintf(1, 'Created session for user %s using group %s\n',...
        char(session.getAdminService().getEventContext().userName),...
        char(session.getAdminService().getEventContext().groupName));
    
    % Information to edit
    imageId = str2double(client.getProperty('image.id'));
    
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
    ellipse = setShapeCoordinates(ellipse, 0, 0, 0);
    
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
    
    % Check that the shape has been added.
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
    
    %Retrieve the roi linked to an image.
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
catch err
    disp(err.message);
end

%Close the session
client.closeSession();