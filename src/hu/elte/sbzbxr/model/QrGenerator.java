package hu.elte.sbzbxr.model;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

//Based on: https://crunchify.com/java-simple-qr-code-generator-example/
public class QrGenerator {
    public static void saveQrToFile() {
        String myCodeText = "https://google.com";
        String filePath = "resources/qrCode.png";
        String fileType = "png";
        File output = new File(filePath);

        try{
            // A class containing static convenience methods for locating
            // ImageReaders and ImageWriters, and performing simple encoding and decoding.
            ImageIO.write(Objects.requireNonNull(getQr(myCodeText)), fileType, output);

            System.out.println("\nCongratulation.. You have successfully created QR Code.. \n" +
                    "Check your code here: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static BufferedImage getQr(String toEncode) {
        try {
            int size = 512;

            Map<EncodeHintType, Object> hintTypeObjectEnumMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hintTypeObjectEnumMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            hintTypeObjectEnumMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            QRCodeWriter qrCodeWriter = new QRCodeWriter(); // throws com.google.zxing.WriterException
            BitMatrix bitMatrix = qrCodeWriter.encode(toEncode, BarcodeFormat.QR_CODE, size,
                    size, hintTypeObjectEnumMap);
            int matrixSize = bitMatrix.getWidth();

            // The BufferedImage subclass describes an Image with an accessible buffer of bufferedImage data.
            BufferedImage bufferedImage = new BufferedImage(matrixSize, matrixSize,
                    BufferedImage.TYPE_INT_RGB);

            // Creates a Graphics2D, which can be used to draw into this BufferedImage.
            bufferedImage.createGraphics();

            // This Graphics2D class extends the Graphics class to provide more sophisticated control over geometry, coordinate transformations, color management, and text layout.
            // This is the fundamental class for rendering 2-dimensional shapes, text and images on the Java(tm) platform.
            Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();

            // setColor() sets this graphics context's current color to the specified color.
            // All subsequent graphics operations using this graphics context use this specified color.
            graphics.setColor(Color.white);

            // fillRect() fills the specified rectangle. The left and right edges of the rectangle are at x and x + matrixSize - 1.
            graphics.fillRect(0, 0, matrixSize, matrixSize);
            graphics.setColor(Color.BLACK);

            for (int i = 0; i < matrixSize; i++) {
                for (int j = 0; j < matrixSize; j++) {
                    if (bitMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }

            return bufferedImage;
        } catch (WriterException e) {
            System.out.println("\nSorry.. Something went wrong...\n");
            e.printStackTrace();
        }
        return null;
    }
}