package com.sciaps.common.swing.utils;

import com.sciaps.common.swing.model.CSV;

/**
 *
 * @author sgowen
 */
public final class CSVUtils
{
    public static int getNumRowsInCSV(CSV csv)
    {
        runCSVNullAssertions(csv);

        return csv.csvRows.size();
    }

    public static int getNumColumnsInCSV(CSV csv)
    {
        runCSVNullAssertions(csv);
        runCSVLengthAssertions(csv);

        return csv.csvRows.get(0).length;
    }

    public static double readDoubleFromCSV(CSV csv, int rowIndex, int columnIndex)
    {
        if (rowIndex == 0)
        {
            throw new IllegalArgumentException("You are trying to convert a CSV header into a double; this is probably NOT what you want to do! Try reading the CSV starting with the 1st row (index 1).");
        }

        String value = readValueFromCSV(csv, rowIndex, columnIndex);

        return Double.parseDouble(value);
    }

    public static String readValueFromCSV(CSV csv, int rowIndex, int columnIndex)
    {
        runCSVNullAssertions(csv);
        runCSVLengthAssertions(csv);

        if (rowIndex < 0 || rowIndex >= csv.csvRows.size())
        {
            throw new IllegalArgumentException("The requested row: " + rowIndex + " is not available");
        }

        if (columnIndex < 0 || columnIndex >= csv.csvRows.get(0).length)
        {
            throw new IllegalArgumentException("The requested column: " + columnIndex + " is not available");
        }

        String value = csv.csvRows.get(rowIndex)[columnIndex];

        return value;
    }

    private static void runCSVNullAssertions(CSV csv)
    {
        if (csv == null)
        {
            throw new IllegalArgumentException("The csv object cannot be null!");
        }

        if (csv.csvRows == null)
        {
            throw new IllegalArgumentException("The csv object cannot have null rows!");
        }
    }

    private static void runCSVLengthAssertions(CSV csv)
    {
        if (csv.csvRows.isEmpty())
        {
            throw new IllegalArgumentException("The csv object cannot have 0 rows!");
        }
    }

    private CSVUtils()
    {
        // Hide Constructor For Static Utility Class
    }
}