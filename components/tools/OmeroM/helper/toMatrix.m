function [mat] = toMatrix(data, pixels)
a = typecast(data, char(pixels.getPixelsType().getValue().getValue()));
b = reshape(a, pixels.getSizeX().getValue(), pixels.getSizeY().getValue());
mat = swapbytes(b);
