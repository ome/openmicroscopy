function [mat] = toMatrix(binaryData, pixels)
% TOMATRIX convert the binaryData from the server to a matlab matrix.
%
% Input
%       binaryData: The data obtained using store.getPlane(...)
%
%       pixels: the pixels object of the image the plane came from.
%
% Output
%       mat: a matrix of size m x n or m x n x l
%            where m, n and l correspond to the x,y and z-dimensions of the
%            pixels
%.

% Input check
assert(isnumeric(binaryData) && isvector(binaryData),...
    'OMERO:toMatrix:wrongInputType', 'Invalid binary data input');
assert(isa(pixels,'omero.model.PixelsI'),...
    'OMERO:toMatrix:wrongInputType', 'Invalid pixels input');

% Cast the binary data into the appropriate format
type = char(pixels.getPixelsType().getValue().getValue());
if strcmp(type,'float'), type = 'single'; end
a = typecast(binaryData, type);

% Check binary and pixels dimensions
nElements = numel(a);
sizeX= pixels.getSizeX().getValue();
sizeY= pixels.getSizeY().getValue();
sizeZ= pixels.getSizeZ().getValue();
planeSize = sizeX * sizeY;
stackSize = sizeX * sizeY * sizeZ;
assert(ismember(nElements, [planeSize, stackSize]), ...
    'OMERO:toMatrix:dimSize',...
    'Length of binary data does not match the pixel dimensions');

% Reshape linear binary data
if nElements == planeSize
    b = reshape(a, sizeX, sizeY); % Convert a plane into a matrix
else
    b = reshape(a, sizeX, sizeY, sizeZ); % Convert a stack into a matrix
end

mat = swapbytes(b);