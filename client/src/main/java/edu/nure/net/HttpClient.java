package edu.nure.net;

import edu.nure.gui.MessagesManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by bod on 06.10.15.
 */
class HttpClient extends Thread{
    private PriorityBlockingQueue<SimpleRequest> pool;
    private CloseableHttpClient client;
    {
        pool = new PriorityBlockingQueue<SimpleRequest>();
    }

    HttpClient(BasicCookieStore store){
        super();
        setDaemon(true);
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return s.equals(sslSession.getPeerHost());
            }
        };
        PoolingHttpClientConnectionManager pooling = new PoolingHttpClientConnectionManager();
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(60000)
                .setConnectTimeout(60000)
                .build();
        client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setMaxConnPerRoute(10000)
                .setMaxConnTotal(10000)
                .setDefaultCookieStore(store)
                .setSSLHostnameVerifier(hostnameVerifier)
                .build();
    }


    public void put(SimpleRequest task){
        pool.add(task);
    }

    private void send(final SimpleRequest request) throws IOException{
        try {
            client.execute(request.getRequest(), new ResponseHandler() {
                @Override
                public Object handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {

                    byte[] buffer = new byte[httpResponse.getEntity().getContent().available()];
                    httpResponse.getEntity().getContent().read(buffer);
                    if (!httpResponse.getLastHeader("content-type").getValue().equals("image/jpg")) {
                        SimpleResponse response = new SimpleResponse(request.getPerformer(), buffer,
                                request.getPriority());
                        XMLParser.putTask(response);
                    } else {
                        request.getPerformer().doBinaryImage(buffer);
                    }
                    return null;
                }
            });
        } catch (IOException ex){
            ex.printStackTrace();
            MessagesManager.errorBox("Ошибка при отправке файла. Попробуйте еще раз.", "Error");
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                SimpleRequest request = pool.take();
                System.out.println(pool.size());
                System.out.println(request.getRequest().toString());
                send(request);
            }
        } catch (InterruptedException ex){
            ex.printStackTrace();
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }
}