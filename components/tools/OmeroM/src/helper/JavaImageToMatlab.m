function [C] = JavaImageToMatlab(javaImage)
H = javaImage.getHeight;
W = javaImage.getWidth;
C=uint8(zeros([H,W,3]));
B= javaImage.getData.getPixels(0,0,W,H,[]);
bidx = 1;
for i=1:H
    for j=1:W
        C(i,j,:)=B(bidx:(bidx+2));
        bidx=bidx+3;
    end
end
