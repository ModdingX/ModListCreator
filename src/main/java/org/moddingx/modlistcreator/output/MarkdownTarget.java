package org.moddingx.modlistcreator.output;

import java.net.URI;
import java.util.Stack;

public class MarkdownTarget implements OutputTarget {

    private final StringBuilder sb = new StringBuilder();
    private final Stack<Integer> lists = new Stack<>();

    @Override
    public void addHeader(String content) {
        this.sb.append("## ").append(content).append("\n\n");
    }

    @Override
    public void addParagraph(String content) {
        this.sb.append(content).append("\n\n");
    }

    @Override
    public void beginList(boolean numbered) {
        this.lists.push(numbered ? 1: 0);
    }

    @Override
    public void addListElement(String content) {
        this.sb.append("  ".repeat(this.lists.size()));
        if (this.lists.peek() == 0) {
            this.sb.append("* ");
        } else {
            int nextIdx = this.lists.pop();
            this.sb.append(nextIdx).append(". ");
            this.lists.push(nextIdx + 1);
        }
        this.sb.append(content).append("\n");
    }

    @Override
    public void endList() {
        this.lists.pop();
    }

    @Override
    public String formatLink(String text, URI url) {
        return "[" + text + "](" + url + ")";
    }

    @Override
    public String result() {
        return this.sb.toString();
    }
}
