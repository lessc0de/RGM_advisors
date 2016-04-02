package orderbook;

import java.math.BigDecimal;

public class ReduceOrder {
	
	private String timeStamp;
	final static char identifier = 'R';
	private String orderId;
	private double size;
	
	public ReduceOrder(String timeStamp, String orderId, Double size) {
		super();
		this.timeStamp = timeStamp;
		this.orderId = orderId;
		this.size = size;
	}
	
	public String getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public double getSize() {
		return size;
	}
	public void setSize(Integer size) {
		this.size = size;
	}
	public char getIdentifier() {
		return identifier;
	}


}
