package eu.nexabg.unischedule.model;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SSubject extends Schedule {

    public Elements cols;

    public SSubject(Element line, Elements cols) {
        super(line);
        this.cols = cols;
    }
}
