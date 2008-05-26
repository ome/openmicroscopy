function [filename] = stripExtension(fileext)

for i = 1:length(fileext),
    if (isSpecialChar(char(fileext(i)))==1)
        f(i)='_';
    else
        f(i)=char(fileext(i));
    end
end
filename = {f};

function [b] = isSpecialChar(c)

g = {'!','@','#','$','%','^','&','*','(',')','=','+','{','}',';',':',',','.','<','>','/','?','~','`','±','§',' '};

for i = 1:length(g)
    if(g{i}==c)
        b = 1;
        return;
    end
end
b = 0;
