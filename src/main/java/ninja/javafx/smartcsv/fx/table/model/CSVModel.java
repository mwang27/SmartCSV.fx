/*
   The MIT License (MIT)
   -----------------------------------------------------------------------------

   Copyright (c) 2015 javafx.ninja <info@javafx.ninja>

   Permission is hereby granted, free of charge, to any person obtaining a copy
   of this software and associated documentation files (the "Software"), to deal
   in the Software without restriction, including without limitation the rights
   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
   copies of the Software, and to permit persons to whom the Software is
   furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in
   all copies or substantial portions of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
   THE SOFTWARE.

*/

package ninja.javafx.smartcsv.fx.table.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ninja.javafx.smartcsv.validation.ValidationState;
import ninja.javafx.smartcsv.validation.Validator;

/**
 * The CSVModel is the client representation for the csv filepath.
 * It holds the data in rows, stores the header and manages the validator.
 */
public class CSVModel {

    private Validator validator;
    private ObservableList<CSVRow> rows = FXCollections.observableArrayList();
    private String[] header;
    private String filepath;

    public CSVModel(String filepath) {
        this.filepath = filepath;
    }

    public String getFilepath() {
        return this.filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    /**
     * sets the validator for the data revalidates
     * @param validator the validator for this data
     */
    public void setValidator(Validator validator) {
        this.validator = validator;
        revalidate();
    }

    /**
     * returns the data as a list of rows of the
     * @return list of rows
     */
    public ObservableList<CSVRow> getRows() {
        return rows;
    }

    /**
     * adds a new and empty row
     * @return the new row
     */
    public CSVRow addRow() {
        CSVRow row = new CSVRow();
        row.setValidator(validator);
        row.setRowNumber(rows.size());
        rows.add(row);
        return row;
    }

    /**
     * sets the column headers as string array
     * @param header the headers of the columns
     */
    public void setHeader(String[] header) {
        this.header = header;
    }

    /**
     * returns the column headers
     * @return the column headers
     */
    public String[] getHeader() {
        return header;
    }


    /**
     * walks through the data and validates each value
     */
    private void revalidate() {
        for (CSVRow row: rows) {
            row.setValidator(validator);
            for (String column: row.getColumns().keySet()) {
                CSVValue value = row.getColumns().get(column).getValue();
                value.setValidator(validator);
                if (validator != null) {
                    value.setValid(validator.isValid(column, value.getValue()));
                } else {
                    value.setValid(new ValidationState());
                }
            }
        }
    }

}
