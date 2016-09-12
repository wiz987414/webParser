import parseLib.StrangeArtsParseUtils;
import parseLib.WebSiteParser;
import saveLib.SaveInFile;
import saveLib.SaveStrategy;
import saveLib.SaveTo;

import java.util.HashMap;
import java.util.Map;

class WebParser {
    private static final Map<String, WebSiteParser> parseStrategy;
    private final SaveStrategy parseSaveStrategy;

    static {
        parseStrategy = new HashMap<>();
        parseStrategy.put("http://www.strangearts.ru", new StrangeArtsParseUtils());
    }

    private SaveStrategy getParseSaveStrategy() {
        return parseSaveStrategy;
    }

    WebParser(SaveTo saveTo, String saveSource) {
        this.parseSaveStrategy = caseSaveStrategy(saveTo, saveSource);
    }

    private SaveStrategy caseSaveStrategy(SaveTo saveTo, String saveSource) {
        SaveStrategy saveStrategy = null;
        switch (saveTo) {
            case FILE: {
                saveStrategy = new SaveInFile(saveSource);
            }
        }
        return saveStrategy;
    }

    void start(String parseSource) {
        parseStrategy.keySet().stream()
                .filter(parseSource::startsWith)
                .forEach(parserImpl ->
                        parseStrategy.get(parserImpl).getParsedData(parseSource, getParseSaveStrategy()));
    }
}
