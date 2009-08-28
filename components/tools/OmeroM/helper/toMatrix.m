% toMatrix: This function converts the binaryData from the server to a
% matlab matrix.
% binaryData: The data obtained using gateway.getPlane(...)
% pixels: the pixels object of the image the plane came from.
function [mat] = toMatrix(binaryData, pixels)
a = typecast(binaryData, char(pixels.getPixelsType().getValue().getValue()));
b = reshape(a, pixels.getSizeX().getValue(), pixels.getSizeY().getValue());
mat = swapbytes(b);
