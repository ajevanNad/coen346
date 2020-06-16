import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * the virtual memory manager
 * @author Ajevan
 *
 */
public class VMM extends Thread {
	
	private ArrayList<Variable> vars; //list of variables
	private int memSize; //size of main memory
	
	private Command currentCmd = null; //the current command to execute
	private Semaphore currentCmdSem = new Semaphore(1, true); //control access to currentCmd, so that the vmm handles
															  //one memory request at a time
	
	private Scheduler sch; //the scheduler
	private Process p; //current process p
	private Semaphore currentProcSem = new Semaphore(1, true); //control access to current process p
	
	private File vmFile = new File("vm.txt"); //memory in disk
	private boolean endVmm = false; //if true, then need to end the vmm
	
	
	public VMM(int memSize) {
		this.memSize = memSize;
		vars = new ArrayList<Variable>(0); 
	}
	
	/**
	 * pass the scheduler to the vmm
	 * @param sch
	 */
	public void setSch(Scheduler sch) {
		this.sch = sch;
	}
	
	/**
	 * pass the process to the vmm
	 * @param p process that is currently using the vmm
	 * @throws InterruptedException
	 */
	public void setProc(Process p) throws InterruptedException {
		currentProcSem.acquire();
		this.p = p;
	}
	
	/**
	 * pass the current command to execute. 
	 * currentCmdSem lock won't be released until the VMM has completed the command.
	 * @param c currentCmd
	 * @throws InterruptedException
	 */
	public void setCmd(Command c) throws InterruptedException {
		currentCmdSem.acquire();
		currentCmd = c;
	}
	
	/**
	 * tell the vmm to end
	 */
	public void setEndVmm() {
		endVmm = true;
	}
	
	/**
	 * remove the variable from disk
	 * @param v variable to remove
	 * @return true if the variable is removed, false otherwise
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private boolean rm(Variable v) throws FileNotFoundException, IOException {
		
		if (!vmFile.exists()) {
			return true;
		}
		
		Scanner sc = new Scanner(vmFile);
		ArrayList<String> ogText = new ArrayList<String>(0); //original variables in disk
		boolean varRemoved = false;
		
		while (sc.hasNext()) {
			String var = sc.next();
			String val = sc.next();
			String lastTime = sc.next();
			
			if (v.getId().equals(var)) {
				varRemoved = true;
				continue;
			}
			
			//copy variables that need to be rewritten to file
			ogText.add(var);
			ogText.add(" ");
			ogText.add(val);
			ogText.add(" ");
			ogText.add(lastTime);
			ogText.add("\n");
		}
		sc.close();
		
		FileWriter fw = new FileWriter(vmFile);
		BufferedWriter bw = new BufferedWriter(fw);
		
		//write back variables to file, excluding the removed variable
		for (int i = 0; i < ogText.size(); i++) {
			bw.write(ogText.get(i));
		}
		
		bw.close();
		fw.close();
		return varRemoved;
	}
	
	/**
	 * store the given variable in the first unassigned spot in the memory
	 * @param v
	 * @throws IOException
	 */
	public void memStore(Variable v) throws IOException {
		
		boolean varFound = false; //variable found or not
		
		//check if var already exists in memory
		if (vars.size() > 0) {
			for (int i = 0; i < vars.size(); i++) {
				if (vars.get(i).getId().equals(v.getId())) {
					
					vars.get(i).setVal(v.getVal());
					p.decExecTime(p.advanceTime());
					vars.get(i).setLastAccess(Process.time);
					varFound = true;
				}
			}
		}
		
		//check if var already exists in disk
		if (!varFound && vmFile.exists()) {
			Scanner sc = new Scanner(vmFile);
			ArrayList<String> ogText = new ArrayList<String>(0); //original variables in disk
			
			while (sc.hasNext()) {
				String var = sc.next();
				String val = sc.next();
				String lastTime = sc.next();
				
				if (v.getId().equals(var)) {
					
					val = v.getVal();
					p.decExecTime(p.advanceTime());
					lastTime = String.valueOf(Process.time);
					varFound = true;
				}
				
				//copy variables that need to be rewritten to file
				ogText.add(var);
				ogText.add(" ");
				ogText.add(val);
				ogText.add(" ");
				ogText.add(lastTime);
				ogText.add("\n");
			}
			sc.close();
			
			FileWriter fw = new FileWriter(vmFile);
			BufferedWriter bw = new BufferedWriter(fw);
			
			//write back variables to file, with updated variable
			for (int i = 0; i < ogText.size(); i++) {
				bw.write(ogText.get(i));
			}
			
			bw.close();
			fw.close();
		}
		
		//if var doesn't exist and there is room in main memory
		if (vars.size() < memSize && !varFound) {
			
			p.decExecTime(p.advanceTime());
			Variable newVar = new Variable(v.getId(), v.getVal(), Process.time);
			vars.add(newVar);
		}
		
		//if var doesn't exist and there is no room in main memory, then add to disk
		else if (!varFound) {
			FileWriter fw = new FileWriter(vmFile, true);
			BufferedWriter bw = new BufferedWriter(fw);
			
			p.decExecTime(p.advanceTime());
			bw.write(String.format("%s	%s	%s\n", v.getId(), v.getVal(), Process.time));
			bw.close();
			fw.close();
		}
		
		System.out.println(String.format("Time: %s, Process %s, store: var %s, val: %s\n", 
				p.getTime(), p.getProcName(), v.getId(), v.getVal()));
		sch.getBufferedwriter().write(String.format("Time: %.0f, Process %s, Store: Variable %s, Value: %s\n", 
				p.getTime(), p.getProcName(), v.getId(), v.getVal()));
	}
	
