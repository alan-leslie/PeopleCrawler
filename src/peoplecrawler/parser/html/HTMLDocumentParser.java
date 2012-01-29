package peoplecrawler.parser.html;

import peoplecrawler.model.FetchedDocument;
import peoplecrawler.model.Outlink;
import peoplecrawler.model.ProcessedDocument;
import peoplecrawler.parser.common.DocumentParser;

import java.util.logging.Level;
import java.util.logging.Logger;
import static org.w3c.dom.Node.ATTRIBUTE_NODE;
import static org.w3c.dom.Node.CDATA_SECTION_NODE;
import static org.w3c.dom.Node.COMMENT_NODE;
import static org.w3c.dom.Node.DOCUMENT_TYPE_NODE;
import static org.w3c.dom.Node.ELEMENT_NODE;
import static org.w3c.dom.Node.ENTITY_NODE;
import static org.w3c.dom.Node.ENTITY_REFERENCE_NODE;
import static org.w3c.dom.Node.NOTATION_NODE;
import static org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE;
import static org.w3c.dom.Node.TEXT_NODE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.cyberneko.html.filters.ElementRemover;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Parser for HTML documents. 
 */
public class HTMLDocumentParser implements DocumentParser {
    private String pageKey = "";

    public ProcessedDocument parse(Reader reader) 
        throws HTMLDocumentParserException {
        
        ProcessedDocument htmlDoc = new ProcessedDocument();
        htmlDoc.setDocumentType(ProcessedDocument.DOCUMENT_TYPE_HTML);
        htmlDoc.setDocumentId(null);
        htmlDoc.setDocumentURL(null);
        InputSource inputSource = new InputSource();
        inputSource.setCharacterStream(reader);
        parseHTML(htmlDoc, inputSource);
        return htmlDoc;
    }
    
    @Override
    public ProcessedDocument parse(FetchedDocument doc)
            throws HTMLDocumentParserException {
        ProcessedDocument htmlDoc = new ProcessedDocument();
        htmlDoc.setDocumentType(ProcessedDocument.DOCUMENT_TYPE_HTML);
        htmlDoc.setDocumentId(doc.getDocumentId());
        htmlDoc.setDocumentURL(doc.getDocumentURL());                    

        try {
            URL theURL = new URL(doc.getDocumentURL());
            pageKey = theURL.getHost();
        } catch (MalformedURLException ex) {
            Logger.getLogger(HTMLDocumentParser.class.getName()).log(Level.SEVERE, null, ex);
        }

        String documentCharset = doc.getContentCharset();
        InputStream contentBytes = new ByteArrayInputStream(doc.getDocumentContent());
        try {
            /* 
             * Up to this point document content was treated as byte array.
             * Here we convert byte array into character based stream. 
             * Processed document will be stored using UTF-8 encoding.
             */
            InputStreamReader characterStream =
                    new InputStreamReader(contentBytes, documentCharset);
            InputSource inputSource = new InputSource();
            inputSource.setCharacterStream(characterStream);
            parseHTML(htmlDoc, inputSource);
        } catch (UnsupportedEncodingException e) {
            throw new HTMLDocumentParserException("Document parsing error: ", e);
        }
        return htmlDoc;
    }

