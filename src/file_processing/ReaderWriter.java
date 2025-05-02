package file_processing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import java.io.IOException;

public class ReaderWriter {
    public static <T> void exportObject(T obj, File f) {
        try {
            FileOutputStream fileOut = new FileOutputStream(f);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(obj);
            out.close();
            fileOut.close();
            System.out.println();
            System.out.println("Serialized data is saved in "+f);
            System.out.println();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public static void main(String args[]) {
        var twoMaps = CorpusProcessor.getWordMaps("brown_tag.txt");
        Map<String, String> abbreviationKey = getAbbreviationKey("./assets/all_tags.txt");
        exportObject(twoMaps.first(), new File("./assets/wordsToTagProbs.ser"));
        exportObject(twoMaps.second(), new File("./assets/legalNextTags.ser"));
        exportObject(abbreviationKey, new File("./assets/abbreviationKey.ser"));
    }

    @SuppressWarnings("unchecked")
    public static <T> T importObject(File f) throws IOException, ClassNotFoundException {
        T importedObj = null;
        FileInputStream fileIn = new FileInputStream(f);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        importedObj = (T) in.readObject();
        in.close();
        fileIn.close();
        return importedObj;
    }

    public static Map<String, String> getAbbreviationKey(String pathname) {
        Map<String, String> tagAbbreviationKey = new TreeMap<>();
        Scanner scanner;
        try {
            scanner = new Scanner(new File(pathname));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        while (scanner.hasNextLine()) {
            String[] line = scanner.nextLine().split("//");
            tagAbbreviationKey.put(line[0], line[1]);
        }
        scanner.close();
        return tagAbbreviationKey;
    }
}

