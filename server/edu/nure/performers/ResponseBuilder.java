package edu.nure.performers;

import edu.nure.db.entity.Transmittable;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Created by bod on 06.10.15.
 */
public class ResponseBuilder {
    public static final String XML_TYPE = "text/xml; charset=UTF-8";
    public static final String HTML_TYPE = "text/html; charset=UTF-8";
    public static final String IMAGE_TYPE = "image/jpg";
    public static final int STATUS_OK = 200;
    public static final int STATUS_ERROR_WRITE = 400;
    public static final int STATUS_ERROR_READ = 500;
    public static final int STATUS_PARAM_ERROR = 700;
    private HttpServletRequest request;
    private String contentType = XML_TYPE;
    private ByteArrayOutputStream body;
    private int action = -1;
    private String text;
    private int status = STATUS_OK;

    public ResponseBuilder(HttpServletRequest request, int action){
        this.request = request;
        body = new ByteArrayOutputStream();
        this.action = action;
    }

    public String getContentType() {
        return contentType;
    }

    public String getParameter(String name){
        String value = request.getParameter(name);
        if (value != null) value = value.replace("'", "\"");

        return value;
    }

    public int getIntParameter(String name){
        return Integer.valueOf(getParameter(name));
    }

    public double getDoubleParameter(String name){
        return Double.valueOf(getParameter(name));
    }

    public void setContentType(String contentType){
        this.contentType = contentType;
    }

    public void add(Transmittable ent){
        add(ent.toXML());
    }

    public void add(String xml){
        add(xml.getBytes());
    }

    public void add(byte[] date){
        try {
            body.write(date);
        } catch (IOException ex){
            System.err.println(ex.getMessage());
        }
    }

    public void setStatus(int status){
        this.status = status;
    }

    public void setText(String text){
        this.text = text;
    }

    public void setAction(int action){
        this.action = action;
    }

    public void writeTo(OutputStream out) throws IOException{
        if(XML_TYPE.equals(contentType)) {
            out.write("<result>".getBytes());
            out.write(("<action id=\""+action+"\" status=\""+status+"\" ").getBytes());
            if(status == STATUS_PARAM_ERROR)
                setText("Переданы ошибочные параметры");

            if(text != null)
                out.write(("text=\"" + text + "\"").getBytes());
            out.write("/>".getBytes());
            out.write(body.toByteArray());
            out.write("</result>".getBytes());

        } else {
           out.write(body.toByteArray());
        }
    }

    public int getAction(){
        return action;
    }

    public HttpServletRequest getRequest(){
        return request;
    }


}
