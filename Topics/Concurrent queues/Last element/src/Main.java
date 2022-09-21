import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

class QueueUtils {
    public static int getLastNumber(Queue<Integer> target) {
        Integer numberToReturn = 0;
        for (Integer i : target) {
            numberToReturn = i;
        }
        return numberToReturn;
    }
}