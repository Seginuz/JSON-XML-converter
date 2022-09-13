import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void processIterator(String[] array) {
        List<String> list = Arrays.stream(array).collect(Collectors.toList());
        ListIterator<String> iterator = list.listIterator();

        while (iterator.hasNext()) {
            String item = iterator.next();
            if (item.startsWith("J")) {
                iterator.set(item.replace("J", ""));
            } else {
                iterator.remove();
            }
        }

        while (iterator.hasPrevious()) {
            System.out.println(iterator.previous());
        }
    }

    /* Do not change code below */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        processIterator(scanner.nextLine().split(" "));
    }
}