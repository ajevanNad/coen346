import java.util.concurrent.Semaphore;

/**
 * simulation of a process
 * @author Ajevan
 *
 */
public class Process extends Thread {

	public enum State {
		STARTED,
		RESUMED,
		PAUSED,
		FINISHED
	}
	
	private String name;
	private Scheduler sch;
	
	private int arrivalTime;
	private double execTime;
	public static double time = 0; //simulated clock of system
	
	private boolean finished = false; //true if the process is finished
	public State state; //the current state of the process
	
	private VMM vmm; //the VMM object
	private boolean alreadyRun = false; //true if this process is already running
	private Semaphore alreadyRunSem = new Semaphore(1, true); //control access to alreadyRun
	
	/**
	 * process constructor
	 * @param name
	 * @param arrivalTime
	 * @param exectime
	 */
	public Process(String name, int arrivalTime, double exectime) {
		this.name = name;
		this.arrivalTime = arrivalTime;
		this.execTime = exectime;
	}
	
	/**
	 * get the state of this process; true if already running, false otherwise.
	 * @return alreadyRun
	 * @throws InterruptedException
	 */
	public boolean getRunState() throws InterruptedException {
		alreadyRunSem.acquire();
		boolean val = alreadyRun;
		alreadyRunSem.release();
		return val;
	}
	
	/**
	 * decrease the execTime by the value provided
	 * @param t amount by which to decrease execTime
	 */
	public void decExecTime(double t) {
		execTime -= t;
	}
	
	/**
	 * pass the scheduler object to this process
	 * @param sch the scheduler that is controlling all the processes
	 */
	public void setScheduler(Scheduler sch) {
		this.sch = sch;
	}
	
	/**
	 * pass the VMM object to this process
	 * @param vmm the VMM that is handling the memory accesses
	 */
	public void setVmm(VMM vmm) {
		this.vmm = vmm;
	}
	
	/**
	 * return the arrival time of this process
	 */
	public int getArrivalTime() {
		return this.arrivalTime;
	}
	
	/**
	 * return the remaining execution time of this process 
	 * @return execTime
	 */
	public double getExecTime() {
		return this.execTime;
	}
	
	/**
	 * get the process name
	 * @return name
	 */
	public String getProcName() {
		return this.name;
	}
	
	/**
	 * returns true if the process is finished
	 * @return finished
	 */
	public boolean isFinished() {
		return finished;
	}
	
	/**
	 * return the current time in the system
	 * @return time
	 */
	public double getTime() {
		return time;
	}
	
	/**
	 * advance the system time by a random time within the quantum length
	 * @return the random number amount by which time was advanced
	 */
	synchronized public double advanceTime() {
		//random number between 100 and 1000
		//max is 1000 since the quantum is 1000
		//min is 100 for convenience
		double rndNum = Math.random();
		rndNum = (rndNum * 900) + 101;
		rndNum = (double) (int) rndNum;
		
		//if the random time is greater than the remaining execution time
		if (rndNum > execTime) {
			rndNum = execTime;
		}
		
		time += rndNum;
		return rndNum;
	}
	
	/**
	 * advance the system time by the value provided
	 * @param t amount by which to advance time
	 */
	synchronized public static void advanceTime(double t) {
		if (t >= 0) {
			time += t;
		}
	}
	
	@SuppressWarnings("deprecation")
	public void run() {
		
		try {
			while (true) {
				alreadyRunSem.acquire();
				alreadyRun = true;
				alreadyRunSem.release();
				
				sch.getBufferedwriter().write(String.format("Time: %.0f, Process %s, %s\n", time, name, State.RESUMED));
				System.out.println(String.format("Time %.2f, Process %s, %s\n", time, name, State.RESUMED));
				
				Command c = sch.getCmd();
				
				if (c != null) {
					vmm.setCmd(c); //pass the command to vmm
					vmm.setProc(this); //let the vmm know which process is executing this command
					vmm.resume();
					this.suspend(); //wait for the vmm to finish
				}
				else {
					execTime -= advanceTime();
				}
				
				sch.getBufferedwriter().write(String.format("Time: %.0f, Process %s, %s\n", time, name, State.PAUSED));
				System.out.println(String.format("Time %.2f, Process %s, %s\n", time, name, State.PAUSED));
				
				//check if the process has finished
				if (execTime <= 0) {
					
					finished = true;
					sch.getBufferedwriter().write(String.format("Time: %.0f, Process %s, %s\n", time, name, State.FINISHED));
					System.out.println(String.format("Time %.2f, Process %s, %s\n", time, name, State.FINISHED));
					sch.schSem.release(); //give the processor back
					break;
				}
				
				alreadyRunSem.acquire();
				alreadyRun = false;
				alreadyRunSem.release();
				
				sch.schSem.release(); //give the processor back
				this.suspend();
			}
		}
		
		catch(Exception ex) {
			System.out.println("Something went wrong in the Process object run method: " + ex.getMessage());
		}
	}
}
