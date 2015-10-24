package edu.nure.listener.results;

import edu.nure.db.entity.*;
import edu.nure.db.entity.constraints.ValidationException;
import edu.nure.listener.exception.UnknownTagNameException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by bod on 01.10.15.
 */
public class DBSelectResult extends DBResult {
    private LinkedList<ResultItem> result;

    public DBSelectResult(int action) {
        super(action);
        result = new LinkedList<ResultItem>();
    }

    public void addResult(String tagName, HashMap<String, String> pairs) throws UnknownTagNameException {
        try {
            String pack = "edu.nure.db.entity.";
            tagName = pack + String.valueOf(tagName.charAt(0)).toUpperCase() + tagName.substring(1);
            System.out.println(tagName);
            Class clazz = Class.forName(tagName);
            String[] classNames = {"User", "Format", "Image", "Order", "Right", "Stock", "Urgency", "Album"};
            for(String classCase: classNames){
                if(clazz.getName().equals(pack + classCase)){
                    Method method = this.getClass().getDeclaredMethod("make" + classCase, pairs.getClass());
                    method.setAccessible(true);
                    System.out.println(method.getName());
                    result.add(new ResultItem(clazz, (Transmittable) method.invoke(this, pairs)));
                }
            }
        } catch (ClassNotFoundException e) {
            throw new UnknownTagNameException("Неизвестные данные в ответе сервера class");
        } catch (NoSuchMethodException e) {
            throw new UnknownTagNameException("Неизвестные данные в ответе сервера method");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new UnknownTagNameException("Неизвестные данные в ответе сервера inv");

        } catch (IllegalAccessException e) {
            throw new UnknownTagNameException("Неизвестные данные в ответе сервера illegal");
        }

    }

    private Transmittable makeUser(HashMap<String, String> pairs) throws ValidationException {
        return new User(
                Integer.valueOf(pairs.get("id")),
                pairs.get("name"),
                pairs.get("phone"),
                pairs.get("email"),
                null,
                new Right(pairs.get("right"), null)
        );

    }

    private Transmittable makeOrder(HashMap<String, String> pairs) throws ValidationException {
        Order or = new Order(
                Integer.valueOf(pairs.get("id")),
                Integer.valueOf(pairs.get("customer")),
                Integer.valueOf(pairs.get("responsible")),
                pairs.get("desc"),
                pairs.get("term"),
                Float.valueOf(pairs.get("for_pay")),
                Integer.valueOf(pairs.get("status")),
                Integer.valueOf(pairs.get("urgency"))
        );
        return or;

    }

    private Transmittable makeImage(HashMap<String, String> pairs) throws ValidationException, ParseException {
        return new Image(
                pairs.get("hash"),
                Integer.valueOf(pairs.get("id")),
                null,
                Integer.valueOf(pairs.get("album")),
                new SimpleDateFormat("yyyy-MM-dd").parse(pairs.get("createdIn"))
        );
    }

    private Transmittable makeFormat(HashMap<String, String> pairs) throws ValidationException{
        return new Format(
                pairs.get("name"),
                Integer.valueOf(pairs.get("width")),
                Integer.valueOf(pairs.get("height")),
                Float.valueOf(pairs.get("price"))
        );

    }

    private Transmittable makeRight(HashMap<String, String> pairs) throws ValidationException{
        return new Right(
                pairs.get("type"),
                pairs.get("desc")
        );
    }

    private Transmittable makeUrgency(HashMap<String, String> pairs) throws ValidationException{
        return new Urgency(
                Integer.valueOf(pairs.get("term")),
                Float.valueOf(pairs.get("factor"))
        );
    }

    private Transmittable makeStock(HashMap<String, String> pairs) throws ValidationException{
        return new Stock(
                Integer.valueOf(pairs.get("id")),
                Integer.valueOf(pairs.get("order")),
                Integer.valueOf(pairs.get("image")),
                pairs.get("desc"),
                pairs.get("format")
        );
    }

    private Transmittable makeAlbum(HashMap<String, String> pairs) throws ValidationException{
        return new Album(
                pairs.get("name"),
                Integer.valueOf(pairs.get("id")),
                Integer.valueOf(pairs.get("userId"))
        );
    }

    public ResultItem[] getResult(){
        return result.toArray(new ResultItem[result.size()]);
    }
}
