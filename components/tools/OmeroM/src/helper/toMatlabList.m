function [matlabList] = toMatlabList(arraylist, varargin)
% Convert a Java ArrayList into a MATLAB vector

% Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
% All rights reserved.
%
% This program is free software; you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Foundation; either version 2 of the License, or
% (at your option) any later version.
%
% This program is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU General Public License for more details.
%
% You should have received a copy of the GNU General Public License along
% with this program; if not, write to the Free Software Foundation, Inc.,
% 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

% Check input
ip = inputParser;
ip.addRequired('arraylist', @(x) isa(x, 'java.util.ArrayList'));
ip.addOptional('classname', '', @ischar);
ip.parse(arraylist, varargin{:});

% Read number of elements
nElements = arraylist.size();
if nElements == 0, matlabList = []; return; end

if isempty(ip.Results.classname),
    % Read class using first element
    classname = class(arraylist.get(0));
else
    classname = ip.Results.classname;
end

% Initialize Matlab list
if ismember(classname, {'int8', 'uint8', 'int16', 'uint16', 'single', 'double'})
    matlabList = zeros(nElements, 1, classname);
else
    castFun = str2func(classname);
    matlabList (1 : arraylist.size()) = castFun();
end

% Fill Matlab array with elements
for i = 0 : nElements - 1,
    matlabList(i+1) = arraylist.get(i);
end
