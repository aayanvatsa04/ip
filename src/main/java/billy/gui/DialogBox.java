package billy.gui;

import java.io.IOException;
import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;

/**
 * One message in the conversation: a picture of who said it, beside their words.
 *
 * <p>The user's messages and Billy's are the same box built two ways round,
 * rather than two classes that would have to be kept in step with each other.
 * Only {@link #flip()} tells them apart.
 */
public class DialogBox extends HBox {

    /** Radius of the round mask over the speaker's picture. */
    private static final double PICTURE_RADIUS = 32.0;

    @FXML
    private Label dialog;
    @FXML
    private ImageView displayPicture;

    /**
     * Builds a dialog box showing what someone said.
     *
     * <p>Private because the two static factory methods below say which kind of
     * box is wanted, which reads better at the call site than a boolean would.
     *
     * @param text what was said
     * @param img the speaker's picture
     */
    private DialogBox(String text, Image img) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource("/view/DialogBox.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        dialog.setText(text);
        displayPicture.setImage(img);
        // A round mask, so square pictures still read as avatars.
        displayPicture.setClip(new Circle(PICTURE_RADIUS, PICTURE_RADIUS, PICTURE_RADIUS));
    }

    /**
     * Returns a box for something the user said, with the picture on the right.
     *
     * @param text what the user typed
     * @param img the user's picture
     * @return the dialog box to add to the conversation
     */
    public static DialogBox getUserDialog(String text, Image img) {
        return new DialogBox(text, img);
    }

    /**
     * Returns a box for something Billy said, with the picture on the left.
     *
     * @param text what Billy replied
     * @param img Billy's picture
     * @return the dialog box to add to the conversation
     */
    public static DialogBox getBillyDialog(String text, Image img) {
        DialogBox box = new DialogBox(text, img);
        box.flip();
        return box;
    }

    /**
     * Turns this box around, so the picture is on the left and the text on the
     * right.
     *
     * <p>Putting the two speakers on opposite sides is what lets the reader tell
     * at a glance who said what, without labeling every message.
     */
    private void flip() {
        ObservableList<Node> children = FXCollections.observableArrayList(this.getChildren());
        Collections.reverse(children);
        getChildren().setAll(children);
        setAlignment(Pos.TOP_LEFT);
        dialog.getStyleClass().add("reply-label");
    }
}
