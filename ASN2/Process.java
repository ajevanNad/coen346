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
	
	private double lastTime; //the last time this process was executed
	private double waitTime = 0; //the amount of time this process spent waiting
	
	private String name;
	private Scheduler sch;
	
	private int arrivalTime;
	private double execTime;
	public static double time = 0; //simulated clock of system
	
	private boolean finished = false; //true if the process is finished
	public State state; //the current state of the process
	
	/**
	 * process constructor
	 * @param name
	 * @param arrivalTime
	 * @param exectime
	 */
	public Process(String name, int arrivalTime, double exectime) {
		this.name = name;
		this.arrivalTime = arrivalTime;
		this.lastTime = arrivalTime;
		this.execTime = exectime;
		this.waitTime = this.arrivalTime - 1;
	}
	
	/**
	 * pass the scheduler object to this process
	 * @param sch the scheduler that is controlling all the processes
	 */
	public void setScheduler(Scheduler sch) {
		this.sch = sch;
	}
	
	/**
	 * return the time this process spent waiting
	 * @return waitTime
	 */
	public double getWaitTime() {
		return this.waitTime;
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
	
	@SuppressWarnings("deprecation")
	public void run() {
		
		try {
			while (true) {
				sch.getBufferedwriter().write(String.format("Time %.2f, Process %s, %s\n", time, name, State.RESUMED));
				
				waitTime += time - lastTime;
				time += 0.1*execTime;
				execTime -= 0.1*execTime;
				lastTime = time;
				
				sch.getBufferedwriter().write(String.format("Time %.2f, Process %s, %s\n", time, name, State.PAUSED));
				
				//have a tolerance so that process doesn't run indefinitely
				if (execTime <= 0.01) {
					
					finished = true;
					sch.getBufferedwriter().write(String.format("Time %.2f, Process %s, %s\n", time, name, State.FINISHED));
					sch.resume(); //return the CPU to the scheduler
					break;
				}
				
				sch.resume(); //return the CPU to the scheduler
				this.suspend();
			}
		}
		
		catch(Exception ex) {
			System.out.println("Something went wrong in the Process object run method: " + ex.getMessage());
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
