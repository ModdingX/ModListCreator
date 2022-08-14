package org.moddingx.modlistcreator.output;

import org.jsoup.nodes.Element;

import java.net.URI;
import java.util.Stack;

public class HtmlTarget implements OutputTarget {
    
    private final Element tag = new Element("body");
    private final Stack<Element> lists = new Stack<>();

    @Override
    public void addHeader(String content) {
        this.tag.appendChild(new Element("h2").append(content));
    }

    @Override
    public void addSubHeader(String content) {
        this.tag.appendChild(new Element("h3").append(content));
    }

    @Override
    public void addParagraph(String content) {
        this.tag.appendChild(new Element("p").append(content));
    }

    @Override
    public void beginList(boolean numbered) {
        this.lists.push(new Element(numbered ? "ol" : "ul"));
    }

    @Override
    public void addListElement(String content) {
        this.lists.peek().appendChild(new Element("li").append(content));
    }

    @Override
    public void endList() {
        Element elem = this.lists.pop();
        if (this.lists.isEmpty()) {
            this.tag.appendChild(elem);
        } else {
            this.lists.peek().appendChild(elem);
        }
        this.tag.appendChild(new Element("br"));
    }

    @Override
    public String formatLink(String text, URI url) {
        return new Element("a").attr("href", url.toString()).append(text).outerHtml();
    }

    @Override
    public String result() {
        return this.tag.html();
    }
}
