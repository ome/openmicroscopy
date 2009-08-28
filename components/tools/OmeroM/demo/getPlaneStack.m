function [stack] = getPlaneStack(gateway, pixelsId, channel, timePoint)
% Return a full 3D stack for the given channel and timePoint.
% This is like calling getPlaneFromPixelsId for all Z slices.
%
% The Gateway object is the service as returned by loadOmero

pixels = gateway.getPixels(pixelsId);
sizeZ = pixels.getSizeZ().getValue();
sizeX = pixels.getSizeX().getValue();
sizeY = pixels.getSizeY().getValue();
stack = zeros(sizeZ, sizeX, sizeY);
for zSection = 1:sizeZ
    rawPlane = gateway.getPlane(pixelsId, zSection-1, channel, timePoint);
    plane = toMatrix(rawPlane, pixels);
    
    %plane2D = omerojava.util.GatewayUtils.getPlane2D(pixels,rawPlane);
    %plane = plane2D.getPixelsArrayAsDouble(1);
    stack(zSection,:,:) = plane;
end
