function [count] = numImages(ImageName)
 count = 0;
 for i = 1:length(ImageName)
    if(~strcmp(ImageName{i},char('/')))
        count = count+1;
    end
end