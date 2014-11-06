package com.example.musicplayer;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    
	private ImageButton imgBtn_Previous;
	private ImageButton imgBtn_PlayOrPause;
	private ImageButton imgBtn_Stop;
	private ImageButton imgBtn_Next;
	private ListView list;
	private SeekBar seekBar;
	private TextView text_Current;
	private TextView text_Duraton;
	private RelativeLayout root_Layout;
	private int number;
	private StatusChangedReceiver receiver;
    private Handler seekBarHandler;
    private int duration;
    private int time;
    private static final int PROGRESS_INCREASE = 0;
    private static final int PROGRESS_PAUSE = 1;
    private static final int PROGRESS_RESET = 2;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        registerListeners();
        number = 1;
        status = MusicService.STATUS_STOPPED;
        duration = 0 ;
        time = 0;
        startService(new Intent(this,MusicService.class));
        bindStatusChangedReceiver();
        sendBroadcastOnCommand(MusicService.COMMAND_CHECK_IS_PLAYING);
        initSeekBarHandler();
    }
    
    private void bindStatusChangedReceiver() {
		receiver = new StatusChangedReceiver();
		IntentFilter filter = new IntentFilter(MusicService.BROADCAST_MUSICSERVICE_UPDATE_STATUS);
		registerReceiver(receiver, filter);
	}

    private void findViews(){
    	imgBtn_Previous = (ImageButton)findViewById(R.id.imageButton1);
    	imgBtn_PlayOrPause = (ImageButton)findViewById(R.id.imageButton2);
    	imgBtn_Stop = (ImageButton)findViewById(R.id.imageButton3);
    	imgBtn_Next = (ImageButton)findViewById(R.id.imageButton4);
    	list = (ListView)findViewById(R.id.listView1);
    	seekBar  = (SeekBar)findViewById(R.id.seekBar1);
    	text_Current = (TextView)findViewById(R.id.textView1);
    	text_Duraton = (TextView)findViewById(R.id.textView2);
    	root_Layout = (RelativeLayout)findViewById(R.id.relativeLayout);
    }
    
    private void registerListeners(){
    	imgBtn_Previous.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View arg0) {
    			// TODO Auto-generated method stub
    			sendBroadcastOnCommand(MusicService.COMMAND_PREVIOUS);
    		}
    	});
    	imgBtn_PlayOrPause.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(isPlaying())
				{
					sendBroadcastOnCommand(MusicService.COMMAND_PAUSE);
				}else if(isPaused())
				{
					sendBroadcastOnCommand(MusicService.COMMAND_RESUME);
				}else if(isStopped())
				{
					sendBroadcastOnCommand(MusicService.COMMAND_PLAY);
				}
				
			}
		});
    	imgBtn_Stop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sendBroadcastOnCommand(MusicService.COMMAND_STOP);
			}
		});
    	imgBtn_Next.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sendBroadcastOnCommand(MusicService.COMMAND_NEXT);
			}
		});
    	list.setOnItemClickListener(new OnItemClickListener() {
    		@Override
    		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
    				long arg3) {
    			// TODO Auto-generated method stub
    			number = arg2 +1;
    			sendBroadcastOnCommand(MusicService.COMMAND_PLAY);
    		}
		});
    	seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
    		@Override
    		public void onStopTrackingTouch(SeekBar arg0) {
    			// TODO Auto-generated method stub
    			sendBroadcastOnCommand(MusicService.COMMAND_SEEK_TO);
    			if (isPlaying()) {
					seekBarHandler.sendEmptyMessageDelayed(PROGRESS_INCREASE, 1000);
				}
    		}
    		@Override
    		public void onStartTrackingTouch(SeekBar arg0) {
    			// TODO Auto-generated method stub
    			seekBarHandler.sendEmptyMessage(PROGRESS_PAUSE);
    		}
    		@Override
    		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
    			// TODO Auto-generated method stub
    			time = arg1;
    			text_Current.setText(formadTime(time));
    		}
    	});
    }
    
    class StatusChangedReceiver extends BroadcastReceiver{
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		// TODO Auto-generated method stub
    		status = intent.getIntExtra("status", -1);
    		switch (status) {
			case MusicService.STATUS_PLAYING:
				time = intent.getIntExtra("time", 0);
				duration = intent.getIntExtra("duration", 0);
				seekBarHandler.removeMessages(PROGRESS_INCREASE);
				seekBarHandler.sendEmptyMessageDelayed(PROGRESS_INCREASE, 1000);
				seekBar.setMax(duration);
				seekBar.setProgress(time);
				text_Duraton.setText(formadTime(duration));
				imgBtn_PlayOrPause.setBackgroundResource(R.drawable.pause);	
				
				Cursor cursor = MainActivity.this.getMusicCursor();
				cursor.moveToPosition(number-1);
				String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.
						AudioColumns.TITLE));
				MainActivity.this.setTitle("正在播放："+title+"- MusicPlayer");
				break;
			case MusicService.STATUS_PAUSED:
				seekBarHandler.sendEmptyMessage(PROGRESS_PAUSE);
				imgBtn_PlayOrPause.setBackgroundResource(R.drawable.play);
				break;
			case MusicService.STATUS_STOPPED:
				seekBarHandler.sendEmptyMessage(PROGRESS_RESET);
				imgBtn_PlayOrPause.setBackgroundResource(R.drawable.play);
				break;
			case MusicService.STATUS_COMPLETED:
				sendBroadcastOnCommand(MusicService.COMMAND_NEXT);
				seekBarHandler.sendEmptyMessage(PROGRESS_RESET);
				imgBtn_PlayOrPause.setBackgroundResource(R.drawable.play);
				break;
			default:
				break;
			}
    	}
    }
    
    
    private void moveNumberToNext()
    {
    	if((number + 1 ) > list.getCount())
    	{
    		number = 1;
    		Toast.makeText(this, this.getString(R.string.tip_reach_bottom), 
    				Toast.LENGTH_LONG).show();
    	}else {
			++number;
		}
    }
    
    private void moveNumberToPrevious() 
    {
		if(number == 1)
		{
			number = list.getCount();
			Toast.makeText(this, this.getString(R.string.tip_reach_top), 
					Toast.LENGTH_LONG).show();
		}else {
			--number;
		}
	}
      
    private void initSeekBarHandler(){
    	seekBarHandler = new Handler(){
    		public void handleMessage(Message msg){
    			super.handleMessage(msg);
    			
    			switch (msg.what) {
				case PROGRESS_INCREASE:
					if (seekBar.getProgress() < duration) {
						seekBar.incrementProgressBy(1000);
						seekBarHandler.sendEmptyMessageDelayed(PROGRESS_INCREASE, 1000);
						text_Current.setText(formadTime(time));
						time += 1000;
					}
					break;
				case PROGRESS_PAUSE:
					seekBarHandler.removeMessages(PROGRESS_INCREASE);
					break;
				case PROGRESS_RESET:
					seekBarHandler.removeMessages(PROGRESS_INCREASE);
					seekBar.setProgress(0);
					text_Current.setText("00:00");
				default:
					break;
				}
    		}
    	};
    }
    
    private void sendBroadcastOnCommand(int command){
    	Intent intent = new Intent(MusicService.BROADCAST_MUSICSERVICE_CONTROL);
    	intent.putExtra("command", command);
    	switch (command) {
		case MusicService.COMMAND_PLAY:
			intent.putExtra("number", number);
			break;
		case MusicService.COMMAND_PREVIOUS:
			moveNumberToPrevious();
			intent.putExtra("number", number);
			break;
		case MusicService.COMMAND_NEXT:
			moveNumberToNext();
			intent.putExtra("number", number);
			break;
		case MusicService.COMMAND_SEEK_TO:
			intent.putExtra("time", time);
			break;
		case MusicService.COMMAND_PAUSE:		
		case MusicService.COMMAND_STOP:
		case MusicService.COMMAND_RESUME:
		default:
			break;
		}
    	sendBroadcast(intent);
    }
    
    private int status;
    
    private boolean isPlaying(){
    	return status == MusicService.STATUS_PLAYING;
    }
    
    private boolean isStopped(){
    	return status == MusicService.STATUS_STOPPED;
    }
    
    private boolean isPaused(){
    	return status == MusicService.STATUS_PAUSED;
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	initMusicList();
    	if(list.getCount() == 0)
    	{
    		imgBtn_Next.setEnabled(false);
    		imgBtn_PlayOrPause.setEnabled(false);
    		imgBtn_Previous.setEnabled(false);
    		imgBtn_Stop.setEnabled(false);
    		Toast.makeText(this, this.getString(R.string.tip_no_music_file), Toast.LENGTH_LONG).show();
    	}else{
    		imgBtn_Next.setEnabled(true);
    		imgBtn_PlayOrPause.setEnabled(true);
    		imgBtn_Previous.setEnabled(true);
    		imgBtn_Stop.setEnabled(true);
    	}
    	PropretyBean proprety = new PropretyBean(MainActivity.this);
    	String  theme = proprety.getTheme();
    	setTheme(theme);
    }
    private void initMusicList(){
    	Cursor cursor = getMusicCursor();
    	setListContent(cursor);
    }
    private void setListContent(Cursor musicCursor){
    	CursorAdapter adapter = new SimpleCursorAdapter(this,android.R.layout.simple_expandable_list_item_2,musicCursor, new String[] {MediaStore.Audio.AudioColumns.TITLE,MediaStore.Audio.AudioColumns.ARTIST}, new int []{android.R.id.text1,android.R.id.text2});
              list.setAdapter(adapter);
    }

    private Cursor getMusicCursor(){
    	ContentResolver resolver = getContentResolver();
    	Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
    	return cursor;
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	if (isStopped()) {
			stopService(new Intent(this,MusicService.class));
		}
    	super.onDestroy();
    }
    
    private String formadTime(int msec){
    	int minute = (msec / 1000) / 60;
    	int second = (msec / 1000) % 60;
    	String minuteString;
    	String secondSrting;
    	if (minute < 10) {
			minuteString = "0" + minute;
		}else{
			minuteString = "" + minute;
		}
    	if (second < 10) {
    		secondSrting = "0" + second;	
		}else {
			secondSrting = "" + second;
		}
    	return minuteString + ":" + secondSrting;
     }
    
     public static final int MENU_THEME = Menu.FIRST;
     public static final int MENU_ABOUT = Menu.FIRST+1;
     


     @Override
     public boolean onCreateOptionsMenu(Menu menu){
    	 menu.add(0,MENU_THEME,0,"主题");
    	 menu.add(0,MENU_ABOUT,1,"关于");
    	 return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
    	// TODO Auto-generated method stub
    	 switch (item.getItemId()) {
		case MENU_THEME:
			new AlertDialog.Builder(this).setTitle("请选择主题").setItems(
					R.array.theme, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							String theme = PropretyBean.THEMES[which];
							setTheme(theme);
							PropretyBean proprety = new PropretyBean(MainActivity.this);
							proprety.setAndSaveTheme(theme);
							
						}
					}).show();
			break;
		case MENU_ABOUT:
			new AlertDialog.Builder(MainActivity.this).setTitle("音乐播放器").
			          setMessage(MainActivity.this.getString(R.string.about)).show();
			break;
		default:
			break;
		}
    	return super.onOptionsItemSelected(item);
     }
     
     private  void setTheme(String theme){
    	 if("简约".equals(theme)){
    		 root_Layout.setBackgroundResource(R.drawable.bg_color);
    	 }else if ("花朵".equals(theme)) {
    		 root_Layout.setBackgroundResource(R.drawable.bg_flow);
    	 }else if ("青藤".equals(theme)) {
    		 root_Layout.setBackgroundResource(R.drawable.bg_tree);
    	 }else if ("古典".equals(theme)) {
    		 root_Layout.setBackgroundResource(R.drawable.bg_sim);
    	 }
     } 
     
    
}
