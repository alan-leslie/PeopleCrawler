package peoplecrawler.parser.html;

import peoplecrawler.utils.CSVFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.w3c.dom.traversal.NodeFilter;

/**
 *
 * @author al
 */
public class HTMLPageSectionFilters {

    private Map<String, SectionFilter> bodyFilters = new HashMap<String, SectionFilter>();
    private Map<String, SectionFilter> linkFilters = new HashMap<String, SectionFilter>();

    SectionFilter getBodyFilter(String key) {
        SectionFilter bodyFilter = bodyFilters.get(key);
        return bodyFilter;
    }

    SectionFilter getLinkFilter(String key) {
        SectionFilter linkFilter = linkFilters.get(key);
        return linkFilter;
    }

    private HTMLPageSectionFilters() {
        initFilter(true);
        initFilter(false);
    }

    public static HTMLPageSectionFilters getInstance() {
        return HTMLPageSectionFiltersHolder.INSTANCE;
    }

    private static class HTMLPageSectionFiltersHolder {

        private static final HTMLPageSectionFilters INSTANCE = new HTMLPageSectionFilters();
    }

    private void initFilter(boolean isBodyFilter) {
        Map<String, SectionFilter> targetFilterMap = linkFilters;
        String filterFileName = "link_filter.txt";
        
        if(isBodyFilter){
            targetFilterMap = bodyFilters;
            filterFileName = "body_filter.txt";
        }
        
        List<String[]> theFileData = CSVFile.getFileData(filterFileName);
        Iterator<String[]> theIterator = theFileData.iterator();

        while (theIterator.hasNext()) {
            String theLineArr[] = theIterator.next();

            if (theLineArr.length > 4) {
                SectionFilter theFilter = null;
                String theHost = theLineArr[0];
                String theSection = theLineArr[1].trim();
                String theAttribute = theLineArr[2].trim();
                String theValue = theLineArr[3].trim();
                short filterType = theLineArr[4].equalsIgnoreCase("S") ? NodeFilter.FILTER_SKIP : NodeFilter.FILTER_ACCEPT;

                if (targetFilterMap.containsKey(theHost)) {
                    theFilter = targetFilterMap.get(theHost);
                } else {
                    theFilter = new SectionFilter();
                }

                SectionFilterSpec theSectionFilter = new SectionFilterSpec(theSection, theAttribute, theValue, filterType);

                if (filterType == NodeFilter.FILTER_SKIP) {
                    theFilter.addSkip(theSectionFilter);
                } else {
                    theFilter.addAccept(theSectionFilter);
                }

                targetFilterMap.put(theHost, theFilter);
            }
        }
    }

    public static void main(String[] args) {
        HTMLPageSectionFilters theFilters = HTMLPageSectionFilters.getInstance();
    }
}
