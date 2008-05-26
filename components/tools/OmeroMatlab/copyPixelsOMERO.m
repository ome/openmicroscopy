function [newPixelsId] = copyPixelsOMERO(serviceFactory, pixelsId, channelList, methodology)

import omero.*;
channels = java.util.ArrayList;
for i = 1:length(channelList),
    channels.add(java.lang.Integer(channelList(i)));
end
pixels = getPixels(serviceFactory,pixelsId);
sizeX = pixels.sizeX.val;
sizeY = pixels.sizeY.val;
sizeT = pixels.sizeT.val;
sizeZ = pixels.sizeZ.val;

newPixelsId = serviceFactory.copyPixels(pixelsId, sizeX, sizeY, sizeT, sizeZ, channels, java.lang.String(methodology));