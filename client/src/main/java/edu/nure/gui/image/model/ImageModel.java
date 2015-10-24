package edu.nure.gui.image.model;

import edu.nure.db.entity.Album;
import edu.nure.db.entity.Image;
import edu.nure.gui.MessagesManager;
import edu.nure.listener.Action;
import edu.nure.listener.ModelChanged;
import edu.nure.listener.ResponseAdapter;
import edu.nure.listener.SelectAdapter;
import edu.nure.listener.results.DBResult;
import edu.nure.listener.results.DBSelectResult;
import edu.nure.listener.results.ResultItem;
import edu.nure.net.HttpManager;
import edu.nure.net.Priority;

import java.util.ArrayList;

/**
 * Created by bod on 14.10.15.
 */
public class ImageModel{

    private final ArrayList<Image> model;
    private Album albumName;
    private final ArrayList<ModelChanged> listeners;
    private volatile boolean started = false;

    public ImageModel(Album albumName) {
        this.albumName = albumName;
        model = new ArrayList<Image>();
        listeners = new ArrayList<ModelChanged>();
    }

    public void addListener(ModelChanged listener){
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    private void fireListener(){
        synchronized (listeners) {
            for (ModelChanged listener : listeners)
                listener.modelChanged();
        }
    }

    public void insert(Image im){
        HttpManager.getManager().sendFile(im,onImageUploaded());
    }

    private ResponseAdapter onImageUploaded(){
        return new ResponseAdapter(){
            @Override
            public void doInsert(DBSelectResult result) {
                Image im = (Image)result.getResult()[0].getEntity();
                addElement(im);
                fireListener();
            }
            @Override
            public void doError(DBResult result) {
                MessagesManager.errorBox("Произошла ошибка во время загрузки изображения", "Ошибка");
            }
        };
    }

    private void addElement(Image im){
        synchronized (model) {
            model.add(im);
        }
    }

    public void remove(Image im){
        synchronized (model){
            for (int i = 0; i < model.size(); i++) {
                if(model.get(i).getId() == im.getId())
                    model.remove(i);
            }
        }
        fireListener();
    }

    public Album getAlbumName() {
        return albumName;
    }

    public void setAlbumName(Album albumName) {
        this.albumName = albumName;
    }

    public Image get(int i){
        synchronized (model) {
            return model.get(i);
        }
    }

    public void load(){
        if(!started) {
            synchronized (model) {
                model.clear();
            }
            started = true;
            loadImages();
        }
    }

    private void loadImages(){
        if(albumName != null)
            HttpManager.getManager().sendGet(HttpManager.HOST, "/image/?action="+ Action.GET_IMAGE+"&albumId="+albumName.getId(),
                    new Priority(Priority.MIN),
                    appendImageAdapter());
    }

    private SelectAdapter appendImageAdapter(){
        return new SelectAdapter(){
            @Override
            public void onImage(DBSelectResult res) {
                for(ResultItem item: res.getResult()){
                    Image theImage = ((Image)item.getEntity());
                    addElement(theImage);
                }
                fireListener();
            }

            @Override
            public void doError(DBResult result) {
                MessagesManager.errorBox("Произошла ошибка во время получения альбома пользователя", "Ошибка");
            }
        };
    }

    public int size(){
        synchronized (model) {
            return model.size();
        }
    }
}
