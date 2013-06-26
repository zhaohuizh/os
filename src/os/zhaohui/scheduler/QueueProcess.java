package os.zhaohui.scheduler;

import java.util.LinkedList;

public class QueueProcess extends LinkedList<Process> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public QueueProcess() {
		super();
	}

	public boolean add(Process process) {
		for (int i = 0; i < this.size(); i++) {
			if (process.readyTime < this.get(i).readyTime
					|| (process.readyTime) == this.get(i).readyTime && process
							.getId() < this.get(i).getId()) {
				this.add(i, process);
				return true;
			}
		}
		this.addLast(process);
		return true;
	}

	public Process pollProcessWithLeastRemainingTime() {
		int index = 0;
		int indexOfPolled = 0;
		int tmpRemTime = Integer.MAX_VALUE;
		for (int i = 0; i < this.size(); i++) {
			if (this.get(i).remainingCpuTime < tmpRemTime) {
				tmpRemTime = this.get(i).remainingCpuTime;
				index = i;
			}
		}
		indexOfPolled = index;
		for (int i = 0; i < this.size(); i++) {
			if(this.get(i).remainingCpuTime == tmpRemTime && i != index){
				if(this.get(i).getId() < this.get(indexOfPolled).getId()){
					indexOfPolled = i;
				}
			}
		}
		
		return this.remove(indexOfPolled);	

	}
}
