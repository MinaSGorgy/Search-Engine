package crawler;

import Database.DatabaseController;
import Tools.DocumentManager;
import BusinessModel.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class WebCrawlingTask implements Callable<List<Seed>> {

    private Seed seed;
    private Set<String> urlSet;
    private static Logger logger =Logger.getLogger(WebCrawlingTask.class.getName());
    WebCrawlingTask(Seed seed, Set <String> urlSet) {
        this.seed = seed;
        this.urlSet = urlSet;
    }

    @Override
    public List<Seed> call(){
        String documentText;
        List<Seed> seeds;
        DatabaseController controller;
        try {
            controller = new DatabaseController();
        } catch (SQLException exception) {
            //logger.log(Level. ,"Error while initializing database connection");
            //exception.printStackTrace();
            return null;
        }
        if(!WebCrawler.robotsManager.isAllowed(seed.getUrl())){
            controller.deleteSeed(seed);
            controller.close();
            return null;
        }
        // Try to get the html document from the web.
        // If for any reason you failed, delete this seed and exit the task
        Document jsoupDoc;
        try {
            jsoupDoc = Jsoup.connect(seed.getUrl()).get();
            if(urlSet.contains(jsoupDoc.location())){
                controller.deleteSeed(seed);
                return null;
            }
            seed.setUrl(jsoupDoc.location());
            Elements links = jsoupDoc.select("a[href]");
            documentText = jsoupDoc.body().text();
            seeds = new LinkedList<>();
            for (Element link : links) {
                if(WebCrawler.robotsManager.isAllowed(link.attr("abs:href"))) {
                    seeds.add(new Seed(link.attr("abs:href"), false));
                }
            }
        } catch (Exception  exception) {
            System.err.println("Error while downloading/parsing document from web");
            //exception.printStackTrace();
            controller.deleteSeed(seed);
            controller.close();
            return null;
        }
        // If every thing succeeded then insert the document and write it to the disk
        BusinessModel.Document indexerDoc = new BusinessModel.Document(
                String.format("%d.html", jsoupDoc.location().hashCode()),
                jsoupDoc.location(), false);
        DocumentManager.writeDocument(documentText, indexerDoc);
        controller.insertDocument(indexerDoc);
        seed.setProcessed(true);
        controller.updateSeed(seed);
        controller.close();
        urlSet.add(seed.getUrl());
        return seeds;
    }
}
