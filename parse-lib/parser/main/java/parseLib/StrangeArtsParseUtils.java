package parseLib;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import saveLib.SaveStrategy;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StrangeArtsParseUtils implements WebSiteParser {
    @Override
    public void getParsedData(String source, SaveStrategy saveStrategy) {
        Elements heroesList = parseHeroesList(source);

        File sourceFolder = new File(saveStrategy.getSaveSource());
        List<String> filesNames = new ArrayList<>();

        if (sourceFolder.isDirectory()) {
            File[] filesList = sourceFolder.listFiles();
            assert filesList != null;
            for (File file : filesList) filesNames.add(file.getName());

            for (Element heroInfo : heroesList) {
                String heroName = saveStrategy.getValidName(heroInfo.text());
                if (!filesNames.contains(heroName + ".txt")) {
                    Elements heroesData = parseHeroInfo(heroInfo, heroesList);
                    if (!saveStrategy.saveData(heroesData)) {
                        saveStrategy.createLog();
                        throw new RuntimeException("Unable to save data in file", new IOException());
                    }
                }
            }
        }
        saveStrategy.createLog();
        /*for (Element item : heroesData) {
            if (!Objects.equals(item.attributes().get("href"), ""))
                System.out.println(item.attributes().get("href"));
            System.out.println(item.text());
        }*/
    }

    private Elements parseHeroesList(String listSource) {
        Document doc;
        int copyNumber = 1;
        try {
            doc = Jsoup.connect(listSource).timeout(10000).ignoreHttpErrors(true).get();
        } catch (IOException e) {
            throw new RuntimeException("Unable to connect to " + listSource, e);
        }
        Elements links = new Elements();
        assert doc != null;
        links.addAll(doc.getElementsByTag("a").stream().filter(linkItem -> linkItem.text().length() > 2
                && linkItem.attributes().get("href").startsWith("/heroes/")
                && Objects.equals(linkItem.parent().tagName(), "span")).collect(Collectors.toList()));
        for (Element checkLink : links) {
            for (Element comparedLink : links) {
                if (Objects.equals(checkLink.text(), comparedLink.text())
                        && !Objects.equals(checkLink.attributes().get("href"), comparedLink.attributes().get("href"))) {
                    checkLink.text(checkLink.text() + copyNumber);
                    copyNumber++;
                }
            }
        }
        return links;
    }

    private Elements parseHeroInfo(Element sourceLink, Elements heroesList) {
        Elements heroInfo = new Elements();
        Document heroDoc;
        String infoSource = "http://www.strangearts.ru" + sourceLink.attributes().get("href");
        try {
            heroDoc = Jsoup.connect(infoSource).timeout(10000).ignoreHttpErrors(true).get();
        } catch (IOException e) {
            throw new RuntimeException("Unable to connect to " + infoSource, e);
        }

        assert heroDoc != null;
        heroInfo.addAll(heroDoc.getElementsByTag("link").stream()
                .filter(dataItem -> Objects.equals(dataItem.attributes().get("rel"), "canonical"))
                .collect(Collectors.toList()));

        heroInfo.addAll(heroDoc.getElementsByTag("title"));
        heroesList.stream()
                .filter(comparedName -> heroInfo.get(0).attributes().get("href").endsWith(comparedName.attributes().get("href")))
                .forEach(comparedName -> heroInfo.get(1).text(comparedName.text()));

        heroInfo.addAll(heroDoc.getElementsByTag("p").stream()
                .filter(dataItem -> (Objects.equals(dataItem.parent().attributes().get("class"), "field-item even") ||
                        Objects.equals(dataItem.parent().attributes().get("class"), "item-fieldgroup")) &&
                        !Objects.equals(dataItem.parent().parent().parent().parent().attributes().get("class"), "message-content") &&
                        !Objects.equals(dataItem.parent().parent().parent().parent().attributes().get("class"), "node node-quotes clearfix"))
                .collect(Collectors.toList()));

        heroInfo.addAll(heroDoc.getElementsByTag("div").stream()
        .filter(dataItem -> Objects.equals(dataItem.parent().parent().attributes().get("class"), "field-item even"))
                .collect(Collectors.toList()));

        return heroInfo;
    }
}
