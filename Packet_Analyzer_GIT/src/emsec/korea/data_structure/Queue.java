package emsec.korea.data_structure;

public class Queue {

    private int qSize = 0;
    private DataObject[] queue;
    private int firstPointer = -1;
    private int lastPointer = -1;
    private int count = 0;

	public Queue() {
		// TODO Auto-generated constructor stub
	}
	
	public int get_queue_size()
	{
		return qSize;
	}

	public synchronized void initialize(int qSize) {
		queue = new DataObject[qSize];
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

	public synchronized boolean enqueue(DataObject item)
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

    public synchronized DataObject dequeue()
    {
		if (isEmpty()) {
			initialize(queue.length);
			return null;
		}
		DataObject item = queue[firstPointer];
		count--;

		queue[firstPointer] = null;
		if (firstPointer + 1 == qSize)
			firstPointer = 0;
		else
			firstPointer++;

		return item;
    }

	public void printQueue() {
		System.out.println("queue is -> ");
		
		if(lastPointer < firstPointer)
		{
			for (int i = firstPointer; i < queue.length; i++) {
				if (firstPointer >= 0)
					queue[i].print();
			}
			
			for (int i = 0; i <= lastPointer; i++) {
				if (firstPointer >= 0)
					queue[i].print();
			}
			
		}
		else
		{
			for (int i = firstPointer; i <= lastPointer; i++) {
				if (firstPointer >= 0)
					queue[i].print();
			}
		}
	}
}