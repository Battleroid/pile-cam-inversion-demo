import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import com.caseyweed.Pile;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import javax.imageio.ImageIO;

public class Main {

    public static void main(String[] args) throws IOException {
	// init with VGA resolution (640x480)
        Webcam cam = Webcam.getDefault();
        cam.setViewSize(WebcamResolution.VGA.getSize());

        // take photo then save
        cam.open();
        BufferedImage sample = cam.getImage();
        cam.close();

        // save color photo
        ImageIO.write(sample, "PNG", new File("sample.png"));

        /* convert to grayscale, create empty BI that is the same size of original but of Gray type,
        use color conversion to convert color image to gray image. */
        BufferedImage gray = new BufferedImage(sample.getWidth(null), sample.getHeight(null), BufferedImage.TYPE_BYTE_GRAY);
        ColorConvertOp op = new ColorConvertOp(sample.getColorModel().getColorSpace(), gray.getColorModel().getColorSpace(), null);
        op.filter(sample, gray);
        ImageIO.write(gray, "PNG", new File("sample_gray.png"));

        // create empty BufferedImage for writing with same dimensions of the gray image
        BufferedImage manipulated = new BufferedImage(gray.getWidth(null), gray.getHeight(null), BufferedImage.TYPE_BYTE_GRAY);

        // go through row/cols and invert values
        for (int row = 0; row < gray.getWidth(); row++) {
            for (int col = 0; col < gray.getHeight(); col++) {
                // get ARGB values
                int[] argb = toARGB(gray.getRGB(row, col));

                // do inversion
                int[] inverted = new int[] {
                        argb[0],
                        255 - argb[1],
                        255 - argb[2],
                        255 - argb[3]
                };

                // convert back to color value then write to inverted image
                int invertedCol = toColor(inverted);
                manipulated.setRGB(row, col, invertedCol);
            }
        }

        // save inverted image
        ImageIO.write(manipulated, "PNG", new File("inverted.png"));

        // create our list of images to add to the pile
        BufferedImage[] images = new BufferedImage[] {
                gray,
                manipulated
        };

        // explicitly create a 2x2 grid with our list of images, while maintaining aspect ratio
        Pile p = new Pile(2, 2, images, true);

        // save pile
        p.savePile("pile_comparison.png");
    }

    /**
     * Convert from 32 bit integer to individual ARGB values based on offsets.
     * @param argb
     * @return int[] in order of ARGB
     */
    public static int[] toARGB(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = (argb & 0xFF);
        return new int[] {a, r, g, b};
    }

    /**
     * Convert int[] with ARGB values back to 32 bit integer for setting BufferedImage pixels
     * @param rgb
     * @return
     */
    public static int toColor(int[] rgb) {
        return (rgb[0] << 24) | (rgb[1] << 16) | (rgb[2] << 8) | rgb[3];
    }
}