    private void parseHTML(ProcessedDocument htmlDoc, InputSource inputSource)
            throws HTMLDocumentParserException {
        // NekoHTML parser
        DOMParser parser = new DOMParser();

        // Create filter to remove elements that we don't care about.
        ElementRemover remover = new ElementRemover();
        // keep only a subset of elements (text and links)
        remover.acceptElement("html", null);
        remover.acceptElement("meta", new String[]{"name", "content"});
        remover.acceptElement("title", null);
        remover.acceptElement("body", null);
        remover.acceptElement("div", new String[]{"class", "id"});
        remover.acceptElement("span", new String[]{"class", "id"});
        remover.acceptElement("ul", new String[]{"class", "id"});
        remover.acceptElement("h1", new String[]{"class", "id"});
        remover.acceptElement("h2", new String[]{"class", "id"});
        remover.acceptElement("h3", new String[]{"class", "id"});
        remover.acceptElement("section", new String[]{"class", "id"});
        remover.acceptElement("base", new String[]{"href"});
        remover.acceptElement("b", null);
        remover.acceptElement("i", null);
        remover.acceptElement("u", null);
        remover.acceptElement("p", new String[]{"class", "id"});
        remover.acceptElement("br", null);
        remover.acceptElement("a", new String[]{"href", "rel"});
        // completely remove these elements
        remover.removeElement("script");
        remover.removeElement("style");

        StringWriter sw = new StringWriter();
        XMLDocumentFilter writer = new HTMLWriter(sw, "UTF-8");

        XMLDocumentFilter[] filters = {remover, writer};
        try {
            parser.setProperty("http://cyberneko.org/html/properties/filters", filters);
        } catch (SAXException e) {
            throw new HTMLDocumentParserException("Property is not supported", e);
        }

        try {
            parser.parse(inputSource);
        } catch (SAXException e) {
            throw new HTMLDocumentParserException("Parsing error: ", e);
        } catch (IOException e) {
            throw new HTMLDocumentParserException("Parsing error: ", e);
        }

        // cleaned up html. 
        String cleanHTML = cleanText(sw.toString());
        htmlDoc.setContent(cleanHTML);

        // just the text
        Node node = parser.getDocument();
        Document doc = parser.getDocument();

        SectionFilter theBodyFilter = HTMLPageSectionFilters.getInstance().getBodyFilter(pageKey);
        String text = cleanText(getFilteredText(node, theBodyFilter));
        htmlDoc.setText(text);

        // content of <title/>
        String title = getTitle(node);
        htmlDoc.setDocumentTitle(title);

        if (htmlDoc.getDocumentURL() != null) {
            String baseUrl = getBaseUrl(node);

            // links to other pages
            List<Outlink> outlinks = extractLinks(node,
                    htmlDoc.getDocumentURL(), baseUrl);
            htmlDoc.setOutlinks(outlinks);
        }
    }

    // todo - replace null with space
    // need to double check that tags have been turned into nulls
    // maybe just change the output so that instead of outputing null for no text
    // tags it outputs space
    private String cleanText(String text) {
        if (text == null) {
            return null;
        }
        String t = text.replaceAll("[ \t]+", " ");
        t = t.replaceAll("[ \t][\r\n]", "\n");
        t = t.replaceAll("[\r\n]+", "\n");
        return t;
    }

    private String getBaseUrl(Node node) {
        if (node == null) {
            return null;
        }
        org.w3c.dom.Document doc = getDocumentNode(node);
        NodeList nodeList = doc.getElementsByTagName("base");
        Node baseNode = nodeList.item(0);
        if (baseNode != null) {
            NamedNodeMap attrs = baseNode.getAttributes();
            if (attrs != null) {
                Node href = attrs.getNamedItem("href");
                if (href != null) {
                    return href.getNodeValue();
                }
            }
        }
        return null;
    }

    private String getTitle(Node node) {
        if (node == null) {
            return "";
        }

        String cleanTitle = null;
        org.w3c.dom.Document doc = getDocumentNode(node);
        NodeList nodeList = doc.getElementsByTagName("title");
        Node matchedNode = nodeList.item(0);
        if (matchedNode != null) {
            String title = matchedNode.getTextContent();
            if (title != null) {
                cleanTitle = title.replaceAll("[\r\n\t]", " ").trim();
            }
        }

        return cleanTitle;
    }

