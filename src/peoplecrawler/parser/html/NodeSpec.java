
package peoplecrawler.parser.html;

/**
 *
 * @author al
 */
public class NodeSpec {

    private final String tagName;
    private final String attribName;
    private final String attribValue;

    public NodeSpec(String tagName,
            String attribName,
            String attribValue) {
        this.tagName = tagName;
        this.attribName = attribName;
        this.attribValue = attribValue;
    }

    String getTagName() {
        return tagName;
    }

    String getAttribName() {
        return attribName;
    }

    String getAttribValue() {
        return attribValue;
    }
}
