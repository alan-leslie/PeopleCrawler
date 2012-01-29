package peoplecrawler.parser.common;

import peoplecrawler.parser.html.HTMLDocumentParser;
//import bookmarkcrawler.parser.msword.MSWordDocumentParser;

public class DocumentParserFactory {
    
    private static DocumentParserFactory instance = new DocumentParserFactory();
    
    public static DocumentParserFactory getInstance() {
        return instance;
    }
    
    private DocumentParserFactory() {
        // empty
    }
    
    /**
     * Returns an instance of the <code>DocumentParser</code> based 
     * on the document type.
     * 
     * @param type document type.
     * @return
     * @throws DocumentParserException
     */
    public DocumentParser getDocumentParser(String type) 
        throws DocumentParserException {
        if( "text/html".equalsIgnoreCase(type) ) {
            return new HTMLDocumentParser();
        }
//        else if( "application/msword".equalsIgnoreCase(type) ) {
//            return new MSWordDocumentParser();
//        }
        else {
            throw new DocumentParserException("Unsupported document type: '"+ type + "'.");
            
        }
    }
}
