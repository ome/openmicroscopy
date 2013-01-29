function [matlabList] = toMatlabList(arraylist)
% Convert a Java ArrayList into a MATLAB vector

% Check input
ip = inputParser;
ip.addRequired('arraylist', @(x) isa(x, 'java.util.ArrayList'));
ip.parse(arraylist);

% Initialize Matlab list
matlabList = zeros(arraylist.size(), 1);
for i=0:arraylist.size()-1,
 matlabList(i+1)=arraylist.get(i);
end
