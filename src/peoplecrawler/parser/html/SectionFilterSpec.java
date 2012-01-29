
package peoplecrawler.parser.html;

/**
 *
 * @author al
 */
public class SectionFilterSpec {

    private final String tagName;
    private final String attribName;
    private final String attribValue;
    private final short filterAction;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SectionFilterSpec other = (SectionFilterSpec) obj;
        if ((this.tagName == null) ? (other.tagName != null) : !this.tagName.equals(other.tagName)) {
            return false;
        }
        if ((this.attribName == null) ? (other.attribName != null) : !this.attribName.equals(other.attribName)) {
            return false;
        }
        if ((this.attribValue == null) ? (other.attribValue != null) : !this.attribValue.equals(other.attribValue)) {
            return false;
        }
        if (this.filterAction != other.filterAction) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + (this.tagName != null ? this.tagName.hashCode() : 0);
        hash = 83 * hash + (this.attribName != null ? this.attribName.hashCode() : 0);
        hash = 83 * hash + (this.attribValue != null ? this.attribValue.hashCode() : 0);
        hash = 83 * hash + this.filterAction;
        return hash;
    }

    public SectionFilterSpec(String tagName,
            String attribName,
            String attribValue,
            short filterAction) {
        this.tagName = tagName;
        this.attribName = attribName;
        this.attribValue = attribValue;
        this.filterAction = filterAction;
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
    
   short getFilterAction() {
        return filterAction;
    }
}
