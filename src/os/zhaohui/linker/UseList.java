package os.zhaohui.linker;

public class UseList {
	private int account = 0;
	private String[] variables;
	public UseList(int account){
		this.account = account;
		this.variables = new String[account];
	}
	public int getAccount() {
		return account;
	}
	public void setAccount(int account) {
		this.account = account;
	}
	public String[] getVariables() {
		return variables;
	}
	public void setVariables(String[] variables) {
		this.variables = variables;
	}
	
}
