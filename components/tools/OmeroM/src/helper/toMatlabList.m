function [matlabList] = toMatlabList(arraylist, varargin)
% TOMATLABLIST Convert a Java ArrayList into a MATLAB array or cell array
%
%    matlabList = toMatlabList(arraylist) converts a java.util.ArrayList
%    into a Matlab array or cell array.
%
%    matlabList = toMatlabList(arraylist, classname) converts a
%    java.util.ArrayList into a Matlab array or cell array and cast each
%    element using the input classname.
%
%    Examples:
%
%        matlabList = toMatlabList(arraylist)
%        matlabList = toMatlabList(arraylist, 'int8')
%
% See also: TOJAVALIST

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
numericclasses = {'int8', 'uint8', 'int16', 'uint16', 'int32', 'uint32',...
    'int64', 'uint64', 'single', 'double'};
if ismember(classname, numericclasses)
    matlabList = zeros(nElements, 1, classname);
elseif isequal(classname, 'char')
    matlabList = cell(nElements, 1);
else
    castFun = str2func(classname);
    if nElements > 1
        matlabList(1 : nElements) = castFun();
    else
        matlabList = castFun();
    end
end

% Fill Matlab array with elements
for i = 0 : nElements - 1,
    if iscell(matlabList),
        matlabList{i+1} = arraylist.get(i);
    else
        matlabList(i+1) = arraylist.get(i);
    end
end
