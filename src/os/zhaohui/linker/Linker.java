package os.zhaohui.linker;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

public class Linker {
	private static int[] moduleBeginAdd;
	private static ArrayList<VariableAddress> variableAddress = new ArrayList<VariableAddress>();
	private static int[] addressSheet;
	private static Map<String, Integer> warningMap = new TreeMap<String, Integer>();
	private static HashMap<String, String> errormap = new HashMap<String, String>();

	/*
	 * read file and save its in a string
	 */
	public static String readFile(String inputName) throws IOException {
		FileReader fr = null;
		StringBuffer buffer = new StringBuffer();
		try {
			int ch = 0;
			fr = new FileReader(inputName);
			char temp[] = { ' ' };
			while ((ch = fr.read(temp)) != -1) {
				buffer.append(temp);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (fr != null) {
				fr.close();
			}
		}
		return buffer.toString();
	}

	/*
	 * split the string
	 */
	public static String[] splitString(String str) {
		String[] splits = str.split("\\s+");
		return splits;
	}

	public static Queue<Module> parseStringArray(String[] splits) {
		Queue<Module> moduleList = new LinkedList<Module>();
		int pointer = 0;
		if (splits[pointer].equals("")) {
			pointer++;
		}
		while (pointer < splits.length) {
			// parse the defList part
			int account = Integer.parseInt(splits[pointer++]);
			DefList defineList = new DefList(account);
			VariableAddress[] variableAddress = new VariableAddress[account];
			for (int tmp = 0; tmp < account; tmp++) {
				String var = splits[pointer++];
				int add = Integer.parseInt(splits[pointer++]);
				VariableAddress varAddress = new VariableAddress(var, add);
				variableAddress[tmp] = varAddress;
			}
			defineList.setVariableAddress(variableAddress);

			// parse the UseList part
			int size = Integer.parseInt(splits[pointer++]);
			UseList useList = new UseList(size);
			String[] varList = new String[size];
			for (int tmp = 0; tmp < size; tmp++) {
				varList[tmp] = splits[pointer++];
			}
			useList.setVariables(varList);

			// parse the Code part
			int codeSize = Integer.parseInt(splits[pointer++]);
			Code code = new Code(codeSize);
			AddressType[] addressType = new AddressType[codeSize];
			for (int tmp = 0; tmp < codeSize; tmp++) {
				AddressKind kind = AddressKind.valueOf(splits[pointer++]);
				int address = Integer.parseInt(splits[pointer++]);
				addressType[tmp] = new AddressType(kind, address);
			}
			code.setSize(codeSize);
			code.setAddresses(addressType);

			// add the Module to the ModuleList
			Module module = new Module(defineList, useList, code);
			moduleList.add(module);
		}

		return moduleList;
	}

	public static Module[] convertQueueToArray(Queue<Module> moduleList) {
		int moduleSize = moduleList.size();
		Module[] moduleArray = new Module[moduleSize];
		for (int tmp = 0; tmp < moduleSize; tmp++) {
			moduleArray[tmp] = moduleList.poll();
		}
		return moduleArray;
	}

	public static void firstTranverse(Module[] moduleArray) {
		int len = moduleArray.length;
		moduleBeginAdd = new int[len];
		int absoluteAddress = 0;
		for (int i = 0; i < len; i++) {
			moduleBeginAdd[i] = absoluteAddress;
			// System.out.println(moduleBeginAdd[i]);
			absoluteAddress += moduleArray[i].getCode().getSize();
		}

		for (int i = 0; i < len; i++) {
			int account = moduleArray[i].getDefineList().getAccount();
			if (account != 0) {
				VariableAddress[] formerAddress = moduleArray[i]
						.getDefineList().getVariableAddress();
				for (int tmp = 0; tmp < account; tmp++) {
					String varName = formerAddress[tmp].getVariable();
					int relativeAddress = formerAddress[tmp].getAddress();
					VariableAddress varAddr = new VariableAddress(varName,
							relativeAddress + moduleBeginAdd[i]);
					variableAddress.add(varAddr);
				}
			}
		}

	}

	public static void secondTranverse(Module[] moduleArray) throws Exception {
		int len = moduleArray.length;
		Queue<Integer> addSheetQueue = new LinkedList<Integer>();
		for (int i = 0; i < len; i++) {
			int size = moduleArray[i].getCode().getSize();
			AddressType[] formerAddressType = moduleArray[i].getCode()
					.getAddresses();
			for (int tmp = 0; tmp < size; tmp++) {
				AddressKind kind = formerAddressType[tmp].getKind();
				int address = formerAddressType[tmp].getAddress();

				if (kind.equals(AddressKind.R)) {
					address += moduleBeginAdd[i];
				} else if (kind.equals(AddressKind.E)) {
					int varPos = address % 1000;
					String varLocal = moduleArray[i].getUseList()
							.getVariables()[varPos];
					int relativeAddress = -1;
					for (int j = 0; j < variableAddress.size(); j++) {
						if (variableAddress.get(j).getVariable()
								.equals(varLocal)) {
							relativeAddress = variableAddress.get(j)
									.getAddress();
						}
					}
					if (relativeAddress == -1) {
						//errormap.put(varLocal, varLocal+ " used but not defined");
						throw new Exception(varLocal+ " used but not defined");
					} else {
						address = address / 1000 * 1000 + relativeAddress;
					}

				}
				addSheetQueue.add(address);
			}
		}
		int queueSize = addSheetQueue.size();
		addressSheet = new int[queueSize];
		for (int i = 0; i < queueSize; i++) {
			addressSheet[i] = addSheetQueue.poll();
		}

	}

	public static void implWarnings(Module[] moduleArray) {
		int len = moduleArray.length;
		Map<String, Boolean> warningHelper = new TreeMap<String, Boolean>();
		for (int i = 0; i < variableAddress.size(); i++) {
			warningHelper.put(variableAddress.get(i).getVariable(), false);
		}
		for (int i = 0; i < len; i++) {
			int account = moduleArray[i].getUseList().getAccount();
			if (account != 0) {
				String[] usedVarAddress = moduleArray[i].getUseList()
						.getVariables();
				for (int tmp = 0; tmp < account; tmp++) {
					warningHelper.put(usedVarAddress[tmp], true);
				}
			}
		}
		for (String str : warningHelper.keySet()) {
			if (warningHelper.get(str) == false) {
				for (int i = 0; i < len; i++) {
					int account = moduleArray[i].getDefineList().getAccount();
					for (int j = 0; j < account; j++) {
						if (moduleArray[i].getDefineList().getVariableAddress()[j]
								.getVariable().equals(str)) {
							warningMap.put(str, i + 1);
						}
					}
				}
			}
		}

	}
	
	

	public static void implErrors(Module[] moduleArray) throws Exception {

		// test the error of multiple declaration
		for (int i = 0; i < variableAddress.size(); i++) {
			for (int j = i + 1; j < variableAddress.size(); j++) {
				if (variableAddress.get(i).getVariable()
						.equals(variableAddress.get(j).getVariable())) {
					//errormap.put(variableAddress.get(i).getVariable(), variableAddress.get(i).getVariable() + " multiply defined");
					throw new Exception(variableAddress.get(i).getVariable() + " multiply defined");
				}
			}
		}

		// test the error of exceeding module size
		for (int i = 0; i < moduleArray.length; i++) {
			int localVarAccount = moduleArray[i].getDefineList().getAccount();
			for (int j = 0; j < localVarAccount; j++) {
				int localVarAdd = moduleArray[i].getDefineList()
						.getVariableAddress()[j].getAddress();
				int moduleSize = moduleArray[i].getCode().getSize();
				if (localVarAdd > moduleSize) {
//					errormap.put(moduleArray[i].getDefineList()
//							.getVariableAddress()[j].getVariable(), "address "
//							+ localVarAdd + " exceeds the module of size "
//							+ moduleSize);
					throw new Exception("address "+ localVarAdd + " exceeds the module of size "+ moduleSize);
				}
			}
		}

	}

	public static void fileWriter(String outputName) throws IOException {
		FileWriter fw = null;
		try {
			fw = new FileWriter(outputName);

			if (errormap.isEmpty()) {
				fw.write("Symbol Table\r\n");
				for (int i = 0; i < variableAddress.size(); i++) {
					fw.write(variableAddress.get(i).getVariable() + "="
							+ variableAddress.get(i).getAddress() + "\r\n");
				}

				fw.write("\rMemory Map\r\n");
				for (int i = 0; i < addressSheet.length; i++) {
					fw.write(i + ":\t" + addressSheet[i] + "\r\n");
				}

			} else {
				for (String str : errormap.keySet()) {
					fw.write("Error: " + errormap.get(str) + "\r\n");
				}
			}

			if (!warningMap.isEmpty()) {
				fw.write("\r\n");
				for (String str : warningMap.keySet()) {
					fw.write("Warning: " + str + " was defined in module "
							+ warningMap.get(str) + " but never used.\r\n");
				}
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (fw != null) {
				fw.close();
			}
		}
	}

	public static void main(String[] args) throws Exception, IOException {
		if (args.length != 2) {
			throw new Exception("Please enter input file and output file with its path!");
		}
		String inputName = args[0];
		String outputName = args[1];
		String str = readFile(inputName);
		String[] splits = str.split("\\s+");
		Queue<Module> moduleList = parseStringArray(splits);
		Module[] moduleArray = convertQueueToArray(moduleList);
		firstTranverse(moduleArray);
		secondTranverse(moduleArray);
		implWarnings(moduleArray);
		implErrors(moduleArray);
		fileWriter(outputName);

	}
}
