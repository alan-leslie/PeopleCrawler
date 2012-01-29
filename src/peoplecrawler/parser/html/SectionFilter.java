package peoplecrawler.parser.html;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeFilter;

/**
 * Filter for nodes that are included in skip or accept 
 * sections.
 */
class SectionFilter implements NodeFilter {

    private List<SectionFilterSpec> skipList;
    private List<SectionFilterSpec> acceptList;
    private List<NodeFilter> acceptFilters = new ArrayList<NodeFilter>();
    
    public SectionFilter() {
        skipList = new ArrayList<SectionFilterSpec>();
        acceptList = new ArrayList<SectionFilterSpec>();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SectionFilter other = (SectionFilter) obj;
        if (this.skipList != other.skipList && (this.skipList == null || !this.skipList.equals(other.skipList))) {
            return false;
        }
        if (this.acceptList != other.acceptList && (this.acceptList == null || !this.acceptList.equals(other.acceptList))) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

     public short acceptNode(Node theNode) {
        short result = FILTER_ACCEPT;
        boolean elementFound = false;

        for (int i = 0; i < skipList.size() && !elementFound; ++i) {
            SectionFilterSpec skipSpec = skipList.get(i);
            Node traversalNode = theNode;

            while (traversalNode != null
                    && elementFound == false) {    // get parent != null
                if (Node.ELEMENT_NODE == traversalNode.getNodeType()) {
                    Element e = (Element) traversalNode;
                    if (e.getNodeName().equalsIgnoreCase(skipSpec.getTagName())) {
                        Attr attributeNode = e.getAttributeNode(skipSpec.getAttribName());
                        if (attributeNode != null
                                && attributeNode.getValue().equalsIgnoreCase(skipSpec.getAttribValue())) {
                            elementFound = true;
                            result = skipSpec.getFilterAction();
                        }
                    }
                }

                traversalNode = traversalNode.getParentNode();
            }
        }

        if (!elementFound) {
            if (acceptList.isEmpty()) {
                result = FILTER_ACCEPT;
            } else {
                result = FILTER_SKIP;

                for (int j = 0; j < acceptList.size() && !elementFound; ++j) {
                    SectionFilterSpec acceptSpec = acceptList.get(j);
                    Node traversalNode = theNode;

                    while (traversalNode != null
                            && elementFound == false) {    // get parent != null
                        if (Node.ELEMENT_NODE == traversalNode.getNodeType()) {
                            Element e = (Element) traversalNode;
                            if (e.getNodeName().equalsIgnoreCase(acceptSpec.getTagName())) {
                                Attr attributeNode = e.getAttributeNode(acceptSpec.getAttribName());
                                if (attributeNode != null
                                        && attributeNode.getValue().equalsIgnoreCase(acceptSpec.getAttribValue())) {
                                    elementFound = true;

                                    result = acceptSpec.getFilterAction();

                                    for (NodeFilter f : acceptFilters) {
                                        result = f.acceptNode(theNode);
                                        if (result == NodeFilter.FILTER_ACCEPT) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        traversalNode = traversalNode.getParentNode();
                    }
                }
            }
        }

        return result;
    }

    public void addAcceptFilter(NodeFilter nestedFilter) {
        acceptFilters.add(nestedFilter);
    }

    public void addAccept(SectionFilterSpec acceptSpec) {
        acceptList.add(acceptSpec);
    }

    public void addSkip(SectionFilterSpec skipSpec) {
        skipList.add(skipSpec);
    }
}
