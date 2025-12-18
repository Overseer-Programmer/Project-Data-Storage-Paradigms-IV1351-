package KthDatabaseApp.view;

import java.util.ArrayList;
import java.util.List;

public class Table {
    private static final int MAX_LIST_OUTPUT_LENGTH = 600;
    private String columnNames[];
    private List<Object[]> rows;

    /**
     * Creates a table structure that can have rows added to it and can be printed out.
     * @param columnNames The names for the columns the table should consist of
     * @throws InvalidRowLengthException
     */
    public Table(String... columnNames) throws InvalidRowLengthException {
        if (columnNames.length == 0) {
            throw new InvalidRowLengthException("Cannot have 0 columns for the table");
        }
        this.columnNames = columnNames;
        rows = new ArrayList<>();
    }
    
    public void addRow(Object... elements) throws InvalidRowLengthException {
        if (elements.length != columnNames.length) {
            throw new InvalidRowLengthException("The row length must be " + columnNames.length);
        }
        rows.add(elements);
    }

    /**
     * Print the whole table
     */
    public void printOut() {
        // Get the column widths required to store the column names and elements
        int[] columnWidths = new int[columnNames.length];
        for (int i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i];
            int longestString = columnName.length();
            for (int j = 0; j < Math.min(rows.size(), MAX_LIST_OUTPUT_LENGTH); j++) {
                String elementString = rows.get(j)[i].toString();
                if (elementString.length() > longestString) {
                    longestString = elementString.length();
                }
            }
            columnWidths[i] = longestString;
        }

        // Print the table, with column names followed by separation line and then the elements
        int rowLength = printRow(columnNames, columnWidths);
        for (int i = 0; i < rowLength; i++) {
            System.out.print("-");
        }
        System.out.println();
        for (int i = 0; i < Math.min(rows.size(), MAX_LIST_OUTPUT_LENGTH); i++) {
            printRow(rows.get(i), columnWidths);
        }
        if (rows.size() > MAX_LIST_OUTPUT_LENGTH) {
            System.out.println("(Capped at " + MAX_LIST_OUTPUT_LENGTH + " rows)");
        }
    }

    private int printRow(Object[] elements, int[] columnWidths) {
        String outputRow = "|";
        for (int i = 0; i < elements.length; i++) {
            outputRow += String.format(" %-" + columnWidths[i] + "s |", elements[i].toString());
        }
        System.out.println(outputRow);
        return outputRow.length();
    }
}
