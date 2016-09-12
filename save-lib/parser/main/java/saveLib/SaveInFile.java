package saveLib;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class SaveInFile implements SaveStrategy {
    private final String saveSource;
    private final File logFile;
    private final List<String> log;

    public SaveInFile(String saveSource) {
        this.saveSource = saveSource;
        this.logFile = new File(saveSource + "_log" + getValidName(Date.from(Instant.now()).toString()) + ".log");
        this.log = new ArrayList<>();
    }

    @Override
    public boolean saveData(Elements parsedData) {
        String fileName = "";
        File newFile = null;
        boolean savedStatus = true;
        if (!Objects.equals(this.saveSource, "")) {
            fileName = this.saveSource + getValidName(parsedData.get(1).text()) + ".txt";
            newFile = new File(fileName);
            this.log.add(fileName);
        }
        assert newFile != null;
        if (!newFile.exists()) {
            savedStatus = createFile(newFile);
            if (!Objects.equals(fileName, ""))
                savedStatus = updateFile(fileName, parsedData, true);
        }
        return savedStatus;
    }

    private boolean updateFile(String fileName, Elements parsedData, boolean writeStatus) {
        boolean updatingStatus = true;
        try (FileWriter writer = new FileWriter(fileName, writeStatus)) {
            for (Element savedData : parsedData) {
                if (!Objects.equals(savedData.attributes().get("href"), ""))
                    writer.write(savedData.attributes().get("href"));
                writer.write(savedData.text() + '\n');
            }
            this.log.add(" - done");
            writer.flush();
        } catch (IOException e) {
            updatingStatus = false;
        }
        return updatingStatus;
    }

    private boolean createFile(File newFile) {
        try {
            return newFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("Write exception", e);
        }
    }

    @Override
    public String getValidName(String baseName) {
        String resultString = baseName;
        if (baseName.contains("\\")) resultString = baseName.replace("\\", "-");
        if (baseName.contains("/")) resultString = baseName.replace("/", "-");
        if (baseName.contains("&#039;")) resultString = baseName.replace("&#039;", "'");
        if (baseName.contains("\"")) resultString = baseName.replace("\"", "''");
        if (baseName.contains(":")) resultString = baseName.replace(":", "=");
        if (baseName.contains("*")) resultString = baseName.replace("*", "x");
        if (baseName.contains("?")) resultString = baseName.replace("?", "");
        if (baseName.contains("<")) resultString = baseName.replace("<", "[");
        if (baseName.contains(">")) resultString = baseName.replace(">", "]");
        if (baseName.contains("|")) resultString = baseName.replace("|", "-");
        return resultString;
    }

    @Override
    public String getSaveSource() {
        return this.saveSource;
    }

    @Override
    public void createLog() {
        try (FileWriter logWriter = new FileWriter(this.logFile.getAbsolutePath(), true)) {
            int counter = 1;
            for (String logString : this.log) {
                counter++;
                logWriter.write(logString);
                if (counter == 2) {
                    logWriter.write('\n');
                    counter = 1;
                }
            }
            logWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        createFile(this.logFile);
    }
}
