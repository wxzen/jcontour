package contour.utils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


public class CsvParser   {
	 
	public static List<Map<String, String>> parse(String paths) {
		List<Map<String, String>> retList = new ArrayList<>();
		Map<Integer,String> shema = new HashMap<>();
		try {
			CSVFormat formator = CSVFormat.DEFAULT;
			String encodeType = EncodeUtils.getEncode(paths, true);
			CSVParser parser = CSVParser.parse(new File(paths),Charset.forName(encodeType),formator);
			List<CSVRecord> records = parser.getRecords();
			boolean head=true;
			for (CSVRecord record : records) {
				if(head){
					int count = 0;
					Iterator<String> it = record.iterator();
					while(it.hasNext()){
						count++;
						shema.put(count, it.next());
					}
					head = false;
				}else{
					Map<String,String> dataMap = new HashMap<>();
					int count = 0;
					Iterator<String> it = record.iterator();
					while(it.hasNext()){
						count++;
						dataMap.put(shema.get(count), it.next());
					}
					retList.add(dataMap);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retList;
		
		        
	}

}
