package com.bot.Enum;

public enum MultiKeyMap {
	早餐("food"),午餐("food"),晚餐("food"),下午茶("food"),宵夜("food"),點心("food"),
	MOMO你說("momo你說");
	
	private String trueKey;
	
	private MultiKeyMap(String trueKey){
		this.trueKey = trueKey;
	}
	
	public static String getTrueKey(String key) {
        for (MultiKeyMap multiKeyMap : MultiKeyMap.values()) {
            if (multiKeyMap.toString().equalsIgnoreCase(key)) {
                return multiKeyMap.trueKey;
            }
        }
        return null;
    }
	
}
