import java.io.File;
import java.util.Arrays;
import java.util.Scanner;

/**
 * driver class with main function that reads input file and starts the scheduler
 * @author Ajevan
 *
 */
public class App {

	public static void main(String[] args) {
		try {
			int numOfElements = 0; //number of elements in the file
			int numOfProc = 0; //number of processes
			Process[] processes; //array of processes from file
			Scheduler sch; //the scheduler object
			
			File inputFile = new File("input.txt");
			Scanner sc = new Scanner(inputFile);
			
			//get the number of elements in the file & make sure there isn't missing data
			while (sc.hasNextInt()) {
				sc.nextInt();
				numOfElements++;
			}
			
			if (numOfElements % 2 != 0) {
				sc.close();
				throw new Exception("Number of elements in file is odd");
			}
			
			//reset the scanner to the beginning
			sc.close(); 
			sc = new Scanner(inputFile);
			
			numOfProc = numOfElements / 2;
			processes = new Process[numOfProc];
			
			//get the processes from file & create object
			for (int i = 0; sc.hasNextInt(); i++) {
				int arrivalTime = sc.nextInt();
				double execTime = sc.nextInt();
				
				if (arrivalTime < 1 || execTime < 0) {
					sc.close();
					throw new Exception("Arrival time is less than system start time or burst time is negative!");
				}
				
				processes[i] = new Process(String.valueOf(i), arrivalTime, execTime);
			}
			
			sc.close();
			Arrays.sort(processes, new ProcessSorter()); //sort processes based on arrival time
			
			sch = new Scheduler(processes, "output.txt");
			sch.start();
			sch.join();
			System.out.println("Done");
		}
		
		catch(Exception ex) {
			System.out.println(ex.getMessage());
		}

	}

}
