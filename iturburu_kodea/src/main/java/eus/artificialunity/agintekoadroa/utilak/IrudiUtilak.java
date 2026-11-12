package eus.artificialunity.agintekoadroa.utilak;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** JavaFX irudiak PNG byte bihurtzeko (PDF txostenerako). */
public final class IrudiUtilak {

    private IrudiUtilak() {
    }

    public static byte[] png(Image irudia) throws IOException {
        ByteArrayOutputStream irteera = new ByteArrayOutputStream();
        ImageIO.write(SwingFXUtils.fromFXImage(irudia, null), "png", irteera);
        return irteera.toByteArray();
    }
}
