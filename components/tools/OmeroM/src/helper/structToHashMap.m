function hmap = structToHashMap(S)
% Convert a matlab struct to a java HashMap

% See http://stackoverflow.com/questions/436852/storing-matlab-structs-in-java-objects
if ((~isstruct(S)) || (numel(S) ~= 1))
    error('structToHashMap:invalid','%s',...
          'structToHashMap only accepts single structures');
end

hmap = java.util.HashMap;
for fn = fieldnames(S)'
    fn = fn{1};
    hmap.put(fn,getfield(S,fn));
end

