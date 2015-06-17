package com.hust.schoolmatechat;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CurrentVersion;
import com.hust.schoolmatechat.engine.UpdateApp;

/**
 * Created by hongliang on 2014/7/31.
 */
public class AboutActivity extends Activity {
	private TextView update_tv,function_tv,feedback_tv,Vnum_tv;
	private ImageView logo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar bar=getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
//		bar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back));
		bar.setDisplayShowHomeEnabled(false);
		setContentView(R.layout.about_layout);
		logo= (ImageView)findViewById(R.id.logo);
		logo.setOnClickListener(listener);
		update_tv = (TextView)findViewById(R.id.about_update);
		update_tv.setOnClickListener(listener);
		Vnum_tv= (TextView)findViewById(R.id.vname);
		String currentName = CurrentVersion.getVerName(this);
		Vnum_tv.setText("窗友\n"+currentName);
		function_tv=(TextView)findViewById(R.id.about_funtion);
		function_tv.setOnClickListener(listener);
		feedback_tv=(TextView)findViewById(R.id.about_feedback);
		feedback_tv.setOnClickListener(listener);
    }
    private OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.about_update:
                	GetNewVersion mGetNewVersion = new GetNewVersion();
            		mGetNewVersion.execute();
                    break;
                case R.id.about_funtion:
                	Intent intent = new Intent();
					intent.setClass(AboutActivity.this, IntroductionActivity.class);
					intent.putExtra("comefrom", "about");
					startActivity(intent);
                    break;
                case R.id.about_feedback:
                	Intent intent2 = new Intent(getApplicationContext(),
        					NewsExploreActivitiy.class);
                	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            		String uid = prefs.getString("USERNAME", "");
        			intent2.putExtra("newsUrl", APPConstant.FEEDBACKURL+uid);				
        			intent2.putExtra("userName", "意见反馈");
        			startActivity(intent2);
                    break;
                case R.id.logo:
                case R.id.vname:
                	Intent intent3 = new Intent(getApplicationContext(),
        					NewsExploreActivitiy.class);
        			intent3.putExtra("newsUrl", APPConstant.INDEXURL);				
        			intent3.putExtra("userName", "关于窗友");
        			startActivity(intent3);
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
	UpdateApp update = new UpdateApp(AboutActivity.this);

	public class GetNewVersion extends AsyncTask<Void, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.
			return update.doUpdateApp();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			if (success) {
				try {
					update.showUpdateDialog();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				Toast.makeText(AboutActivity.this,"您的版本已是最新版本~",Toast.LENGTH_SHORT).show();
			}
			super.onPostExecute(success);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
	}
}