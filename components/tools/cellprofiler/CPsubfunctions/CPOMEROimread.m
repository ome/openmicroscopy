function [LoadedImage, handles] = OMEROImread(varargin)

% CellProfiler is distributed under the GNU General Public License.
% See the accompanying file LICENSE for details.
%
% Developed by the Whitehead Institute for Biomedical Research.
% Copyright 2003,2004,2005.
%
% Authors:
%   Anne E. Carpenter
%   Thouis Ray Jones
%   In Han Kang
%   Ola Friman
%   Steve Lowe
%   Joo Han Chang
%   Colin Clarke
%   Mike Lamprecht
%   Peter Swire
%   Rodrigo Ipince
%   Vicky Lay
%   Jun Liu
%   Chris Gang
%
% Website: http://www.cellprofiler.org
%
% $Revision: 4553 $

if nargin == 0 %returns the vaild image extensions
    LoadedImage = 'Any file supported by bioformats. (see http://www.openmicroscopy.org.uk/downloads/#bio-formats)';
    return
elseif nargin == 4,
    omeroGateway = varargin{1};
    CurrentFileName = varargin{2};
    c = str2num(varargin{3});
    handles = varargin{4};
    [pixelsId, z, t] = parseFileDetails(CurrentFileName);

    rawPlane = omeroGateway.getPlane(pixelsId, z, c, t);
    pixels = omeroGateway.getPixels(pixelsId);

    pixelType = pixels.getPixelsType.getValue.getValue.toCharArray;
    rawPlane = swapbytes (typecast (rawPlane, pixelType));
    W = pixels.getSizeX.getValue;
    H = pixels.getSizeY.getValue;
    plane2D = zeros(W, H, pixelType);
    for j=1:H
        for i=1:W
            plane2D(i,j)=rawPlane((j-1)*W+i);
        end
    end
    LoadedImage = im2double(plane2D);
end
