public class Launcher {
    public static void main(String[] args) {
        String parseSource = "http://www.strangearts.ru/marvel-heroes";
        WebParser parser = new WebParser(saveLib.SaveTo.FILE, "C:/Users/usersPlugins/");
        parser.start(parseSource);
    }
}
