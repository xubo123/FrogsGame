package com.hust.schoolmatechat;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.hust.schoolmatechat.engine.AppEngine;
import com.hust.schoolmatechat.engine.CYLog;

/**
 * Created by hongliang on 2014/7/31.
 */
public class SoundsActivity extends Activity {
	private static final String TAG = "SoundsActivity";
    private RadioButton btn1;
    private RadioButton btn2;
    private RadioButton btn3;
    private RadioButton btn4;
    private RadioGroup radioGroup;
    private Switch switch1;
    private Switch switch2;
    private Switch switch3;
    private Switch switch4;
    private boolean flag;
    private String kindOfVoice;
    private  int vioceNum;
    
//    GetDataFromConfigurationTable Test=new Configuration(SoundsActivity.this);
    ContentValues values = new ContentValues();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar bar=getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
//		bar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back));
		bar.setDisplayShowHomeEnabled(false);
        setContentView(R.layout.sounds_layout);
         inite();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.classical:
//                    	values.put("KindofVoice", 0);
//                    	flag=Test.updateConfiguration(values, "UID=?", new String[]{"1"});
                        flag=AppEngine.getInstance(getBaseContext()).setKindofVoice( "classical");
                        Toast.makeText(SoundsActivity.this, "0"+flag, Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.dingdong:
//                    	values.put("KindofVoice", 1);
//                    	flag=Test.updateConfiguration(values, "UID=?", new String[]{"1"});
                    	flag=AppEngine.getInstance(getBaseContext()).setKindofVoice("dingdong");
                        Toast.makeText(SoundsActivity.this,"1"+flag, Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.systemwarning:
//                    	values.put("KindofVoice", 2);
//                    	flag=Test.updateConfiguration(values, "UID=?", new String[]{"1"});
                    	flag=AppEngine.getInstance(getBaseContext()).setKindofVoice("systemwarning");
                        Toast.makeText(SoundsActivity.this,"2"+flag, Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.theme:
//                    	values.put("KindofVoice", 3);
//                    	flag=Test.updateConfiguration(values, "UID=?", new String[]{"1"});
                    	flag=AppEngine.getInstance(getBaseContext()).setKindofVoice("theme");
                        Toast.makeText(SoundsActivity.this,"3"+flag, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }
    private void inite(){
        radioGroup = (RadioGroup)findViewById(R.id.Rg05);
        btn1=(RadioButton)findViewById(R.id.classical);
        btn2=(RadioButton)findViewById(R.id.dingdong);
        btn3=(RadioButton)findViewById(R.id.systemwarning);
        btn4=(RadioButton)findViewById(R.id.theme);
        switch1=(Switch)findViewById(R.id.person_vibrate_sw);   
        switch2=(Switch)findViewById(R.id.person_groupvibrate_sw);
        switch3=(Switch)findViewById(R.id.person_sound_sw);
        switch4=(Switch)findViewById(R.id.person_groupSound_sw);
        switch1.setOnCheckedChangeListener(listener);
        switch2.setOnCheckedChangeListener(listener);
        switch3.setOnCheckedChangeListener(listener);
        switch4.setOnCheckedChangeListener(listener);
        //switch按钮的状态设置
        Boolean vibrate = AppEngine.getInstance(getBaseContext()).getShaketoRemind();
        Boolean groupvibrate = AppEngine.getInstance(getBaseContext()).getGroupShaketoRemind();
        Boolean sound = AppEngine.getInstance(getBaseContext()).getVoicetoRemind();
        Boolean groupSound = AppEngine.getInstance(getBaseContext()).getGroupVoicetoRemind();
        if(vibrate){
        	switch1.setChecked(true);
    		AppEngine.getInstance(getBaseContext()).setShaketoRemind(true);	
    	}else{
    		switch1.setChecked(false);
    	}
        if(groupvibrate){
        	switch2.setChecked(true);
    		AppEngine.getInstance(getBaseContext()).setGroupShaketoRemind( true);
    		
    	}else{
    		switch2.setChecked(false);
    	}
        if(sound){
        	switch3.setChecked(true);
    		AppEngine.getInstance(getBaseContext()).setVoicetoRemind( true);
    		
    	}else{
    		switch3.setChecked(false);
    	}
        if(groupSound){
        	switch4.setChecked(true);
    		AppEngine.getInstance(getBaseContext()).setGroupVoicetoRemind( true);
    		
    	}else{
    		switch4.setChecked(false);
    	}
        
        //switch按钮的状态设置
        
        //声音种类的初始化
        
        kindOfVoice=AppEngine.getInstance(getBaseContext()).getKindofVoice();

//        CYLog.i(TAG,"-->>"+kindOfVoice);
       
        if("classical".equals(kindOfVoice))
         {
        		btn1.setChecked(true);
        		CYLog.i(TAG,"-->>"+"classical");	
        		
        
        }
       
         	if("dingdong".equals(kindOfVoice)){
         		btn2.setChecked(true);
         		CYLog.i(TAG,"-->>"+"dingdong");	}
         		
     
          if("systemwarning".equals(kindOfVoice)){
        	  btn3.setChecked(true);
        	 CYLog.i(TAG,"-->>"+"systemwarning");
        	

         }
          if("theme".equals(kindOfVoice)){
        	  btn4.setChecked(true);
        	 CYLog.i(TAG,"-->>"+"theme");	
        	
         }

         	}
    

        

        
    
    private CompoundButton.OnCheckedChangeListener listener= new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
           switch (compoundButton.getId()){
               case R.id.person_vibrate_sw :
                   if(b){
                	   flag=AppEngine.getInstance(getBaseContext()).setShaketoRemind(true);
                  	 Toast.makeText(SoundsActivity.this,"振动开"+flag,Toast.LENGTH_SHORT).show();
                       CYLog.i(TAG,"振动开"+flag);
                   }
                   else {
                	   flag= AppEngine.getInstance(getBaseContext()).setShaketoRemind(false);
                    	 Toast.makeText(SoundsActivity.this,"振动关"+flag,Toast.LENGTH_SHORT).show();
                	   CYLog.i(TAG,"振动关"+flag);
                   }


                   break;
               case R.id.person_groupvibrate_sw:
                   if(b) {
                	   flag= AppEngine.getInstance(getBaseContext()).setGroupShaketoRemind(true);
                    	 Toast.makeText(SoundsActivity.this,"群振动开"+flag,Toast.LENGTH_SHORT).show();
                	   CYLog.i(TAG,"群振动开"+flag);
                   }
                	  
                   else {
                	   flag= AppEngine.getInstance(getBaseContext()).setGroupShaketoRemind( false);
                  	 Toast.makeText(SoundsActivity.this,"群振动关"+flag,Toast.LENGTH_SHORT).show();
                	   CYLog.i(TAG,"群振动关"+flag);
                   }
                break;
            case R.id.person_sound_sw:
                if(b) {
                	flag= AppEngine.getInstance(getBaseContext()).setVoicetoRemind( true);
               	 Toast.makeText(SoundsActivity.this,"声音开"+flag,Toast.LENGTH_SHORT).show();
                	CYLog.i(TAG,"声音开"+flag);
                }
                else {
                	flag= AppEngine.getInstance(getBaseContext()).setVoicetoRemind( false);
                  	 Toast.makeText(SoundsActivity.this,"声音关"+flag,Toast.LENGTH_SHORT).show();
                	CYLog.i(TAG,"声音关"+flag);
                }

                break;
            case R.id.person_groupSound_sw:
                if(b) {
                	flag= AppEngine.getInstance(getBaseContext()).setGroupVoicetoRemind( true);
               	 Toast.makeText(SoundsActivity.this,"群声音开"+flag,Toast.LENGTH_SHORT).show();
                	CYLog.i(TAG,"群声音开"+flag);
                }
                else {
                	flag	= AppEngine.getInstance(getBaseContext()).setGroupVoicetoRemind( false);
                	Toast.makeText(SoundsActivity.this,"群声音关"+flag,Toast.LENGTH_SHORT).show();
                	CYLog.i(TAG,"群声音关"+flag);
                }

                break;
           }
        }
    };

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
