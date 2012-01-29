package peoplecrawler;

import java.util.ArrayList;
import java.util.List;

public class FetchAndProcessCrawler {

    public static final int DEFAULT_MAX_DEPTH = 3;
    public static final int DEFAULT_MAX_DOCS = 1000;
    //INSTANCE VARIABLES
    // A reference to the crawled data
    CrawlData crawlData;
    // The location where we will store the fetched data
    String rootDir;
    // total number of iterations
    int maxDepth = DEFAULT_MAX_DEPTH;
    // max number of pages that will be fetched within every crawl/iteration.
    int maxDocs = DEFAULT_MAX_DOCS;
    List<String> seedUrls;
    URLFilter urlFilter;

    public FetchAndProcessCrawler(String dir, int maxDepth, int maxDocs) {

        rootDir = dir;

//        if (rootDir == null || rootDir.trim().length() == 0) {
//
//            String prefix = System.getProperty("iweb.home");
//            if (prefix == null) {
//                prefix = "..";
//            }
//
//            rootDir = System.getProperty("iweb.home") + System.getProperty("file.separator") + "data";
//        }

        rootDir = rootDir + System.getProperty("file.separator") + "crawl-" + System.currentTimeMillis();

        this.maxDepth = maxDepth;

        this.maxDocs = maxDocs;

        this.seedUrls = new ArrayList<String>();

        /* default url filter configuration */
        this.urlFilter = new URLFilter();
        urlFilter.setAllowFileUrls(true);
        urlFilter.setAllowHttpUrls(true);
    }

    public void run() {

        crawlData = new CrawlData(rootDir);

        BasicWebCrawler webCrawler = new BasicWebCrawler(crawlData);
        webCrawler.addSeedUrls(getSeedUrls());

        webCrawler.setURLFilter(urlFilter);

        long t0 = System.currentTimeMillis();

        /* run crawl */
        webCrawler.fetchAndProcess(maxDepth, maxDocs);

        System.out.println("Timer (s): [Crawler processed data] --> "
                + (System.currentTimeMillis() - t0) * 0.001);

    }

    public List<String> getSeedUrls() {

        return seedUrls;
    }

    public void addUrl(String val) {
        URLNormalizer urlNormalizer = new URLNormalizer();
        seedUrls.add(urlNormalizer.normalizeUrl(val));
    }

    public void setAllUrls() {

        setDefaultUrls();


    }

    public void setDefaultUrls() {

    }

    public void setUrlFilter(URLFilter urlFilter) {
        this.urlFilter = urlFilter;
    }

    private void setFilesOnlyUrlFilter() {
        /* configure url filter to accept only file:// urls */
        URLFilter urlFilter = new URLFilter();
        urlFilter.setAllowFileUrls(true);
        urlFilter.setAllowHttpUrls(false);
        setUrlFilter(urlFilter);
    }

    public void setUrls(String val) {

    }

    public void addDocSpam() {

    }

    /**
     * @return the rootDir
     */
    public String getRootDir() {
        return rootDir;
    }

    /**
     * @param rootDir the rootDir to set
     */
    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    /**
     * @return the maxNumberOfCrawls
     */
    public int getMaxNumberOfCrawls() {
        return maxDepth;
    }

    /**
     * @param maxNumberOfCrawls the maxNumberOfCrawls to set
     */
    public void setMaxNumberOfCrawls(int maxNumberOfCrawls) {
        this.maxDepth = maxNumberOfCrawls;
    }

    /**
     * @return the maxNumberOfDocsPerCrawl
     */
    public int getMaxNumberOfDocsPerCrawl() {
        return maxDocs;
    }

    /**
     * @param maxNumberOfDocsPerCrawl the maxNumberOfDocsPerCrawl to set
     */
    public void setMaxNumberOfDocsPerCrawl(int maxNumberOfDocsPerCrawl) {
        this.maxDocs = maxNumberOfDocsPerCrawl;
    }

    /**
     * @return the crawlData
     */
    public CrawlData getCrawlData() {
        return crawlData;
    }
}
