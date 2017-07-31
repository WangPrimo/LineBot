package com.bot.Enum;

public enum CommandHelp {
	
	randomArrayCommand("隨機結果指令", "每個指令下有若干個結果，由系統隨機輸出"),
	binaryCommand("雙人指令", "在指令後以一個空白鍵做間隔後再接上目標，輸出含有發話者及目標兩個人的語句。若沒有授權機器人取用資料的權限則會以機器人取代發話者輸出結果"),
	unaryCommand("單人指令", "輸出以發話者為主角的一句話，需授權給機器人取用資料的權限否則無法使用");
	
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
