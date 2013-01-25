function [matlabList] = toMatlabList(arraylist)
% Convert a Java ArrayList into a MATLAB vector

% Check input
assert(isa(arraylist, 'java.util.ArrayList'),...
    'OMERO:toMatlabList:wrongInputType',...
    'Input must be a Java array');

% Initialize Matlab list
matlabList = zeros(arraylist.size(), 1);
for i=0:arraylist.size()-1,
 matlabList(i+1)=arraylist.get(i);
end
