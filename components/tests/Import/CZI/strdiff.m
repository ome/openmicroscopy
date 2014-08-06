% Remove the occurances of string s2 from string s1
% Input: 
% s1 - big string 
% s2 - forbidded string to be removed
% 
% Output: 
% s - string containing s1 without s2
% 
function s = strdiff(s1, s2)

n = length(s2);

f = strfind(s1, s2); % find all occurances of 2nd string in 1st string

if(~isempty(f))
    bad_inds = f;
    for i=1:n-1
        bad_inds = union(bad_inds, f+i);
    end
    good_inds = setdiff(1:length(s1), bad_inds);
    s = s1(good_inds);
else % don't change the string
    s = s1;
end