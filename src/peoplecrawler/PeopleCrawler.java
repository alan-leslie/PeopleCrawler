/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package peoplecrawler;

/**
 *
 * @author al
 */
public class PeopleCrawler {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        FetchAndProcessCrawler crawler = new FetchAndProcessCrawler("/home/al/wiki_scots", 2, 2000);
crawler.addUrl("http://en.wikipedia.org/wiki/Famous_Scots");
//crawler.addUrl("http://127.0.0.1/William_Paterson_%28explorer%29.html");
//        crawler.addUrl("http://en.wikipedia.org/wiki/William_Paterson_%28explorer%29");
//crawler.addUrl("http://en.wikipedia.org/wiki/John_Watson_Gordon");
//crawler.addUrl("http://en.wikipedia.org/wiki/List_of_Scottish_musicians");
//crawler.addUrl("http://en.wikipedia.org/wiki/List_of_Scottish_scientists");
//http://en.wikipedia.org/wiki/List_of_Scottish_actors");
//crawler.addUrl("http://localhost/bookmarks_adjusted.html");
//crawler.addUrl("http://lifehacker.com/5835369/how-do-i-securely-wipe-a-computer-before-donating-it-to-charity");

//crawler.addUrl("http://www.bbc.co.uk/news/technology-14973447");
//crawler.addUrl("http://localhost/technology-laser.html");
//crawler.addUrl("http://localhost/google-plus.html");
//        crawler.addUrl("http://localhost/retro-game-arcade.html");        
//        crawler.addUrl("http://localhost/linux-cars.html");
//        crawler.addUrl("http://localhost/how-to-get-c-like-performance-in-java.html");
//        crawler.addUrl("http://localhost/download-map-area-added-to-labs-in.html");     
//        crawler.addUrl("http://localhost/GoogleMapsForAndroidTechCrunch.html");     
     
//        .setDefaultUrls(); 
        crawler.run();
    }
}
