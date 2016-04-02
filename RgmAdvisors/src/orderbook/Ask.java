package orderbook;

import java.math.BigDecimal;

public class Ask extends AddOrderToBook {
	
	final static char side = 'S';
	
	public Ask(String timeStamp, String orderId, BigDecimal price, Double size) {
		super(timeStamp, orderId, price, size);
		// TODO Auto-generated constructor stub
	}

	public char getSide() {
		return side;
	}

	@Override
	public String toString() {
		return getTimeStamp()
				+ " " + getIdentifier() + " " + getOrderId() + " "+side+" "
				+ getPrice() + " " + getSize();
	}

	
}
