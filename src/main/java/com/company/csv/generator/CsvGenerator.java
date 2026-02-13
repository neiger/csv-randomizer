package com.company.csv.generator;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.*;

public class CsvGenerator {

    private static final String ITEM_NUMBER_COL = "ITEM_NUMBER";
    private static final String COMPONENT_COL = "COMPONENT_MPN";
    private static final int MIN_COUNT = 1;  // Minimum rows per ITEM_NUMBER
    private static final int MAX_COUNT = 10; // Maximum rows per ITEM_NUMBER

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();

        if (args.length < 3) {
            System.out.println("Usage: java -jar csv-generator.jar <template.csv> <output.csv> <numDistinctItemNumbers>");
            System.exit(1);
        }

        Path templatePath = Path.of(args[0]);
        Path outputPath = Path.of(args[1]);
        int targetItemCount = Integer.parseInt(args[2]);

        System.out.println("▶ Loading template: " + templatePath);
        List<String[]> templateRows;
        try (CSVReader reader = new CSVReader(new FileReader(templatePath.toFile()))) {
            templateRows = reader.readAll();
        }

        if (templateRows.size() < 2) {
            throw new IllegalStateException("Template CSV has no data rows.");
        }

        String[] header = templateRows.get(0);

        int itemIndex = -1;
        int componentIndex = -1;
        for (int i = 0; i < header.length; i++) {
            if (ITEM_NUMBER_COL.equals(header[i])) itemIndex = i;
            if (COMPONENT_COL.equals(header[i])) componentIndex = i;
        }
        if (itemIndex == -1) throw new IllegalStateException("ITEM_NUMBER column not found.");
        if (componentIndex == -1) throw new IllegalStateException("COMPONENT_MPN column not found.");

        System.out.println("▶ Generating " + targetItemCount + " distinct ITEM_NUMBERs...");

        List<String[]> outputRows = new ArrayList<>();
        outputRows.add(header);

        Random random = new Random();
        int seed = 0;
        int templateSize = templateRows.size() - 1; // exclude header
        int templateIdx = 1; // start after header

        for (int i = 0; i < targetItemCount; i++) {
            // Pick a template row (cycle if needed)
            String[] templateRow = templateRows.get(templateIdx).clone();
            templateIdx++;
            if (templateIdx > templateSize) templateIdx = 1;

            // Generate a new ITEM_NUMBER
            String originalItem = templateRow[itemIndex];
            String newItemNumber = generatePreservingFormatUnique(originalItem, seed++, random);
            templateRow[itemIndex] = newItemNumber;

            // Random number of rows for this ITEM_NUMBER
            int rowCount = MIN_COUNT + random.nextInt(MAX_COUNT - MIN_COUNT + 1);

            for (int r = 0; r < rowCount; r++) {
                // Clone row for each repetition
                String[] rowClone = templateRow.clone();

                // Generate unique COMPONENT_MPN per row to maintain uniqueness
                String originalComponent = rowClone[componentIndex];
                rowClone[componentIndex] = generatePreservingFormatUnique(originalComponent, seed++, random);

                outputRows.add(rowClone);
            }
        }

        System.out.println("▶ Writing output: " + outputPath);
        try (CSVWriter writer = new CSVWriter(new FileWriter(outputPath.toFile()))) {
            for (String[] row : outputRows) {
                writer.writeNext(row);
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("✅ Done. Distinct ITEM_NUMBERs: " + targetItemCount);
        System.out.println("⏱ Total rows generated: " + (outputRows.size() - 1));
        System.out.println("⏱ Time: " + (end - start) + " ms");
    }

    /**
     * Preserves letters/digits format and appends numeric suffix to ensure uniqueness.
     */
    private static String generatePreservingFormatUnique(String value, int seed, Random random) {
        StringBuilder sb = new StringBuilder();
        for (char c : value.toCharArray()) {
            if (Character.isDigit(c)) {
                sb.append(random.nextInt(10));
            } else if (Character.isLetter(c)) {
                sb.append(Character.isUpperCase(c)
                        ? (char) ('A' + random.nextInt(26))
                        : (char) ('a' + random.nextInt(26)));
            } else {
                sb.append(c);
            }
        }
        sb.append(String.format("%03d", seed)); // ensures uniqueness
        return sb.toString();
    }
}