    private String getRobotsMeta(Node node) {
        if (node == null) {
            return null;
        }
        org.w3c.dom.Document doc = getDocumentNode(node);
        NodeList nodeList = doc.getElementsByTagName("meta");
        if (nodeList != null) {
            for (int i = 0, n = nodeList.getLength(); i < n; i++) {
                Node currentNode = nodeList.item(i);
                NamedNodeMap attrs = currentNode.getAttributes();
                if (attrs != null) {
                    Node contentNode = attrs.getNamedItem("content");
                    Node nameNode = attrs.getNamedItem("name");
                    if (nameNode != null && contentNode != null) {
                        if ("ROBOTS".equalsIgnoreCase(nameNode.getNodeValue())) {
                            if (contentNode != null) {
                                return contentNode.getNodeValue();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private String getText(Node node) {
        if (node == null) {
            return "";
        }

        org.w3c.dom.Document doc = getDocumentNode(node);
        org.w3c.dom.traversal.DocumentTraversal traversable =
                (DocumentTraversal) doc;
        int whatToShow = NodeFilter.SHOW_TEXT;

        NodeIterator nodeIterator =
                traversable.createNodeIterator(node, whatToShow, null, true);

        StringBuilder text = new StringBuilder();
        Node currentNode = null;
        while ((currentNode = nodeIterator.nextNode()) != null) {
            text.append(currentNode.getNodeValue());
        }
        return text.toString();
    }

    private String getFilteredText(Node node,
            SectionFilter theFilter) {
        if (node == null) {
            return "";
        }

        org.w3c.dom.Document doc = getDocumentNode(node);
        org.w3c.dom.traversal.DocumentTraversal traversable =
                (DocumentTraversal) doc;
        int whatToShow = NodeFilter.SHOW_TEXT;

        NodeIterator nodeIterator = traversable.createNodeIterator(node,
                NodeFilter.SHOW_TEXT,
                theFilter,
                true);

//        NodeIterator nodeIterator = 
//            traversable.createNodeIterator(node, whatToShow, null, true);

        StringBuilder text = new StringBuilder();
        Node currentNode = null;
        while ((currentNode = nodeIterator.nextNode()) != null) {
            String theNodeValue = currentNode.getNodeValue();

            if (theNodeValue == null) {
                text.append(" ");
            } else {
                text.append(theNodeValue);
                if(!Character.isWhitespace(theNodeValue.charAt(theNodeValue.length() - 1))){
                    text.append(" ");
                }
            }
        }
        return text.toString();
    }

    private org.w3c.dom.Document getDocumentNode(Node node) {
        if (node == null) {
            return null;
        }

        if (Node.DOCUMENT_NODE == node.getNodeType()) {
            return (org.w3c.dom.Document) node;
        } else {
            return node.getOwnerDocument();
        }
    }

    private boolean isNoFollowForDocument(Node node) {
        boolean noFollow = false;

        // Check <META name="robots" content="..."/>
        String robotsMeta = getRobotsMeta(node);
        if (robotsMeta != null && robotsMeta.toLowerCase().indexOf("nofollow") > -1) {
            noFollow = true;
        }

        return noFollow;
    }

    private List<Outlink> extractLinks(Node node, String docUrl, String baseUrl) {
        if (isNoFollowForDocument(node)) {
            return new ArrayList<Outlink>();
        }

        org.w3c.dom.Document doc = getDocumentNode(node);
        DocumentTraversal traversableDoc = (DocumentTraversal) doc;
        NodeFilter linkFilter = getLinkNodeFilter();
        SectionFilter theSectionFilter = HTMLPageSectionFilters.getInstance().getLinkFilter(pageKey);
        NodeFilter theFilter = linkFilter;
        if (theSectionFilter != null) {
            theFilter = theSectionFilter;
            theSectionFilter.addAcceptFilter(linkFilter);
        }

        // todo - do not want general links in the share section
        // and internal links (including #)
        NodeIterator iterator = traversableDoc.createNodeIterator(node,
                NodeFilter.SHOW_ELEMENT,
                theFilter,
                true);
        Node currentNode = null;

        List<Outlink> outlinks = new ArrayList<Outlink>();

        while ((currentNode = iterator.nextNode()) != null) {
            String href = currentNode.getAttributes().
                    getNamedItem("href").getNodeValue();
            boolean nofollow = isNoFollowPresent(currentNode);
            if (nofollow == false) {
                if ("BASE".equalsIgnoreCase(node.getNodeName())) {
                    // ignore this link
                } else {
                    String url = buildUrl(href, baseUrl, docUrl);
                    if (url != null) {
                        String anchorText = getAnchorText(currentNode);
                        Outlink link = new Outlink(url, anchorText);
                        outlinks.add(link);
                    }
                }
            }
        }

        return outlinks;
    }

    /*
     * Extracts url protocol if present. Handles two cases: 
     * 
     * 1. "<protocol>://<host>"
     * 2. "mailto:<email address>"
     */
    private String extractProtocol(String url) {
        String protocol = null;
        if (url.startsWith("mailto:")) {
            protocol = "mailto";
        } else {
            int i = url.indexOf("://");
            if (i > -1) {
                protocol = url.substring(0, i);
            }
        }
        return protocol;
    }

    private String extractParent(String url) {
        String parent = url;
        int i = url.lastIndexOf("/");
        if (i > -1) {
            parent = url.substring(0, i + "/".length());
        }
        return parent;
    }

    /*
     * Builds absolute URL. For relative URLs will use source document URL and
     * base URL.
     */
    private String buildUrl(String href, String baseUrl, String documentUrl) {

        String url = null;

        String protocol = extractProtocol(href);

        if (protocol != null) {
            url = href;
        } else if (baseUrl != null) {
            url = baseUrl + href;
        } else if (href.startsWith("/")) {
            try {
                URL docUrl = new URL(documentUrl);
                if (docUrl.getPort() == -1) {
                    url = docUrl.getProtocol() + "://" + docUrl.getHost() + href;
                } else {
                    url = docUrl.getProtocol() + "://" + docUrl.getHost()
                            + ":" + docUrl.getPort() + href;
                }
            } catch (MalformedURLException e) {
                url = null;
            }
        } else {
            url = extractParent(documentUrl) + href;
        }

        return url;
    }

    private String getAnchorText(Node currentNode) {
        String text = getText(currentNode);
        String cleanText = null;
        if (text != null) {
            cleanText = text.replaceAll("[\r\n\t]", " ").trim();
        }
        return cleanText;
    }

    /*
     * Checks for presense of rel="nofollow" attribute.
     */
    private boolean isNoFollowPresent(Node currentNode) {
        Node relAttr = currentNode.getAttributes().getNamedItem("rel");
        boolean nofollow = false;
        if (relAttr != null) {
            String relAttrValue = relAttr.getNodeValue();
            if ("nofollow".equalsIgnoreCase(relAttrValue)) {
                nofollow = true;
            }
        }
        return nofollow;
    }

    private NodeFilter getLinkNodeFilter() {
        CompositeFilter linkFilter = new CompositeFilter();
        // For now doing the simplest thing possible - only consider <A> elements
        linkFilter.addAcceptFilter(new ElementNodeFilter("a", "href"));
        /*
        Other elements to consider:
        
        linkFilter.addAcceptFilter(new LinkNodeFilter("frame", "src"));
        linkFilter.addAcceptFilter(new LinkNodeFilter("link", "href"));
         */
        return linkFilter;
    }

    /*
     * Get the story-body node
     * @return - node list representing the summary section
     *
     */
    private Node getNode(Document theDocument,
            NodeSpec theNodeSpec) {
        Node retVal = null;

        NodeList theList = theDocument.getElementsByTagName(theNodeSpec.getTagName());

        if (theList != null) {
            int theListLength = theList.getLength();

            if (theListLength > 0) {
                for (int i = 0; i < theListLength && retVal == null; ++i) {
                    Node theNode = theList.item(i);
                    NamedNodeMap theAttributes = theNode.getAttributes();
//                        System.out.println("Attributes start");
//                        
//                        for(int j = 0; j < theAttributes.getLength(); ++j) {
//                            String theName = theAttributes.item(j).getNodeName();
//                            String theValue = theAttributes.item(j).getNodeValue();
//                            System.out.println("Attr is :" + theName + "-" + theValue);
//                        }
//                        System.out.println("Attributes end");

                    Node namedItem = theAttributes.getNamedItem(theNodeSpec.getAttribName());

                    if (namedItem != null) {
                        String theClassName = namedItem.getNodeValue();

                        if (theClassName.equalsIgnoreCase(theNodeSpec.getAttribValue())) {
                            retVal = theNode;
                        }
                    }
                }
            }
        }

        return retVal;
    }

    static void listNodes(Node node, String indent) {
        String nodeName = node.getNodeName();
        System.out.println(indent + " Node: " + nodeName);
        short type = node.getNodeType();
        System.out.println(indent + " Node Type: " + nodeType(type));
        if (type == TEXT_NODE) {
            System.out.println(indent + " Content is: " + ((Text) node).getWholeText());
        }

        NodeList list = node.getChildNodes();
        if (list.getLength() > 0) {
            System.out.println(indent + " Child Nodes of " + nodeName + " are:");
            for (int i = 0; i < list.getLength(); i++) {
                listNodes(list.item(i), indent + "  ");
            }
        }

        System.out.flush();
    }

    static String nodeType(short type) {
        switch (type) {
            case ELEMENT_NODE:
                return "Element";
            case DOCUMENT_TYPE_NODE:
                return "Document type";
            case ENTITY_NODE:
                return "Entity";
            case ENTITY_REFERENCE_NODE:
                return "Entity reference";
            case NOTATION_NODE:
                return "Notation";
            case TEXT_NODE:
                return "Text";
            case COMMENT_NODE:
                return "Comment";
            case CDATA_SECTION_NODE:
                return "CDATA Section";
            case ATTRIBUTE_NODE:
                return "Attribute";
            case PROCESSING_INSTRUCTION_NODE:
                return "Attribute";
        }
        return "Unidentified";
    }
}
