package peoplecrawler.parser.common;

import peoplecrawler.model.FetchedDocument;
import peoplecrawler.model.ProcessedDocument;

/**
 * Interface for parsing document that was retrieved/fetched during
 * collection phase.  
 */
public interface DocumentParser {
    public ProcessedDocument parse(FetchedDocument doc) 
        throws DocumentParserException;
}
