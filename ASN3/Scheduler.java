import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

/**
 * simulation of a process scheduler that is responsible for scheduling a given list of processes.
 * The scheduler is running on a machine with two CPUs. The scheduling policy is non-preemptive round-robin
 * @author Ajevan
 *
 */
public class Scheduler extends Thread {

	private FileWriter filewriter;
	private BufferedWriter bufferedwriter;
	
	private Process[] waitq; //wait queue is where process waits until its arrival time
	private Process[] readyq = new Process[0]; //ready queue is where process stays until end of execution
	
	private int waitInd = 0; //waitq index
	private int readyInd = -1; //readyq index
	private int rrInd = 0; //round robin index
	
	private boolean isProcessInWaitq = true; //true if there are processes in the wait queue
	private boolean allProcFinished = false; //true if all processes are finished
	
	private ArrayList<Command> cmds; //list of commands
	private VMM vmm; //the VMM
	private Semaphore cmdSem = new Semaphore(1, true); //control access to cmds
	public Semaphore schSem = new Semaphore(2, true); //only 2 processors available
	
	/**
	 * the scheduler constructor
	 * @param processes array of processes
	 * @param cmds list of commands
	 * @param memArraySize size of the main memory
	 * @param fn filename of the output file
	 * @throws IOException
	 */
	public Scheduler(Process[] processes, Command[] cmds, int memArraySize, String fn) throws IOException {
		this.waitq = processes;
		filewriter = new FileWriter(fn);
		bufferedwriter = new BufferedWriter(filewriter);
		
		this.cmds = new ArrayList<Command>(Arrays.asList(cmds));
		vmm = new VMM(memArraySize);
	}
	
	/**
	 * get the next available command
	 * @return Command; if none available, returns null
	 * @throws InterruptedException
	 */
	public Command getCmd() throws InterruptedException {
		Command c = null;
		cmdSem.acquire();
		
		if (cmds.size() > 0) {
			c = cmds.get(0);
			cmds.remove(0);
		}
		
		cmdSem.release();
		return c;
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
			vmm.start();
			vmm.setSch(this); //set this scheduler in vmm so that it can use the bufferedwriter
			
			while (!allProcFinished) {
				boolean newProcStart = false; //true if a NEW process was started. **NOT IF A PROCESS WAS RESUMED**
				int processors = 0; //number of processors being used
				
				//if there are processes waiting & we have reached the arrival time of one of them
				if (isProcessInWaitq) {
					schSem.acquire(2); //acquire processors
					
					if (Process.time >= waitq[waitInd].getArrivalTime()) {
						processors = 0;
						
						while(isProcessInWaitq && Process.time >= waitq[waitInd].getArrivalTime() && processors < 2) {
							processors++;
							newProcStart = true;
							
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
							
							//if the next RR index is this process, we increment the index so that this process
							//doesn't execute twice
							if (rrInd == readyInd) {
								rrInd++;
							}
							
							//ONLY when writing to output, format time to 0 decimal places; in reality time is of type double
							bufferedwriter.write(String.format("Time: %.0f, Process %s, %s\n", 
									Process.time, readyq[readyInd].getProcName(), Process.State.STARTED));
							
							System.out.println(String.format("Time %.2f, Process %s, %s\n", 
									Process.time, readyq[readyInd].getProcName(), Process.State.STARTED));
							
							readyq[readyInd].setScheduler(this);
							readyq[readyInd].setVmm(vmm);
						}
						
						if(processors >= 1) {
							readyq[readyInd].start();
						}
						
						if(processors == 2) {
							readyq[readyInd-1].start();
						}
						
						if(processors == 1) {
							schSem.release(1);
						}
					}
					else {
						schSem.release(2);
					}
				}
				
				//processes that are just being resumed
				if(readyq.length > 0 && !newProcStart) {
					int limit = readyq.length; //if the limit reaches 0 in the loop, then it means there are no processes
											   //available to resume at the moment
					
					while (processors<2 && limit>0) {
						
						if (schSem.tryAcquire()) {
							//update RR indexing
							rrInd = rrInd % readyq.length;
							
							//if this process is not already running
							if (!readyq[rrInd].getRunState()) {
								readyq[rrInd++].resume();
								processors++;
							}
							else {
								rrInd++;
								schSem.release();
							}
							limit--;
						}
					}
				}
				
				else if (!newProcStart) {
					Process.advanceTime(1);
				}
				
				//remove process in readyq that has finished execution
				if (readyq.length > 0 && readyq[sjf()].isFinished()) {
					ArrayList<Process> temparr = new ArrayList<Process>(Arrays.asList(readyq));
					temparr.remove(sjf());
					readyq = temparr.toArray(new Process[temparr.size()]);
				}
				
				//all processes are done
				if (readyq.length == 0 && !isProcessInWaitq) {
					allProcFinished = true;
				}
			}
			
			vmm.setEndVmm(); //tell vmm to end its thread
			vmm.resume();
			vmm.join(); //wait for vmm to end
			bufferedwriter.close();
		}
		
		catch(Exception ex) {
			System.out.println(ex.getMessage());
			System.out.println(ex.getStackTrace()[0].getLineNumber());
		}
	}
}
