package com.hust.schoolmatechat;

import java.io.File;

import com.hust.schoolmatechat.FaceInput.FaceConversionUtil;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.utils.OpenFiles;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.SpannedString;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

public class ChatDetailActivity extends Activity {
	public String filePath = "";
	ImageView image;
	TextView text;
	Button open;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_chat_detail);
		Intent parentIntent = getIntent();
		String content = parentIntent.getStringExtra("TEXT");
		text = (TextView) findViewById(R.id.textViewchatdetail);
		image = (ImageView) findViewById(R.id.imagechatdetail);
		open = (Button) findViewById(R.id.open_file);
		String url = "";
		if (content.toString().startsWith(APPConstant.CMD_PREFIX_FILE_SEND)) {
			url = content.toString().replaceAll(
					APPConstant.CMD_PREFIX_FILE_SEND, "");

			if (url.startsWith("http://")) {
				String[] path = url.split("all_user_files");
				int start = url.lastIndexOf("/");
				String filetype = url.substring(start - 1, start);
				final String filename = url.substring(start + 1, url.length());
				int nstart = filename.indexOf("_");
				String realname = filename.substring(nstart + 1,
						filename.length());
				filePath = APPConstant.CHAT_FILE + File.separator + path[1];
				final File file = new File(filePath);
				
				if (filetype.equals("" + APPConstant.PICTURE)) {
					text.setVisibility(View.GONE);

					if (file.exists()) {
						// Uri uri = Uri.fromFile(file);
						// image.setImageURI(uri);
						Bitmap bm = BitmapFactory.decodeFile(filePath);
						image.setImageBitmap(bm);
					} else {
						Bitmap bitmap = BitmapFactory.decodeResource(
								getResources(), R.drawable.imageerror);
						image.setImageBitmap(bitmap);
					}
				} else if (filetype.equals("" + APPConstant.NORMAL_FILE)) {
					image.setVisibility(View.GONE);

					SpannableString spannableString = FaceConversionUtil
							.getInstace().getExpressionString(this, content);
					text.append(spannableString);
					//text.append("\n" + realname);
				//	CYLog.d("===", "++++++++++++++" + file.isFile());
					open.setVisibility(View.VISIBLE);
					final String mUrl = url;
					if(!file.exists()){
						
						open.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								Intent downloadFileIntent = new Intent("com.schoolmatechat.downloadFile");
								downloadFileIntent.putExtra("URL", mUrl);
								sendBroadcast(downloadFileIntent);
								Toast.makeText(ChatDetailActivity.this, "文件未成功下载，正在重新下载，请稍后再试",
										Toast.LENGTH_SHORT).show();
							}
						});
					}else{
						open.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								int start = filename.lastIndexOf(".");
								String filetype = filename.substring(start + 1,
										filename.length());
								if (file != null) {
									Intent intent;
									if (OpenFiles.checkEndsWithInStringArray(
											filename,
											getResources().getStringArray(
													R.array.fileEndingImage))) {
										intent = OpenFiles.getImageFileIntent(file);
										startActivity(intent);
									} else if (OpenFiles
											.checkEndsWithInStringArray(
													filename,
													getResources()
															.getStringArray(
																	R.array.fileEndingWebText))) {
										intent = OpenFiles.getHtmlFileIntent(file);
										startActivity(intent);
									} else if (OpenFiles
											.checkEndsWithInStringArray(
													filename,
													getResources()
															.getStringArray(
																	R.array.fileEndingPackage))) {
										intent = OpenFiles.getApkFileIntent(file);
										startActivity(intent);

									} else if (OpenFiles
											.checkEndsWithInStringArray(
													filename,
													getResources()
															.getStringArray(
																	R.array.fileEndingAudio))) {
										intent = OpenFiles.getAudioFileIntent(file);
										startActivity(intent);
									} else if (OpenFiles
											.checkEndsWithInStringArray(
													filename,
													getResources()
															.getStringArray(
																	R.array.fileEndingVideo))) {
										intent = OpenFiles.getVideoFileIntent(file);
										startActivity(intent);
									} else if (OpenFiles
											.checkEndsWithInStringArray(
													filename,
													getResources().getStringArray(
															R.array.fileEndingText))) {
										intent = OpenFiles.getTextFileIntent(file);
										startActivity(intent);
									} else if (OpenFiles
											.checkEndsWithInStringArray(
													filename,
													getResources().getStringArray(
															R.array.fileEndingPdf))) {
										intent = OpenFiles.getPdfFileIntent(file);
										startActivity(intent);
									} else if (OpenFiles
											.checkEndsWithInStringArray(
													filename,
													getResources().getStringArray(
															R.array.fileEndingWord))) {
										intent = OpenFiles.getWordFileIntent(file);
										startActivity(intent);
									} else if (OpenFiles
											.checkEndsWithInStringArray(
													filename,
													getResources()
															.getStringArray(
																	R.array.fileEndingExcel))) {
										intent = OpenFiles.getExcelFileIntent(file);
										startActivity(intent);
									} else if (OpenFiles
											.checkEndsWithInStringArray(
													filename,
													getResources().getStringArray(
															R.array.fileEndingPPT))) {
										intent = OpenFiles.getPPTFileIntent(file);
										startActivity(intent);
									} else {
										Toast.makeText(ChatDetailActivity.this,
												"无法打开，请安装相应的软件！", Toast.LENGTH_SHORT)
												.show();
									}
								} else {
									Toast.makeText(ChatDetailActivity.this,
											"暂不支持此文件类型！", Toast.LENGTH_SHORT).show();

								}
							}
						});
					}
					
					
				}
			} else {
				image.setVisibility(View.GONE);
				SpannableString spannableString = FaceConversionUtil
						.getInstace().getExpressionString(this, content);
				text.append(spannableString);
			//	text.append("\n文件发送失败！");
			//	CYLog.d("===", "" + spannableString);
			}
		} else {
			image.setVisibility(View.GONE);
			SpannableString spannableString = FaceConversionUtil.getInstace()
					.getExpressionString(this, content);
			text.setText(spannableString);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat_detail, menu);
		return true;
	}

	public void share() {

		File file = new File(filePath);
		Intent shareInt = new Intent(Intent.ACTION_SEND);
		shareInt.putExtra(Intent.EXTRA_SUBJECT, "选择分享方式");
		shareInt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		shareInt.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		shareInt.setType("image/png");
		String text = "【来自窗友app的图片分享】";
		shareInt.putExtra("Kdescription", text);// 微信朋友圈专用
		shareInt.putExtra(Intent.EXTRA_TEXT, text);
		startActivity(shareInt);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			if (image.getVisibility() != View.GONE)
				share();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
