function [pixelsList] = getPixelsFromImageOMERO(serviceFactory, imageId)

pixelsList = serviceFactory.getPixelsFromImage(imageId);
%pixelsList = toMatlabList(list);