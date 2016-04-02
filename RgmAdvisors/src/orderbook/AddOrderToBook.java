package orderbook;

import java.math.BigDecimal;

public class AddOrderToBook {

	private String timeStamp;
	final static char identifier = 'A';
	private String orderId;
	private BigDecimal price;
	private Double size;
	
	public AddOrderToBook(String timeStamp, String orderId, BigDecimal price,
			Double size) {
		
		this.timeStamp = timeStamp;
		this.orderId = orderId;
		this.price = price;
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
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	public Double getSize() {
		return size;
	}
	public void setSize(Double size) {
		this.size = size;
	}
	public char getIdentifier() {
		return identifier;
	}
}
