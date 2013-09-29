package au.edu.rmit.misc;

import java.util.Arrays;

import au.edu.rmit.querying.QueryResult;

public class MinHeap {
    public QueryResult[] heap;
    int itemCount;

    public static void main(String[] args)
    {
        MinHeap myHeap = new MinHeap(8);
        
        myHeap.addItem(new QueryResult("0",2));
        System.out.println(Arrays.toString(myHeap.heap));
        
        myHeap.addItem(new QueryResult("1",4));
        System.out.println(Arrays.toString(myHeap.heap));
        
        myHeap.addItem(new QueryResult("2",7));
        System.out.println(Arrays.toString(myHeap.heap));
        
        myHeap.addItem(new QueryResult("3",8));
        System.out.println(Arrays.toString(myHeap.heap));
        
        myHeap.addItem(new QueryResult("4",12));
        System.out.println(Arrays.toString(myHeap.heap));
        
        myHeap.addItem(new QueryResult("5",9));
        System.out.println(Arrays.toString(myHeap.heap));
        
        myHeap.addItem(new QueryResult("6",6));
        System.out.println(Arrays.toString(myHeap.heap));
        
        myHeap.addItem(new QueryResult("7",11));
        System.out.println(Arrays.toString(myHeap.heap));
        
        myHeap.addItem(new QueryResult("8",10));
        System.out.println(Arrays.toString(myHeap.heap));
        
        myHeap.addItem(new QueryResult("9",13));
        System.out.println(Arrays.toString(myHeap.heap));
    }

    public MinHeap(int heapSize)
    {
        this.heap = new QueryResult[heapSize];
        this.itemCount = 0;
    }

    public void addItem(QueryResult result)
    {
        if (itemCount == this.heap.length && result.getScore() <= this.heap[0].getScore())
        {
            return;
        }
        else if (itemCount < this.heap.length)
        {
            for (int i = this.heap.length - 2; i >= 0; i--)
            {
                this.heap[i+1] = this.heap[i];
            }
            this.heap[0] = result;
            this.itemCount++;
            this.heapify();
        }
        else
        {
            this.heap[0] = result;
            this.heapify();
        }
    }

    private void heapify()
    {
        int i = 0;
        int childPos = getLeftChildIndex(i);

        while (childPos < this.itemCount)
        {
            if (childPos + 1 < this.itemCount
                    && this.heap[childPos].getScore() > this.heap[childPos + 1].getScore())
            {
                childPos++;
            }

            if (this.heap[i].getScore() <= this.heap[childPos].getScore())
            {
                break;
            }
            else
            {
                QueryResult parentValue = this.heap[i];
                QueryResult childValue = this.heap[childPos];

                this.heap[i] = childValue;
                this.heap[childPos] = parentValue;

                i = childPos;
                childPos = getLeftChildIndex(i);
            }
        }
    }

    @SuppressWarnings("unused")
    private int getParentIndex(int index)
    {
        return index/2;
    }

    private int getLeftChildIndex(int index)
    {
        return ((index+1)*2)-1;
    }

    @SuppressWarnings("unused")
    private int getRightChildIndex(int index)
    {
        return ((index+1)*2 + 1)-1;
    }
    
    public double getLowestScore()
    {
        if (this.itemCount == 0)
        {
            return 0.0;
        }
        else
        {
            return (heap[0].getScore());
        }
    }
    
    public QueryResult[] getSortedHeap()
    {
        QueryResult[] sortedHeap = new QueryResult[this.itemCount];
        
        for (int i = 0; i < this.itemCount; i++)
        {
            sortedHeap[i] = this.heap[i];
        }
        
        Arrays.sort(sortedHeap);
        
        return sortedHeap;
    }
}
