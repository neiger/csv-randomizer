package com.company.csv.generator;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CsvGenerator {

    // Parent group column
    private static final String GROUP_COLUMN = "ITEM_NUMBER";

    // Columns that must keep consistent relationships
    private static final Set<String> RELATIONSHIP_COLUMNS = Set.of(
            "CHILD_ITEM_NUMBER",
            "COMPONENT_MPN",
            "MANUFACTURER_ID"
    );

    private static final int MIN_COUNT = 1;
    private static final int MAX_COUNT = 10;

    // Keeps deterministic mapping (same input -> same output)
    private static final Map<String, String> VALUE_MAP = new HashMap<>();

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();

        if (args.length < 3) {
            System.out.println(
                    "Usage: java -jar csv-generator.jar <template.csv> <output.csv> <numDistinctItemNumbers>");
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

        int groupIndex = -1;
        Set<Integer> relationshipIndexes = new HashSet<>();

        for (int i = 0; i < header.length; i++) {

            if (GROUP_COLUMN.equals(header[i])) {
                groupIndex = i;
            }

            if (RELATIONSHIP_COLUMNS.contains(header[i])) {
                relationshipIndexes.add(i);
            }
        }

        if (groupIndex == -1) {
            throw new IllegalStateException("ITEM_NUMBER column not found.");
        }

        System.out.println("▶ Group column: ITEM_NUMBER");
        System.out.println("▶ Relationship columns: " + relationshipIndexes);
        System.out.println("▶ Generating " + targetItemCount + " ITEM_NUMBER groups...");

        Random random = new Random();
        int seed = 0;

        int templateSize = templateRows.size() - 1;
        int templateIdx = 1;

        try (CSVWriter writer =
                     new CSVWriter(
                             Files.newBufferedWriter(outputPath),
                             ',',
                             CSVWriter.DEFAULT_QUOTE_CHARACTER,
                             CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                             CSVWriter.DEFAULT_LINE_END)) {

            // Write header
            writer.writeNext(header, false);

            int totalRows = 0;

            for (int i = 0; i < targetItemCount; i++) {

                // Pick template row
                String[] templateRow = templateRows.get(templateIdx).clone();

                templateIdx++;
                if (templateIdx > templateSize) {
                    templateIdx = 1;
                }

                // Generate GROUP value once
                templateRow[groupIndex] =
                        generatePreservingFormatUnique(
                                templateRow[groupIndex],
                                seed++,
                                random);

                int rowCount =
                        MIN_COUNT + random.nextInt(MAX_COUNT - MIN_COUNT + 1);

                for (int r = 0; r < rowCount; r++) {

                    String[] rowClone = templateRow.clone();

                    // Apply relationship mapping
                    for (Integer idx : relationshipIndexes) {

                        String original = rowClone[idx];
                        String mappedValue = VALUE_MAP.get(original);
                        if (mappedValue == null) {
                            mappedValue =
                            generatePreservingFormatUnique(
                                original,
                                seed++,
                                random);
                        VALUE_MAP.put(original, mappedValue);
                        }
                        rowClone[idx] = mappedValue;
                    }
                    writer.writeNext(rowClone, false);
                    totalRows++;

                    if (totalRows % 1000 == 0) {
                        System.out.println("   ▸ Generated " + totalRows + " rows...");
                    }
                }
            }

            long end = System.currentTimeMillis();

            System.out.println("✅ Done.");
            System.out.println("⏱ Total rows generated: " + totalRows);
            System.out.println("⏱ Time: " + (end - start) + " ms");
        }
    }

    /**
     * Preserves letters/digits/symbols format while generating unique values.
     */
    private static String generatePreservingFormatUnique(
            String value,
            int seed,
            Random random) {

        if (value == null || value.isEmpty()) return value;

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

        // Ensures uniqueness
        sb.append(String.format("%03d", seed));

        return sb.toString();
    }
}