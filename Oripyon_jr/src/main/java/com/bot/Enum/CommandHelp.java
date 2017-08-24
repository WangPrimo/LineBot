package com.bot.Enum;

public enum CommandHelp {
	
	randomArrayCommand("隨機結果指令", "每個指令下有若干個結果，由系統隨機輸出"),
	binaryCommand("目標指令", "在指令後以一個空白鍵做間隔後再接上目標，使你對目標執行一個動作。若沒有授權機器人取用資料的權限則則由草泥馬代勞"),
	unaryCommand("動作指令", "發話者自己執行指令的動作，需授權給機器人取用資料的權限才能使用");
	
	public String chineseCommand;
	public String discription;
	
	private CommandHelp(String chineseCommand, String discription){
		this.chineseCommand = chineseCommand;
		this.discription = discription;
		
	}
	
	public static CommandHelp getCommandHelp(String key) {
        for (CommandHelp commandHelp : CommandHelp.values()) {
            if (commandHelp.toString().equalsIgnoreCase(key)) {
                return commandHelp;
            }
        }
        return null;
    }
}
