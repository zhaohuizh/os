package os.zhaohui.linker;

public class DefList {
	private int account;
	private VariableAddress[] variableAddress;
	public DefList(int account){
		this.account = account;
		this.variableAddress = new VariableAddress[account];
	}
	public int getAccount() {
		return account;
	}
	public void setAccount(int account) {
		this.account = account;
	}
	public VariableAddress[] getVariableAddress() {
		return variableAddress;
	}
	public void setVariableAddress(VariableAddress[] variableAddress) {
		this.variableAddress = variableAddress;
	}
	
}
