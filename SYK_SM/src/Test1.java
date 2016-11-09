import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Test1 {

	public static void main(String args[]) throws Exception {
		System.out.println("hello");

		String str = "{\"transactions\":[{\"id\":\"10001\",\"accountId\":\"222333\",\"postDate\":\"2014-08-12\",\"merchantName\":\"Starbucks\",\"category\":\"Dining\",\"type\":\"DEBIT\",\"amount\":\"10.32\"},{\"id\":\"10002\",\"accountId\":\"222333\",\"postDate\":\"2014-07-29\",\"merchantName\":\"Dennys\",\"category\":\"Dining\",\"type\":\"DEBIT\",\"amount\":\"32.12\"},{\"id\":\"10003\",\"accountId\":\"222333\",\"postDate\":\"2014-08-05\",\"merchantName\":\"Home Depot\",\"category\":\"Home Improvement\",\"type\":\"DEBIT\",\"amount\":\"344.93\"},{\"id\":\"10004\",\"accountId\":\"222333\",\"postDate\":\"2014-08-10\",\"merchantName\":\"Online Payment\",\"category\":\"Payment/Credit\",\"type\":\"CREDIT\",\"amount\":\"200.00\"},{\"id\":\"10005\",\"accountId\":\"222333\",\"postDate\":\"2014-09-01\",\"merchantName\":\"Subway\",\"category\":\"Dining\",\"type\":\"DEBIT\",\"amount\":\"5.58\"}]}";
		System.out.println(getBalanceForCategory(str, "DiniNG"));
	}

	public static final String DEBIT_TYPE = "DEBIT";
	public static final String CREDIT_TYPE = "CREDIT";

	public static BigDecimal getBalanceForCategory(String inputJSON, String category) throws Exception {
		if (inputJSON != null) {
			BigDecimal balance = new BigDecimal(0.0);
			BigDecimal totalBalance = new BigDecimal(0.0);
			JSONParser parser = new JSONParser();
			HashMap<String, BigDecimal> categorySumMap = new HashMap<String, BigDecimal>();

			try {
				JSONObject jsonObject = (JSONObject) parser.parse(inputJSON);
				List<?> txns = (List<?>) jsonObject.get("transactions");
				if (txns != null && txns.size() > 0) {
					for (Object txn : txns) {
						JSONObject innerObject = (JSONObject) parser.parse(txn.toString());
						String categoryLower = innerObject.get("category").toString().toLowerCase();
						BigDecimal catBalance = categorySumMap.get(categoryLower);

						if (catBalance == null) {
							catBalance = new BigDecimal(0.0);
						}
						
						BigDecimal amount  = new BigDecimal(innerObject.get("amount").toString());
						if (innerObject.get("type").equals(DEBIT_TYPE)) {
							catBalance = catBalance.add(amount);
							totalBalance = totalBalance.add(amount);
						} else if (innerObject.get("type").equals(CREDIT_TYPE)) {
							catBalance = catBalance.subtract(amount);
							totalBalance = totalBalance.subtract(amount);
						} else {
							throw new Exception("Invalid Type");
						}

						categorySumMap.put(categoryLower, catBalance);
					}
				}

				if (category == null) {
					balance = totalBalance;
				} else if(categorySumMap.get(category.toLowerCase()) != null) {
					balance= categorySumMap.get(category.toLowerCase());
				}

				return balance;
			} catch (ParseException exp) {
				return null;
			}

		} else {
			return null;
		}

	}
}