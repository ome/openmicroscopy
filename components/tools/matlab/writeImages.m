function writeImages(id, z)
g = createGateway('/Users/donald/OMERO3-TRUNK/dist/etc/ice.config','root','omero')
pixels = g.getPixels(id);
timePoints = pixels.sizeT.val;
for i = 1:timePoints, img(i,:,:) = getPlane(g,id,0,i-1,z);end;

for i = 1:timePoints,
    str = strcat('image_t',num2str(i),'_z',num2str(z),'.tiff');
    image = uint16(squeeze(img(i,:,:)));
    imwrite(image,str,'tiff');
end
end