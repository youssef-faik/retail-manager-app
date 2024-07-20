package com.example.gestioncommercial.invoice;

import com.example.gestioncommercial.DataAccessObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListInvoicesController implements Initializable {
    @FXML
    private TableView<Invoice> invoicesTableView;

    @FXML
    private Button deleteButton, newButton, updateButton;

    private DataAccessObject dao;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dao = new DataAccessObject();

        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        TableColumn<Invoice, String> idColumn = new TableColumn<>("ID");
        TableColumn<Invoice, String> issueDateColumn = new TableColumn<>("Date d'émission");
        TableColumn<Invoice, String> clientColumn = new TableColumn<>("Client");
        TableColumn<Invoice, String> totalExcludingTaxesColumn = new TableColumn<>("Total (HT)");
        TableColumn<Invoice, String> totalTaxesColumn = new TableColumn<>("Taxes");
        TableColumn<Invoice, String> totalIncludingTaxesColumn = new TableColumn<>("Total (TTC)");

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        issueDateColumn.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        clientColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClient().getName()));
        totalExcludingTaxesColumn.setCellValueFactory(new PropertyValueFactory<>("totalExcludingTaxes"));
        totalTaxesColumn.setCellValueFactory(new PropertyValueFactory<>("totalTaxes"));
        totalIncludingTaxesColumn.setCellValueFactory(new PropertyValueFactory<>("totalIncludingTaxes"));

        invoicesTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


        invoicesTableView.getColumns().addAll(
                idColumn,
                issueDateColumn,
                clientColumn,
                totalExcludingTaxesColumn,
                totalTaxesColumn,
                totalIncludingTaxesColumn
        );

        invoicesTableView.setOnMouseClicked(e -> {
            if (invoicesTableView.getSelectionModel().getSelectedItem() != null) {
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });

        getAllInvoices();
    }

    private void getAllInvoices() {
        String query = """
                select I.id as id, issue_date, name, total_excluding_taxes, total_including_taxes, total_taxes
                from invoice as I
                    join Client as C on I.id_client = C.id;""";

        invoicesTableView.setItems(dao.getAllInvoices(query));
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    public void addInvoice(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("form-invoice.fxml")));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.showAndWait();
        getAllInvoices();
    }

    public void updateInvoice(ActionEvent actionEvent) throws IOException {
        Invoice selectedInvoice = invoicesTableView.getSelectionModel().getSelectedItem();
        if (selectedInvoice != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("form-invoice.fxml"));
            Parent root = fxmlLoader.load();

            InvoiceController invoiceController = fxmlLoader.getController();

            Invoice invoice = dao.getInvoiceById(selectedInvoice.getId());
            invoiceController.initInvoiceUpdate(invoice);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            getAllInvoices();
        }
    }

    public void deleteInvoice(ActionEvent actionEvent) throws SQLException {
        Invoice selectedInvoice = invoicesTableView.getSelectionModel().getSelectedItem();
        if (selectedInvoice != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Confirmation de suppression");
            alert.setContentText("Voulez vous supprimer cette facture?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                String deleteInvoiceItemsQuery = "DELETE FROM invoice_item WHERE id_invoice = " + selectedInvoice.getId();
                String deleteInvoiceQuery = "DELETE FROM invoice WHERE id = " + selectedInvoice.getId();
                dao.saveData(deleteInvoiceItemsQuery);
                dao.saveData(deleteInvoiceQuery);
                getAllInvoices();
            }
        }
    }
}
