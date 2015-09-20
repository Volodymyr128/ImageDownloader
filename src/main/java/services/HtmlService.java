package services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

public class HtmlService {

    public static List<String> parseUrl(String pageUrl) throws IOException {
        Document doc = Jsoup.connect(pageUrl).get();
        Elements images = doc.getElementsByTag("img");
        return images.stream().map(e -> e.absUrl("src")).collect(Collectors.toList());
    }
}
