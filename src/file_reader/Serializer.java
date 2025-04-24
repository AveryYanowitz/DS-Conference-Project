package file_reader;

import java.util.Map;
import java.util.Set;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import main.Word;
import main.Utilities;

public class Serializer {
    public static <T> void exportObject(T obj, File f) {
        try {
            FileOutputStream fileOut = new FileOutputStream(f, true);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(obj);
            out.close();
            fileOut.close();
            System.out.println("Serialized data is saved in "+f);
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public static void main(String args[]) {
        var mapTuple = CorpusReader.getWordMaps("brown_tag.txt");
        exportObject(mapTuple, new File("./assets/maps.ser"));
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

