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
    
    %% Shapes creation
    % Create a rectangle
    disp('Create rectangular shape');
    rectangle = createRectangle(0, 0, 10, 20);
    rectangle.setFillColor(rint(hex2dec('7DFF0000')));    % 'aRGB' - 50% red
    rectangle.setStrokeColor(rint(hex2dec('FF000000')));  % 'aRGB' - 100% black
    rectangle.setTextValue(rstring('Rectangle'));
    % indicate on which plane to attach the shape
    rectangle = setShapeCoordinates(rectangle, 0, 0, 0);

    % Create an ellipse
    disp('Create ellipsoidal shape');
    ellipse = createEllipse(10, 10, 10, 10);
    ellipse.setFillColor(rint(hex2dec('7D0000FF')));    % 'aRGB' - 50% blue
    ellipse.setStrokeColor(rint(hex2dec('FF000000')));  % 'aRGB' - 100% black
    ellipse.setTextValue(rstring('Ellipse'));
    % indicate on which plane to attach the shape
    ellipse = setShapeCoordinates(ellipse, 0, 0, 1);
    
    % Create a point
    disp('Create point shape');
    point = createPoint(5, 4);
    point.setTextValue(rstring('Point'));
    % indicate on which plane to attach the shape
    point = setShapeCoordinates(point, 0, 0, 2);
    
    % Create a line
    disp('Create line shape');
    line = createLine([10 15], [10 20]);
    line.setStrokeColor(rint(hex2dec('FF000000')));  % 'aRGB' - 100% black
    line.setTextValue(rstring('Line'));
    % indicate on which plane to attach the shape
    line = setShapeCoordinates(line, 0, 0, 3);
    
    % Create a polyline
    disp('Create polyline shape');
    polyline = createPolyline([1 5 10 8], [1 5 5 10]);
    polyline.setStrokeColor(rint(hex2dec('FF000000')));  % 'aRGB' - 100% black
    polyline.setTextValue(rstring('Polyline'));
    % indicate on which plane to attach the shape
    polyline = setShapeCoordinates(polyline, 0, 0, 4);
    
    % Create a polygon
    disp('Create polygon shape');
    polygon = createPolygon([1 5 10 8], [1 5 5 10]);
    polygon.setStrokeColor(rint(hex2dec('FF000000')));  % 'aRGB' - 100% black
    polygon.setTextValue(rstring('Polygon'));
    % indicate on which plane to attach the shape
    polygon = setShapeCoordinates(polygon, 0, 0, 5);
    
    % Create the roi.
    roi = omero.model.RoiI;
    % Attach the shape to the roi, several shapes can be added.
    roi.addShape(rectangle);
    roi.addShape(ellipse);
    roi.addShape(point);
    roi.addShape(line);
    roi.addShape(polyline);
    roi.addShape(polygon);
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
        elseif (isa(shape, 'omero.model.Point'))
            cx = shape.getCx().getValue();
            cy = shape.getCy().getValue();
            fprintf(1, '  Point x: %g, y: %g\n', cx, cy);
        elseif (isa(shape, 'omero.model.Line'))
            x1 = shape.getX1().getValue();
            x2 = shape.getX2().getValue();
            y1 = shape.getY1().getValue();
            y2 = shape.getY2().getValue();
            fprintf(1, '  Line (x1, y1): (%g, %g), (x2, y2): (%g, %g)\n',...
                x1, y1, x2, y2);
        elseif isa(shape, 'omero.model.Polyline')
            points = shape.getPoints().getValue();
            fprintf(1, '  Poyline: %s\n', char(points));
        elseif (isa(shape, 'omero.model.Polygon'))
            points = shape.getPoints().getValue();
            fprintf(1, '  Polygon: %s\n', char(points));
        end
    end

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
    m = true(sizeY/8 - 4, sizeX/8 - 4);
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