	/**
	 * removes the variable from the memory,so the page which was holding this variable becomes available for storage
	 * @param v variable to remove
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void memFree(Variable v) throws FileNotFoundException, IOException {
		boolean varFound = false; //variable found or not
		
		//if variable is in main memory
		for (int i = 0; i < vars.size(); i++) {
			if (vars.get(i).getId().equals(v.getId())) {
				
				vars.remove(i);
				varFound = true;
				p.decExecTime(p.advanceTime());
				
				System.out.println(String.format("Time: %s, Process %s, release: var %s\n", 
						p.getTime(), p.getProcName(), v.getId()));
				sch.getBufferedwriter().write(String.format("Time: %.0f, Process %s, Release: Variable %s\n", 
						p.getTime(), p.getProcName(), v.getId()));
			}
		}
		
		if (!varFound) { //if variable is in disk
			if (rm(v)) {
				p.decExecTime(p.advanceTime());
				
				System.out.println(String.format("Time: %s, Process %s, release: var %s\n", 
						p.getTime(), p.getProcName(), v.getId()));
				sch.getBufferedwriter().write(String.format("Time: %.0f, Process %s, Release: Variable %s\n", 
						p.getTime(), p.getProcName(), v.getId()));
			}
		}	
	}
	
	/**
	 * if the variable exists in memory, return its value. Otherwise return -1 
	 * @param v variable to lookup
	 * @return value if found, otherwise return -1
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public String memLookup(Variable v) throws FileNotFoundException, IOException {
		
		//variable in main memory
		for (int i = 0; i < vars.size(); i++) {
			if (vars.get(i).getId().equals(v.getId())) {
				
				p.decExecTime(p.advanceTime());
				vars.get(i).setLastAccess(Process.time);
				
				System.out.println(String.format("Time: %s, Process %s, Lookup: Variable %s, Value: %s\n", 
						p.getTime(), p.getProcName(), v.getId(), v.getVal()));
				sch.getBufferedwriter().write(String.format("Time: %.0f, Process %s, Lookup: Variable %s, Value: %s\n", 
						p.getTime(), p.getProcName(), vars.get(i).getId(), vars.get(i).getVal()));
				
				return vars.get(i).getVal();
			}
		}
		
		//vm file doesn't exist
		if (!vmFile.exists()) {
			p.decExecTime(p.advanceTime());
			return Integer.toString(-1);
		}
		
		//variable in disk
		Scanner sc = new Scanner(vmFile);
		
		while (sc.hasNext()) {
			String var = sc.next();
			String val = sc.next();
			String lastTime = sc.next();
			
			//if there is room in main memory
			if (v.getId().equals(var) && vars.size() < memSize) {
				
				p.decExecTime(p.advanceTime());
				vars.add(new Variable(var, val, Process.time));
				rm(v);
				sc.close();
				
				System.out.println(String.format("Time: %s, Process %s, Lookup: Variable %s, Value: %s\n", 
						p.getTime(), p.getProcName(), v.getId(), v.getVal()));
				sch.getBufferedwriter().write(String.format("Time: %.0f, Process %s, Lookup: Variable %s, Value: %s\n", 
						p.getTime(), p.getProcName(), var, val));
				
				return val;
			}
			
			//if no room in main memory, do swap
			else if (v.getId().equals(var)) {
				Variable oldest = vars.get(0);
				int oldInd = 0;
				
				//find the oldest variable to do the swap
				for (int i = 0; i < vars.size(); i++) {
					if (vars.get(i).getLastAccess() < oldest.getLastAccess()) {
						oldest = vars.get(i);
						oldInd = i;
					}
				}
				
				vars.remove(oldInd); //main memory now has opening
				p.decExecTime(p.advanceTime());
				double swapTime = Process.time - 10;
				
				System.out.println(String.format("Time: %s, Memory Manager, Swap: Variable %s with Variable %s\n", 
						swapTime, v.getId(), oldest.getId()));
				sch.getBufferedwriter().write(String.format("Time: %.0f, Memory Manager, Swap: Variable %s with "
						+ "Variable %s\n", swapTime, v.getId(), oldest.getId()));
				
				//add the old variable to disk
				FileWriter fw = new FileWriter(vmFile, true);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(String.format("%s	%s	%s\n", oldest.getId(), oldest.getVal(), swapTime));
				bw.close();
				fw.close();
				
				vars.add(new Variable(var, val, Process.time)); //add variable found in disk to main memory
				rm(v); //and remove variable from disk since now it is in main memory
				
				System.out.println(String.format("Time: %s, Process %s, Lookup: Variable %s, Value: %s\n", 
						p.getTime(), p.getProcName(), v.getId(), v.getVal()));
				sch.getBufferedwriter().write(String.format("Time: %.0f, Process %s, Lookup: Variable %s, Value: %s\n", 
						p.getTime(), p.getProcName(), var, val));
				
				sc.close();
				return val;
			}
		}
		sc.close();
		
		//if variable doesn't exist
		p.decExecTime(p.advanceTime());
		sch.getBufferedwriter().write(String.format("Time: %.0f, Process %s, Lookup: -1\n", p.getTime(), p.getProcName()));
		return Integer.toString(-1);
	}
	
	@SuppressWarnings("deprecation")
	public void run() {
		try {
			while (true) {
				if (currentCmd != null) {
					String cmd = currentCmd.getCmd();
					String var = currentCmd.getVar();
					String val = currentCmd.getVal();
					
					if (cmd.equals("STORE")) {
						Variable v = new Variable(var, val, Process.time);
						memStore(v);
					}
					
					else if (cmd.equals("LOOKUP")) {
						Variable v = new Variable(var, val, Process.time);
						memLookup(v);
					}
					
					else if (cmd.equals("RELEASE")) {
						Variable v = new Variable(var, val, Process.time);
						memFree(v);
					}
					
					currentCmd = null;
					currentCmdSem.release(); //this now allows another process to ask the vmm to perform a command
					p.resume(); //vmm done, let process continue
					currentProcSem.release(); //this now allows another process to set itself as one using the vmm
				}
				
				//terminate VMM thread
				if (endVmm) {
					break;
				}
				
				this.suspend(); //wait until another memory access is required
			}
		}
		catch (Exception ex) {
			System.out.println("Something went wrong in the VMM run method: " + ex.getMessage());
		}
	}

}
