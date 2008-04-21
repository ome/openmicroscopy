function [image] = getPlaneOMERO(gateway, imageId, channel, time , zSection)

image = gateway.getPlane(imageId, channel, time, zSection);