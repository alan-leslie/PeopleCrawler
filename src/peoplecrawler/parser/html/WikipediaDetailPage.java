package peoplecrawler.parser.html;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
            
            if(theLength > 0){
                retVal =  theData.item(0);      
            } else {
                theData = (NodeList) infoboxTableXpath.evaluate("html/body//table[@class='infobox vcard']", theDocument, XPathConstants.NODESET);
                theLength = theData.getLength();  
                
                if(theLength > 0){
                    retVal =  theData.item(0);      
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

        try {
            XPath tableRowXPath = XPathFactory.newInstance().newXPath();
            NodeList theData = (NodeList) tableRowXPath.evaluate("./tr", tableNode, XPathConstants.NODESET);
            int theLength = theData.getLength();
            
            for (int i = 0; i < theLength; ++i) {
                XPath summaryHeadersXpath = XPathFactory.newInstance().newXPath();
                NodeList theSummaryHeaders = (NodeList) summaryHeadersXpath.evaluate("./th", theData.item(i), XPathConstants.NODESET);

                if (theSummaryHeaders != null){
                    for(int j = 0; j < theSummaryHeaders.getLength(); ++j){
                        theBuilder.append(" ");
                        theBuilder.append(theSummaryHeaders.item(j).getTextContent());                       
                    }
                }
                
                XPath summaryDetailsXpath = XPathFactory.newInstance().newXPath();
                NodeList theSummaryDetails = (NodeList) summaryDetailsXpath.evaluate("./td", theData.item(i), XPathConstants.NODESET);

                if (theSummaryDetails != null){
                    for(int j = 0; j < theSummaryDetails.getLength(); ++j){
                        theBuilder.append(" ");
                        theBuilder.append(theSummaryDetails.item(j).getTextContent());                       
                    }                    
                }
            }
        } catch (XPathExpressionException ex) {
            System.out.println("Xpath failure in getSummary");
            //theLogger.log(Level.SEVERE, null, ex);
        }

        retVal = theBuilder.toString();
        return retVal;
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
           
            if(listLength > 0){
                retVal = theData.item(0);             
            }
        } catch (XPathExpressionException ex) {
            System.out.println("Xpath failure in getFirstPara");
            //theLogger.log(Level.SEVERE, null, ex);
        }

        return retVal;
    }
}
