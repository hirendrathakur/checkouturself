package com.flipkart.fashion.services;

import org.opencv.core.Mat;
import org.opencv.core.Point;

/**
 * Created by kinshuk.bairagi on 11/08/17.
 */
public  class IPJService {

  public static   void  overlayImage(Mat background, Mat foreground, Mat output, Point location){
        background.copyTo(output);
        for(int y = (int) Math.max(location.y , 0); y < background.rows(); ++y){
            int fY = (int) (y + location.y);
            if(fY >= foreground.rows())
                break;
            for(int x = (int) Math.max(location.x, 0); x < background.cols(); ++x){
                int fX = (int) (x + location.x);
                if(fX >= foreground.cols()){
                    break;
                }

                double opacity;
                double[] finalPixelValue = new double[4];
                opacity = foreground.get(fY , fX)[3];

                finalPixelValue[0] = background.get(fY, fX)[0];
                finalPixelValue[1] = background.get(fY, fX)[1];
                finalPixelValue[2] = background.get(fY, fX)[2];
                finalPixelValue[3] = background.get(fY, fX)[3];

                for(int c = 0;  c < output.channels(); ++c){
                    if(opacity > 0){
                        double foregroundPx =  foreground.get(fY, fX)[c];
                        double backgroundPx =  background.get(fY, fX)[c];

                        float fOpacity = (float) (opacity / 255);
                        finalPixelValue[c] = ((backgroundPx * ( 1.0 - fOpacity)) + (foregroundPx * fOpacity));
                        if(c==3){
                            finalPixelValue[c] = foreground.get(fY,fX)[3];
                        }
                    }
                }
                output.put(fY, fX,finalPixelValue);
            }
        }
    }
}
