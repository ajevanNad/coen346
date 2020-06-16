/**
 * class that contains the variable info
 * @author Ajevan
 *
 */
public class Variable {
	private String id; //variable id
	private String val; //value of the variable
	private double lastAccess; //last time when this variable was accessed
	
	public Variable(String id, String val, double lastAccess) {
		this.id = id;
		this.val = val;
		this.lastAccess = lastAccess;
	}
	
	/**
	 * return this variable's id
	 * @return id
	 */
	public String getId() {
		return this.id;
	}
	
	/**
	 * return this variable's value
	 * @return val
	 */
	public String getVal() {
		return this.val;
	}
	
	/**
	 * set this variable's value to what is provided
	 * @param v new value
	 */
	public void setVal(String v) {
		val = v;
	}
	
	/**
	 * return the time this variable was last accessed
	 * @return lastAccess
	 */
	public double getLastAccess() {
		return this.lastAccess;
	}
	
	/**
	 * set the last accessed time to the value provided
	 * @param newAccess new access time
	 */
	public void setLastAccess(double newAccess) {
		this.lastAccess = newAccess;
	}

}
