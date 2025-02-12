package eu.nexabg.unischedule.model;

import org.jsoup.nodes.Element;

public abstract class Schedule {

    public Element line;

    public Schedule(Element line) {
        this.line = line;
    }
}
