package peoplecrawler.parser.html;

import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import peoplecrawler.model.FetchedDocument;

/**
 * Model of wikipedia detail page (contains details of period position).
 * @author al
 */
public class WikipediaDetailPage {

    private final String theURL;
    FetchedDocument theEntity;
    private final Document theDocument;
    private NodeList theSummary = null;
    private Node theFirstPara = null;
    private static String theBaseURL = "http://en.wikipedia.org";

    /**
     * Constructs model of wikipedia detailpage.
     * @param theEntity 
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException  
     */
    public WikipediaDetailPage(FetchedDocument theEntity) throws IOException, ParserConfigurationException, SAXException {
        theURL = theEntity.getDocumentURL();
        this.theEntity = theEntity;
        HTMLPageParser theParser = new HTMLPageParser();
        theDocument = theParser.getParsedPage(theEntity);
    }

    /**
     * Finds the period from the page.
     * @return -valid period or null if unobtainable
     */
    public static String getBaseURL() {
        return theBaseURL;
    }

    /**
     * 
     * @return -valid URL of this page.
     */
    public String getURL() {
        return theURL;
    }

    /*
     * Get the summary data from the page
     * @return - node list representing the summary section
     *
     */
    public Node getSummary() {
        Node retVal = null;

        try {
            XPath infoboxTableXpath = XPathFactory.newInstance().newXPath();
            NodeList theData = (NodeList) infoboxTableXpath.evaluate("html/body//table[@class='infobox biography vcard']", theDocument, XPathConstants.NODESET);
            int theLength = theData.getLength();

            if (theLength > 0) {
                retVal = theData.item(0);
            } else {
                theData = (NodeList) infoboxTableXpath.evaluate("html/body//table[@class='infobox vcard']", theDocument, XPathConstants.NODESET);
                theLength = theData.getLength();

                if (theLength > 0) {
                    retVal = theData.item(0);
                }
            }
        } catch (XPathExpressionException ex) {
            System.out.println("Xpath failure in getSummary");
            //theLogger.log(Level.SEVERE, null, ex);
        }

        return retVal;
    }

    public String getTableText(Node tableNode) {
        StringBuilder theBuilder = new StringBuilder();
        String retVal = "";
        String theText = "";

        try {
            XPath tableRowXPath = XPathFactory.newInstance().newXPath();
            NodeList theData = (NodeList) tableRowXPath.evaluate(".//tr", tableNode, XPathConstants.NODESET);
            int theLength = theData.getLength();

            for (int i = 0; i < theLength; ++i) {
                XPath summaryHeadersXpath = XPathFactory.newInstance().newXPath();
                NodeList theSummaryHeaders = (NodeList) summaryHeadersXpath.evaluate("./th", theData.item(i), XPathConstants.NODESET);

                if (theSummaryHeaders != null) {
                    for (int j = 0; j < theSummaryHeaders.getLength(); ++j) {
                        theBuilder.append(" ");
                        theText = theSummaryHeaders.item(j).getTextContent();
                        if (theText != null && !theText.isEmpty()) {
                            theBuilder.append(getAsciiText(theText));
                        }
                    }
                }

                XPath summaryDetailsXpath = XPathFactory.newInstance().newXPath();
                NodeList theSummaryDetails = (NodeList) summaryDetailsXpath.evaluate("./td", theData.item(i), XPathConstants.NODESET);

                if (theSummaryDetails != null) {
                    for (int j = 0; j < theSummaryDetails.getLength(); ++j) {
                        theBuilder.append(" ");
                        theText = theSummaryDetails.item(j).getTextContent();
                        if (theText != null && !theText.isEmpty()) {
                            theBuilder.append(getAsciiText(theText));
                        }
                    }
                }
            }
        } catch (XPathExpressionException ex) {
            System.out.println("Xpath failure in getSummary");
        } catch (IndexOutOfBoundsException ex) {
            System.out.println("Out of bounds");
        }

        retVal = theBuilder.toString();
        return retVal;
    }

    public static String getParaText(Node n) {
        String theAsciiText = "";
        NodeList children = n.getChildNodes();
        if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
                Node childNode = children.item(i);

                if (childNode.getNodeType() == Node.TEXT_NODE) {
                    Text txtNode = (Text) childNode;
                    String theText = txtNode.getNodeValue();
                    theAsciiText = getAsciiText(theText);
                }
            }
        }

        return theAsciiText;
    }

    /*
     * Get the first paragraph (usually an abstract of the page).
     * @return node representing the first para
     *
     */
    public Node getFirstPara() {
        Node retVal = null;

        try {
            XPath firstParaXpath = XPathFactory.newInstance().newXPath();
            NodeList theData = (NodeList) firstParaXpath.evaluate("html/body//div[@id='bodyContent']//p", theDocument, XPathConstants.NODESET);
            int listLength = theData.getLength();

            if (listLength > 0) {
                retVal = theData.item(0);
            }
        } catch (XPathExpressionException ex) {
            System.out.println("Xpath failure in getFirstPara");
            //theLogger.log(Level.SEVERE, null, ex);
        }

        return retVal;
    }

    static char asciiFromUTF(int codePoint) {
        char retVal = ' ';
        switch (codePoint) {
            case 8211:
                retVal = '-';
                break;
        }

        return retVal;
    }

    static String getAsciiText(String theText) {
        StringBuilder theBuilder = new StringBuilder();
        int lengthInChars = theText.length();
        int noOfCodePoints = theText.codePointCount(0, lengthInChars - 1);

        try {
            if (lengthInChars > 0
                    && lengthInChars > noOfCodePoints) {
                for (int offset = 0; offset < lengthInChars;) {
                    final int codePoint = theText.codePointAt(offset);
                    char theCharAt = theText.charAt(offset);

                    if (codePoint >= 0 && codePoint < 128) {
                        theBuilder.append(theCharAt);
                    } else {
                        theBuilder.append(WikipediaDetailPage.asciiFromUTF(codePoint));
                    }

                    offset += Character.charCount(codePoint);
                }
            } else {
                theBuilder.append(theText);
            }
        } catch (IndexOutOfBoundsException exc) {
            System.out.println("out of bounds");
        }

        return theBuilder.toString();
    }
}
