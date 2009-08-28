function plane = getPlaneFromImageId(gateway, imageId, zSlice, channel, timePoint)
% Get the given z,c,t plane from a Pixels object given its Image id.
% This assumes that each Image only has a single Pixels (which is the
% case)
%
% The Gateway object is the service as returned by loadOmero

pixels = gateway.getPixelsFromImage(imageId);
pixelsId = pixels.get(0).getId().getValue();
rawPlane = gateway.getPlane(pixelsId, zSlice, channel , timePoint);
plane = toMatrix(rawPlane, pixels);

% plane2D = omerojava.util.GatewayUtils.getPlane2D(pixels.get(0), rawPlane);
% plane = plane2D.getPixelsArrayAsDouble(1);

end
