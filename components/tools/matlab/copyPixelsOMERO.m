function [newPixelsId] = copyPixels(blitzGateway, pixelsId, channelList, methodology)

import omero.*;
channels = java.util.ArrayList;
for i = 1:length(channelList),
    channels.add(java.lang.Integer(channelList(i)));
end
pixels = getPixelsOMERO(blitzGateway,pixelsId);
sizeX = pixels.sizeX.val;
sizeY = pixels.sizeY.val;
sizeT = pixels.sizeT.val;
sizeZ = pixels.sizeZ.val;

newPixelsId = blitzGateway.copyPixels(pixelsId, sizeX, sizeY, sizeT, sizeZ, channels, java.lang.String(methodology));