package orderbook;

import java.math.BigDecimal;
import java.util.HashMap;

public class BuyOutput {
	
	public static final String DEFAULT_VALUE = "NA";
	private String timeStamp;
	private final char identifier = 'B';
	private String value;
	private HashMap<String, Double> shareUsage;
	
	public BuyOutput(String timeStamp, String value) {
		this.timeStamp = timeStamp;
		this.value = value;
	}
	
	public BuyOutput (String timeStamp){
		this.timeStamp = timeStamp;
		this.value = DEFAULT_VALUE;	
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public HashMap<String, Double> getSharesUsed() {
		return shareUsage;
	}
	public void setSharesUsed(HashMap<String, Double> askSharesUsed) {
		this.shareUsage = askSharesUsed;
	}
	public char getIdentifier() {
		return identifier;
	}

	@Override
	public String toString() {
		return timeStamp + " "
				+ identifier + " " + value;
	}


}
