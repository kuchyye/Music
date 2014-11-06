package com.example.musicplayer;



import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.Toast;

public class MusicService extends Service {
    public static final int COMMAND_UNKNOWN = -1;
    public static final int COMMAND_PLAY = 0;
    public static final int COMMAND_PAUSE = 1;
    public static final int COMMAND_STOP = 2;
    public static final int COMMAND_RESUME = 3;
    public static final int COMMAND_PREVIOUS = 4;
    public static final int COMMAND_NEXT = 5;
    public static final int COMMAND_CHECK_IS_PLAYING = 6;
    public static final int COMMAND_SEEK_TO = 7;
    
    public static final int STATUS_PLAYING = 0;
    public static final int STATUS_PAUSED = 1;
    public static final int STATUS_STOPPED = 2;
    public static final int STATUS_COMPLETED = 3;
    
    public static final String BROADCAST_MUSICSERVICE_CONTROL = "MusicService.ACTION_CONTROL";
    public static final String BROADCAST_MUSICSERVICE_UPDATE_STATUS = "MusicService.ACTION_UPDATE";
    
    private CommandReceiver receiver;

    @Override
    public IBinder onBind(Intent intent) {
    	// TODO Auto-generated method stub
    	return null;
    }
    
	public void onCreate() {
		super.onCreate();
		bindCommandReceiver();
	};
	public void onDestroy() {
		if(player != null){
			player.release();
		}
		super.onDestroy();
	};
	
	private void bindCommandReceiver() {
		receiver = new CommandReceiver();
		IntentFilter filter = new IntentFilter(BROADCAST_MUSICSERVICE_CONTROL);
		registerReceiver(receiver, filter);
	}
	
	class CommandReceiver extends BroadcastReceiver{
		public void  onReceive(Context context,Intent intent) {
			int commend = intent.getIntExtra("command",COMMAND_UNKNOWN);
			switch (commend) {
			case COMMAND_SEEK_TO:
				seekTo(intent.getIntExtra("time", 0));
				break;
			case COMMAND_PLAY:
			case COMMAND_PREVIOUS:
			case COMMAND_NEXT:
				int number = intent.getIntExtra("number", 1);
				Toast.makeText(MusicService.this, "正在播放第"+number+"首",Toast.LENGTH_LONG).show();
				play(number);
				break;
			case COMMAND_PAUSE:
				pause();
				break;
			case COMMAND_STOP:
				stop();
				break;
			case COMMAND_RESUME:
				resume();
				break;
			case COMMAND_CHECK_IS_PLAYING:
				if (player.isPlaying()) {
					sendBroadcastOnStatusChanged(MusicService.STATUS_PLAYING);
				}
				break;
			case COMMAND_UNKNOWN:
			default:
				break;
			}		
		}
	}
	
    private void sendBroadcastOnStatusChanged(int status) {
		Intent intent = new Intent(BROADCAST_MUSICSERVICE_UPDATE_STATUS);
		intent.putExtra("status", status);
		if (status == STATUS_PLAYING) {
			intent.putExtra("time", player.getCurrentPosition());
			intent.putExtra("duration", player.getDuration());
		}
		sendBroadcast(intent);
	}

    private MediaPlayer player;
    
    private void load(int number)
    {
    	if(player != null)
    	{
    		player.release();
    	}
    	Uri musicUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,""+number );
    	player = MediaPlayer.create(this, musicUri);
    	player.setOnCompletionListener(completionListener);
    }
    
    OnCompletionListener completionListener = new OnCompletionListener() {	
		@Override
		public void onCompletion(MediaPlayer mp) {
			// TODO Auto-generated method stub
			if(mp.isLooping()){
				replay();
			}else{
				sendBroadcastOnStatusChanged(MusicService.STATUS_COMPLETED);
			}
			
		}
	};
	
    private void play(int number)
    {
    	if(player != null && player.isPlaying())
    	{
    		player.stop();
    	}
    	load(number);
    	player.start();
    	sendBroadcastOnStatusChanged(MusicService.STATUS_PLAYING);
    }
    
    private void pause()
    {
    	if(player.isPlaying()){
    		player.pause();
    		sendBroadcastOnStatusChanged(MusicService.STATUS_PAUSED);
    	}
    }
    
    private void stop()
    {
    	if(player != null)
    	{
    		player.stop();
    		sendBroadcastOnStatusChanged(MusicService.STATUS_STOPPED);
    	}
    }
    
    private void resume()
    {
		player.start();
		sendBroadcastOnStatusChanged(MusicService.STATUS_PLAYING);
	}
    
    private void replay()
    {
		player.start();
		sendBroadcastOnStatusChanged(MusicService.STATUS_PLAYING);
	}
    
    private void seekTo(int time){
    	if(player != null)
    	{
    		player.seekTo(time);
    	}
    }
}
