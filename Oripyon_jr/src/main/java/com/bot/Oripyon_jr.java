/*
 * Copyright 2016 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.bot;

import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineMessagingService;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

@SpringBootApplication
@LineMessageHandler
public class Oripyon_jr {
	@Autowired
	private LineMessagingService lineMessagingService;
	
	int seed;
	Random random = new Random();
	String[] noodle = {"廢物","垃圾","蘿莉控","意淫業務的變態","處男","嫩","頂新"};
	ArrayList<String> scoreCards;
	HashMap<String, String> binaryCommand;
	HashMap<String, String> unaryCommand;
	
	
    public static void main(String[] args) {
        SpringApplication.run(Oripyon_jr.class, args);
    }

    @EventMapping
    public TextMessage handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
    	
        jsonParser();
        
        String returnMessage = replyString(event);
        
        if(StringUtils.isEmpty(returnMessage)){
        	return null;
        }else{
        	return new TextMessage(returnMessage);
        }
        
    }

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        System.out.println("event: " + event);
    }
    
    private void jsonParser(){
    	try {
    		TypeReference<HashMap<String, String>> map = new TypeReference<HashMap<String,String>>(){};
		TypeReference<ArrayList<String>> array = new TypeReference<ArrayList<String>>(){};
        	ObjectMapper mapper = new ObjectMapper();
    		
		binaryCommand = mapper.readValue(getClass().getResourceAsStream("/command/binary.json"),map);
		unaryCommand = mapper.readValue(getClass().getResourceAsStream("/command/unary.json"),map);
		scoreCards = mapper.readValue(getClass().getResourceAsStream("/command/scoreCard.json"),array);
	} catch (JsonParseException e) {
		e.printStackTrace();
	} catch (JsonMappingException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
    }
    
    private String replyString(MessageEvent<TextMessageContent> event){
    	String message = event.getMessage().getText();
        
        try {
		//UserProfileResponse sender = lineMessagingService.getProfile(event.getSource().getSenderId()).execute().body();
		if(message.startsWith("!")){
			String key = message.split(" ")[0].substring(1);
			String target = message.substring(key.length() + 1);

			//if(binaryCommand.get(key) != null && !StringUtils.isEmpty(target)){
			//	return binaryCommand.get(key).replace("@{}", target).replace("{}", sender.getDisplayName());
			//}
			//if(unaryCommand.get(key) != null){
			//	return unaryCommand.get(key).replace("{}", sender.getDisplayName());
			//}
			if("jolin".equals(key)){
				seed = random.nextInt(scoreCards.size());
        			return scoreCards.get(seed);
			}
		}
	} catch (IOException e) {
		e.printStackTrace();
	}
        
        if(message.contains("陳彥霖")){
        	seed = random.nextInt(noodle.length);
        	return noodle[seed];
        }
        return null;
    }
}
