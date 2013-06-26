package os.zhaohui.pageReplacement;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class PageReplacePolicy {
	private int memorySize;
	private int[] memorySlot;
	private int[] usedCounter;
	private int[] rBit;
	private int pointer = 0;
	private Queue<int[]> memoryResult = new LinkedList<int[]>();
	private Queue<Integer> chanceList = new LinkedList<Integer>();
	private int[] access;
	private int pfCounter;
	private String policyName;

	public String readFile(String fileName) {
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
			try {
				if (fr != null)
					fr.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		return buffer.toString();
	}

	public void parseString(String str) {
		String[] splits = str.split("\\s+");
		int len = splits.length;
		access = new int[len];
		for (int i = 0; i < len; i++) {
			access[i] = Integer.parseInt(splits[i]);
		}
	}

	public void fifo(int pageNo) {
		boolean flag = false;
		for (int i = 0; i < memorySize; i++) {
			if (memorySlot[i] == 0) {
				memorySlot[i] = pageNo;
				flag = true;
				pfCounter++;
				break;
			}
			if (memorySlot[i] == pageNo) {
				flag = true;
				break;
			}
		}
		if (!flag) {
			memorySlot[pointer] = pageNo;
			movePointer();
			pfCounter++;
		}
	}

	public void secondChance(int pageNo) {
		boolean flag = false;
		for (int i = 0; i < memorySize; i++) {
			if (memorySlot[i] == 0) {
				memorySlot[i] = pageNo;
				rBit[i] = 1;
				chanceList.add(pageNo);
				flag = true;
				pfCounter++;
				break;
			}
			if (memorySlot[i] == pageNo) {
				rBit[i] = 1;
				flag = true;
				break;
			}
		}
		if (!flag) {
			int index = findReplaceForSecondChance();
			memorySlot[index] = pageNo;
			chanceList.add(pageNo);
			pfCounter++;
			rBit[index] = 1;
		}

	}

	public void lru(int pageNo, int timer) {
		boolean flag = false;
		for (int i = 0; i < memorySize; i++) {
			if (memorySlot[i] == 0) {
				memorySlot[i] = pageNo;
				flag = true;
				usedCounter[i] = timer;
				pfCounter++;
				break;
			}
			if (memorySlot[i] == pageNo) {
				flag = true;
				usedCounter[i] = timer;
				break;
			}
		}
		if (!flag) {
			int smallestIndex = findSmallestTimeCounter();
			memorySlot[smallestIndex] = pageNo;
			usedCounter[smallestIndex] = timer;
			pfCounter++;
		}
	}

	private void movePointer() {
		if (pointer >= memorySize - 1) {
			pointer = 0;
		} else {
			pointer++;
		}
	}

	private int findSmallestTimeCounter() {
		int tmp = Integer.MAX_VALUE;
		int smallestIndex = 0;
		for (int i = 0; i < usedCounter.length; i++) {
			if (usedCounter[i] < tmp) {
				tmp = usedCounter[i];
				smallestIndex = i;
			}
		}
		return smallestIndex;
	}

	private int findReplaceForSecondChance() {
		while (true) {
			int tmp = chanceList.poll();
			int indexOfTmp = 0;
			for (int i = 0; i < memorySlot.length; i++) {
				if (memorySlot[i] == tmp) {
					indexOfTmp = i;
				}
			}
			if (rBit[indexOfTmp] == 1) {
				rBit[indexOfTmp] = 0;
				chanceList.add(tmp);
			} else {
				return indexOfTmp;
			}

		}
	}

	private String arrayToString(int[] array) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			if (array[i] == 0) {
				buffer.append("  ");
			} else {
				buffer.append(array[i] + " ");
			}
		}
		return buffer.toString();
	}

	public void outPut(String outputName) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(outputName);
			Iterator<int[]> it = memoryResult.iterator();
			while (it.hasNext()) {
				fw.write(arrayToString(it.next()));
				fw.write("\n");
			}
			fw.write("\n");
			double pf = (double) pfCounter / memoryResult.size();
			DecimalFormat twoDecimal = new DecimalFormat("#.##");
			String pfTwoDecimal = twoDecimal.format(pf);
			fw.write("Percentage of Page faults = " + pfTwoDecimal);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (fw != null) {
					fw.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	private void mainLogic(int policy) {
		memorySlot = new int[memorySize];
		rBit = new int[memorySize];
		usedCounter = new int[memorySize];

		for (int i = 0; i < access.length; i++) {
			switch (policy) {
			case 0:
				policyName = "fifo";
				fifo(access[i]);
				break;
			case 1:
				policyName = "secondchance";
				secondChance(access[i]);
				break;
			case 2:
				policyName = "lru";
				lru(access[i], i);
				break;
			default:
				break;
			}

			int[] tempArray = new int[memorySize];
			for (int j = 0; j < memorySlot.length; j++) {
				tempArray[j] = memorySlot[j];
			}
			memoryResult.add(tempArray);

		}
	}

	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("Wrong parameters!");
			return;
		}
		try {
			int policy = Integer.parseInt(args[0]);
			int memorySize = Integer.parseInt(args[1]);
			if (policy != 0 && policy != 1 && policy != 2) {
				System.out
						.println("Please enter the right choice for policy: 0, FIFO; 1, Second Chance; 2, Least Recently Used");
				return;
			}
			PageReplacePolicy replacePolicy = new PageReplacePolicy();
			replacePolicy.memorySize = memorySize;
			String fileName = args[2];

			String str = replacePolicy.readFile(fileName);
			replacePolicy.parseString(str);
			replacePolicy.mainLogic(policy);
			String outputFileName = fileName.substring(0, fileName.lastIndexOf('.'))
					+ "."+replacePolicy.policyName + ".txt";
			replacePolicy.outPut(outputFileName);
			
		} catch (NumberFormatException e) {
			System.out.println("");
			e.printStackTrace();
		} 

	}
}
