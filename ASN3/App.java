import java.io.File;
import java.util.Arrays;
import java.util.Scanner;

/**************************************************************************************
 * THIS PROGRAM MIGHT NEED TO BE RUN MULTIPLE TIMES SINCE IT IS USING THREAD
 * RESUME/SUSPEND METHODS, WHICH HAVE UNPREDICTABLE BEHAVIOUR
 * 
 * Hence, the program might be frozen, or the program might run to completion,
 * but may contain errors in the output. Therefore, always check the output file
 * to make sure that the program ran correctly.
 * ************************************************************************************
 */
/**************************************************************************************
 * THE OUTPUT IN THE CONSOLE IS FOR DEBUGGING ONLY.
 * THE ACTUAL OUTPUT IS IN THE output.txt FILE.
 * ************************************************************************************
 */
/**************************************************************************************
 * BEFORE EVERY RUN OF THE PROGRAM, DELETE THE vm.txt FILE SO THAT THE CONTENTS
 * LEFT ON DISK FROM THE PREVIOUS RUN DOES NOT CORRUPT THE CURRENT RUN.
 * ************************************************************************************
 */

/**
 * driver class with main function that reads input files and starts the scheduler
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
			int memArraySize = 0; //main memory size
			Command[] cmds; //array of commands
			
			//*****************************************************
			//read processes from file
			//*****************************************************
			File procFile = new File("processes.txt");
			Scanner sc = new Scanner(procFile);
			
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
			sc = new Scanner(procFile);
			
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
				
				processes[i] = new Process(String.valueOf(i), arrivalTime*1000, execTime*1000);
			}
			
			sc.close();
			Arrays.sort(processes, new ProcessSorter()); //sort processes based on arrival time
			
			//*****************************************************
			//read memory config file
			//*****************************************************
			File memFile = new File("memconfig.txt");
			sc = new Scanner(memFile);
			
			memArraySize = sc.nextInt();
			sc.close();
			
			if (memArraySize < 1) {
				throw new Exception("Invalid main memory size");
			}
			
			//*****************************************************
			//read commands file
			//*****************************************************
			File cmdFile = new File("commands.txt");
			sc = new Scanner(cmdFile);
			numOfElements = 0;
			
			//get the number of elements in the file
			while (sc.hasNextLine()) {
				sc.nextLine();
				numOfElements++;
			}
			
			//reset the scanner to the beginning
			sc.close();
			sc = new Scanner(cmdFile);
			
			cmds = new Command[numOfElements];
			
			//get the commands from file & create object
			for (int i = 0; sc.hasNext(); i++) {
				String cmd = sc.next().toUpperCase();
				
				if (cmd.equals("STORE")) {
					String var = sc.next();
					String val = sc.next();
					cmds[i] = new Command(cmd, var, val);
				}
				
				else if(cmd.equals("LOOKUP")) {
					String var = sc.next();
					cmds[i] = new Command(cmd, var, "");
				}
				
				else if(cmd.equals("RELEASE")) {
					String var = sc.next();
					cmds[i] = new Command(cmd, var, "");
				}
				
				else {
					sc.close();
					throw new Exception("Command Invalid! Check commands.txt file.");
				}
				
			}
			sc.close();
			
			sch = new Scheduler(processes, cmds, memArraySize, "output.txt");
			sch.start();
			sch.join();
			System.out.println("Done");
		}
		
		catch(Exception ex) {
			System.out.println(ex.getMessage());
		}

	}

}
