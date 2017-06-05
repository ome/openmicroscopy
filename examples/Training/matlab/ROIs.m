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
  
    % Information to edit
    imageId = p.imageid;
    
    % Load image
    image = getImages(session, imageId);
    assert(~isempty(image), 'OMERO:ROIs', 'Image id not valid');
    
% Create shapes
% =============

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
        if (isa(shape, 'omero.model.Rectangle'))
            %handle rectangle
            x = shape.getX().getValue();
            y = shape.getY().getValue();
            width = shape.getWidth().getValue();
            height = shape.getHeight().getValue();
            fprintf(1, '  Rectangle X: %g, Y: %g, Width: %g, Height: %g\n',...
                x, y, width, height);      
        elseif (isa(shape, 'omero.model.Ellipse'))
            x = shape.getX().getValue();
            y = shape.getY().getValue();
            radiusx = shape.getRadiusX().getValue();
            radiusy = shape.getRadiusY().getValue();
            fprintf(1, '  Ellipse X: %g, Y: %g, RadiusX: %g, RadiusY: %g\n',...
                x, y, radiusx, radiusy);
        elseif (isa(shape, 'omero.model.Point'))
            x = shape.getX().getValue();
            y = shape.getY().getValue();
            fprintf(1, '  Point X: %g, Y: %g\n', x, y);
        elseif (isa(shape, 'omero.model.Line'))
            x1 = shape.getX1().getValue();
            x2 = shape.getX2().getValue();
            y1 = shape.getY1().getValue();
            y2 = shape.getY2().getValue();
            fprintf(1, '  Line (X1, Y1): (%g, %g), (X2, Y2): (%g, %g)\n',...
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

    % Create and apply transforms to shapes and add them to ROI
    
    % Apply shearing alone to a rectangle (Shear factor set to 10 in the x
    % and y dimensions in this example)
    % create rectangle (shape object)
    rectangle = createRectangle(0, 0, 10, 20);
    rectangle = setShapeCoordinates(rectangle, 0, 0, 0);
    % x and y shear factors
    xShear = 10;
    yShear = 10;
    % create transform object
    newTform = omero.model.AffineTransformI;
    newTform.setA00(rdouble(1));
    newTform.setA10(rdouble(yShear));
    newTform.setA01(rdouble(xShear));
    newTform.setA11(rdouble(1));
    newTform.setA02(rdouble(0));
    newTform.setA12(rdouble(0));
    % apply transform
    rectangle.setTransform(newTform);
    
    % Apply rotation alone to an ellipse object
    % (angle of rotation set to 10 degrees)
    % create ellipse (shape object)
    ellipse = createEllipse(0, 0, 10, 20);
    setShapeCoordinates(ellipse, 0, 0, 0);
    % set angle of rotation
    theta = 10;
    % create transform object
    newTform = omero.model.AffineTransformI;
    newTform.setA00(rdouble(cos(theta)));
    newTform.setA10(rdouble(-sin(theta)));
    newTform.setA01(rdouble(sin(theta)));
    newTform.setA11(rdouble(cos(theta)));
    newTform.setA02(rdouble(0));
    newTform.setA12(rdouble(0));
    % apply transform
    ellipse.setTransform(newTform);
    
    % Apply translation alone to a point object (10 pixels in the x and y
    % direction)
    % create point (shape object)
    point = createPoint(5, 4);
    setShapeCoordinates(point, 0, 0, 0);
    % translation parameters
    xTranslate = 10;
    yTranslate = 10;
    % create transform object
    newTform = omero.model.AffineTransformI;
    newTform.setA00(rdouble(1));
    newTform.setA10(rdouble(0));
    newTform.setA01(rdouble(0));
    newTform.setA11(rdouble(1));
    newTform.setA02(rdouble(xTranslate));
    newTform.setA12(rdouble(yTranslate));
    % apply transform
    point.setTransform(newTform);
    
    % Apply a scale change alone to a polygon object (set scale factor to 10
    % in the x  and y direction)
    % create polygon (shape object)
    polygon = createPolygon([1 5 10 8], [1 5 5 10]);
    setShapeCoordinates(polygon, 0, 0, 0);
    % scaling parameters
    xScale = 10;
    yScale = 10;
    % create transform object
    newTform = omero.model.AffineTransformI;
    newTform.setA00(rdouble(xScale));
    newTform.setA10(rdouble(0));
    newTform.setA01(rdouble(0));
    newTform.setA11(rdouble(yScale));
    newTform.setA02(rdouble(0));
    newTform.setA12(rdouble(0));
    % apply transform
    polygon.setTransform(newTform);
    
    % Create the roi.
    roi = omero.model.RoiI;
    % Attach the shape to the roi, several shapes can be added.
    roi.addShape(rectangle);
    roi.addShape(ellipse);
    roi.addShape(point);
    roi.addShape(polygon);
    % Link the roi and the image
    roi.setImage(omero.model.ImageI(imageId, false));
    % Save the ROI
    roi = session.getUpdateService().saveAndReturnObject(roi);
    fprintf(1, 'Created ROIs with Transform objects %g\n', roi.getId().getValue());
    
    % Check that the transforms have been added to the shapes.
    fprintf(1, 'Reading shapes attached to ROI %g\n', roi.getId().getValue());
    nShapes = roi.sizeOfShapes;
    fprintf(1, 'Found %g shapes\n', nShapes);
    for i = 1 : nShapes
        shape = roi.getShape(i - 1);
        
        %http://blog.openmicroscopy.org/data-model/future-plans/2016/06/20/shape-transforms/
        transform = shape.getTransform;
        xScaling = transform.getA00.getValue;
        xShearing = transform.getA01.getValue;
        xTranslation = transform.getA02.getValue;
            
        yScaling = transform.getA11.getValue;
        yShearing = transform.getA10.getValue;
        yTranslation = transform.getA12.getValue;
        
        %tformMatrix = [A00, A10, 0; A01, A11, 0; A02, A12, 1];
        tformMatrix = [xScaling, yShearing, 0; xShearing, yScaling, 0; xTranslation, yTranslation, 1];
        
        fprintf(1, 'Shape Type : %s\n', char(shape.toString));
        fprintf(1, 'xScaling : %s\n', num2str(tformMatrix(1,1)));
        fprintf(1, 'yScaling : %s\n', num2str(tformMatrix(2,2)));
        fprintf(1, 'xShearing : %s\n', num2str(tformMatrix(2,1)));
        fprintf(1, 'yShearing : %s\n', num2str(tformMatrix(1,2)));
        fprintf(1, 'xTranslation: %s\n', num2str(tformMatrix(3,1)));
        fprintf(1, 'yTranslation: %s\n', num2str(tformMatrix(3,2)));
    end
%%
% end-code
%%

catch err
    client.closeSession();
    throw(err);
end

% Close the session
client.closeSession();
