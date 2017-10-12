package gui.controls;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;


public class GroupBox extends StackPane {


    private Label titleLabel = new Label();
    private StackPane contentPane = new StackPane();
    private Node content;


    public void setContent(Node content) {
        content.setStyle("-fx-padding: 26 10 10 10;");
        contentPane.getChildren().add(content);
    }


    public Node getContent() {
        return content;
    }


    public void setTitle(String title) {
        titleLabel.setText(" " + title + " ");
    }


    public String getTitle() {
        return titleLabel.getText();
    }


    public GroupBox() {
        titleLabel.setText("default title");
        titleLabel.setStyle("-fx-background-color:whitesmoke;-fx-translate-y: -7;-fx-font-size: 11px");
        StackPane.setAlignment(titleLabel, Pos.TOP_LEFT);
        StackPane.setMargin(titleLabel,new Insets(0,0,0,20));

        setStyle("-fx-content-display: top; -fx-border-insets: 20 15 15 15; -fx-background-color:  transparent; -fx-border-color: lightgrey; -fx-border-width: 1;");
        getChildren().addAll(titleLabel, contentPane);
    }


}
