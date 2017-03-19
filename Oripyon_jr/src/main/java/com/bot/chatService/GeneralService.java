package com.bot.chatService;

import java.util.Random;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;

public class GeneralService {
	int seed;
	Random random = new Random();
	String[] noodle = {"廢物","垃圾","蘿莉控","意淫業務的變態","處男","嫩","頂新"};
	
	
	public String messageProcess(MessageEvent<TextMessageContent> event){
		String message = event.getMessage().getText();

		if(message.contains("陳彥霖")){
        	seed = random.nextInt(noodle.length);
        	message = noodle[seed];//"廢物";
        }else{
        	message = null;
        }
		
		return message;
	}
	
}
