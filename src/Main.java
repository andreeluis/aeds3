import java.util.ArrayList;
import java.util.List;

import controller.AppController;
import db.index.bplustree.BPlusTree;
import db.index.hash.ExtendedHash;
import db.index.inverted.InvertedIndex;
import model.Movie;
import model.MovieMenuFactory;
import model.interfaces.BaseIndexStrategy;
import view.Menu;

public class Main {
  public static void main(String[] args) throws Exception {
    List<BaseIndexStrategy<Movie>> indexes = new ArrayList<>();
    indexes.add(new BPlusTree<Movie>(10));
    indexes.add(new ExtendedHash<Movie>(10));
    indexes.add(new InvertedIndex<Movie>("Title", Movie::getTitle));
    // indexes.add(new InvertedIndex<Movie>(dbPath, "Description", Movie::getMovieInfo));

    AppController<Movie> movieControler = new AppController<>(Movie.class.getConstructor(), indexes);

    new Menu<Movie>(movieControler, new MovieMenuFactory());
  }
}
