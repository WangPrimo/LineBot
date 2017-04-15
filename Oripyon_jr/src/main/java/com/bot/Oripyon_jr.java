package com.bot;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

import com.bot.Enum.MultiKeyMap;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineMessagingService;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

@SpringBootApplication
@LineMessageHandler
public class Oripyon_jr {
	
	static HashMap<String, String[]> randomArrayCommand;
	static HashMap<String, String> binaryCommand;
	static HashMap<String, String> unaryCommand;
	
	static{
		try {
			TypeReference<HashMap<String, String>> typeMapString = new TypeReference<HashMap<String,String>>(){};
    		TypeReference<HashMap<String, String[]>> typeMapArray = new TypeReference<HashMap<String, String[]>>(){};
			ObjectMapper mapper = new ObjectMapper();
	    		
			binaryCommand = mapper.readValue(Oripyon_jr.class.getResourceAsStream("/command/binary.json"), typeMapString);
			unaryCommand = mapper.readValue(Oripyon_jr.class.getResourceAsStream("/command/unary.json"), typeMapString);
			randomArrayCommand = mapper.readValue(Oripyon_jr.class.getResourceAsStream("/command/randomArray.json"), typeMapArray);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Autowired
	private LineMessagingService lineMessagingService;
	
	int seed;
	Random random = new Random();
	
	
    public static void main(String[] args) {
        SpringApplication.run(Oripyon_jr.class, args);
    }

    @EventMapping
    public Message handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
    	
        String stringMessage = replyString(event);
        
        if(!StringUtils.isEmpty(stringMessage)){
        	return new TextMessage(stringMessage);
        }
        
        return null;
    }

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        System.out.println("event: " + event);
    }
    
    private String replyString(MessageEvent<TextMessageContent> event){
	String message = event.getMessage().getText();
        
        try {
			if(message.startsWith("!")){
				String key = message.split(" ")[0].substring(1);
				String target = message.substring(key.length() + 1);
	
				if(event.getSource().getUserId() != null){
					UserProfileResponse sender = lineMessagingService.getProfile(event.getSource().getUserId()).execute().body();
					if(binaryCommand.get(key) != null && !StringUtils.isEmpty(target)){
						return binaryCommand.get(key).replace("@{}", target).replace("{}", sender.getDisplayName());
					}
					if(unaryCommand.get(key) != null){
						return unaryCommand.get(key).replace("{}", sender.getDisplayName());
					}
				}
	
				//key取不到value則檢查是否為multikey
				if(randomArrayCommand.get(key) == null){
					key = MultiKeyMap.getTrueKey(key);
				}
				String[] randomArray =  randomArrayCommand.get(key);
				if(randomArray != null){
					randomArray = probabilityControl(randomArray);
					return randomArray[seed];
				}
			}
        } catch (IOException e) {
          e.printStackTrace();
        }
        
        return null;
    }
    
    //檢查RandomArray是否有機率設定並作相關處理
    private String[] probabilityControl(String[] randomArray){
    	int withoutProbability = randomArray.length;
    	BigDecimal hundred = new BigDecimal(100);
    	double probabilityCount = 0;
    	BigDecimal probability;
    	
    	//迴圈取得有設定機率的總和及未設定機率的個數
    	for(String stringValue:randomArray){
    		if(stringValue.split("%=").length > 1){
    			//吃進的機率參數只取到小數2位
    			probability = new BigDecimal(stringValue.split("%=")[1]).setScale(2, BigDecimal.ROUND_DOWN);
    			probabilityCount += probability.doubleValue();
    			withoutProbability --;
    		}
    	}
    	
    	//機率總和大於100，不使用該array中的設定
    	if(probabilityCount > 100){
    		for(int i=0;i<randomArray.length;i++){
    			//將機率設定的字串從內容中切除
        		randomArray[i] = randomArray[i].split("%=")[0].trim();
    		}
    		
    		seed = random.nextInt(randomArray.length);
    		return randomArray;
    	}
    	
    	//未設定機率之內容的出現機率 ＝ 100 - 有設定機率總和 / 未設定機率個數
    	//算出之機率再乘以100將scope拉大為1-10000
    	int generalProbability = hundred.subtract(new BigDecimal(probabilityCount)).divide(new BigDecimal(withoutProbability), 2, BigDecimal.ROUND_HALF_UP).multiply(hundred).intValue(); 
    	
    	int scopeSeed = random.nextInt(10000) + 1;
    	int scope = 0;
    	boolean checkDone = false;
    	
    	//將每一個內容各自的scope疊加直到值大於seed便輸出該Array index
    	for(int i=0;i<randomArray.length;i++){
    		String stringValue = randomArray[i];
    		
    		scope += stringValue.split("%=").length > 1 ?
    			new BigDecimal(stringValue.split("%=")[1]).setScale(2, BigDecimal.ROUND_DOWN).multiply(hundred).intValue():
    			generalProbability;
    		
    		//將機率設定的字串從內容中切除
    		randomArray[i] = stringValue.split("%=")[0].trim();
    		
    		if(scope >= scopeSeed && !checkDone){
    			System.out.println(scope);
    			System.out.println(scopeSeed);
    			System.out.println(i);
    			System.out.println(randomArray[i]);
    			seed = i;
    			checkDone = true;
    		}
    	}
    	
    	return randomArray;
    }
	
}
