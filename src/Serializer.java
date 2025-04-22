import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Set;

public class Serializer {
    public static <K, V> void exportMap(Map<K, V> map, File f) {
        try {
            FileOutputStream fileOut = new FileOutputStream(f, true);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(map);
            out.close();
            fileOut.close();
            System.out.println("Serialized data is saved in "+f);
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public static void main(String args[]) {
        var freqsTagsTuple = CorpusReader.getWordMaps("brown_tag.txt");
        Map<Word, Integer> wordsWithFreqs = freqsTagsTuple.map1();
        Map<String, Set<String>> followingTags = freqsTagsTuple.map2();
        Map<String, Set<String>> wordsWithTags = Utilities.extractTags(wordsWithFreqs);

        exportMap(wordsWithFreqs, new File("assets","wordsWithFreqs.ser"));
        exportMap(followingTags, new File("assets","followingTags.ser"));
        exportMap(wordsWithTags, new File("assets","wordsWithTags.ser"));
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> importMap(File f) throws IOException, ClassNotFoundException {
        Map<K, V> importedMap = null;
        FileInputStream fileIn = new FileInputStream(f);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        importedMap = (Map<K, V>) in.readObject();
        in.close();
        fileIn.close();
        return importedMap;
    }

    public static <K1, K2, K3, V1, V2, V3> void importExportTest(Map<K1,V1> map1, Map<K2, V2> map2, Map<K3, V3> map3) {
        try {
            File file = new File("assets","brown_tiny.ser");

            exportMap(map1, file);
            exportMap(map2, file);
            exportMap(map3, file);
            
            for (int i = 0; i < 3; i++) {
                var importedMap = importMap(file);
                System.out.println("Map "+i+"...");
                Utilities.printMap(importedMap, 5, false);
            }

        } catch (Exception e) {
            System.out.println("There was a problem...");
        }

    }

}
