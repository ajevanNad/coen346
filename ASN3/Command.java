/**
 * class that contains command info
 * @author Ajevan
 *
 */
public class Command {
	private final String cmd; //command
	private final String var; //variable ID
	private final String val; //value
	
	public Command(String cmd, String var, String val) {
		this.cmd = cmd;
		this.var = var;
		this.val = val;
	}
	
	/**
	 * return the command
	 * @return cmd
	 */
	public String getCmd() {
		return this.cmd;
	}
	
	/**
	 * return the variable id
	 * @return var
	 */
	public String getVar() {
		return this.var;
	}
	
	/**
	 * return the value 
	 * @return val
	 */
	public String getVal() {
		return this.val;
	}

}
