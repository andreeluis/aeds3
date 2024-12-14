import java.util.ArrayList;
import java.util.List;

import controller.AppController;
import db.index.bplustree.BPlusTree;
import db.index.hash.ExtendedHash;
import db.index.inverted.InvertedIndex;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import model.Movie;
import model.MovieMenuFactory;
import model.interfaces.BaseIndexStrategy;
import view.GUIController;
import view.Menu;

public class Main extends Application {
  @Override
  public void start(Stage primaryStage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("view/main.fxml"));

    List<BaseIndexStrategy<Movie>> indexes = new ArrayList<>();
    indexes.add(new BPlusTree<Movie>(10));
    indexes.add(new ExtendedHash<>(10));
    indexes.add(new InvertedIndex<Movie>("Title", Movie::getTitle));
    //indexes.add(new InvertedIndex<Movie>("Description", Movie::getMovieInfo));

    AppController<Movie> movieControler = new AppController<>(Movie.class.getConstructor(), indexes);
    MovieMenuFactory movieMenuFactory = new MovieMenuFactory();

    GUIController<Movie> controller = new GUIController<Movie>(movieControler, movieMenuFactory.getEntityName());
    new Menu<Movie>(movieControler, movieMenuFactory);

    loader.setController(controller);
    FlowPane root = loader.load();
    root.setAlignment(Pos.CENTER);
    Scene scene = new Scene(root, 1200, 700);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void main(String[] args) throws Exception {
    launch(args);
  }
}
