package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ByteSize implements Token {
    private final long bytes;
    private final String original;

    public ByteSize(String value) {

            if (value == null || !value.matches("^\\d+(\\.\\d+)?[A-Za-z]+$")) {
        throw new IllegalArgumentException("Invalid byte size format: " + value);
            }
        this.original = value;
        String numStr = value.replaceAll("[^0-9.]", "");
        String unit = value.replaceAll("[0-9.]", "").toUpperCase();
        double num = Double.parseDouble(numStr);
        
        switch (unit) {
            case "KB":
                bytes = (long) (num * 1000);
                break;
            case "MB":
                bytes = (long) (num * 1000 * 1000);
                break;
            case "GB":
                bytes = (long) (num * 1000 * 1000 * 1000);
                break;
            case "TB":
                bytes = (long) (num * 1000 * 1000 * 1000 * 1000);
                break;
            case "PB":
                bytes = (long) (num * 1000 * 1000 * 1000 * 1000 * 1000);
                break;
            case "KIB":
                bytes = (long) (num * 1024);
                break;
            case "MIB":
                bytes = (long) (num * 1024 * 1024);
                break;
            case "GIB":
                bytes = (long) (num * 1024 * 1024 * 1024);
                break;
            case "TIB":
                bytes = (long) (num * 1024 * 1024 * 1024 * 1024);
                break;
            case "PIB":
                bytes = (long) (num * 1024 * 1024 * 1024 * 1024 * 1024);
                break;
            case "B":
            default:
                bytes = (long) num;
        }
    }

    public long getBytes() {
        return bytes;
    }

    @Override
    public Object value() {
        return bytes;
    }

    @Override
    public TokenType type() {
        return TokenType.BYTE_SIZE;
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("value", original);
        object.addProperty("bytes", bytes);
        object.addProperty("type", type().name());
        return object;
    }
}
