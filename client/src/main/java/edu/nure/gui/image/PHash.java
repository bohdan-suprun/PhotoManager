package edu.nure.gui.image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;

/**
 * Created by bod on 04.10.15.
 */
public class PHash {
    public static String hash(byte[] buffer){
        try {
            double[][] matrix = dct(grayScale(resize(buffer)));
            return buildHash(avg(matrix), matrix);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "0";
    }
    private static BufferedImage resize(byte[] buffer) throws IOException {
        Image img = ImageIO.read(new ByteArrayInputStream(buffer));
        int width = img.getWidth(null);
        int height = img.getHeight(null);
        AffineTransform transform = new AffineTransform(
                ((double) 32) / width, 0, 0,
                ((double) 32) / height, 0, 0);
        AffineTransformOp transformer = new AffineTransformOp(transform, new RenderingHints(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC));
        BufferedImage fullImage = ImageIO.read(new ByteArrayInputStream(buffer));
        BufferedImage miniImage = new BufferedImage(32, 32, BufferedImage.TYPE_3BYTE_BGR);
        transformer.filter(fullImage, miniImage);
        return miniImage;
    }

    private static BufferedImage grayScale(BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();
        for(int i=0; i<height; i++){
            for(int j=0; j<width; j++){
                Color c = new Color(image.getRGB(j, i));
                int sum = c.getGreen() + c.getBlue() + c.getRed();
                sum /= 3;
                Color newColor = new Color(sum,sum,sum);
                image.setRGB(j,i,newColor.getRGB());
            }
        }
        return image;
    }
    private static double[][] dct(BufferedImage im){
        int height = im.getHeight();
        int width = im.getWidth();
        double[][] matrix = new double[height][width];
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++) {
                Color c = new Color(im.getRGB(i, j));
                matrix[i][j] = ct(i,j) * c.getRed() * ct(j, i);
            }
        double[][] newMatrix = new double[8][8];
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                newMatrix[i][j] = matrix[i][j];

        return newMatrix;
    }
    private static double ct(int i, int j){
        int n = 32;
        if(i == 0)
            return 1.0/Math.sqrt(n);
        else
            return Math.sqrt(2.0/n)*Math.cos(((2 * j + 1) * i * Math.PI) / (2.0 * n));
    }

    private static double avg(double[][] matrix){
        double sum = 0;
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[0].length; j++)
                sum += matrix[i][j];
        return sum / (matrix.length*matrix[0].length);
    }

    private static String buildHash(double avg, double[][] matrix){
        StringBuilder hash = new StringBuilder();
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[0].length; j++) {
                if(i == 0 && j == 0) continue;
                hash.append((matrix[i][j] >= avg) ? '1' : '0');
            }
        return new BigInteger(hash.toString(),2).toString(16);
    }
    public static void main(String[] args) throws FileNotFoundException, IOException {
        File f = new File("src/main/resources/test.jpg");
        FileInputStream in = new FileInputStream(f);
        byte[] bu = new byte[in.available()];
        in.read(bu);
        System.out.println(hash(bu));

    }
}
