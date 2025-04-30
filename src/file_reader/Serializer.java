package file_reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

public class Serializer {
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
        exportObject(twoMaps, new File("./assets/maps.ser"));
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

}

