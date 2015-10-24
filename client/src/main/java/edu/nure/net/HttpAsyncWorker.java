package edu.nure.net;

import edu.nure.listener.results.DBResult;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by bod on 01.10.15.
 */
class HttpAsyncWorker extends Thread {
    private CloseableHttpAsyncClient httpClient;
    private BasicCookieStore cookieStore;
    private PriorityBlockingQueue<SimpleRequest> requests;
    int cnt = 0;

    HttpAsyncWorker(BasicCookieStore store){
        super();
        cookieStore = store;
        setDaemon(true);
        requests = new PriorityBlockingQueue<SimpleRequest>();
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return s.equals(sslSession.getPeerHost());
            }
        };
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(3000)
                .setConnectTimeout(3000).build();
        httpClient = HttpAsyncClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setMaxConnPerRoute(10000)
                .setMaxConnTotal(10000)
                .setDefaultCookieStore(cookieStore)
                .setSSLHostnameVerifier(hostnameVerifier)
                .build();

    }

    private void send(final SimpleRequest request){
        cnt++;
        System.out.println(cnt);
        httpClient.execute(request.getRequest(), new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse httpResponse) {
                try {
                    cnt--;
                        byte[] buffer = new byte[httpResponse.getEntity().getContent().available()];
                        httpResponse.getEntity().getContent().read(buffer);
                    if(!httpResponse.getLastHeader("content-type").getValue().equals("image/jpg")) {
                        SimpleResponse response = new SimpleResponse(request.getPerformer(), buffer,
                                request.getPriority());
                        XMLParser.putTask(response);
                    } else{
                        request.getPerformer().doBinaryImage(buffer);
                    }
                } catch (IOException e) {
                    XMLParser.notifyListeners(new DBResult(-1, 600, "Внутрення ошибка приложения"));
                }
            }

            @Override
            public void failed(Exception e) {
               // put(request);
                //XMLParser.notifyListeners(new DBResult(-1, 600, "Ошибка при выполнении запроса "+cnt));
                //e.printStackTrace();
                System.err.println("timeout");
            }

            @Override
            public void cancelled() {
                //put(request);
                //XMLParser.notifyListeners(new DBResult(-1, 600, "Запрос отменен"));
                System.err.println("canceled");

            }
        });
    }

    void put(SimpleRequest request){
        requests.add(request);
    }

    public BasicCookieStore getCookieStore() {
        return cookieStore;
    }

    @Override
    public void run() {
        try {
            httpClient.start();
            while (true) {
                SimpleRequest request = requests.take();
                System.out.println(request.getRequest().toString());
                send(request);
            }
        } catch (InterruptedException ex){
            ex.printStackTrace();
        }
    }


}
