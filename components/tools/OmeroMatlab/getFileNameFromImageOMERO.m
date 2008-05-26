function [fileName] = getFileNameFromImageOMERO(serviceFactory, imageId)

image = serviceFactory.getImage(java.lang.Long(imageId));
fileName = char(image.name.val);