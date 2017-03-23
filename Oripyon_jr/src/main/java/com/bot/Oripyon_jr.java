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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineMessagingClient;
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
	private LineMessagingClient lineMessagingClient;
	
	int seed;
	Random random = new Random();
	String[] noodle = {"廢物","垃圾","蘿莉控","意淫業務的變態","處男","嫩","ㄧ"};
	
	HashMap<String, String> binaryCommand;
	HashMap<String, String> unaryCommand;
	String returnMessage;
	
	
    public static void main(String[] args) {
        SpringApplication.run(Oripyon_jr.class, args);
    }

    @EventMapping
    public TextMessage handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
        jsonParser();
        
        String message = event.getMessage().getText();
        System.out.println(message);
        System.out.println("sender : " + event.getSource().getSenderId());
        System.out.println("user : " + event.getSource().getUserId());
        if(lineMessagingClient == null){
        	System.out.println("in");
        }
        CompletableFuture<UserProfileResponse> sender = lineMessagingClient.getProfile(event.getSource().getSenderId());
        System.out.println(sender);
        
        
        if(message.startsWith("!")){
        	String key = message.split(" ")[0].substring(1);
        	String target = message.substring(key.length() + 1);
		
        	if(binaryCommand.get(key) != null && !StringUtils.isEmpty(target)){
        		sender.whenComplete((profile, throwable) -> {
        			returnMessage = binaryCommand.get(key).replace("{}", profile.getDisplayName()).replace("{}", target);
                });
        		
        	}
        	if(unaryCommand.get(key) != null){
        		sender.whenComplete((profile, throwable) -> {
        			returnMessage = unaryCommand.get(key).replace("{}", profile.getDisplayName());
                });
        	}
        }
        
        if(message.contains("陳彥霖")){
        	seed = random.nextInt(noodle.length);
        	returnMessage = noodle[seed];
        }
        
        return new TextMessage(returnMessage);
    }

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        System.out.println("event: " + event);
    }
    
    private void jsonParser(){
    	try {
    		TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String,String>>(){};
        	ObjectMapper mapper = new ObjectMapper();
    		
		binaryCommand = mapper.readValue(getClass().getResourceAsStream("/command/binary.json"),typeRef);
		unaryCommand = mapper.readValue(getClass().getResourceAsStream("/command/unary.json"),typeRef);
			
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
