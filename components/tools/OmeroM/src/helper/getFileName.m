function [name] = getFileName(gateway, imageId)
image = getImage(gateway, imageId);
name = char(image.name.val);