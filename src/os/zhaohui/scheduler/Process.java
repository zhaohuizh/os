package os.zhaohui.scheduler;

public class Process {
	private int id;
	private int cpuTime;
	private int ioTime;
	private int arrTime;
	private int halfCpuTime;
	public int timer;
	public int ioTimer;
	public boolean flag;
	public int remainingCpuTime;
	public int readyTime;
	public Process(int id, int cpuTime, int ioTime, int arrTime){
		this.id = id;
		this.cpuTime = cpuTime;
		this.ioTime = ioTime;
		this.arrTime = arrTime;
		this.halfCpuTime = (int) (0.5 * cpuTime + 0.5);
		this.timer = halfCpuTime;
		this.ioTimer = ioTime;
		this.flag = true;
		this.remainingCpuTime = cpuTime;
		this.readyTime = arrTime;
	}
	public int getHalfCpuTime() {
		return halfCpuTime;
	}
	public void setHalfCpuTime(int halfCpuTime) {
		this.halfCpuTime = halfCpuTime;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getCpuTime() {
		return cpuTime;
	}
	public void setCpuTime(int cpuTime) {
		this.cpuTime = cpuTime;
	}
	public int getIoTime() {
		return ioTime;
	}
	public void setIoTime(int ioTime) {
		this.ioTime = ioTime;
	}
	public int getArrTime() {
		return arrTime;
	}
	public void setArrTime(int arrTime) {
		this.arrTime = arrTime;
	}
	
}
