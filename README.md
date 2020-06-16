# ASN1
The goal of the program is to have a recursive threading method to find the defective bulbs and the number of threads that have been created for this purpose.

# ASN2
The goal of the program is to simulate a process scheduler that is responsible for scheduling a given list of processes. The scheduler is running on a machine with one CPU. The scheduling policy is a type of non-preemptive round-robin scheduling and works as follows:
- Scheduler works in a cyclic manner, i.e. it gives the CPU to a process for a quantum of time and then get the CPU back.
- The quantum for each process is equal to 10 percent of the remaining execution time of the process
- Each process comes with its own arrival time and burst time.
- Each time, the scheduler gives the CPU to a process (say P1) that has the shortest remaining processing time, but this should not starve other processes in the queue and which are ready to start. These processes should be allocated to the CPU before it is given back to P1, i.e. include some fairness for long jobs already in the queue.
- In the case that two or more processes have equal remaining time for completion, the scheduler gives priority to the older process (i.e. process that has been in the system for longer time).

# ASN3
The goal of the program is to simulate the operating system’s virtual memory management, process scheduling, and concurrency/synchronization control. Hence,
- Each process will be simulated by threads
- The scheduler will be running on its own thread and will use a non-preemptive round-robin policy to choose processes
- The simulated system will have 2 CPUs
- Each quantum is chosen to be 1000 in this program
- Processes try to store, retrieve, and release “variables” to/from memory
- Processes continuously pick a command from the command list and if there are no commands, then the process runs as ASN2
