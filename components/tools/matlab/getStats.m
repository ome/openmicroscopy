function [stats] = getStats()

a = dir();
fileCount = 1;
for i = 1:length(a), 
    if(~a(i).isdir && strcmp(a(i).name,'.DS_Store')==0)
        imageFile{fileCount} = a(i).name;
        fileCount = fileCount+1;
    end
end;

a = dir('output');
maskFileCount = 1;
for i = 1:length(a), 
    if(~a(i).isdir && strcmp(a(i).name,'.DS_Store')==0)
        imageMaskFile{maskFileCount} = a(i).name;
        maskFileCount = maskFileCount+1;
    end
end;
fileCount = fileCount-1;
maskFileCount = fileCount;

for i = 1:fileCount
    a = imread(imageFile{i},'tiff');
    imageI(i,:,:) = squeeze(a(:,:,1));
    cd 'output';
    a = imread(imageMaskFile{i},'bmp');
    maskI(i,:,:) = squeeze(a(:,:,1));
    cd '..';
end
stats.image = 0;
stats.cellCnt =0;
stats.X = 0;
stats.y = 0;
stats.mean = 0;
stats.stddev = 0;
stats.skew = 0;
stats.kurtosis = 0;
stats.sum = 0;

fout = fopen('outputFile.csv', 'w');
saveHeader(fout, stats);
for i = 8:8
    image = squeeze(imageI(i,:,:)); 
    mask = squeeze(maskI(i,:,:));
    cellCnt = 1;
    for cnt = 1:255,
        [x,y] = find(mask==cnt);
        if(length(x)==0)
            continue;
        end
        maxx = max(x);
        minx = min(x);
        maxy = max(y);
        miny = min(y);
        imagePatch = image(minx:maxx,miny:maxy);
        maskPatch = mask(minx:maxx, miny:maxy);
        [k,l] = size(maskPatch);
        uImagePatch = double(imagePatch);
        uMaskPatch = double(maskPatch==cnt);
        uMaskImage = uImagePatch.*uMaskPatch;
        result = reshape(uMaskImage,k*l,1);
        
        a = imageStats(i, cellCnt, mean(x), mean(y), result);
        size(a)
        stats{i,cellCnt,:} = a;
        saveStats(fout, stats);
        cellCnt = cellCnt+1;
    end
end
fclose(fout);

