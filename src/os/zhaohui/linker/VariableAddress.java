package os.zhaohui.linker;

public class VariableAddress {
	private String variable;
	private int address;
	public VariableAddress(String variable, int address){
		this.variable = variable;
		this.address = address;
	}
	public String getVariable() {
		return variable;
	}
	public void setVariable(String variable) {
		this.variable = variable;
	}
	public int getAddress() {
		return address;
	}
	public void setAddress(int address) {
		this.address = address;
	}
	
	
}
