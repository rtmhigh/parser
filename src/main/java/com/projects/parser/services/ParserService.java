package com.projects.parser.services;

import com.projects.parser.entities.Article;
import com.projects.parser.entities.Hub;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static com.projects.parser.utils.Constants.*;

@Service
public class ParserService {

    public List<Article> getArticles() {
        try {
            FORMATTER.setTimeZone(TimeZone.getTimeZone(ZONE));
            Document document = Jsoup.connect(URL + "/ru/all").get();
            Elements articlesList = getElements(document, ".tm-articles-list");
            Elements articlesListItem = getElements(articlesList, ".tm-articles-list__item");
            return builder(articlesListItem);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Elements getElements(Object o, String className) {
        if (o instanceof Document) {
            return ((Document) o).select(className);
        }
        if (o instanceof Elements) {
            return ((Elements) o).select(className);
        } else {
            return ((Element) o).select(className);
        }
    }

    private static List<Article> builder(Elements elements) {
        List<Article> articles = new ArrayList<>();
        elements.forEach(
                listItem -> {
                    try {
                        Elements articleSnippetDatetime = getElements(listItem, ".tm-article-snippet__datetime-published");
                        String dateTime = articleSnippetDatetime.get(0).childNode(0).attributes().get("datetime");
                        Date date = FORMATTER.parse(dateTime);
                        Elements articleSnippetTitle = getElements(listItem, ".tm-article-snippet__title-link");
                        String title = articleSnippetTitle.get(0).childNode(0).childNodes().get(0).toString();
                        List<Hub> hubs = new ArrayList<>();
                        Elements articleSnippetHubs = getElements(listItem, ".tm-article-snippet__hubs");
                        articleSnippetHubs.get(0).childNodes().forEach(node -> {
                            String subtitle = node.childNodes().get(0).childNodes().get(0).childNodes().get(0).toString();
                            Hub hub = Hub.builder()
                                    .subtitle(subtitle)
                                    .build();
                            hubs.add(hub);
                        });
                        String image = "";
                        Elements articleSnippetLeadImage = getElements(listItem, ".tm-article-snippet__lead-image");
                        if (!articleSnippetLeadImage.isEmpty()) {
                            image = articleSnippetLeadImage
                                    .get(0)
                                    .attr("src");
                        }
                        String href = articleSnippetTitle.get(0).attr("href");
                        Document document = Jsoup.connect(URL + href).get();
                        Elements articleFormattedBody = getElements(document, ".article-formatted-body");
                        List<String> content = articleFormattedBody.get(0).childNodes().get(0).childNodes().stream()
                                .map(Node::toString)
                                .collect(Collectors.toList());
                        Article article = Article.builder()
                                .date(date)
                                .title(title)
                                .hubs(hubs)
                                .image(image)
                                .readMore(href)
                                .content(content)
                                .build();
                        articles.add(article);
                    } catch (ParseException | IOException e) {
                        e.printStackTrace();
                    }
                }
        );
        return articles;
    }
}
