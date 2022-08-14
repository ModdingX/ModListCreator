package org.moddingx.modlistcreator.output;

import java.net.URI;
import java.util.function.Supplier;

public interface OutputTarget {
    
    void addHeader(String content);
    void addParagraph(String content);
    void beginList(boolean numbered);
    void addListElement(String content);
    void endList();
    String formatLink(String text, URI url);
    
    String result();

    enum Type {
        
        PLAIN_TEXT("txt", PlainTextTarget::new),
        HTML("html", HtmlTarget::new),
        MARKDOWN("md", MarkdownTarget::new);
        
        public final String extension;
        private final Supplier<? extends OutputTarget> factory;

        Type(String extension, Supplier<? extends OutputTarget> factory) {
            this.extension = extension;
            this.factory = factory;
        }
        
        public OutputTarget create() {
            return this.factory.get();
        }
    }
}
