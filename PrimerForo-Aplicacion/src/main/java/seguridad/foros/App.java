package seguridad.foros;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

/**
 * Hello world!
 *
 */
public class App extends Application
{

    private MySQLCredentials currentCredentials;
    private Connection currentConnection;

    public static void main( String[] args )
    {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Laboratorio 1");
        //create the grid layout for the window
        Scene scene = generateMainScene();
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private Scene generateMainScene() {
        GridPane pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        pane.setHgap(10);
        pane.setVgap(10);

        final TextField userTextField = new TextField();
        userTextField.setPromptText("Numero de tarjeta");
        pane.add(userTextField, 0, 1);

        Button searchButton = new Button("Buscar");
        pane.add(searchButton, 0,2);
        searchButton.setOnAction(event -> searchCreditCardNumber(userTextField.getText()));

        Button configureButton = new Button("Configurar");
        pane.add(configureButton, 1,2);
        configureButton.setOnAction(event -> configureDatabaseCredentials());

        pane.setPadding(new Insets(25,25,25,25));
        return new Scene(pane, 300, 300);
    }

    private void configureDatabaseCredentials() {
        Dialog<MySQLCredentials> dialog = new Dialog<>();
        dialog.setTitle("Configuracion");

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        TextField user = new TextField();
        user.setPromptText("Usuario");
        PasswordField password = new PasswordField();
        password.setPromptText("Clave");
        TextField host = new TextField();
        host.setPromptText("Direccion del servidor (incluir puerto)");
        TextField databaseName = new TextField();
        databaseName.setPromptText("Nombre de la base de datos");

        if(currentCredentials != null) {
            user.setText(currentCredentials.username);
            password.setText(currentCredentials.password);
            host.setText(currentCredentials.host);
            databaseName.setText(currentCredentials.databaseName);
        }

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 10));
        gridPane.add(user, 0, 1);
        gridPane.add(password, 0, 2);
        gridPane.add(host, 0, 3);
        gridPane.add(databaseName, 0, 4);
        dialog.getDialogPane().setContent(gridPane);

        dialog.setResultConverter(dialogButton -> {
            if(dialogButton == okButton) {
                return new MySQLCredentials(user.getText(), host.getText(), password.getText(), databaseName.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result  -> {
            currentCredentials = result;
            if(currentConnection != null) {
                try {
                    currentConnection.close();
                    currentConnection = null;
                } catch (SQLException e) {
                    e.printStackTrace();
                    //do nothing
                }
            }
        });
    }

    private void searchCreditCardNumber(String text) {
        if(text == null || text.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error Buscando la tarjeta");
            alert.setContentText("El campo de numero de tarjeta no debe de estar vacio");

            alert.showAndWait();
            return;
        }


        if(currentCredentials == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error Buscando la tarjeta");
            alert.setContentText("Favor configure las propiedades de la conexion.");

            alert.showAndWait();
            return;
        }
        PreparedStatement preparedStatement = null;
        try {
            Properties connectionProps = new Properties();
            connectionProps.put("user", currentCredentials.username);
            connectionProps.put("password", currentCredentials.password);
            if(currentConnection == null) {
                currentConnection = DriverManager.getConnection(
                        "jdbc:mysql://" + currentCredentials.host +"/"+
                                currentCredentials.databaseName,
                        connectionProps);
            }
             preparedStatement = currentConnection.prepareStatement("SELECT concat(Name, \" \", FirstName, \" \", LastName) AS Name " +
                    "FROM CreditCardInformation " +
                    "WHERE sha2(?, 0) = hashedCreditCard ");
            preparedStatement.setString(1, text);
            preparedStatement.setMaxRows(1);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Encontrada");
                alert.setHeaderText("Tarjeta Encontrada");
                alert.setContentText("La Tarjeta pertence a " + resultSet.getString("Name"));
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No se encontro la tarjeta");
                alert.setHeaderText("Tarjeta No Existe");
                alert.setContentText("La tarjeta buscada no existe");
                alert.showAndWait();
            }

        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error en la conexion");
            alert.setContentText("No es posible ejecutar la consulta");

            // Create expandable Exception.
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String exceptionText = sw.toString();

            Label label = new Label("The exception stacktrace was:");

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            // Set expandable Exception into the dialog pane.
            alert.getDialogPane().setExpandableContent(expContent);

            alert.showAndWait();
        } finally {
            if(preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    //not reporting
                }
            }
        }
    }

    private class MySQLCredentials {
        final String username;
        final String host;
        final String password;
        final String databaseName;

        private MySQLCredentials(String username, String host, String password,  String databaseName) {
            this.username = username;
            this.host = host;
            this.password = password;
            this.databaseName = databaseName;
        }
    }
}
