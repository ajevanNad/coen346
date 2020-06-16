import java.util.Comparator;

/**
 * class that implements the compare method for Process so that sorting can be done based on arrival time
 * @author Ajevan
 *
 */
public class ProcessSorter implements Comparator<Process> {

	@Override
	public int compare(Process p1, Process p2) {
		return Integer.compare(p1.getArrivalTime(), p2.getArrivalTime());
	}
}
