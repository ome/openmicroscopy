function [filename, z, t, c] = parseFileName(str)

[v, j] = regexp(str,'[z][0-9]*[t][0-9]*[c][0-9]*');
idx = v-1;
filename = str(1:idx)
endExp = str(v:j);
[v, j] = regexp(endExp, '[c|t|z][0-9]*');
z = endExp(v(1)+1:j(1));
t = endExp(v(2)+1:j(2));
c = endExp(v(3)+1:j(3));
