package GUI;

import Universe.Company;
import Universe.World;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class BuyOutController {

    World w = World.INSTANCE;
    private Company company;
    private Stage stage;

    @FXML
    private TextField myTextField;

    @FXML
    private Label myLabel;

    public void setCompany(String companyName){
        this.company = w.getCompanies().get(companyName);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void buyOut()
    {
        try{
            int value = Integer.parseInt(myTextField.getText());
            this.company.buyOut(value);
            stage.close();

        } catch(NumberFormatException e){
            myLabel.setText("Enter only numbers please");
        } catch(Exception e){
            System.out.println("Error!");
        }
    }
}
