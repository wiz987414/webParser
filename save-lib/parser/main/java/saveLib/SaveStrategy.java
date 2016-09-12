package saveLib;

import org.jsoup.select.Elements;

public interface SaveStrategy {
    boolean saveData(Elements parsedData);

    String getValidName(String baseName);

    String getSaveSource();

    void createLog();
}
