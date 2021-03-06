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

package ninja.javafx.smartcsv.fx;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import ninja.javafx.smartcsv.FileReader;
import ninja.javafx.smartcsv.csv.CSVFileReader;
import ninja.javafx.smartcsv.csv.CSVFileWriter;
import ninja.javafx.smartcsv.fx.table.ObservableMapValueFactory;
import ninja.javafx.smartcsv.fx.table.ValidationCellFactory;
import ninja.javafx.smartcsv.fx.table.model.CSVModel;
import ninja.javafx.smartcsv.fx.table.model.CSVValue;
import ninja.javafx.smartcsv.fx.table.model.CSVRow;
import ninja.javafx.smartcsv.validation.ValidationFileReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static javafx.application.Platform.runLater;

/**
 * main controller of the application
 */
@Component
public class SmartCSVController extends FXMLController {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // injections
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Autowired
    private CSVFileReader csvLoader;

    @Autowired
    private ValidationFileReader validationLoader;

    @Autowired
    private CSVFileWriter csvFileWriter;

    @FXML
    private BorderPane applicationPane;

    @FXML
    private Label stateline;


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // injections
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private ValidationCellFactory cellFactory = new ValidationCellFactory();
    private final LoadCSVService loadCSVService = new LoadCSVService();
    private final SaveCSVService saveCSVService = new SaveCSVService();
    private CSVModel model;


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // init
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        stateline.setVisible(false);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // setter
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Value("${fxml.smartcvs.view}")
    @Override
    public void setFxmlFilePath(String filePath) {
        this.fxmlFilePath = filePath;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // actions
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @FXML
    public void openCsv(ActionEvent actionEvent) {
        loadFile(csvLoader, "CSV files (*.csv)", "*.csv", "Open CSV");
    }

    @FXML
    public void openConfig(ActionEvent actionEvent) {
        loadFile(validationLoader, "JSON files (*.json)", "*.json", "Open Validation Configuration");
    }

    @FXML
    public void saveCsv(ActionEvent actionEvent) {
        saveCSVService.restart();
    }

    @FXML
    public void saveAsCsv(ActionEvent actionEvent) {
        saveFile(csvFileWriter, "CSV files (*.csv)", "*.csv");
    }

    @FXML
    public void close(ActionEvent actionEvent) {
        Platform.exit();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void loadFile(FileReader fileReader, String filterText, String filter, String title) {
        final FileChooser fileChooser = new FileChooser();

        //Set extension filter
        final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(filterText, filter);
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setTitle(title);

        //Show open file dialog
        final File file = fileChooser.showOpenDialog(applicationPane.getScene().getWindow());
        if (file != null) {
            loadCSVService.setFile(file);
            loadCSVService.setFileReader(fileReader);
            loadCSVService.restart();
        }
    }

    private void saveFile(CSVFileWriter writer, String filterText, String filter) {
        if (model != null) {
            final FileChooser fileChooser = new FileChooser();

            //Set extension filter
            final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(filterText, filter);
            fileChooser.getExtensionFilters().add(extFilter);

            File initfile = new File(model.getFilepath());
            fileChooser.setInitialDirectory(initfile.getParentFile());
            fileChooser.setInitialFileName(initfile.getName());
            fileChooser.setTitle("Save File");

            //Show open file dialog
            final File file = fileChooser.showOpenDialog(applicationPane.getScene().getWindow());
            if (file != null) {
                model.setFilepath(file.getAbsolutePath());
                saveCSVService.restart();
            }
        }
    }

    /**
     * Creates new table view and add the new content
     */
    private void resetContent() {
        model = csvLoader.getData();
        model.setValidator(validationLoader.getValidator());

        TableView<CSVRow> tableView = new TableView<>();

        for (String column: model.getHeader()) {
            addColumn(column, tableView);
        }
        tableView.getItems().setAll(model.getRows());
        tableView.setEditable(true);

        applicationPane.setCenter(tableView);
    }

    /**
     * Adds a column with the given name to the tableview
     * @param header name of the column header
     * @param tableView the tableview
     */
    private void addColumn(String header, TableView tableView) {
        TableColumn column = new TableColumn(header);
        column.setCellValueFactory(new ObservableMapValueFactory(header));
        column.setCellFactory(cellFactory);
        column.setEditable(true);
        column.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<CSVRow, CSVValue>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<CSVRow, CSVValue> event) {
                event.getTableView().getItems().get(event.getTablePosition().getRow()).
                getColumns().get(header).
                        setValue(event.getNewValue());
            }
        });

        tableView.getColumns().add(column);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // inner class
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Service class for async load of a csv file
     */
    private class LoadCSVService extends Service {

        private File file = null;
        private FileReader fileReader;

        public void setFile(File value) {
            file = value;
        }
        public void setFileReader(FileReader fileReader) {
            this.fileReader = fileReader;
        }

        @Override
        protected Task createTask() {
            return new Task() {
                @Override
                protected Void call() throws Exception {
                    if (file != null) {
                        try {
                            fileReader.read(file);
                            runLater(SmartCSVController.this::resetContent);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    return null;
                }
            };
        }
    }

    /**
     * Service class for async load of a csv file
     */
    private class SaveCSVService extends Service {

        @Override
        protected Task createTask() {
            return new Task() {
                @Override
                protected Void call() throws Exception {
                    try {
                        csvFileWriter.saveFile(model);
                        runLater(SmartCSVController.this::resetContent);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }
            };
        }
    }
}
