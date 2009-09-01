function [correct] = getPlaneStackTest(gateway, pixelsId, channel, timepoint)

correct = 1;
stack = getPlaneStack(gateway, pixelsId, channel, timepoint);
pixels = gateway.getPixels(pixelsId);
[zSections, X, Y] = size(stack);
if(zSections ~= pixels.getSizeZ().getValue())
    correct = 0;
end
if(X ~= pixels.getSizeX().getValue())
    correct = 0;
end
if(Y ~= pixels.getSizeY().getValue())
    correct = 0;
end
