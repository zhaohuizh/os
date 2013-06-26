package os.zhaohui.linker;

public class Code {
	private int size = 0;
	private AddressType[] addresses;
	
	public Code( int size){
		this.size = size;
		this.addresses = new AddressType[size];
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public AddressType[] getAddresses() {
		return addresses;
	}

	public void setAddresses(AddressType[] addresses) {
		this.addresses = addresses;
	}
	
}
