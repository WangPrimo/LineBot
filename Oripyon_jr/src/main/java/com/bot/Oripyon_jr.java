package com.bot;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

import com.bot.Enum.CommandHelp;
import com.bot.Enum.MultiKeyMap;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineMessagingService;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

@SpringBootApplication
@LineMessageHandler
public class Oripyon_jr {
	
	private final String Change_Line = "\r\n";
	
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
	
	Random random = new Random();
	BigDecimal hundred = new BigDecimal(100);
	
	
    public static void main(String[] args) {
        SpringApplication.run(Oripyon_jr.class, args);
    }

    @EventMapping
    public Message handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
    	String command = event.getMessage().getText();
    	String senderId = event.getSource().getUserId();
    	Message message = null;
    	
    	message = replyString(command, senderId);
        if(message != null){
        	return message;
        }
        message = getCommandHelp(command);
        if(message != null){
        	return message;
        }
        
        return null;
    }
    
    @EventMapping
    public Message handlePostbackEvent(PostbackEvent event){
    	String command = event.getPostbackContent().getData();
    	System.out.println(command);
    	
    	Message message = null;
    	message = getCommandHelp(command);
        if(message != null){
        	return message;
        }
        
    	return null;
    }

    private TextMessage replyString(String command, String senderId){
    	try {
			if(command.startsWith("!") || command.startsWith("！")){
				String key = command.split(" ")[0].substring(1);
				String target = command.substring(key.length() + 1);
	
				UserProfileResponse sender = null;
				if(!StringUtils.isEmpty(senderId)){
					 sender = lineMessagingService.getProfile(senderId).execute().body();
				}
				
				//有sender使用sender，否則使用草泥馬作為指令中發起動作的人
				if(binaryCommand.get(key) != null && !StringUtils.isEmpty(target)){
					String senderName = sender == null ? "草泥馬先生" : sender.getDisplayName();
					return new TextMessage(binaryCommand.get(key).replace("@{}", target).replace("{}", senderName));
				}
				//有sender資料才使用unaryCommand
				if(unaryCommand.get(key) != null && sender != null){
					return new TextMessage(unaryCommand.get(key).replace("{}", sender.getDisplayName()));
				}
	
				//key取不到value則檢查是否為multikey
				if(randomArrayCommand.get(key) == null){
					key = MultiKeyMap.getTrueKey(key);
				}
				String[] randomArray =  randomArrayCommand.get(key);
				if(randomArray != null){
					return new TextMessage(probabilityControl(randomArray));
				}
			}
			if(command.equalsIgnoreCase("/roll")){
				int score = random.nextInt(100) + 1;
				return new TextMessage("你擲出了" + score + "點(1-100)");
			}
        }catch(IOException e){
        	e.printStackTrace();
        }
        
        return null;
    }
    
    //檢查RandomArray是否有機率設定並作相關處理
    private String probabilityControl(String[] randomArray){
    	//統計有設定機率的選項
    	DoubleSummaryStatistics statistics = Arrays.asList(randomArray).stream().filter(v -> v.split("%=").length > 1).mapToDouble(v -> Double.valueOf(v.split("%=")[1])).summaryStatistics();
    	double probabilityCount = new BigDecimal(statistics.getSum()).setScale(2, BigDecimal.ROUND_DOWN).doubleValue();
    	int withoutProbability = randomArray.length - (int)statistics.getCount();
    	
    	
    	//機率總和大於100或等於0，直接隨機輸出陣列內容
    	if(probabilityCount > 100 || probabilityCount == 0){
    		return randomArray[random.nextInt(randomArray.length)].split("%=")[0].trim();
    	}
    	
    	//未設定機率之內容的出現機率 ＝ 100 - 有設定機率總和 / 未設定機率個數
    	//算出之機率再乘以100將scope拉大為1-10000
    	int generalProbability = hundred.subtract(new BigDecimal(probabilityCount)).divide(new BigDecimal(withoutProbability), 2, BigDecimal.ROUND_HALF_UP).multiply(hundred).intValue(); 
    	
    	int scopeSeed = random.nextInt(10000) + 1;
    	int scope = 0;
    	
    	//將每一個內容各自的scope疊加直到值大於seed便輸出該筆內容
    	for(String stringValue:randomArray){
    		scope += stringValue.split("%=").length > 1 ?
    			new BigDecimal(stringValue.split("%=")[1]).setScale(2, BigDecimal.ROUND_DOWN).multiply(hundred).intValue():
    			generalProbability;
    			
    		if(scope >= scopeSeed){
    			return stringValue.split("%=")[0].trim();
    		}
    	}
    	
    	return randomArray[random.nextInt(randomArray.length)].split("%=")[0].trim();
    }
    
    @SuppressWarnings("unchecked")
	private Message getCommandHelp(String command){
    	try {
    		System.out.println("In CommandHelp!");
	    	String[] callCommandHelp = {"command", "指令"};

	    	if(command.startsWith("!") || command.startsWith("！")){
	    		String key = command.split(" ")[0].substring(1);
				String target = command.substring(key.length() + 1);
				System.out.println("key = " + key);
				System.out.println("target = " + target);
	    		
				if(Arrays.asList(callCommandHelp).contains(key)){
					System.out.println("StringUtils.isEmpty(target) = " + StringUtils.isEmpty(target));
					if(StringUtils.isEmpty(target)){
						List<Action> actions = new ArrayList<>();
			        	for(CommandHelp commandHelp:CommandHelp.values()){
			        		String actionCommand = "!command ".concat(commandHelp.name());
			        		actions.add(new PostbackAction(commandHelp.chineseCommand, actionCommand, "說說關於" + commandHelp.chineseCommand + "的內容吧 (ﾟ∀ﾟ )"));
			        	}
			        	
			        	String imgpath = null;
			        	String title = "草泥馬指令助手";
			        	String text = "想知道什麼呢（·´ｪ`·）?";
			        	ButtonsTemplate buttonsTemplate = new ButtonsTemplate(imgpath, title, text, actions);
			        	
			        	return new TemplateMessage(title, buttonsTemplate);
					}else{
						CommandHelp commandHelp = CommandHelp.getCommandHelp(target);
						System.out.println("commandHelp != null = " + commandHelp != null);
						if(commandHelp != null){
							Field field= this.getClass().getField(commandHelp.name());
							StringBuffer sb = new StringBuffer();
							sb.append(commandHelp.chineseCommand + Change_Line);
							sb.append(commandHelp.description + Change_Line);
							for(String commandKey:((HashMap<String, Object>)field.get(this)).keySet()){
								sb.append(commandKey + Change_Line);
							}
							return new TextMessage(sb.toString());
						}
					}
				}
	    	}
    	} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
    	return null;
    }
	
}
