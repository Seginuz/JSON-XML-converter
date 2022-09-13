import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        List<String> sequence = Arrays.stream(scanner.nextLine().split(" ")).toList();
        List<String> find = Arrays.stream(scanner.nextLine().split(" ")).toList();

        System.out.print(Collections.indexOfSubList(sequence, find));
        System.out.print(" " + Collections.lastIndexOfSubList(sequence, find));
    }
}