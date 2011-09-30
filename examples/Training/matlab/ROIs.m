% Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
% All rights reversed.
% Use is subject to license terms supplied in LICENSE.txt

% ROIs

% To learn about the model see  http://www.ome-xml.org/wiki/ROI/2010-04.
% Note that annotation can be linked to ROI.  

%Information to edit.
imageId = 27544;

% ROIs
try
    [client, session] = connect();
    proxy = session.getContainerService();
    list = proxy.getImages(omero.model.Image.class, java.util.Arrays.asList(java.lang.Long(imageId)), omero.sys.ParametersI());
    if (list.size == 0)
        exception = MException('OMERO:ROIs', 'Image Id not valid');
        throw(exception);
    end
    
    %Create ROI. In this example, we create an ROI with a rectangular shape and
    %attach it to an image. 
    % First create a rectangular shape.
    rect = omero.model.RectI;
    rect.setX(omero.rtypes.rdouble(0));
    rect.setY(omero.rtypes.rdouble(0));
    rect.setWidth(omero.rtypes.rdouble(10));
    rect.setHeight(omero.rtypes.rdouble(20));
    % indicate on which plane to attach the shape
    rect.setTheZ(omero.rtypes.rint(0));
    rect.setTheT(omero.rtypes.rint(0));

    ellipse = omero.model.EllipseI;
    ellipse.setCx(omero.rtypes.rdouble(10));
    ellipse.setCy(omero.rtypes.rdouble(10));
    ellipse.setRx(omero.rtypes.rdouble(10));
    ellipse.setRy(omero.rtypes.rdouble(10));
    % indicate on which plane to attach the shape
    ellipse.setTheZ(omero.rtypes.rint(0));
    ellipse.setTheT(omero.rtypes.rint(0));
    
    % Create the roi.
    roi = omero.model.RoiI;
    % Attach the shape to the roi, several shapes can be added.
    roi.addShape(rect);
    roi.addShape(ellipse);
    % Link the roi and the image
    roi.setImage(omero.model.ImageI(imageId, false));
    iUpdate = session.getUpdateService(); 
    %save
    roi = iUpdate.saveAndReturnObject(roi);
    % Check that the shape has been added.
    numShapes = roi.sizeOfShapes;
    for ns = 1:numShapes
       shape = roi.getShape(ns-1);
       if (isa(shape, 'omero.model.Rect'))
           %handle rectangle
           rectangle = shape;
           rectangle.getX().getValue()
       elseif (isa(shape, 'omero.model.Ellipse'))
           ellipse = shape;
           ellipse.getCx().getValue()
       elseif (isa(shape, 'omero.model.Point'))
           point = shape;
           point.getX().getValue();
       elseif (isa(shape, 'omero.model.Line'))
           line = shape;
           line.getX1().getValue();
       end
    end

    %Retrieve the roi linked to an image.
    service = session.getRoiService();
    roiResult = service.findByImage(imageId, []);
    rois = roiResult.rois;
    n = rois.size;
    for i = 1:n
       roi = rois.get(i-1);
       shapes = roi.copyShapes();
       %remove the first shape
       if (shapes.size > 0)
           shape = shapes.get(0);
           roi.removeShape(shape);
           roi = iUpdate.saveAndReturnObject(roi);
           roi.getId().getValue()
       end
    end
catch err
    disp(err.message);
end

%Close the session
client.closeSession();