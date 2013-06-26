package os.zhaohui.scheduler;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

public class Scheduler {
	private static Process[] processes;
	private static int finishTime;
	private static String cpuUtil;
	private static int[] turnaround;
	private static TreeMap<Integer, String> clockCycle = new TreeMap<Integer, String>();

	private static String readFile(String fileName) throws IOException {
		FileReader fr = null;
		StringBuffer buffer = new StringBuffer();
		try {
			fr = new FileReader(fileName);
			char[] cbuf = { ' ' };
			while (fr.read(cbuf) != -1) {
				buffer.append(cbuf);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (fr != null)
				fr.close();
		}
		return buffer.toString();

	}

	private static void parseString(String str) throws Exception {
		String[] splits = str.split("\\s+");
		if (splits.length % 4 != 0) {
			throw new RuntimeException("Illegal Input File!!");
		}
		ArrayList<int[]> processList = new ArrayList<int[]>();

		for (int i = 0; i < splits.length; i = i + 4) {
			int[] process = new int[4];
			process[0] = Integer.parseInt(splits[i]);
			process[1] = Integer.parseInt(splits[i + 1]);
			process[2] = Integer.parseInt(splits[i + 2]);
			process[3] = Integer.parseInt(splits[i + 3]);
			processList.add(process);
		}
		int tmpIndex = 0;
		int tmpValue = Integer.MAX_VALUE;
		int indexFlag = 0;
		processes = new Process[processList.size()];
		turnaround = new int[processList.size()];
		while (!processList.isEmpty()) {
			for (int i = 0; i < processList.size(); i++) {
				if (processList.get(i)[3] < tmpValue) {
					tmpValue = processList.get(i)[3];
					tmpIndex = i;
				}
			}
			Process process = new Process(processList.get(tmpIndex)[0],
					processList.get(tmpIndex)[1], processList.get(tmpIndex)[2],
					processList.get(tmpIndex)[3]);
			processes[indexFlag] = process;
			tmpValue = Integer.MAX_VALUE;
			processList.remove(tmpIndex);
			indexFlag++;
		}

	}

	private static void fcfs() {
		// ignore the situation which arrive times are the same
		QueueProcess processQueue = new QueueProcess();
		LinkedList<Process> blockedProcess = new LinkedList<Process>();

		int counter = 0;
		int i = 0;
		Process currProcess = null;
		int cpuNotBusyCounter = 0;
		while (true) {
			// if a process get ready, add it to the ready queue
			if (i < processes.length && counter == processes[i].getArrTime()) {
				Process tmpProcess = processes[i];
				processQueue.add(tmpProcess);
				i++;
			}
			// if the running process exists
			if (currProcess != null && currProcess.timer > 0) {
				//if ready queue and blocked queue is empty, only compute running
				if (processQueue.isEmpty() && blockedProcess.isEmpty()) {
					clockCycle.put(counter, currProcess.getId() + ":running ");
				} 
				// if read queue is not empty, running and compute some ready process
				else if (!processQueue.isEmpty() && blockedProcess.isEmpty()) {
					StringBuffer buffer = new StringBuffer();
					buffer.append(currProcess.getId() + ":running ");
					for (int j = 0; j < processQueue.size(); j++) {
						buffer.append(processQueue.get(j).getId() + ":ready ");
					}
					clockCycle.put(counter, buffer.toString());
				}
				// if blocked queue is not empty, running and blocked
				else if (processQueue.isEmpty() && !blockedProcess.isEmpty()) {
					StringBuffer buffer = new StringBuffer();

					for (int j = 0; j < blockedProcess.size(); j++) {
						buffer.append(blockedProcess.get(j).getId()
								+ ":blocked ");
						blockedProcess.get(j).ioTimer--;

					}
					for (int j = blockedProcess.size() - 1; j >= 0; j--) {
						if (blockedProcess.get(j).ioTimer <= 0) {
							Process curr = blockedProcess.remove(j);
							curr.readyTime = counter;
							processQueue.add(curr);
						}
					}
					buffer.append(currProcess.getId() + ":running ");
					clockCycle.put(counter, buffer.toString());

				}
				//block and ready queues are both not empty
				else {
					StringBuffer buffer = new StringBuffer();

					for (int j = 0; j < blockedProcess.size(); j++) {
						buffer.append(blockedProcess.get(j).getId()
								+ ":blocked ");
						blockedProcess.get(j).ioTimer--;
					}

					buffer.append(currProcess.getId() + ":running ");
					for (int j = 0; j < processQueue.size(); j++) {
						buffer.append(processQueue.get(j).getId() + ":ready ");
					}
					for (int j = blockedProcess.size() - 1; j >= 0; j--) {
						if (blockedProcess.get(j).ioTimer <= 0) {
							Process curr = blockedProcess.remove(j);
							curr.readyTime = counter;
							processQueue.add(curr);
						}
					}
					clockCycle.put(counter, buffer.toString());
				}
				currProcess.timer--;
				counter++;
				continue;
			}
			// running process exists and already run the half cpu time
			else if (currProcess != null) {
				// the process just run the first time
				if (currProcess.flag) {
					//for iotime ==0, go on runing
					if (currProcess.getIoTime() == 0) {
						currProcess.flag = false;
						currProcess.timer = currProcess.getCpuTime()-currProcess.getHalfCpuTime();
					} else {
						Process curr = currProcess;
						curr.timer = curr.getHalfCpuTime();
						curr.flag = false;
						blockedProcess.add(curr);
						currProcess = null;
					}
				} 
				// the process run the second time
				else {
					turnaround[currProcess.getId()] = counter
							- currProcess.getArrTime();
					currProcess = null;

				}
				continue;
			}
			// if running process is null, get a process from ready queue as
			// running process
			if (currProcess == null && !processQueue.isEmpty()) {
				currProcess = processQueue.poll();
				continue;
			}
			// no process is running and only blocked processes
			else if (currProcess == null && !blockedProcess.isEmpty()) {
				StringBuffer buffer = new StringBuffer();
				for (int j = 0; j < blockedProcess.size(); j++) {
					buffer.append(blockedProcess.get(j).getId() + ":blocked ");
					blockedProcess.get(j).ioTimer--;
				}
				cpuNotBusyCounter++;
				//remove the process which has ended blocked status and add to ready queue
				for (int j = blockedProcess.size() - 1; j >= 0; j--) {
					if (blockedProcess.get(j).ioTimer <= 0) {
						Process curr = blockedProcess.remove(j);
						curr.readyTime = counter;
						processQueue.add(curr);
					}
				}
				clockCycle.put(counter, buffer.toString());
				counter++;
				continue;

			}
			// no process is running and also no process in the ready queue and
			// blocked queue, wait for the arrive of another process
			if (currProcess == null && i < processes.length) {
				clockCycle.put(counter, ": ");
				counter++;
				continue;
			}
			// no process is coming, loop exits
			else {
				break;
			}
		}
		finishTime = counter - 1;
		double tmpCpuUtil = 1 - (double) cpuNotBusyCounter / (double) counter;
		DecimalFormat twoDecimal = new DecimalFormat("#.##");
		cpuUtil = twoDecimal.format(tmpCpuUtil);

	}

	private static void roundRobin2() {

		// ignore the situation which arrive times are the same
		QueueProcess processQueue = new QueueProcess();
		LinkedList<Process> blockedProcess = new LinkedList<Process>();

		int counter = 0;
		int i = 0;
		Process currProcess = null;
		int cpuNotBusyCounter = 0;
		int rrFlag = 0;
		while (true) {
			// if a process get ready, add it to the processqueue
			if (i < processes.length && counter == processes[i].getArrTime()) {
				Process tmpProcess = processes[i];
				processQueue.add(tmpProcess);
				i++;
			}
			// if running process exists
			if (currProcess != null && currProcess.timer > 0 && rrFlag < 2) {
				if (processQueue.isEmpty() && blockedProcess.isEmpty()) {
					clockCycle.put(counter, currProcess.getId() + ":running ");
				} else if (!processQueue.isEmpty() && blockedProcess.isEmpty()) {
					StringBuffer buffer = new StringBuffer();
					buffer.append(currProcess.getId() + ":running ");
					for (int j = 0; j < processQueue.size(); j++) {
						buffer.append(processQueue.get(j).getId() + ":ready ");
					}
					clockCycle.put(counter, buffer.toString());
				} else if (processQueue.isEmpty() && !blockedProcess.isEmpty()) {
					StringBuffer buffer = new StringBuffer();

					for (int j = 0; j < blockedProcess.size(); j++) {
						buffer.append(blockedProcess.get(j).getId()
								+ ":blocked ");
						blockedProcess.get(j).ioTimer--;

					}
					for (int j = blockedProcess.size() - 1; j >= 0; j--) {
						if (blockedProcess.get(j).ioTimer <= 0) {
							Process curr = blockedProcess.remove(j);
							curr.readyTime = counter;
							processQueue.add(curr);
						}
					}
					buffer.append(currProcess.getId() + ":running ");
					clockCycle.put(counter, buffer.toString());

				} else {
					StringBuffer buffer = new StringBuffer();

					for (int j = 0; j < blockedProcess.size(); j++) {
						buffer.append(blockedProcess.get(j).getId()
								+ ":blocked ");
						blockedProcess.get(j).ioTimer--;
					}

					buffer.append(currProcess.getId() + ":running ");
					for (int j = 0; j < processQueue.size(); j++) {
						buffer.append(processQueue.get(j).getId() + ":ready ");
					}
					for (int j = blockedProcess.size() - 1; j >= 0; j--) {
						if (blockedProcess.get(j).ioTimer <= 0) {
							Process curr = blockedProcess.remove(j);
							curr.readyTime = counter;
							processQueue.add(curr);
						}
					}
					clockCycle.put(counter, buffer.toString());
				}
				currProcess.timer--;
				rrFlag++;
				counter++;
				continue;
			}
			// running process exists and already run
			 else if (currProcess != null && currProcess.timer <= 0) {
				if (currProcess.flag) {
					if (currProcess.getIoTime() == 0) {
						currProcess.flag = false;
						currProcess.timer = currProcess.getCpuTime()-currProcess.getHalfCpuTime();
					} else {
						Process curr = currProcess;
						curr.timer = curr.getHalfCpuTime();
						curr.flag = false;
						blockedProcess.add(curr);
						currProcess = null;
						rrFlag = 0;
					}
				} else {
					turnaround[currProcess.getId()] = counter
							- currProcess.getArrTime();
					currProcess = null;
					rrFlag = 0;
				}
				continue;
			} else if (currProcess != null && rrFlag >= 2) {
				Process curr = currProcess;
				curr.readyTime = counter;
				processQueue.add(curr);
				currProcess = null;
				rrFlag = 0;
				continue;
			}
			// if running process is null, get a process from ready queue as
			// running process
			if (currProcess == null && !processQueue.isEmpty()) {
				currProcess = processQueue.poll();
				continue;
			}
			// no process is running and only blocked processes
			else if (currProcess == null && !blockedProcess.isEmpty()) {
				StringBuffer buffer = new StringBuffer();
				for (int j = 0; j < blockedProcess.size(); j++) {
					buffer.append(blockedProcess.get(j).getId() + ":blocked ");
					blockedProcess.get(j).ioTimer--;
				}
				cpuNotBusyCounter++;
				for (int j = blockedProcess.size() - 1; j >= 0; j--) {
					if (blockedProcess.get(j).ioTimer <= 0) {
						Process curr = blockedProcess.remove(j);
						curr.readyTime = counter;
						processQueue.add(curr);
					}
				}
				clockCycle.put(counter, buffer.toString());
				counter++;
				continue;

			}
			// no process is running and also no process in the ready queue and
			// blocked queue, wait for the arrive of another process
			if (currProcess == null && i < processes.length) {
				clockCycle.put(counter, ": ");
				counter++;
				continue;
			}
			// no process is coming, loop exits
			else {
				break;
			}
		}
		finishTime = counter - 1;
		double tmpCpuUtil = 1 - (double) cpuNotBusyCounter / (double) counter;
		DecimalFormat twoDecimal = new DecimalFormat("#.##");
		cpuUtil = twoDecimal.format(tmpCpuUtil);
	}

	private static void srjf() {

		// ignore the situation which arrive times are the same
		QueueProcess processQueue = new QueueProcess();
		LinkedList<Process> blockedProcess = new LinkedList<Process>();

		int counter = 0;
		int i = 0;
		Process currProcess = null;
		int cpuNotBusyCounter = 0;
		while (true) {
			// if a process get ready, add it to the processqueue
			if (i < processes.length && counter == processes[i].getArrTime()) {
				Process tmpProcess = processes[i];
				processQueue.add(tmpProcess);
				i++;
			}
			// if running process exists
			if (currProcess != null && currProcess.timer > 0) {
				if (processQueue.isEmpty() && blockedProcess.isEmpty()) {
					clockCycle.put(counter, currProcess.getId() + ":running ");
				} else if (!processQueue.isEmpty() && blockedProcess.isEmpty()) {
					StringBuffer buffer = new StringBuffer();
					buffer.append(currProcess.getId() + ":running ");
					for (int j = 0; j < processQueue.size(); j++) {
						buffer.append(processQueue.get(j).getId() + ":ready ");
					}
					clockCycle.put(counter, buffer.toString());
				} else if (processQueue.isEmpty() && !blockedProcess.isEmpty()) {
					StringBuffer buffer = new StringBuffer();

					for (int j = 0; j < blockedProcess.size(); j++) {
						buffer.append(blockedProcess.get(j).getId()
								+ ":blocked ");
						blockedProcess.get(j).ioTimer--;

					}
					for (int j = blockedProcess.size() - 1; j >= 0; j--) {
						if (blockedProcess.get(j).ioTimer <= 0) {
							Process curr = blockedProcess.remove(j);
							processQueue.add(curr);
						}
					}
					buffer.append(currProcess.getId() + ":running ");
					clockCycle.put(counter, buffer.toString());

				} else {
					StringBuffer buffer = new StringBuffer();

					for (int j = 0; j < blockedProcess.size(); j++) {
						buffer.append(blockedProcess.get(j).getId()
								+ ":blocked ");
						blockedProcess.get(j).ioTimer--;
					}

					buffer.append(currProcess.getId() + ":running ");
					for (int j = 0; j < processQueue.size(); j++) {
						buffer.append(processQueue.get(j).getId() + ":ready ");
					}
					for (int j = blockedProcess.size() - 1; j >= 0; j--) {
						if (blockedProcess.get(j).ioTimer <= 0) {
							Process curr = blockedProcess.remove(j);
							processQueue.add(curr);
						}
					}
					clockCycle.put(counter, buffer.toString());
				}
				currProcess.timer--;
				currProcess.remainingCpuTime--;
				if (currProcess.timer > 0) {
					Process curr = currProcess;
					processQueue.add(curr);
					currProcess = null;
				}
				counter++;
				continue;
			}
			// running process exists and already run
			else if (currProcess != null) {
				if (currProcess.flag) {
					if (currProcess.getIoTime() == 0) {
						currProcess.flag = false;
						currProcess.timer = currProcess.getCpuTime()-currProcess.getHalfCpuTime();
					} else {
						Process curr = currProcess;
						curr.timer = curr.getHalfCpuTime();
						curr.flag = false;
						blockedProcess.add(curr);
						currProcess = null;
					}
				} else {
					turnaround[currProcess.getId()] = counter
							- currProcess.getArrTime();
					currProcess = null;

				}
				continue;
			}
			// if running process is null, get a process from ready queue as
			// running process
			if (currProcess == null && !processQueue.isEmpty()) {
				currProcess = processQueue.pollProcessWithLeastRemainingTime();
				continue;
			}
			// no process is running and only blocked processes
			else if (currProcess == null && !blockedProcess.isEmpty()) {
				StringBuffer buffer = new StringBuffer();
				for (int j = 0; j < blockedProcess.size(); j++) {
					buffer.append(blockedProcess.get(j).getId() + ":blocked ");
					blockedProcess.get(j).ioTimer--;
				}
				cpuNotBusyCounter++;
				for (int j = blockedProcess.size() - 1; j >= 0; j--) {
					if (blockedProcess.get(j).ioTimer <= 0) {
						Process curr = blockedProcess.remove(j);
						processQueue.add(curr);
					}
				}
				clockCycle.put(counter, buffer.toString());
				counter++;
				continue;

			}
			// no process is running and also no process in the ready queue and
			// blocked queue, wait for the arrive of another process
			if (currProcess == null && i < processes.length) {
				clockCycle.put(counter, ": ");
				counter++;
				continue;
			}
			// no process is coming, loop exits
			else {
				break;
			}
		}
		finishTime = counter - 1;
		double tmpCpuUtil = 1 - (double) cpuNotBusyCounter / (double) counter;
		DecimalFormat twoDecimal = new DecimalFormat("#.##");
		cpuUtil = twoDecimal.format(tmpCpuUtil);

	}

	private static void output(String outputName) throws IOException {
		FileWriter fw = null;
		try {
			fw = new FileWriter(outputName);
			for (int i = 0; i < clockCycle.size(); i++) {
				fw.write(i + " " + clockCycle.get(i) + "\n");
			}
			fw.write("\n");
			fw.write("Finishing time: " + finishTime + "\n");
			fw.write("CPU utilization: " + cpuUtil + "\n");
			for (int i = 0; i < turnaround.length; i++) {
				fw.write("Turnaround process " + i + ": " + turnaround[i]
						+ "\n");
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (fw != null) {
				fw.close();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		String fileName = "/home/administrator/Documents/programs/scheduler/scheduler_in1.txt";
		int choose = 1;
//		if(args.length != 2 ){
//			System.out.println("Please enter the right file name and option!");
//			return;
//		}
//		
//		String fileName = args[0];
//		int choose = Integer.parseInt(args[1]);
		if(choose < 0 || choose > 2){
			System.out.println("The option could only be 0, 1 or 2!");
			return;
		}

		String str = readFile(fileName);
		parseString(str);
		switch (choose) {
		case 0:
			fcfs();
			break;
		case 1:
			roundRobin2();
			break;
		case 2:
			srjf();
			break;
		default:
			break;
		}
		String outputFileName = fileName.substring(0, fileName.indexOf('.'))
				+ "-"+choose + ".txt";
		output(outputFileName);

	}

}
