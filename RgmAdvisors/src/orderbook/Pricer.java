package orderbook;

import static java.lang.System.err;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Pricer {
	
	private Map<String, Bid> bidsByPrice;
	private Map<String, Ask> asksByPrice;
	private SellOutput currentSelling;
	private BuyOutput currentBuying;
	private List<String> errorFile;
	private static String currentTimeStamp;
	
	public enum  TradeChangeIn{
		
		BUYING_INCOME, SELLING_INCOME;
		
	}	
	

	public Pricer(String file, Double targetSize) throws IOException {
		
		this.errorFile 				= new LinkedList<String>();
		this.bidsByPrice			= new HashMap<String, Bid>();
		this.asksByPrice			= new HashMap<String, Ask>();
		this.currentSelling         = new SellOutput ("0", "NA");
		this.currentBuying         	= new BuyOutput ("0", "NA");
		

		String inputFile 	= file+".csv";
		String outputFile 	= file+"Output.csv";
		String errorLogFile = file+"Errors.csv";
		
		List<String> outputList = new LinkedList<String>();
		
		List<String> dataList = Pricer.readList( inputFile, false );
		
		while( dataList.size() > 0 ){

			String recordStr 					= dataList.remove(0);
			TradeChangeIn TradeChangeInobj 		= consumeInputLine(recordStr, bidsByPrice, asksByPrice, this.getCurrentSelling(), this.getCurrentBuying(), errorFile, targetSize, Pricer.currentTimeStamp);
			String outputLine="";
			
			if(TradeChangeInobj!=(null))
			{	outputLine = startTrading(this.bidsByPrice, this.asksByPrice, this.getCurrentBuying(), this.getCurrentSelling(), targetSize, TradeChangeInobj, Pricer.currentTimeStamp);
				
				if(outputLine!=null)
					outputList.add(outputLine);
			}
		}
		
		writeList(outputFile, outputList);
		writeList(errorLogFile, errorFile);
	}

	private String startTrading(Map<String, Bid> bidsByPrice,
			Map<String, Ask> asksByPrice, BuyOutput currentBuying,
			SellOutput currentSelling, Double targetSize, TradeChangeIn TradeChangeInobj, String currentTimeStamp) {
		// TODO Auto-generated method stub
		
		String Lineoutput = null;
		
			if(TradeChangeInobj.equals(TradeChangeIn.BUYING_INCOME)){
				
				String previousBuyValue 	= currentBuying.getValue();
				currentBuying 				= findCheapestShares(asksByPrice, targetSize, currentTimeStamp );
				String newBuyValue 			= currentBuying.getValue();
				
				if(!previousBuyValue.equals(newBuyValue))
						Lineoutput 			= currentBuying.toString();
			}
		
			if(TradeChangeInobj.equals(TradeChangeIn.SELLING_INCOME)){
				
				String previousSellValue	= currentSelling.getValue();
				currentSelling 				= findExpensiveShares(bidsByPrice, targetSize, currentTimeStamp );
				String newSellValue 		= currentSelling.getValue();
				
				if(!previousSellValue.equals(newSellValue))
					   Lineoutput 			= currentSelling.toString();
			}

		return Lineoutput;
	}



	private SellOutput findExpensiveShares(Map<String, Bid> bidsByPrice,
			Double targetSize, String timeStamp) {
		
		SellOutput currentSelling 								= new SellOutput(timeStamp); //System.out.println(" currentSelling "+currentSelling.toString());
		HashMap<String, Double> bidByUsage 						= new HashMap<String, Double>();
		TreeMap <BigDecimal, ArrayList<Bid>> SortedAsksByPrice 	= new TreeMap<BigDecimal, ArrayList<Bid>>();
		
		for(String orderid: bidsByPrice.keySet()){
			
			if(!SortedAsksByPrice.containsKey(bidsByPrice.get(orderid).getPrice()))
				SortedAsksByPrice.put(bidsByPrice.get(orderid).getPrice(), new ArrayList<Bid>());
			
			SortedAsksByPrice.get(bidsByPrice.get(orderid).getPrice()).add(bidsByPrice.get(orderid));
			
		}
		
		Double sharesNeeded = targetSize;
		BigDecimal value	= new BigDecimal (0);
		
			for(BigDecimal price : SortedAsksByPrice.descendingKeySet()){
				for(Bid bid : SortedAsksByPrice.get(price)){
				
				 if (sharesNeeded-bid.getSize()<=0){
					 bidByUsage.put(bid.getOrderId(), sharesNeeded);	
					 currentSelling.setSharesUsed(bidByUsage);
					value = value.add(new BigDecimal (bid.getPrice().doubleValue()*sharesNeeded));
					currentSelling.setValue(value.toString());
					this.setCurrentSelling(currentSelling);
					return currentSelling;
				}
				
				 else
						{
							sharesNeeded-=bid.getSize();
							bidByUsage.put(bid.getOrderId(), bid.getSize());
							value = value.add(new BigDecimal (bid.getPrice().doubleValue()*bid.getSize()));

						}
				}
			}
		
			this.setCurrentSelling(currentSelling);
			return currentSelling;
	}



	private BuyOutput findCheapestShares(Map<String, Ask> askByPrice,
			Double targetSize, String timeStamp) {
		
		BuyOutput currentBuying				 	= new BuyOutput(timeStamp);
		HashMap<String, Double> askByUsage 		= new HashMap<String, Double>();
		
		TreeMap <BigDecimal, ArrayList<Ask>> SortedAsksByPrice = new TreeMap<BigDecimal, ArrayList<Ask>>();
		
		for(String orderid: askByPrice.keySet()){
			
			if(!SortedAsksByPrice.containsKey(askByPrice.get(orderid).getPrice()))
				SortedAsksByPrice.put(askByPrice.get(orderid).getPrice(), new ArrayList<Ask>());
			
			SortedAsksByPrice.get(askByPrice.get(orderid).getPrice()).add(askByPrice.get(orderid));
			
		}
		
		Double sharesNeeded = targetSize;
		BigDecimal value	= new BigDecimal (0);
		
			for(BigDecimal price : SortedAsksByPrice.keySet()){
				for(Ask ask : SortedAsksByPrice.get(price)){

				 if (sharesNeeded-ask.getSize()<=0){
					askByUsage.put(ask.getOrderId(), sharesNeeded);	
					currentBuying.setSharesUsed(askByUsage);
					value = value.add(new BigDecimal (ask.getPrice().doubleValue()*sharesNeeded));
					currentBuying.setValue(value.toString());
					this.setCurrentBuying(currentBuying);
					return currentBuying;
				}
				
				 else
						{
							sharesNeeded-=ask.getSize();
							askByUsage.put(ask.getOrderId(), ask.getSize());
							value = value.add(new BigDecimal (ask.getPrice().doubleValue()*ask.getSize()));
							continue;
						}
									
				}
			}
		
			this.setCurrentBuying(currentBuying);
			return currentBuying;
	}



	private static TradeChangeIn consumeInputLine(String recordStr, Map<String, Bid> bidsByPrice, Map<String, Ask> asksByPrice, SellOutput currentSelling, BuyOutput currentBuying, List<String> errorFile, Double targetSize, String currentTimeStamp) {
		
		String[] inputline = recordStr.split( " " );
		TradeChangeIn TradeChangeInobj = null;
		
		
		if(inputline.length==4)
		{
			ReduceOrder currentReduceOrderObj;
			AddOrderToBook changeOrderToBookObj;
		
			try 
			{
				
				String timeStamp	= (inputline[0]);
				String orderId 		= inputline[2];
				Double size 		= Double.parseDouble(inputline[3]);	
				char identifier 	= inputline[1].charAt(0);
				
				
				if(!(identifier==(ReduceOrder.identifier) && (inputline[1].length()==1)))
						throw new Exception("Incorrect identifier "+inputline[1]+" instead of "+ReduceOrder.identifier);
				
				currentReduceOrderObj = new ReduceOrder(timeStamp, orderId, size);
				currentTimeStamp = timeStamp;
				Pricer.currentTimeStamp = timeStamp;
				
				if(bidsByPrice.containsKey(currentReduceOrderObj.getOrderId()))
				{
					changeOrderToBookObj = bidsByPrice.get(currentReduceOrderObj.getOrderId());
					Double totalNewNumberShares = (double) 0;
					if(currentReduceOrderObj.getSize() < changeOrderToBookObj.getSize())
						{
							changeOrderToBookObj.setSize(changeOrderToBookObj.getSize()-currentReduceOrderObj.getSize());
							totalNewNumberShares = changeOrderToBookObj.getSize();
						}
					
					else bidsByPrice.remove(currentReduceOrderObj.getOrderId());

					if(!currentSelling.getValue().equals(SellOutput.DEFAULT_VALUE))
						if (currentSelling.getSharesUsed().containsKey(currentReduceOrderObj.getOrderId())){
												
						if(currentSelling.getSharesUsed().get(currentReduceOrderObj.getOrderId()) > totalNewNumberShares)
							TradeChangeInobj = TradeChangeIn.SELLING_INCOME;
						
					}
						
				}
				
				else if(asksByPrice.containsKey(currentReduceOrderObj.getOrderId()))
				{
					changeOrderToBookObj 		= asksByPrice.get(currentReduceOrderObj.getOrderId());
					Double totalNewNumberShares = (double) 0;
					
					if(currentReduceOrderObj.getSize() < changeOrderToBookObj.getSize())
						{
							changeOrderToBookObj.setSize(changeOrderToBookObj.getSize()-currentReduceOrderObj.getSize());
							totalNewNumberShares = changeOrderToBookObj.getSize();
						}
					
					else asksByPrice.remove(currentReduceOrderObj.getOrderId());

				if(!currentBuying.getValue().equals(BuyOutput.DEFAULT_VALUE))
					if(currentBuying.getSharesUsed().containsKey(currentReduceOrderObj.getOrderId())){
						
						if(currentBuying.getSharesUsed().get(currentReduceOrderObj.getOrderId()) > totalNewNumberShares)
							TradeChangeInobj 	= TradeChangeIn.BUYING_INCOME;
					}
					

				}
				
				
			} catch (Exception e) { System.out.println(e);
				errorFile.add(" Error with ReduceOrder line "+recordStr+" with exception "+e);
			}
			
			
 

			
		}
		
	else if(inputline.length==6)
		{
			//Format: timestamp A order-id side price size
			
			try {
				
				String timeStamp	= (inputline[0]);
				String orderId 		= inputline[2];
				Double size 		= Double.parseDouble(inputline[5]);	
				BigDecimal price	= new BigDecimal(inputline[4]);
				char identifier 	= inputline[1].charAt(0);
				char side 			= inputline[3].charAt(0);
				
				
				if(!(identifier==(AddOrderToBook.identifier) && (inputline[1].length()==1)))
						throw new Exception("Incorrect identifier "+inputline[1]+" instead of "+AddOrderToBook.identifier);
				
				AddOrderToBook AddOrderToBookObj;
				currentTimeStamp = timeStamp;
				Pricer.currentTimeStamp = timeStamp;
				
				if(side==(Bid.side))
				{
					AddOrderToBookObj 		= new Bid(timeStamp, orderId, price, size);
					
						bidsByPrice.put(orderId, (Bid) AddOrderToBookObj);
						
						
					if(currentSelling.getValue().equals(SellOutput.DEFAULT_VALUE) && (bidShareCount(bidsByPrice) >= targetSize))
						TradeChangeInobj 	= TradeChangeIn.SELLING_INCOME;
			
					else if (!currentSelling.getValue().equals(SellOutput.DEFAULT_VALUE)){
						
						BigDecimal minPrice = getLowestPrice(currentSelling, bidsByPrice);
					
						if(AddOrderToBookObj.getPrice().compareTo( (minPrice))>0)
							TradeChangeInobj = TradeChangeIn.SELLING_INCOME;
					}
				}
				
				 if(side==(Ask.side))
				{
					AddOrderToBookObj 		= new Ask(timeStamp, orderId, price, size);
					
					asksByPrice.put(orderId, (Ask) AddOrderToBookObj);
					
					if(currentBuying.getValue().equals(BuyOutput.DEFAULT_VALUE) && (askShareCount(asksByPrice) >= targetSize))
						TradeChangeInobj 	= TradeChangeIn.BUYING_INCOME;
			
					else if (!currentBuying.getValue().equals(BuyOutput.DEFAULT_VALUE)){
						
						BigDecimal maxPrice = getMaximumPrice(currentBuying, asksByPrice);
					
						if(AddOrderToBookObj.getPrice().compareTo( (maxPrice))<0)
							TradeChangeInobj = TradeChangeIn.BUYING_INCOME;
					}

					
				}
								
				
			} catch (Exception e) { System.out.println(e);
				errorFile.add(" Error with AddOrderToBook line "+recordStr+" with exception "+e);
			}

			
		}
		
		else errorFile.add(" Length mismatch for the input line with AddOrderToBook line "+recordStr);

		
		return TradeChangeInobj;
		
	}

	private static Double askShareCount(Map<String, Ask> asksByPrice) {

		Double count = (double) 0;
		
		for(String orderid: asksByPrice.keySet()){
			count	+=(asksByPrice.get(orderid).getSize());
		}	
		return count;
	}



	private static Double bidShareCount(Map<String, Bid> bidsByPrice) {

		Double count = (double) 0;
		
		for(String orderid: bidsByPrice.keySet()){
			count	+=bidsByPrice.get(orderid).getSize();
		}
		
		return count;
	}



	private static BigDecimal getMaximumPrice(BuyOutput currentBuying,
			Map<String, Ask> asksByPrice) {
	
		BigDecimal maxPrice = new BigDecimal(0);
		ArrayList<BigDecimal> pricesShares = new ArrayList<BigDecimal>();
		
		for(String orderid : currentBuying.getSharesUsed().keySet()){
			pricesShares.add(asksByPrice.get(orderid).getPrice());
		}
		
		if(pricesShares.isEmpty())
			return maxPrice;
		
		else maxPrice = Collections.max(pricesShares);
		
		return maxPrice;

	}



	private static BigDecimal getLowestPrice(SellOutput currentSelling, Map<String, Bid> bidsByPrice) {
		// TODO Auto-generated method stub
		
		BigDecimal minPrice = new BigDecimal(0);;
		ArrayList<BigDecimal> pricesShares = new ArrayList<BigDecimal>();
		
		for(String orderid : currentSelling.getSharesUsed().keySet()){
			pricesShares.add(bidsByPrice.get(orderid).getPrice());
		}
		
		if(pricesShares.isEmpty())
			return minPrice;
		
		else minPrice = Collections.min(pricesShares);
		
		return minPrice;
	}


	public static List<String> readList( String inputFileName, boolean hasHeader ) 
			throws IOException{

		List<String> recordList = new LinkedList<String>();
		File inputFile = new File( inputFileName );
		BufferedReader br = new BufferedReader( new FileReader( inputFile ));

		if( !inputFile.exists() || !inputFile.canRead()){
			err.printf( "Error: Cannot read file %s", inputFileName );
		}

		else{

			String newRecord = null;

			if( hasHeader )
				newRecord = br.readLine();

			while( ( ( newRecord = br.readLine() ) != null ))
				recordList.add( newRecord );

			br.close();

		}

		return recordList;
	}

	public static void writeList( String outputFileName,
			List<String> recordList ) throws IOException{
		// Writes a list of records in a file
		File outputFile = new File( outputFileName );
		BufferedWriter bw = new BufferedWriter( new FileWriter( outputFile ));
		for( String record : recordList ){
			bw.write( record );
			bw.newLine();
		}
		bw.close();
	}


	public Map<String, Bid> getBidsByPrice() {
		return bidsByPrice;
	}


	public void setBidsByPrice(Map<String, Bid> bidsByPrice) {
		this.bidsByPrice = bidsByPrice;
	}


	public Map<String, Ask> getAsksByPrice() {
		return asksByPrice;
	}


	public void setAsksByPrice(Map<String, Ask> asksByPrice) {
		this.asksByPrice = asksByPrice;
	}


	public SellOutput getCurrentSelling() {
		return currentSelling;
	}


	public void setCurrentSelling(SellOutput currentSelling) {
		this.currentSelling = currentSelling;
	}


	public BuyOutput getCurrentBuying() {
		return currentBuying;
	}


	public void setCurrentBuying(BuyOutput currentBuying) {
		this.currentBuying = currentBuying;
	}

	
	//***********************main function
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		String fileName = args[0];
		Double targetsize = new Double(args[1]);
		
		Pricer pricerObj = new Pricer(fileName, targetsize);

	}

}
