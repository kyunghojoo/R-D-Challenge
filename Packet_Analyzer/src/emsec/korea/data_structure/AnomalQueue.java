package emsec.korea.data_structure;

public class AnomalQueue {

    private int qSize = 0;
    private AnomalObject[] queue;
    private int firstPointer = -1;
    private int lastPointer = -1;
    private int count = 0;
    
	public AnomalQueue() {
		
	}
	
	public int get_queue_size()
	{
		return qSize;
	}
	
	public synchronized int get_element_count()
	{
		return count;
	}
	
	public synchronized AnomalObject getFirstElement()
	{
		if(isEmpty())
			return new AnomalObject();
		else
			return queue[firstPointer];
	}

	public synchronized void initialize(int qSize) {
		queue = new AnomalObject[qSize];
		this.qSize = qSize;
		firstPointer = -1;
		lastPointer = -1;
	}

	public synchronized boolean isEmpty() {
		if (count == 0)
		{
			return true;
		}
		return false;
	}

	public synchronized boolean isFull() {
		if (count == qSize)
		{
			return true;
		}
		return false;
	}

	public synchronized boolean enqueue(AnomalObject item)
	{
		if (isFull()) {
			return false;
		} else {
			count++;
			if (lastPointer + 1 < qSize) {
				queue[lastPointer + 1] = item;
				lastPointer++;

			} else {
				lastPointer = -1;
				queue[lastPointer + 1] = item;
				lastPointer++;
			}

			if (firstPointer == -1)
				firstPointer++;
		}
		
		return true;
	}

    public synchronized AnomalObject dequeue()
    {
		if (isEmpty()) {
			initialize(queue.length);
			return null;
		}
		AnomalObject item = queue[firstPointer];
		count--;

		queue[firstPointer] = null;
		if (firstPointer + 1 == qSize)
			firstPointer = 0;
		else
			firstPointer++;

		return item;
    }
}