import java.io.File;
import java.util.Scanner;

/**
 * recursive  threading  method  to  find  the defective bulbs and  the  number  of  threads
 * that have been created for this purpose
 * @author Ajevan
 *
 */
public class BtTraversal {
	
	public static int numOfThreads = 1; //number of threads; initially we have the main thread
	
	/**
	 * recursive method that finds defective bulbs through the use of separate threads for the left
	 * and right sub arrays
	 * @param a array filled with "0" and "1" for defective and correct bulbs respectively
	 * @param lo the sub array start index 
	 * @param hi the sub array end index
	 */
	public static void findDefective(int[] a, int lo, int hi) {
		
		// if this is a single element sub array, check if bulb defective
		if (hi <= lo) {
			if (a[lo] == 0) {
				System.out.println("Bulb #" + (lo+1) + " is defective.");
			}
			return;
		}
		
		try {
			// if this array has a defective bulb, then separate into 2 sub arrays with each its own thread
			for (int i = lo; i <= hi; i++) {
				if (a[i] == 0) {
					
					int mid = lo + (hi - lo)/2; //pivot point
					
					numOfThreads++;
					Thread t1 = new Thread(new Runnable() {
						public void run() {
							findDefective(a, lo, mid); //left sub array
						}
					});
					t1.start();
					
					numOfThreads++;
					Thread t2 = new Thread(new Runnable() {
						public void run() {
							findDefective(a, mid + 1, hi); //right sub array
						}
					});
					t2.start();
					
					t1.join(); //wait for the 2 threads to finish
					t2.join();
					break;
				}
			}
		}
		catch (Exception ex) {
			System.out.println("Something went wrong in findDefective method");
		}
	}
	
	public static void main(String[] args) {
		
		try {
			File inputFile = new File("input.txt");
			Scanner sc = new Scanner(inputFile);
			
			int[] elements; //array filled with "0" and "1" for defective and correct bulbs respectively
			int actualSize = -1; //the actual size of the array; start at -1 since the first element is the array size
			boolean getSize = true; //if its the first line, we know that it is the array size
			
			//get the actual number of elements in the file
			while (sc.hasNextInt()) {
				sc.nextInt();
				actualSize++;
			}
			
			//reset the scanner to the beginning
			sc.close(); 
			sc = new Scanner(inputFile);
			
			elements = new int[actualSize];
			
			//load the array with the bulbs
			for (int i = 0; sc.hasNextInt(); i++) {
				if (getSize) {
					if (sc.nextInt() != actualSize) {
						sc.close();
						throw new Exception("Array size doesn't match!");
					}
					getSize = false;
				}
				
				elements[i] = sc.nextInt();
				
				if (elements[i] != 0 && elements[i] != 1) {
					sc.close();
					throw new Exception("Elements are not a 0 or 1!");
				}
			}
			sc.close();
			
			int lastIndex = actualSize - 1;
			Thread t = new Thread(new Runnable() {
				public void run() {
					findDefective(elements, 0, lastIndex);
				}
			});
			t.start();
			t.join();
			
			System.out.println("The number of threads for this problem is: " + numOfThreads);
		}
		catch (Exception ex) {
			System.out.println(ex.getMessage());
		}

	}

}
