function [result] = goodeval(functionName, goodFunctionList, varargin)
    

if(~inList(functionName, goodFunctionList))  
    return;
end
    
feval(functionName, varargin{:});
end

function [in] = inList(fn, fnList)

in = 0;
for i=1:length(fnList)
    if(strcmp(fn,fnList{i}))
        in =1;
    end
end

end
