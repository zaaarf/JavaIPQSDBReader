package com.ipqualityscore.JavaIPQSDBReader;

public class Column {
    private String Name;
    private String RawValue;
    private Bitmask Type;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getRawValue() {
        return RawValue;
    }

    public void setRawValue(String rawValue) {
        RawValue = rawValue;
    }

    public Bitmask getType() {
        return Type;
    }

    public void setType(Bitmask type) {
        Type = type;
    }
}
