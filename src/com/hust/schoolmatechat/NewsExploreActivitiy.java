package com.hust.schoolmatechat;

import java.io.File;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import com.hust.schoolmatechat.engine.CYLog;

public class NewsExploreActivitiy extends Activity {
	protected static final String TAG = "NewsExploreActivitiy";
	private WebView webView = null;
	private String url,tittle,image,userName;
	private ActionBar bar = null;
	private boolean isNews = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		// bar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back));
		bar.setDisplayShowHomeEnabled(false);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chatitemlist_newsexplore);
		webView = (WebView) this.findViewById(R.id.webview_newsexplore);

		// ��ȡƵ�����洫��������Url
		Intent parentIntent = getIntent();
		url = parentIntent.getStringExtra("newsUrl");
		image = parentIntent.getStringExtra("image");
		tittle = parentIntent.getStringExtra("tittle");
		userName = parentIntent.getStringExtra("userName");
		bar.setTitle(userName);
		if (tittle!=null&&!tittle.equals("")) {
			isNews = true;
		}
		CYLog.i(TAG,url);

		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webView.setWebChromeClient(new MyWebChromeClient());
		webView.loadUrl(url);
		webView.requestFocus();
		webView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				CYLog.v(TAG, "view.loadUrl(url)=" + url);
				view.loadUrl(url);
				return true;
			}
		});
	}

	public class MyWebChromeClient extends WebChromeClient {
		@Override
		public void onCloseWindow(WebView window) {
			super.onCloseWindow(window);
		}

		@Override
		public boolean onCreateWindow(WebView view, boolean dialog,
				boolean userGesture, Message resultMsg) {
			return super.onCreateWindow(view, dialog, userGesture, resultMsg);
		}

		/**
		 * ����Ĭ�ϵ�window.alertչʾ���棬����title����ʾΪ��������file:////��
		 */
		public boolean onJsAlert(WebView view, String url, String message,
				JsResult result) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(
					view.getContext());
			CYLog.v(TAG, "url=" + url + ",message=" + message + ",result="
					+ result);
			builder.setTitle(userName).setMessage(message)
					.setPositiveButton("ȷ��", null);

			// ����Ҫ�󶨰����¼�
			// ����keycode����84֮��İ���
			builder.setOnKeyListener(new OnKeyListener() {
				public boolean onKey(DialogInterface dialog, int keyCode,
						KeyEvent event) {
					CYLog.v("onJsAlert", "keyCode==" + keyCode + "event="
							+ event);
					return true;
				}
			});
			// ��ֹ��Ӧ��back�����¼�
			builder.setCancelable(false);
			AlertDialog dialog = builder.create();
			dialog.show();
			result.confirm();// ��Ϊû�а��¼�����Ҫǿ��confirm,����ҳ�������ʾ�������ݡ�
			return true;
			// return super.onJsAlert(view, url, message, result);
		}

		public boolean onJsBeforeUnload(WebView view, String url,
				String message, JsResult result) {
			return super.onJsBeforeUnload(view, url, message, result);
		}

		/**
		 * ����Ĭ�ϵ�window.confirmչʾ���棬����title����ʾΪ��������file:////��
		 */
		public boolean onJsConfirm(WebView view, String url, String message,
				final JsResult result) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(
					view.getContext());
			builder.setTitle(userName).setMessage(message)
					.setPositiveButton("ȷ��", new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							result.confirm();
						}
					}).setNeutralButton("ȡ��", new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							result.cancel();
						}
					});
			builder.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					result.cancel();
				}
			});

			// ����keycode����84֮��İ��������ⰴ�����¶Ի�����Ϣ��ҳ���޷��ٵ����Ի��������
			builder.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode,
						KeyEvent event) {
					CYLog.v("onJsConfirm", "keyCode==" + keyCode + "event="
							+ event);
					return true;
				}
			});
			// ��ֹ��Ӧ��back�����¼�
			// builder.setCancelable(false);
			AlertDialog dialog = builder.create();
			dialog.show();
			return true;
			// return super.onJsConfirm(view, url, message, result);
		}

		/**
		 * ����Ĭ�ϵ�window.promptչʾ���棬����title����ʾΪ��������file:////��
		 * 
		 */
		public boolean onJsPrompt(WebView view, String url, String message,
				String defaultValue, final JsPromptResult result) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(
					view.getContext());

			builder.setTitle(userName).setMessage(message);

			final EditText et = new EditText(view.getContext());
			et.setSingleLine();
			et.setText(defaultValue);
			builder.setView(et).setPositiveButton("ȷ��", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					result.confirm(et.getText().toString());
				}

			}).setNeutralButton("ȡ��", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					result.cancel();
				}
			});

			// ����keycode����84֮��İ��������ⰴ�����¶Ի�����Ϣ��ҳ���޷��ٵ����Ի��������
			builder.setOnKeyListener(new OnKeyListener() {
				public boolean onKey(DialogInterface dialog, int keyCode,
						KeyEvent event) {
					CYLog.v("onJsPrompt", "keyCode==" + keyCode + "event="
							+ event);
					return true;
				}
			});

			// ��ֹ��Ӧ��back�����¼�
			// builder.setCancelable(false);
			AlertDialog dialog = builder.create();
			dialog.show();
			return true;
			// return super.onJsPrompt(view, url, message, defaultValue,
			// result);
		}

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			super.onProgressChanged(view, newProgress);
		}

		@Override
		public void onReceivedIcon(WebView view, Bitmap icon) {
			super.onReceivedIcon(view, icon);
		}

		@Override
		public void onReceivedTitle(WebView view, String title) {
			super.onReceivedTitle(view, title);
		}

		@Override
		public void onRequestFocus(WebView view) {
			super.onRequestFocus(view);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
			webView.goBack();// ����ǰһ��ҳ��
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		case R.id.share:
			shares();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	public void shares() {
		int start = image.lastIndexOf("/");
		String iconname = image.substring(start + 1, image.length());
		String baseDir = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		File file = new File(baseDir + File.separator + "chuangyou"
				+ File.separator + "icon" + File.separator + iconname);
		Intent shareInt = new Intent(Intent.ACTION_SEND);
		shareInt.putExtra(Intent.EXTRA_SUBJECT, "ѡ�����ʽ");
		shareInt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		shareInt.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		shareInt.setType("image/png");
		String text= tittle+"\n"+url+"\n�����Դ���app��";
		shareInt.putExtra("Kdescription",text);//΢������Ȧר��
		shareInt.putExtra(Intent.EXTRA_TEXT,text);
		startActivity(shareInt);

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		if (isNews) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.newsmenus, menu);
		}
		return true;
	}

}
