package peoplecrawler.parser.html;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.parser.HtmlParser;
import org.lobobrowser.html.test.SimpleUserAgentContext;
import org.w3c.dom.Document;

import org.xml.sax.SAXException;
import peoplecrawler.model.FetchedDocument;

/**
 * Parses HTML pages using CORBA parser
 * @author al
 */
public class HTMLPageParser {

    private final Logger theLogger = null;

    HTMLPageParser() {
        //theLogger = logger;
    }
    
    /*
     * @param - theURL the page to be parsed
     * @return - parsed html of the page
     */
    Document getParsedPage(FetchedDocument theEntity) throws IOException, ParserConfigurationException, SAXException {
        Document theResult = null;

        if (theEntity == null) {
            return theResult;
        } else {
            try {
                UserAgentContext uacontext = new SimpleUserAgentContext();

                // In this case we will use a standard XML document
                // as opposed to Cobra's HTML DOM implementation.
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                InputStream in = null;

                try {
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    in = new ByteArrayInputStream(theEntity.getDocumentContent());
                    Reader reader = new InputStreamReader(in, "ISO-8859-1");
                   
                    theResult = builder.newDocument();
                    HtmlParser parser = new HtmlParser(uacontext, theResult);
                    parser.parse(reader);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception e) {
                        }
                    }
                }          
            } finally {
            }
        }

        return theResult;
    }
}
