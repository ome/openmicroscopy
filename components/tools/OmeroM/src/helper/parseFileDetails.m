function [imageId, z, t] = parseFileDetails(str)

scanned = sscanf(str,'FileId%dz%dt%d');
imageId = scanned(1);
z = scanned(2);
t = scanned(3);