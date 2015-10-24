package edu.nure.gui.image;

import edu.nure.db.entity.Image;
import edu.nure.listener.ResponseAdapter;
import edu.nure.listener.ResponseListener;
import edu.nure.listener.results.DBResult;
import edu.nure.net.HttpManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

/**
 * Created by bod on 05.10.15.
 */
public class PreviewImageLabel extends JLabel{
    private Image image;

    public PreviewImageLabel(final Image image, MouseAdapter click) {
        this.image = image;
        addMouseListener(click);
        loadImage();
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setImage(Image image) {
        this.image = image;
        loadImage();
    }


    public Image getImage(){
        return image;
    }

    private void loadImage(){
        if(image != null && image.getImage() == null) {
            HttpManager.getManager().loadPreview(image.getId(), image.getAlbum(), getListener());
        } else {
            if (image != null)
                showImage();
            if (image == null)
                setIcon(new ImageIcon("src/main/resources/unknown.jpg"));
        }
    }

    private ResponseListener getListener(){
        return new ResponseAdapter() {
            @Override
            public void doError(DBResult result) {
                setVisible(false);
            }

            @Override
            public void doBinaryImage(byte[] im) {
                System.out.println("Loaded image");
                image.setImage(im);
                showImage();
            }
        };
    }

    private void showImage(){
        setIcon(new ImageIcon(image.getImage()));
        setVisible(true);
    }

}
