package os.zhaohui.linker;

public class Module {
	private DefList defineList;
	private UseList useList;
	private Code code;
	public Module(){
		this.defineList = null;
		this.useList = null;
		this.code = null;
	}
	public Module(DefList defineList, UseList useList, Code code){
		this.defineList = defineList;
		this.useList = useList;
		this.code = code;
	}

	public DefList getDefineList() {
		return defineList;
	}

	public void setDefineList(DefList defineList) {
		this.defineList = defineList;
	}

	public UseList getUseList() {
		return useList;
	}

	public void setUseList(UseList useList) {
		this.useList = useList;
	}

	public Code getCode() {
		return code;
	}

	public void setCode(Code code) {
		this.code = code;
	}
	
}
