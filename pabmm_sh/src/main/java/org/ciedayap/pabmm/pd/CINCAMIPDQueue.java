/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.pabmm.pd;

import java.util.Observable;
import java.util.concurrent.ArrayBlockingQueue;
import org.ciedayap.cincamimisconversor.QueueException;

/**
 * It represents the queue of the instances to be translated to a Table
 * 
 * @author Mario Diván
 * @version 1.0
 */
public class CINCAMIPDQueue extends Observable{
    private final ArrayBlockingQueue<CINCAMIPD> pdQueue;
    
    /**
     * Default constructor. It is responsible for defining the queue capacity to 10 
     * and the operation mode will be FIFO (Fisrt Input, First Output).
     */    
    public CINCAMIPDQueue()
    {
        pdQueue=new ArrayBlockingQueue(10,true);
    }
 
    /**
     * It is responsible for defining the queue capacity and the operation mode.
     * @param capacity The queue capacity
     * @param fifo if {@code true} then queue accesses for threads blocked
     *        on insertion or removal, are processed in FIFO order;
     *        if {@code false} the access order is unspecified.
     */
    public CINCAMIPDQueue(int capacity, boolean fifo)
    {
        pdQueue=new ArrayBlockingQueue(((capacity<10)?10:capacity),fifo);
    }    
    
    /**
     * It is responsible for incorporating a new CINCAMI/PD message at the end 
     * of the queue
     * @param definition The project definition to be incorporated
     * @return TRUE if the project definition was incorporated and the observers were notified.
     * @throws QueueException This exception is raised when there is not an 
     * initialized queue.
     */
    public synchronized boolean add(CINCAMIPD definition) throws QueueException
    {
        if(definition==null) return false;
        if(pdQueue==null) throw new QueueException("CINCAMIPD Queue not found");
        
        boolean rdo=pdQueue.offer(definition);
        
        if(rdo)
        {
            this.notifyObservers();
            return true;
        }
        
        return false;
    }

    /**
     * It is responsible for incorporating a new CINCAMI/PD message at the end 
     * of the queue. if the queue is full, the first element is remnoved and the 
     * new element is incorporated at the end of the queue.
     * @param definition The project definition to be incorporated
     * @return TRUE if the project definition was incorporated and the observers were notified.
     * @throws QueueException This exception is raised when there is not an 
     * initialized queue.
     * @throws InterruptedException if interrupted while waiting
     */
    public synchronized boolean addKeepingLast(CINCAMIPD definition) throws QueueException, InterruptedException
    {
        if(definition==null) return false;
        if(pdQueue==null) throw new QueueException("CINCAMIPD Queue not found");
        
        boolean rdo=pdQueue.offer(definition);
        
        while(!rdo)
        {
           pdQueue.take();
           rdo=pdQueue.offer(definition);
        }
        
        if(rdo)
        {
            this.notifyObservers();
            return true;
        }
        
        return false;
    }  
    
    /**
     * It gives the first element from the queue without remove the element from the list
     * @return The first element in the queue without remove it. Additionally,
     * it returns null if the queue is empty.
     * @throws QueueException This exception is raised when there is not an initialized queue.
     */
    public synchronized CINCAMIPD firstAvailable() throws QueueException
    {
        if(pdQueue==null) throw new QueueException("CINCAMIPD Queue not found");
        
        return pdQueue.peek();        
    }
    
    /**
     * It removes all the elements from the queue.
     * 
     * @throws QueueException This exception is raised when there is not an initialized queue.
     */
    public synchronized void clear() throws QueueException
    {
         if(pdQueue==null) throw new QueueException("CINCAMIPD Queue not found");

         pdQueue.clear();
         this.notifyObservers();
    }
    
    /**
     * It gives the first element from the queue, removing it.
     * @return The first element from the queue, removing it. Additionally, it returns
     * null if the queue is empty.
     * @throws QueueException This exception is raised when there is not an initialized queue.
     */
    public synchronized CINCAMIPD firstAvailableandRemove() throws QueueException
    {
        if(pdQueue==null) throw new QueueException("CINCAMIPD Queue not found");
        if(pdQueue.isEmpty()) return null;
        
        CINCAMIPD element=pdQueue.poll();        
        if(element!=null) this.notifyObservers();
        
        return element;    
    }
    
    /**
     * It gives the first element from the queue, removing it. If the element is not 
     * available, then it waits until the element becomes available.
     * @return The first element from the queue, removing it. Additionally, it 
     * returns null if the queue is empty.
     * @throws QueueException This exception is raised when there is not an initialized queue.
     * @throws InterruptedException if interrupted while waiting
     */
    public synchronized CINCAMIPD firstAvailableandRemoveW() throws QueueException, InterruptedException
    {
        if(pdQueue==null) throw new QueueException("CINCAMIPD Queue not found");
        if(pdQueue.isEmpty()) return null;
        
        CINCAMIPD element=pdQueue.take();        
        if(element!=null) this.notifyObservers();
        
        return element;            
    }
    
    /**
     * It returns the remaining capacity related to the queue
     * @return The remaining capacity of the queue
     * @throws QueueException This exception is raised when there is not an initialized queue
     */
    public  int remainingCapacity() throws QueueException
    {
        if(pdQueue==null) throw new QueueException("CINCAMIPD Queue not found");
        
        return pdQueue.remainingCapacity();
    }

    /**
     * It returns the quantity of elements in the queue
     * @return The quantity of elements in the queue
     */
    public  int size() 
    {
        if(pdQueue==null) return 0;
        
        return pdQueue.size();
    }    
    
    /**
     * It indicates whether the queue is null or not.
     * @return TRUE the queue is null, FALSE otherwise.
     */
    public boolean isEmpty()
    {
        if(pdQueue==null) return true;
        
        return pdQueue.isEmpty();
    }
    
}
