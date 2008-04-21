function [D] = loadImage(imageNo)
gateway = createGateway('/Users/donald/OMERO3-TRUNK/dist/etc/ice.config','root','omero');
pixels = gateway.getPixels(imageNo);
for i = 1:pixels.sizeZ.val,
    image = getImage(gateway,imageNo,0,0,i-1);
    i
    mask = imread(strcat('/Users/donald/Desktop/output/out/image_',num2str(i),'.bmp'));
    mask = squeeze(mask(:,:,1));
    image = image.*(mask>0);
    image = imresize(image,0.5);
    D(:,:,i)=image;
end
figure;
colordef(gcf,'black')
D = squeeze(D);
[x y z D] = subvolume(D, [nan nan nan nan nan pixels.sizeZ.val]);
p = patch(isosurface(x,y,z,D, 5), 'FaceColor', 'red', 'EdgeColor', 'none');
p2 = patch(isocaps(x,y,z,D, 5), 'FaceColor', 'interp', 'EdgeColor', 'none');
isonormals(x,y,z,D,p);
view(3);
daspect([1 1 .4])
colormap(gray(100))
camva(9);
box on
camlight(40, 40);
camlight(-20,-10);
lighting gouraud
