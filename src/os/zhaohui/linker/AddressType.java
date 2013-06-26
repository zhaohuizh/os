package os.zhaohui.linker;

public class AddressType {
	private AddressKind kind;
	private int address;
	public AddressType( AddressKind kind, int address){
		this.kind = kind;
		this.address = address;
	}
	
	public AddressKind getKind() {
		return kind;
	}
	public void setKind(AddressKind kind) {
		this.kind = kind;
	}
	public int getAddress() {
		return address;
	}
	public void setAddress(int address) {
		this.address = address;
	}
	
	

}
