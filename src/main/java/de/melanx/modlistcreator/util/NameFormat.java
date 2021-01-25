package de.melanx.modlistcreator.util;

public enum NameFormat {
    DEFAULT,
    VERSION,
    NAME,
    NAME_VERSION;

    public static NameFormat get(String value) {
        return NameFormat.valueOf(value.toUpperCase());
    }
}
