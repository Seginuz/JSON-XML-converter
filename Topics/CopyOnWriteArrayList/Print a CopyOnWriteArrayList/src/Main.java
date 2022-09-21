import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Iterator;

class PrintElementsClass {

    public static void printElements(CopyOnWriteArrayList<String> onWriteArrayList, String elementToAdd) {
        onWriteArrayList.add(elementToAdd);
        Iterator<String> iterator = onWriteArrayList.iterator();
        while (iterator.hasNext()) {
            System.out.print(iterator.next() + " ");
        }
    }
}