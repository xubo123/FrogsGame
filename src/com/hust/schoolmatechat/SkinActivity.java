package com.hust.schoolmatechat;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.hust.schoolmatechat.engine.AppEngine;
import com.hust.schoolmatechat.engine.CYLog;

public class SkinActivity extends Activity {
	private static final String TAG = "SkinActivity";
	private RadioButton btn1;
	private RadioButton btn2;
	private RadioButton btn3;
	private RadioGroup radioGroup;
	private String kindOfSkin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		// bar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back));
		bar.setDisplayShowHomeEnabled(false);
		setContentView(R.layout.skin_layout);
		radioGroup = (RadioGroup) findViewById(R.id.person_skin_rg);
		btn1 = (RadioButton) findViewById(R.id.person_kind1_rb);
		btn2 = (RadioButton) findViewById(R.id.person_kind2_rb);
		btn3 = (RadioButton) findViewById(R.id.person_kind3_rb);
		setSkin();
		radioGroup
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup arg0, int i) {
						// TODO Auto-generated method stub
						switch (i) {
						case R.id.person_kind1_rb:
							onSkinChange(0);
							Toast.makeText(SkinActivity.this, "kind1",
									Toast.LENGTH_SHORT).show();
							// 此处添加选择皮肤kind1后的动作
							break;
						case R.id.person_kind2_rb:
							onSkinChange(1);
							Toast.makeText(SkinActivity.this, "kind2",
									Toast.LENGTH_SHORT).show();
							// 此处添加选择皮肤kind2后的动作
							break;
						case R.id.person_kind3_rb:
							onSkinChange(2);
							Toast.makeText(SkinActivity.this, "kind3",
									Toast.LENGTH_SHORT).show();
							// 此处添加选择皮肤kind3后的动作
							break;
						}

					}

				});

	}

	public void onSkinChange(int skinId) {
		AppEngine.getInstance(SkinActivity.this).setTheme("" + skinId);

	}

	public void setSkin() {// 根据选择的皮肤设置背景
		int index = Integer.parseInt(AppEngine.getInstance(SkinActivity.this)
				.getTheme());
		// kindOfSkin=AppEngine.getInstance(SkinActivity.this).getTheme("201012909");
		CYLog.i(TAG, "皮肤" + index);
		if (index == 0) {
			CYLog.i(TAG, "查询" + 0);
			btn1.setChecked(true);
		}
		if (index == 1) {
			CYLog.i(TAG, "查询" + 1);
			btn2.setChecked(true);
		}
		if (index == 2) {
			CYLog.i(TAG, "查询" + 2);
			btn3.setChecked(true);
		}

	}

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
