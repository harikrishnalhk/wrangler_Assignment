package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TimeDuration implements Token {
    private final long nanoseconds;
    private final String original;

    public TimeDuration(String value) {
        if (value == null || !value.matches("^\\d+(\\.\\d+)?[A-Za-z]+$")) {
        throw new IllegalArgumentException("Invalid time duration format: " + value);
        }
        this.original = value;
        String numStr = value.replaceAll("[^0-9.]", "");
        String unit = value.replaceAll("[0-9.]", "").toUpperCase();
        double num = Double.parseDouble(numStr);
        
        switch (unit) {
            case "NS":
                nanoseconds = (long) num;
                break;
            case "US":
                nanoseconds = (long) (num * 1000);
                break;
            case "MS":
                nanoseconds = (long) (num * 1000 * 1000);
                break;
            case "S":
                nanoseconds = (long) (num * 1000 * 1000 * 1000);
                break;
            case "M":
                nanoseconds = (long) (num * 1000 * 1000 * 1000 * 60);
                break;
            case "H":
                nanoseconds = (long) (num * 1000 * 1000 * 1000 * 60 * 60);
                break;
            case "D":
                nanoseconds = (long) (num * 1000 * 1000 * 1000 * 60 * 60 * 24);
                break;
            default:
                nanoseconds = (long) (num * 1000 * 1000); // default to ms
        }
    }

    public long getNanoseconds() {
        return nanoseconds;
    }

    @Override
    public Object value() {
        return nanoseconds;
    }

    @Override
    public TokenType type() {
        return TokenType.TIME_DURATION;
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("value", original);
        object.addProperty("nanoseconds", nanoseconds);
        object.addProperty("type", type().name());
        return object;
    }
}
