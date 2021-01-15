package de.melanx.modlistcreator;

public enum ExportType {
    HTML("html", "htm"),
    MARKDOWN("md", "markdown");

    private final String[] args;

    ExportType(String... args) {
        this.args = args;
    }

    public static ExportType getType(String arg) {
        for (ExportType type : ExportType.values()) {
            for (String argument : type.args) {
                if (arg.equals(argument)) {
                    return type;
                }
            }
        }
        throw new IllegalArgumentException(arg + " is no possible argument.");
    }
}
