package com.hungama.myplay.activity.data;

import java.util.List;
import java.util.Map;

import com.hungama.myplay.activity.services.ServerCommandsService;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class ServerCommandsManager {

	private static final String TAG = "ServerCommandsManager";
	
	public static final String CODE = "code";
	
	public static void sendServerCommands(Context context, List<Map<String, String>> serverCommands){
		
		if(serverCommands != null && !serverCommands.isEmpty()){
			
			for(Map<String, String> command : serverCommands){
					
				String commandCode = command.get(CODE);
				
				if(!TextUtils.isEmpty(commandCode)){
					
					// Start handling the server commands
					Intent intent = new Intent(context.getApplicationContext(), ServerCommandsService.class);
					intent.putExtra(ServerCommandsService.COMMAND, commandCode);
					context.startService(intent);
				}
			}
		}
	}
}
