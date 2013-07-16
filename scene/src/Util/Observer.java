package Util;

import java.util.LinkedList;

import Util.Subscriber;

public abstract class Observer {
	
	protected LinkedList<Subscriber> subscriber;
	
	public Observer(){
		this.subscriber = new LinkedList<Subscriber>();
	}
	
	public void subscribe(Subscriber subscriber){
		this.subscriber.add(subscriber);
	}
	
	public boolean unsubscribe(Subscriber subscriber){
		boolean success = false;
		for(Subscriber s : this.subscriber){
			if(s.equals(subscriber)){
				this.subscriber.remove(s);
				success = true;
			}
		}
		return success;
	}
	
	public void notifyObservers() {
		for(Subscriber s : this.subscriber){
			s.handleEvent();
		}
	}
}
