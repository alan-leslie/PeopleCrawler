package peoplecrawler.parser.html;

/**
 * model a html link
 * @author al
 */
public class HTMLLink {
    private String theText;
    private String theHREF;
    private static String theBaseURL = "http://en.wikipedia.org";

    /*
     * @param - text 
     * @param - lhref
     */    
    HTMLLink(String text, 
            String href)
    {
        theText = text;
        theHREF = href;
        
        if (theHREF.indexOf("http://") != 0) {
            theHREF = theBaseURL + theHREF;
        }
    }
    
    /**
     * 
     * @return - the text of the hyperlink
     */
    public String getText(){
        return theText;
    }
    
    /**
     * 
     * @return - the hyperlink URL
     */
    public String getHREF(){
        return theHREF;
    }   
}
