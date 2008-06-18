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
    blitzGateway = varargin{1};
    CurrentFileName = varargin{2};
    c = str2num(varargin{3});
    handles = varargin{4};
    [pixelsId, z, t] = parseFileDetails(CurrentFileName);
    LoadedImage = im2double(getPlane(blitzGateway, pixelsId, z, c, t))/65535;
end
   
    
    