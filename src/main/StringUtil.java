package main;

import java.util.Collection;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import file_processing.Pair;

public class StringUtil {

    @SuppressWarnings("resource")
    public static String getString(String prompt) {
        Scanner reader = new Scanner(System.in);
        System.out.print(prompt);
        return reader.nextLine();
    }

    @SuppressWarnings("resource")
    public static Set<String> getSet(String prompt, Collection<String> possibilities) {
        Set<String> filteredAnswers = new TreeSet<>();
        do {
            Scanner reader = new Scanner(System.in);
            System.out.println(prompt);
            System.out.println("Valid answers: "+possibilities);
            System.out.print("Enter as comma-separated list or type 'ALL': ");
            String[] answers = reader.nextLine().split(", ");
            if (answers[0].toLowerCase().equals("all")) {
                return null;
            }

            for (String answer : answers) {
                if (possibilities.contains(answer)) {
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

    @SuppressWarnings("resource")
    public static double getDouble(String prompt, int lowerBound, int upperBound) {
        double input = lowerBound - 0.5;
        while (input < lowerBound || input > upperBound) {
            Scanner scanner = new Scanner(System.in);
            System.out.print(prompt+" (Enter a number between "+lowerBound+" and "+upperBound+") ");
            try {
                input = scanner.nextDouble();
            } catch (InputMismatchException e) {
                System.out.println("That's not a number!");
                continue;
            }
        }
        return input;
    }

    @SuppressWarnings("resource")
    public static int getInt(String prompt, int lowerBound, int upperBound) {
        int input = lowerBound - 1;
        while (input < lowerBound || input > upperBound) {
            Scanner scanner = new Scanner(System.in);
            System.out.print(prompt+" (Enter a whole number between "+lowerBound+" and "+upperBound+") ");
            try {
                input = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("That's not an integer!");
                continue;
            }
        }
        return input;

    }

    // Only returns positive integers
    public static int getInt(String prompt) {
        return getInt(prompt, 0, Integer.MAX_VALUE);
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

    public static String formatParse(Pair<String, Double> parseAndProb, TagAtlas tagAtlas) {
        StringBuilder sb = new StringBuilder();
        String[] tags = parseAndProb.first().split(" ");
        if (tagAtlas.isVerbose()) {
            for (String tag : tags) {
                sb.append(tagAtlas.getLongForm(tag));
                sb.append(" ");
            }
        } else {
            for (String tag : tags) {
                sb.append(tag);
                sb.append(" ");
            }
        }
        sb.append("\n Probability: ");
        sb.append(parseAndProb.second());
        sb.append("\n");
        return sb.toString();
    }

    public static String formatVerboseParse(Pair<String, Integer> parseAndProb) {
        return "";
    }

    // I don't know what to name this
    private static boolean isExpandedAlpha(char ch) {
        return acceptAnyway(ch) || Character.isAlphabetic(ch);
    }

    private static boolean acceptAnyway(char ch) {
        return ch == '\'' || ch == '"' || ch == '_';
    }

}