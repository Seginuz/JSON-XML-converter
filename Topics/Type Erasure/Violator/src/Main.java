import java.util.ArrayList;

/**
 * Class to work with
 */
class Violator {

    public static List<Box<? extends Bakery>> defraud() {
        List<Box<? extends Bakery>> bakeryList = new ArrayList<>();
        bakeryList.add(new Box<Bakery>());
        bakeryList.add(new Box<Cake>());

        Box<Paper> paperBox = new Box<>();
        paperBox.put(new Paper());
        Box rawBox = paperBox;
        bakeryList.add(rawBox);

        return bakeryList;
    }

}