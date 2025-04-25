package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class StringUtil {

    @SuppressWarnings("resource")
    public static String getString(String prompt) {
        Scanner reader = new Scanner(System.in);
        System.out.print(prompt);
        return reader.nextLine();
    }

    @SuppressWarnings("resource")
    public static List<String> getList(String prompt, String[] possibilities) {
        List<String> answerKey = Arrays.asList(possibilities);
        List<String> filteredAnswers = new ArrayList<>();
        do {
            Scanner reader = new Scanner(System.in);
            System.out.println(prompt);
            System.out.println("Valid answers: "+answerKey);
            System.out.print("Enter as comma-separated list or type 'ALL': ");
            String[] answers = reader.nextLine().split(", ");
            if (answers[0].equals("ALL")) {
                return answerKey;
            }
            
            for (String answer : answers) {
                if (answerKey.contains(answer)) {
                    filteredAnswers.add(answer);
                }
            }
        } while (filteredAnswers.size() == 0); // Repeat if no valid tags were given
        return filteredAnswers;
    }

    @SuppressWarnings("resource")
    public static boolean getYN(String prompt) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(prompt+" (Y/N) ");
            String s = scanner.next();
            if (s.toLowerCase().equals("y") || s.toLowerCase().equals("yes")) {
                return true;
            }
            if (s.toLowerCase().equals("n") || s.toLowerCase().equals("no")) {
                return false;
            }
            System.out.println("Didn't understand, please try again.");
        }
    }

    public static String stripNonAlpha(String oldString) {
        StringBuilder sb = new StringBuilder();
        for (char ch : oldString.toCharArray()) {
            if (isExpandedAlpha(ch)) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static boolean hasNonAlpha(String s) {
        for (char ch : s.toCharArray()) {
            if (!Character.isAlphabetic(ch) && !acceptAnyway(ch)) {
                return true;
            }
        }
        return false;
    }

    // I don't know what to name this
    private static boolean isExpandedAlpha(char ch) {
        return acceptAnyway(ch) || Character.isAlphabetic(ch);
    }

    private static boolean acceptAnyway(char ch) {
        return ch == '\'' || ch == '"' || ch == '_';
    }

}