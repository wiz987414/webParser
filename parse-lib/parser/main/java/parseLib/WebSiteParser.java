package parseLib;

import saveLib.SaveStrategy;

public interface WebSiteParser {
    void getParsedData(String source, SaveStrategy saveStrategy);
}
