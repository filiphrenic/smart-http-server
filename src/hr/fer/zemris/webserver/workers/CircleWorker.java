package hr.fer.zemris.webserver.workers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import hr.fer.zemris.webserver.IWebWorker;
import hr.fer.zemris.webserver.RequestContext;

public class CircleWorker implements IWebWorker {

    private static final int IMG_SIZE = 200;
    private static final double FACT = 0.6;

    /**
     * Creates an image to present to the client. A green filled circle.
     */
    @Override
    public void processRequest(RequestContext context) {
        BufferedImage image = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_3BYTE_BGR);

        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, IMG_SIZE, IMG_SIZE);
        g2d.setColor(Color.GREEN);
        final int radius = (int) (IMG_SIZE / 2 * FACT);
        final int cor = IMG_SIZE / 2 - radius;
        g2d.fillOval(cor, cor, radius, radius);
        g2d.dispose();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", bos);
            context.setMimeType("image/png");
            context.write(bos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
