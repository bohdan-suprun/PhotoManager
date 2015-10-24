package edu.nure.net;

import edu.nure.listener.ResponseListener;
import edu.nure.listener.results.DBResult;
import edu.nure.listener.results.DBSelectResult;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by bod on 01.10.15.
 */

class XMLParser extends Thread {
    private static PriorityBlockingQueue<SimpleResponse> tasks;
    private static CopyOnWriteArrayList<ResponseListener> listeners;

    static {
        tasks = new PriorityBlockingQueue<SimpleResponse>();
        listeners = new CopyOnWriteArrayList<ResponseListener>();
    }

    XMLParser(){
        super();
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            while (true) {
                SimpleResponse response = tasks.take();
                parse(response);
            }
        }catch (InterruptedException ex){
            System.err.println("Interrupted");
        }
    }

    private void parse(SimpleResponse response){
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            MyParser handler = new MyParser(response.getPerformer());
            System.out.println(new String(response.getResponse()));
            parser.parse(new ByteArrayInputStream(response.getResponse()), handler);

        } catch (ParserConfigurationException e) {
            notifyListeners(new DBResult(-1, 600, "PARSE Ошибка чтения ответа сервера"+e.getMessage()));
        } catch (SAXException e) {
            notifyListeners(new DBResult(-1, 600, "SAX Ошибка чтения ответа сервера"+e.getMessage()));
        } catch (IOException e) {
            notifyListeners(new DBResult(-1, 600, "IO Ошибка чтения ответа сервера"+e.getMessage()));
        }


    }

    static void putTask(SimpleResponse resp){
        tasks.add(resp);
    }

    static void addListener(ResponseListener listener){
        listeners.add(listener);
    }

    static void notifyListeners(DBResult result){
        for(ResponseListener listener: listeners) {
            chooseMethod(result, listener);
        }
    }

    static void notifyMe(DBResult result, ResponseListener listener){
        chooseMethod(result, listener);
    }

    private static void chooseMethod(DBResult result, ResponseListener listener){
        int status = result.getStatus();
        int action = result.getAction();
        if (status == 200){

           if (action >= 100 && action <= 199) listener.doInsert((DBSelectResult)result);
           if (action >= 200 && action <= 299 || action == 1) listener.doSelect((DBSelectResult)result);
           if (action >= 300 && action <= 399) listener.doUpdate(result);
           if (action >= 400 && action <= 499) listener.doDelete(result);
        } else listener.doError(result);
    }
}
