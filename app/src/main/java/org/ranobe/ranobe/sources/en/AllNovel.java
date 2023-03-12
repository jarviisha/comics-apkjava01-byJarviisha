package org.ranobe.ranobe.sources.en;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.ranobe.ranobe.models.Chapter;
import org.ranobe.ranobe.models.ChapterItem;
import org.ranobe.ranobe.models.DataSource;
import org.ranobe.ranobe.models.Filter;
import org.ranobe.ranobe.models.Lang;
import org.ranobe.ranobe.models.Novel;
import org.ranobe.ranobe.models.NovelItem;
import org.ranobe.ranobe.network.HttpClient;
import org.ranobe.ranobe.sources.Source;
import org.ranobe.ranobe.util.NumberUtils;
import org.ranobe.ranobe.util.SourceUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AllNovel implements Source {

    private final String baseUrl = "https://allnovel.org";
    private final int sourceId = 7;

    @Override
    public DataSource metadata() {
        DataSource source = new DataSource();
        source.sourceId = sourceId;
        source.url = baseUrl;
        source.name = "All Novel";
        source.lang = Lang.eng;
        source.dev = "punpun";
        source.logo = "https://allnovel.org/uploads/thumbs/logo23232-21abb9ad59-98b3a84b69aa4c92e8b001282e110775.png";
        return source;
    }


    @Override
    public List<NovelItem> novels(int page) throws Exception {
        String web = baseUrl + "/most-popular?page=" + page;
        return parse(HttpClient.GET(web, new HashMap<>()));
    }


    private List<NovelItem> parse(String body) throws IOException {
        List<NovelItem> items = new ArrayList<>();
        Element doc = Jsoup.parse(body).select("div.col-truyen-main.archive").first();

        if (doc == null) return items;

        for (Element element : doc.select("div.row")) {
            String url = element.select("h3.truyen-title > a").attr("href").trim();

            if (url.length() > 0) {
                NovelItem item = new NovelItem(url);
                item.sourceId = sourceId;
                item.name = element.select("h3.truyen-title > a").text().trim();
                Element img = Jsoup.parse(HttpClient.GET(baseUrl + url, new HashMap<>()));
                item.cover = baseUrl + img.select("div.books img").attr("src");


                items.add(item);
            }

        }

        return items;
    }

    @Override
    public Novel details(String url) throws Exception {
        Novel novel = new Novel(url);
        Element doc = Jsoup.parse(HttpClient.GET(baseUrl + url, new HashMap<>()));
        novel.sourceId = sourceId;
        novel.name = doc.select("div.books h3.title").text().trim();
        novel.cover = baseUrl + doc.select("div.books img").attr("src").trim();
        novel.summary = doc.select("div.desc-text > p").text().trim();
        novel.rating = NumberUtils.toFloat(doc.select("input#rateVal").attr("value")) / 2;


        for (Element element : doc.select("div.info")) {

            novel.authors = Arrays.asList(element.select("div:eq(0) > a").text().split(","));
            List<String> genres = new ArrayList<>();
            for (Element a : element.select("div:eq(2) > a")) genres.add(a.text());
            novel.genres = genres;
            novel.status = element.select("div:eq(4) > a").text();

        }

        return novel;
    }

    @Override
    public List<ChapterItem> chapters(String url) throws Exception {
        List<ChapterItem> items = new ArrayList<>();
        Element novelId = Jsoup.parse(HttpClient.GET(baseUrl + url, new HashMap<>())); // getNovelId
        String id = novelId.select("div#rating").attr("data-novel-id");

        String base = baseUrl.concat("/ajax-chapter-option?novelId=").concat(id);
        Element doc = Jsoup.parse(HttpClient.GET(base, new HashMap<>()));

        for (Element element : doc.select("select option")) {
            ChapterItem item = new ChapterItem(url);

            item.url = element.attr("value").trim();
            item.name = element.text().trim();
            item.id = NumberUtils.toFloat(item.name);
            items.add(item);
        }
        return items;
    }

    @Override
    public Chapter chapter(String novelUrl, String chapterUrl) throws Exception {
        Chapter chapter = new Chapter(novelUrl);
        Element doc = Jsoup.parse(HttpClient.GET(baseUrl + chapterUrl, new HashMap<>()));

        chapter.url = baseUrl + chapterUrl;
        chapter.content = "";

        doc.select("div.chapter-c").select("p").append("::");
        chapter.content = SourceUtils.cleanContent(
                doc.select("div.chapter-c").text().replaceAll("::", "\n\n").trim()
        );

        return chapter;
    }

    @Override
    public List<NovelItem> search(Filter filters, int page) throws Exception {
        if (filters.hashKeyword()) {
            String keyword = filters.getKeyword();
            String web = SourceUtils.buildUrl(baseUrl, "/search?keyword=", keyword, "&page=", String.valueOf(page));
            return parse(HttpClient.GET(web, new HashMap<>()));
        }
        return new ArrayList<>();
    }
}
