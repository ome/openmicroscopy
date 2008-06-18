function [name] = getFileName(gateway, imageId)
image = gateway.getImage(java.lang.Long(imageId));
name = char(image.name.val);