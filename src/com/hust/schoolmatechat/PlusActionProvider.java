package com.hust.schoolmatechat;

import android.content.Context;
import android.content.Intent;
import android.view.ActionProvider;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;

public class PlusActionProvider extends ActionProvider {

	private Context context;

	public PlusActionProvider(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public View onCreateActionView() {
		return null;
	}

	@Override
	public void onPrepareSubMenu(SubMenu subMenu) {
		subMenu.clear();
		subMenu.add(context.getString(R.string.plus_group_chat))
				.setIcon(R.drawable.ofm_group_chat_icon)
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						Intent intent = new Intent();
						intent.setClass(context, CreateGroupChatActivity.class);
						context.startActivity(intent);
		                //Toast.makeText(context, context.getString(R.string.plus_group_chat), Toast.LENGTH_SHORT).show();  
						return true;
					}
				});
/*		subMenu.add(context.getString(R.string.plus_add_friend))
				.setIcon(R.drawable.ofm_add_icon)
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
		                Toast.makeText(context, context.getString(R.string.plus_add_friend), Toast.LENGTH_SHORT).show();  
						return false;
					}
				});
		subMenu.add(context.getString(R.string.plus_video_chat))
				.setIcon(R.drawable.ofm_video_icon)
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
		                Toast.makeText(context, context.getString(R.string.plus_video_chat), Toast.LENGTH_SHORT).show();  
						return false;
					}
				});
		subMenu.add(context.getString(R.string.plus_scan))
				.setIcon(R.drawable.ofm_qrcode_icon)
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
		                Toast.makeText(context, context.getString(R.string.plus_scan), Toast.LENGTH_SHORT).show();  
						return false;
					}
				});
		subMenu.add(context.getString(R.string.plus_take_photo))
				.setIcon(R.drawable.ofm_camera_icon)
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
		                Toast.makeText(context, context.getString(R.string.plus_take_photo), Toast.LENGTH_SHORT).show();  
						return false;
					}
				});*/
	}

	@Override
	public boolean hasSubMenu() {
		return true;
	}

}