package edu.nure.gui.image;

import edu.nure.db.entity.Image;
import edu.nure.gui.WrapLayout;
import edu.nure.gui.image.model.ImageModel;
import edu.nure.listener.ImageDeleted;
import edu.nure.listener.ModelChanged;

import javax.swing.*;
import java.awt.event.MouseAdapter;

/**
 * Created by bod on 05.10.15.
 */
public class AlbumPanel extends JPanel {
    private ImageModel model;
    private MouseAdapter previewClicked;
    
    public AlbumPanel(final ImageModel m, MouseAdapter click){
        previewClicked = click;
        setLayout(new WrapLayout());
        setModel(m);

        ImageView.getWindow().addImageDeletedListener(new ImageDeleted() {
            @Override
            public void imageDeleted(Image im) {
                model.remove(im);
            }
        });
    }

    private void repaintModel(){
        removeAll();
        System.out.println("size - "+model.size()+" "+model.getAlbumName().getName()+this);
        synchronized (model) {
            for (int i = 0; i < model.size(); i++) {
                add(new PreviewImageLabel(model.get(i), previewClicked));
            }
        }
    }

    @Override
    public void setVisible(boolean v){
        super.setVisible(v);
    }

    public ImageModel getModel(){
        return model;
    }

    public JScrollPane getScrolled(){
        JScrollPane pane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setViewportView(this);
        return pane;
    }

    public void setModel(ImageModel m){
        this.model = m;
        this.model.addListener(new ModelChanged() {
            @Override
            public void modelChanged() {
                repaintModel();
            }
        });
        repaintModel();
    }

}
