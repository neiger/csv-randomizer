package com.company.csv.randomizer;

import com.opencsv.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CsvRandomizer {

    private static final Set<String> TARGET_COLUMNS = Set.of(
            "ITEM_NUMBER",
            "CHILD_ITEM_NUMBER",
            "COMPONENT_MPN",
            "MANUFACTURER_ID"
    );

    private static final Map<String, String> VALUE_MAP = new HashMap<>();
    private static final Random RANDOM = new Random();

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();

        if (args.length < 2) {
            System.out.println("Usage: java CsvRandomizer <input.csv> <output.csv>");
            System.exit(1);
        }

        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);

        System.out.println("▶ Loading CSV: " + input);

        List<String[]> allRows;
        try (CSVReader reader = new CSVReader(new FileReader(input.toFile()))) {
            allRows = reader.readAll();
        }

        if (allRows.size() < 2) {
            throw new IllegalStateException("CSV has no data rows.");
        }

        String[] header = allRows.get(0);

        Map<Integer, String> targetIndexes = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            if (TARGET_COLUMNS.contains(header[i])) {
                targetIndexes.put(i, header[i]);
            }
        }

        if (targetIndexes.isEmpty()) {
            System.out.println("⚠️ No target columns found to randomize.");
        } else {
            System.out.println("▶ Columns to randomize: " + targetIndexes.values());
        }

        System.out.println("▶ Randomizing " + (allRows.size() - 1) + " rows...");
        
try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(output.toFile()))
        // .withSeparator(',')
        // .withQuoteChar('"')
        // .build()) {
         .withSeparator(',')
         .withQuoteChar(CSVWriter.DEFAULT_QUOTE_CHARACTER)
         .withEscapeChar(CSVWriter.DEFAULT_ESCAPE_CHARACTER)
         .withLineEnd(CSVWriter.DEFAULT_LINE_END)
        // .withApplyQuotesToAll(false)
         .build()) {
            writer.writeNext(header, false);

            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
                for (Integer idx : targetIndexes.keySet()) {
                    row[idx] = randomizePreserveFormat(row[idx]);
                }
                writer.writeNext(row, false);

                if (i % 1000 == 0) { // log every 1000 rows for large files
                    System.out.println("   ▸ Processed " + i + " rows...");
                }
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("✅ CSV randomized successfully: " + output);
        System.out.println("⏱ Total rows processed: " + (allRows.size() - 1));
        System.out.println("⏱ Time taken: " + (end - start) + " ms");
    }

    private static String randomizePreserveFormat(String value) {
        if (value == null || value.isEmpty()) return value;

        if (VALUE_MAP.containsKey(value)) {
            return VALUE_MAP.get(value);
        }

        StringBuilder sb = new StringBuilder();

        for (char c : value.toCharArray()) {
            if (Character.isDigit(c)) {
                sb.append(RANDOM.nextInt(10));
            } else if (Character.isLetter(c)) {
                sb.append(Character.isUpperCase(c)
                        ? (char) ('A' + RANDOM.nextInt(26))
                        : (char) ('a' + RANDOM.nextInt(26)));
            } else {
                sb.append(c); // keep symbols like -, /, _
            }
        }

        String randomized = sb.toString();
        VALUE_MAP.put(value, randomized);
        return randomized;
    }
}
