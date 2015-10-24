package edu.nure.db.entity.constraints;

import java.util.regex.Pattern;

/**
 * Created by bod on 17.09.15.
 */
public class Validator {
    public static final String PHONE_VALIDATOR = "^\\+\\d{2}\\(\\d{3}\\)\\s\\d{3}\\-\\d{2}\\-\\d{2}$";
    public static final String NAME_VALIDATOR = "^[[A-я]\\s'іІїЇєЄъьЁёэЭ]{3,100}$";
    public static final String EMAIL_VALIDATOR = "^([a-z0-9_\\+-]+\\.)*[a-z0-9_\\+-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)*\\.[a-z]{2,6}$";
    public static final String PASS_VALIDATOR = "^[0-9A-z@$\\*#]{5,}$";

    public static String validate(String value, final String pattern) throws ValidationException {
            if (!Pattern.compile(pattern).matcher(value).matches())
                throw new ValidationException("Неверный формат данных: проверьте правильность введенных данных");
            return value;
    }

    public static Object validate(Object value, Comparable cmp) throws ValidationException{
        if(cmp.compareTo(value) != 0)
            throw new ValidationException(value.toString());
        return value;
    }
}


