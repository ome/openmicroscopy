function plane = getPlaneFromPixelsId(gateway, pixelsId, z, c, t)
% Get the given z,c,t plane from a Pixels object given its id.
% The Gateway object is the service as returned by loadOmero

pixels = gateway.getPixels(pixelsId);
rawPlane = gateway.getPlane(pixelsId, z, c , t);
plane = toMatrix(rawPlane, pixels);

% plane2D = omerojava.util.GatewayUtils.getPlane2D(pixels, rawPlane);
% plane = plane2D.getPixelsArrayAsDouble(1);

end
