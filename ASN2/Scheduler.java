import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * simulation of a process scheduler that is responsible for scheduling a given list of processes.
 * The scheduler is running on a machine with one CPU. The scheduling policy is a type of non-preemptive round-robin
 * scheduling
 * @author Ajevan
 *
 */
public class Scheduler extends Thread{
	
	private FileWriter filewriter;
	private BufferedWriter bufferedwriter;
	
	private Process[] waitq; //wait queue is where process waits until its arrival time
	private Process[] readyq = new Process[0]; //ready queue is where process stays until end of execution
	
	private int waitInd = 0; //waitq index
	private int readyInd = -1; //readyq index
	
	private boolean isProcessInWaitq = true; //true if there are processes in the wait queue
	private boolean allProcFinished = false; //true if all processes are finished
	
	/**
	 * the scheduler constructor
	 * @param processes array of processes
	 * @param fn filename of the output file
	 * @throws IOException
	 */
	public Scheduler(Process[] processes, String fn) throws IOException {
		this.waitq = processes;
		filewriter = new FileWriter(fn);
		bufferedwriter = new BufferedWriter(filewriter);
	}
	
	/**
	 * return the BufferedWriter object used to write to the output file
	 * @return bufferedwriter
	 */
	public BufferedWriter getBufferedwriter() {
		return this.bufferedwriter;
	}
	
	/**
	 * return the index of the process with the shortest remaining execution time that is ready
	 * @return index in ready queue
	 */
	private int sjf() {
		int ind = 0; //index in readyq of the process with the shortest remaining execution time
		
		for (int i = 0; i < readyq.length; i++) {
			if (readyq[i].getExecTime() < readyq[ind].getExecTime()) {
				ind = i;
			}
		}
		return ind;
	}
	
	@SuppressWarnings("deprecation")
	public void run() {
		
		try {
			while (!allProcFinished) {
				
				//if there are processes waiting & we have reached the arrival time of one of them
				if (isProcessInWaitq && Process.time >= waitq[waitInd].getArrivalTime()) {
					
					//add process from wait queue to ready queue
					ArrayList<Process> temparr = new ArrayList<Process>(Arrays.asList(readyq));
					temparr.add(waitq[waitInd]);
					readyq = temparr.toArray(new Process[temparr.size()]);
					
					waitInd++;
					
					//if index outside waitq, then no more waiting processes
					if (waitInd > waitq.length - 1) {
						isProcessInWaitq = false;
					}
					
					readyInd = readyq.length - 1; //the index of the newest process in the ready queue
					
					//ONLY when writing to output, format time to 2 decimal places; in reality time is of type double
					bufferedwriter.write(String.format("Time %.2f, Process %s, %s\n", 
							Process.time, readyq[readyInd].getProcName(), Process.State.STARTED));
					
					readyq[readyInd].setScheduler(this);
					readyq[readyInd].start();
					this.suspend(); //scheduler stops using CPU while process is using CPU
				}
				
				//processes that are just being resumed
				else if (readyq.length > 0){
					
					readyq[sjf()].resume(); //choose process with the shortest remaining execution time
					this.suspend(); //scheduler stops using CPU while process is using CPU
				}
				
				else {
					Process.time += 0.01;
				}
				
				//remove process in readyq that has finished execution
				if (readyq.length > 0 && readyq[sjf()].isFinished()) {
					ArrayList<Process> temparr = new ArrayList<Process>(Arrays.asList(readyq));
					temparr.remove(sjf());
					readyq = temparr.toArray(new Process[temparr.size()]);
				}
				
				if (readyq.length == 0 && !isProcessInWaitq) {
					allProcFinished = true;
				}
			}
			
			bufferedwriter.write("-------------------------------------\nWaiting Times:\n");
			
			for (int i = 0; i < waitq.length; i++) {
				bufferedwriter.write(String.format("Process %s: %.2f\n", waitq[i].getProcName(), waitq[i].getWaitTime()));
			}
			
			bufferedwriter.close();
		}
		
		catch(Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
