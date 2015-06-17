package com.hust.schoolmatechat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService.DataCenterManagerBiner;
import com.hust.schoolmatechat.dao.ClassmateDao;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.entity.ContactsEntity;
import com.hust.schoolmatechat.postClass.FriendProfile;
import com.hust.schoolmatechat.postClass.HttpCommand;
import com.hust.schoolmatechat.postClass.HttpupLoad_gson;
import com.hust.schoolmatechat.postClass.userProfile;
import com.hust.schoolmatechat.register.GetHandObj;
import com.hust.schoolmatechat.register.HttpupLoad;
import com.hust.schoolmatechat.utils.ImageTools;
import com.hust.schoolmatechat.utils.ImageUtils;

/**
 * Created by hongliang on 2014/8/2.
 */

public class AccountActivity extends Activity {
	private static final String TAG = "AccountActivity";
	private TextView MySchool;
	private RelativeLayout address_rl, phoneNum_rl, sex_rl, photo_rl, email_rl,
			sign_rl, grade_rl;
	private TextView person_name_tv, address_tv, phoneNum_tv, sex_tv, sign_tv,
			email_tv, accountNum_tv;
	private EditText et_name;
	private AccountActivity accountActivity;
	private ImageView person_photo_iv;
	private Button post;
	int num;
	String partofUrl;
	private boolean sexType;

	private ContentValues content = new ContentValues();
	private JSONObject json2;
	private String pictureUrl = null;
	private String accountNum;
	private String password;
	private GetHandObj getContent;

	private static final String IMAGE_UNSPECIFIED = "image/*";
	private static final String TEMP_PIC_FILE = "_tempTEMP_PIC_FILE.jpg";

	private static final int DEFAULT_PICTURE = 0;
	private static final int CHOOSE_PICTURE = 1;
	private static final int TAKE_PICTURE = 2;

	private static final int CROP = 3;
	private static final int CROP_PICTURE = 4;

	private static final int SCALE = 5;// ��Ƭ��С����

	private HttpupLoad GetTask;
	private HttpupLoad pictureHttpUpload;
	private File tempPicture;
	private Context mContext;
	
	// gson��ʹ��
	HttpupLoad_gson httpupLoad_gson;
	private  boolean sex_type;     //����Ϊtrue��Ů��Ϊfalse;


	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				try {
					JSONObject sss = new JSONObject(pictureHttpUpload
							.getLoaddata().getStrResult());
					if (sss.has("obj")) {
						pictureUrl = sss.getString("obj");

						String temp[] = pictureUrl.split("/");
						String baseDir = Environment
								.getExternalStorageDirectory()
								.getAbsolutePath();
						// ���ش洢·��
						String targetDir = baseDir + File.separator
								+ "chuangyou" + File.separator + "icon"
								+ File.separator + temp[temp.length - 2];
						File dirFile = new File(targetDir);
						if (!dirFile.exists()) {
							dirFile.mkdirs();
						}

						// �����Ѿ��洢����ͼ��
						final File file = new File(dirFile,
								temp[temp.length - 1]);
						// �����ļ�
						if (tempPicture != null && tempPicture.exists()) {
							ImageUtils.copyFile(tempPicture, file);
							// ɾ����ʱ�ļ�
							tempPicture.delete();
							tempPicture = null;
						}

						// ���±�����Ϣ,��֪ͨ����
						ContactsEntity selfContactsEntity = dataCenterManagerService
								.getUserSelfContactsEntity();
						selfContactsEntity.setPicture(sss.getString("obj"));
						dataCenterManagerService
								.updateSelfContactsEntity(selfContactsEntity, true);

						// �޸�ͼƬ��ʾ
						if (person_photo_iv != null) {
							ImageUtils.setUserHeadIcon(person_photo_iv,
									dataCenterManagerService
											.getUserSelfContactsEntity()
											.getPicture(), this);
						}

						Toast.makeText(mContext, "ͼƬ�޸ĳɹ�", Toast.LENGTH_SHORT)
								.show();
					} else {
						// ɾ����ʱ�ļ�
						if (tempPicture != null && tempPicture.exists()) {
							tempPicture.delete();
							tempPicture = null;
						}

						Toast.makeText(mContext, "ͼƬ�ϴ�ʧ�ܣ�������",
								Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 3:
//				try {
//					CYLog.i(TAG, "�޸Ľ��" + GetTask.getLoaddata().getStrResult());
//					post.setClickable(true);
//					if (getContent.getIfsuccess(GetTask.getLoaddata()
//							.getStrResult())) {
//						updateLocalContactsInfo();
//						Toast.makeText(mContext, "�޸ĳɹ�", Toast.LENGTH_SHORT)
//								.show();
//					} else {
//						Toast.makeText(mContext, "�޸�ʧ�ܣ�������", Toast.LENGTH_SHORT)
//								.show();
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
				
				
				// gson ʹ�� 
				try {
					CYLog.i(TAG, "�޸Ľ��" + httpupLoad_gson.getLoaddata().getStrResult());
					post.setClickable(true);
					if (getContent.getIfsuccess(httpupLoad_gson.getLoaddata()
							.getStrResult())) {
						updateLocalContactsInfo();
						Toast.makeText(mContext, "�޸ĳɹ�", Toast.LENGTH_SHORT)
								.show();
					} else {
						Toast.makeText(mContext, "�޸�ʧ�ܣ�������", Toast.LENGTH_SHORT)
								.show();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	};

	/** �Խ��������ķ��� */
	private DataCenterManagerService dataCenterManagerService;
	ServiceConnection dataCenterManagerIntentConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			dataCenterManagerService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// ����һ��MsgService����
			dataCenterManagerService = ((DataCenterManagerBiner) service)
					.getService();

			initAccountActivity();
		}
	};

	@Override
	public void onDestroy() {
		// add ȡ���� ibind
		unbindService(dataCenterManagerIntentConn);
		super.onDestroy();
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * �����������Ĺ������
		 */
		final Intent dataCenterManagerIntent = new Intent(this,
				DataCenterManagerService.class);
		bindService(dataCenterManagerIntent, dataCenterManagerIntentConn,
				Context.BIND_ABOVE_CLIENT);
	}

	private void initAccountActivity() {
		Bundle selfInfo = new Bundle();
		Intent selfInfoIntent = getIntent();
		selfInfo = selfInfoIntent.getExtras();

		mContext = this;
		accountActivity = this;
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setDisplayShowHomeEnabled(false);
		setContentView(R.layout.account_layout);

		accountNum = dataCenterManagerService.getUserSelfContactsEntity()
				.getUserAccount();
		password = dataCenterManagerService.getUserSelfContactsEntity()
				.getPassword();
//		pictureUrl = "3";

		try {
			JSONObject json1 = new JSONObject();
			json2 = new JSONObject();
			json1.put("accountNum", accountNum);
			json1.put("password", password);
			json2.put("content", json1);
			json2.put("command", APPConstant.USER_PROFILE_IMAGE_FILE_UPLOAD);
		} catch (Exception e) {
			e.printStackTrace();
		}
		CYLog.i(TAG, "the jsonStr : " + json2.toString());
		getContent = new GetHandObj();
		// MySchool = (TextView)findViewById(R.id.person_school_tv);
		// MySchool.setOnClickListener(listener);
		person_name_tv = (TextView) findViewById(R.id.person_name_tv);
		accountNum_tv = (TextView) findViewById(R.id.accountNum_tv);
		et_name = (EditText) findViewById(R.id.et_person_name);
		sign_rl = (RelativeLayout) findViewById(R.id.sign);

		sign_tv = (TextView) findViewById(R.id.person_sign_tv);
		address_rl = (RelativeLayout) findViewById(R.id.address);

		address_tv = (TextView) findViewById(R.id.address_tv);
		phoneNum_rl = (RelativeLayout) findViewById(R.id.phoneNum);

		phoneNum_tv = (TextView) findViewById(R.id.phoneNum_tv);

		email_rl = (RelativeLayout) findViewById(R.id.email);

		email_tv = (TextView) findViewById(R.id.email_tv);
		grade_rl = (RelativeLayout) findViewById(R.id.grade);
		grade_rl.setOnClickListener(listener);

		sex_rl = (RelativeLayout) findViewById(R.id.sex);
		sex_rl.setOnClickListener(listener);
		sex_tv = (TextView) findViewById(R.id.person_sex_tv);
		post = (Button) findViewById(R.id.post);
		post.setOnClickListener(listener);

		accountNum_tv.setText(dataCenterManagerService
				.getUserSelfContactsEntity().getAccountNum());
		phoneNum_tv.setText(dataCenterManagerService
				.getUserSelfContactsEntity().getPhoneNum());

		String auth = dataCenterManagerService.getUserSelfContactsEntity()
				.getAuthenticated();
		String name = dataCenterManagerService
				.getUserSelfContactsEntity().getName();
		// �û��Լ�����Ϣ����δ��֤���û�
		if (selfInfo == null && (auth == null || !auth.equals("1"))) {
			// ��֤�û�
			et_name.setText(name);
			et_name.setVisibility(View.VISIBLE);
		} else {
			person_name_tv.setText(name);
			person_name_tv.setVisibility(View.VISIBLE);
		}

		if (dataCenterManagerService.getUserSelfContactsEntity().getSex()
				.equals("0")) {
			sex_tv.setText("��");
			sex_type=true;
//			Random random=new Random();
//			int pictureNum=11;
//			num=pictureNum;
//			// ���±�����Ϣ,��֪ͨ����
//			ContactsEntity selfContactsEntity_1 = dataCenterManagerService
//					.getUserSelfContactsEntity();
//			selfContactsEntity_1.setPicture(String.valueOf(pictureNum));
//			dataCenterManagerService
//					.updateSelfContactsEntity(selfContactsEntity_1, true);
			
		}
		if (dataCenterManagerService.getUserSelfContactsEntity().getSex()
				.equals("1")) {
			sex_tv.setText("Ů");
			sex_type=false;
		}
		// sex_tv.setText(dataCenterManagerService.getUserSelfContactsEntity().getSex());
		address_tv.setText(dataCenterManagerService.getUserSelfContactsEntity()
				.getAddress());
		email_tv.setText(dataCenterManagerService.getUserSelfContactsEntity()
				.getEmail());
		sign_tv.setText(dataCenterManagerService.getUserSelfContactsEntity()
				.getSign());

		// ͼƬ����������֧�ֵ���޸�
		
		photo_rl = (RelativeLayout) findViewById(R.id.photo_rl);
		person_photo_iv = (ImageView) findViewById(R.id.person_photo_iv);
		ImageUtils.setUserHeadIcon(person_photo_iv, dataCenterManagerService
				.getUserSelfContactsEntity().getPicture(), handler);

		// ���ѵ���Ϣ
		if (selfInfo != null) {
			post.setVisibility(View.GONE);
		} else {
			// person_photo_iv.setOnLongClickListener(LongClickLintener);
			photo_rl.setOnClickListener(listener);// ͼƬ�������ܵ��
			// person_photo_iv.setOnClickListener(listener);
			sign_rl.setOnClickListener(listener);
			address_rl.setOnClickListener(listener);
		//	phoneNum_rl.setOnClickListener(listener);
			email_rl.setOnClickListener(listener);
			sex_rl.setOnClickListener(listener);
		}

	}

	private OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			// ͼƬ�������ܵ��
			case R.id.photo_rl:
				// ��ͼ����ʾ�� falseֱ��ѡ��ͼƬ�ϴ���true�ü����ϴ�
				showPicturePicker(AccountActivity.this, false);
				break;

			case R.id.phoneNum:
				final EditText name = new EditText(AccountActivity.this);
				name.setText(phoneNum_tv.getText());
				name.requestFocus();
				name.setFocusable(true);
				new AlertDialog.Builder(AccountActivity.this)
						.setTitle("�ֻ���")
						.setIcon(android.R.drawable.ic_dialog_info)
						.setView(name)
						.setPositiveButton("ȷ��",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(
											DialogInterface dialogInterface,
											int i) {
										String PhoneNumStr = name.getText()
												+ "";
										if (PhoneNumStr != null
												&& !PhoneNumStr.equals("")
												&& isMobileNO(PhoneNumStr)) {
											phoneNum_tv.setText(name.getText());
										} else {
											Toast.makeText(
													getApplicationContext(),
													"��������ȷ�ֻ���",
													Toast.LENGTH_SHORT).show();
										}

									}
								}).setNegativeButton("ȡ��", null).show();
				break;
			case R.id.email:
				final EditText email = new EditText(AccountActivity.this);
				email.setText(email_tv.getText());  
				email.requestFocus();
				email.setFocusable(true);
				new AlertDialog.Builder(AccountActivity.this)
						.setTitle("��������")
						.setIcon(android.R.drawable.ic_dialog_info)
						.setView(email)
						.setPositiveButton("ȷ��",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(
											DialogInterface dialogInterface,
											int i) {
										String emailStr = email.getText() + "";
										if (emailStr != null
												&& !emailStr.equals("")
												&& isEmailFromat(emailStr)) {
											email_tv.setText(email.getText());
										} else {
											Toast.makeText(
													getApplicationContext(),
													"��������ȷ����",
													Toast.LENGTH_SHORT).show();
										}

									}
								}).setNegativeButton("ȡ��", null).show();
				break;
			case R.id.sign:
				final EditText sign = new EditText(AccountActivity.this);
				sign.setText(sign_tv.getText());
				sign.requestFocus();
				sign.setFocusable(true);
				new AlertDialog.Builder(AccountActivity.this)
						.setTitle("����ǩ��")
						.setIcon(android.R.drawable.ic_dialog_info)
						.setView(sign)
						.setPositiveButton("ȷ��",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(
											DialogInterface dialogInterface,
											int i) {

										sign_tv.setText(sign.getText());

									}
								}).setNegativeButton("ȡ��", null).show();
				break;
			case R.id.address:
				final EditText birthPlace = new EditText(AccountActivity.this);
				birthPlace.setText(address_tv.getText());
				birthPlace.requestFocus();
				birthPlace.setFocusable(true);
				new AlertDialog.Builder(AccountActivity.this)
						.setTitle("��ַ")
						.setIcon(android.R.drawable.ic_dialog_info)
						.setView(birthPlace)
						.setPositiveButton("ȷ��",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(
											DialogInterface dialogInterface,
											int i) {
										address_tv.setText(birthPlace.getText());
										content.put("Address",
												birthPlace.getText() + "");
										// flag=Test2.updateUserProfile(content,
										// "UID=?",new String[]{"1"});
										// Toast.makeText(AccountActivity.this,
										// ""+flag, Toast.LENGTH_SHORT).show();

									}
								}).setNegativeButton("ȡ��", null).show();
				break;

			case R.id.grade:
				Intent intent = new Intent();
				String auth = dataCenterManagerService
						.getUserSelfContactsEntity().getAuthenticated();
				if (auth == null || !auth.equals("1")) {
					EditText etName = accountActivity.getName();
					String userName = (String) etName.getText().toString();
					CYLog.i(TAG, "input user name : " + userName);
					if (userName != null && !userName.equals("")) {
						intent.putExtra("userName", userName);
						intent.setClass(getApplicationContext(),
								StudyExActivity.class);
						startActivity(intent);
					} else {
						Toast.makeText(mContext, "������д��ʵ����", Toast.LENGTH_SHORT)
								.show();
					}
				} else {
					intent.putExtra("userName", dataCenterManagerService
							.getUserSelfContactsEntity().getName());
					intent.setClass(getApplicationContext(),
							StudyExActivity.class);
					startActivity(intent);
				}
				break;

			case R.id.post: {
				post.setClickable(false);
//				try {
//
//					JSONObject json_1 = getUpdateInfo();
//					JSONObject json_2 = new JSONObject();
//					// JSONArray intrestType = arraytoJSon(intrestTypelist);
//					// JSONArray channel = arraytoJSon(channellist);
//					// String[] baseInfoId = user.getBaseInfoId();
//					// JSONArray baseInfoIdList = arraytoJSon(baseInfoId);
//					// json1.put("baseInfoId", baseInfoIdList);
//					// json1.put("intrestType", intrestType);
//					// json1.put("channels", channel);
//					json_2.put("content", json_1);
//					json_2.put("command",
//							APPConstant.USER_PROFILE_UPDATE_USER_PROFILE);
//					CYLog.i(TAG,"---------->" + json_2);
//
//					GetTask = new HttpupLoad(APPConstant.getUSERURL(), json_2,
//							handler, 3, getApplicationContext());
//					GetTask.execute();
//					
//					
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
				
				//  gsonʹ��
				
				userProfile user=new userProfile(accountNum,et_name.getText().toString(),
						phoneNum_tv.getText().toString(),
						password,
						address_tv.getText().toString(),
						sign_tv.getText().toString(),
						email_tv.getText().toString());
				HttpCommand httpCommand=new HttpCommand(APPConstant.USER_PROFILE_UPDATE_USER_PROFILE, user);
				String mapJson_1=httpCommand.getJsonStr();

				System.out.println("gson"+mapJson_1);
				httpupLoad_gson=new HttpupLoad_gson(APPConstant.getUSERURL(), mapJson_1, handler, 3,getApplicationContext());
				httpupLoad_gson.execute();
				
				
				
				
			}
				break;
			}
		}
	};

	public EditText getName() {
		return et_name;
	}

	// �û��Զ�����Ƭ�Ĵ���
	private OnLongClickListener LongClickLintener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View view) {
			// ��ͼ����ʾ�� false��֧�ֲü�������ҵ�
			showPicturePicker(AccountActivity.this, false);
			return true;
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case TAKE_PICTURE:
				// �������ڱ��ص�ͼƬȡ������С����ʾ�ڽ�����
				// Bitmap bitmap = BitmapFactory.decodeFile(Environment
				// .getExternalStorageDirectory()
				// + File.separator
				// + TEMP_PIC_FILE);

				Uri imageUri = Uri.fromFile(new File(Environment
						.getExternalStorageDirectory(), TEMP_PIC_FILE));
				Bitmap bitmap = this.getThumbnail(imageUri, 500);
				Bitmap newBitmap = ImageTools.zoomBitmap(bitmap,
						bitmap.getWidth() / SCALE, bitmap.getHeight() / SCALE);
				// ����Bitmap�ڴ�ռ�ýϴ�������Ҫ�����ڴ棬����ᱨout of memory�쳣
				bitmap.recycle();
				// ������ʱ�ļ�
				ImageTools.savePhotoToSDCard(newBitmap, Environment
						.getExternalStorageDirectory().getAbsolutePath(),
						TEMP_PIC_FILE);

				// ׼���ϴ�ͼƬ��������
				tempPicture = new File(
						Environment.getExternalStorageDirectory()
								+ File.separator + TEMP_PIC_FILE);
				pictureHttpUpload = new HttpupLoad(APPConstant.getUSERURL(),
						json2, handler, 1, this, tempPicture);
				pictureHttpUpload.execute();
				break;

			case CHOOSE_PICTURE:
				// ContentResolver resolver = getContentResolver();
				// ��Ƭ��ԭʼ��Դ��ַ
				Uri originalUri = data.getData();
				try {
					// ʹ��ContentProviderͨ��URI��ȡԭʼͼƬ
					// Bitmap photo =
					// MediaStore.Images.Media.getBitmap(resolver,
					// originalUri);
					Bitmap photo = getThumbnail(originalUri, 500);
					if (photo != null) {
						// Ϊ��ֹԭʼͼƬ�������ڴ��������������Сԭͼ��ʾ��Ȼ���ͷ�ԭʼBitmapռ�õ��ڴ�
						Bitmap smallBitmap = ImageTools.zoomBitmap(photo,
								photo.getWidth() / SCALE, photo.getHeight()
										/ SCALE);
						// �ͷ�ԭʼͼƬռ�õ��ڴ棬��ֹout of memory�쳣����
						photo.recycle();
						// ������ʱ�ļ�
						ImageTools.savePhotoToSDCard(smallBitmap, Environment
								.getExternalStorageDirectory()
								.getAbsolutePath(), TEMP_PIC_FILE);
						// ׼���ϴ�ͼƬ��������
						tempPicture = new File(
								Environment.getExternalStorageDirectory()
										+ File.separator + TEMP_PIC_FILE);
						pictureHttpUpload = new HttpupLoad(
								APPConstant.getUSERURL(), json2, handler, 1,
								this, tempPicture);
						pictureHttpUpload.execute();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case CROP:
				Uri uri = null;
				if (data != null) {
					uri = data.getData();
				} else {
					CYLog.i(TAG,"File");
					String fileName = getSharedPreferences("temp",
							Context.MODE_WORLD_WRITEABLE).getString("tempName",
							"");
					uri = Uri.fromFile(new File(Environment
							.getExternalStorageDirectory(), fileName));
				}
				cropImage(uri, 500, 500, CROP_PICTURE);
				break;

			case CROP_PICTURE:
				Bitmap photo = null;
				Uri photoUri = data.getData();
				if (photoUri != null) {
					photo = BitmapFactory.decodeFile(photoUri.getPath());
				}
				if (photo == null) {
					Bundle extra = data.getExtras();
					if (extra != null) {
						photo = (Bitmap) extra.get("data");
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
					}
				}
				// ������ʱ�ļ�
				ImageTools.savePhotoToSDCard(photo, Environment
						.getExternalStorageDirectory().getAbsolutePath(),
						TEMP_PIC_FILE);
				// ׼���ϴ�ͼƬ��������
				tempPicture = new File(
						Environment.getExternalStorageDirectory()
								+ File.separator + TEMP_PIC_FILE);
				pictureHttpUpload = new HttpupLoad(APPConstant.getUSERURL(),
						json2, handler, 1, this, tempPicture);
				pictureHttpUpload.execute();
				break;
			default:
				break;
			}
		}
	}

	public void showPicturePicker(Context context, boolean isCrop) {
		final boolean crop = isCrop;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("ͼƬ��Դ");
		builder.setNegativeButton("ȡ��", null);
		builder.setItems(new String[] { "ʹ��Ĭ��ͼ��", "���", "����" },
				new DialogInterface.OnClickListener() {
					// ������
					int REQUEST_CODE;

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DEFAULT_PICTURE:
							Intent intent_1 = new Intent();
							intent_1.putExtra("sex", sex_type);
							intent_1.putExtra("partofUrl", partofUrl);
							intent_1.putExtra("accountNum", accountNum);
							intent_1.putExtra("password", password);
							intent_1.setClass(getApplicationContext(),
									PictureSelectActivity.class);
							startActivity(intent_1);
							finish();
							break;

						case CHOOSE_PICTURE:
							Intent openAlbumIntent = new Intent(
									Intent.ACTION_GET_CONTENT);
							if (crop) {
								REQUEST_CODE = CROP;
							} else {
								REQUEST_CODE = CHOOSE_PICTURE;
							}
							openAlbumIntent
									.setDataAndType(
											MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
											IMAGE_UNSPECIFIED);
							startActivityForResult(openAlbumIntent,
									REQUEST_CODE);
							break;

						case TAKE_PICTURE:
							Uri imageUri = null;
							String fileName = null;
							Intent openCameraIntent = new Intent(
									MediaStore.ACTION_IMAGE_CAPTURE);
							if (crop) {
								REQUEST_CODE = CROP;
								ImageTools.deletePhotoAtPathAndName(Environment
										.getExternalStorageDirectory()
										.getAbsolutePath(), TEMP_PIC_FILE);

								// ���汾�ν�ͼ��ʱ�ļ�����
								fileName = TEMP_PIC_FILE;
							} else {
								REQUEST_CODE = TAKE_PICTURE;
								fileName = TEMP_PIC_FILE;
							}
							imageUri = Uri.fromFile(new File(Environment
									.getExternalStorageDirectory(), fileName));
							// ָ����Ƭ����·����SD������TEMP_PIC_FILEΪһ����ʱ�ļ���ÿ�����պ����ͼƬ���ᱻ�滻
							openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
									imageUri);
							startActivityForResult(openCameraIntent,
									REQUEST_CODE);
							break;

						default:
							break;
						}
					}
				});
		builder.create().show();
	}

	/**
	 * ����uri��ȡͼƬ ���out of memory����
	 * 
	 * @param uri
	 * @param size
	 *            ����size������Ϊ�ǿ�͸ߵ����ֵ
	 * @return
	 */
	public Bitmap getThumbnail(Uri uri, int size) {
		try {
			InputStream input = getContentResolver().openInputStream(uri);
			BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
			onlyBoundsOptions.inJustDecodeBounds = true;
			onlyBoundsOptions.inDither = true;// optional
			onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional
			BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
			input.close();
			if ((onlyBoundsOptions.outWidth == -1)
					|| (onlyBoundsOptions.outHeight == -1))
				return null;
			int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight
					: onlyBoundsOptions.outWidth;
			double ratio = (originalSize > size) ? (originalSize / size) : 1.0;
			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
			bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
			bitmapOptions.inDither = true;// optional
			bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional
			input = getContentResolver().openInputStream(uri);
			Bitmap bitmap = BitmapFactory.decodeStream(input, null,
					bitmapOptions);
			input.close();
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static int getPowerOfTwoForSampleRatio(double ratio) {
		int k = Integer.highestOneBit((int) Math.floor(ratio));
		if (k == 0)
			return 1;
		else
			return k;
	}

	// ��ȡͼƬ
	public void cropImage(Uri uri, int outputX, int outputY, int requestCode) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", outputX);
		intent.putExtra("outputY", outputY);
		intent.putExtra("outputFormat", "JPEG");
		intent.putExtra("noFaceDetection", true);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, requestCode);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	// ��ȡ�޸���Ϣ
	public JSONObject getUpdateInfo() {

		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("accountNum", accountNum);
			// jsonObject.put("sex", user.getSex());
			jsonObject.put("name", et_name.getText());//����֤�û�ʹ��edittext
			jsonObject.put("phoneNum", phoneNum_tv.getText());
			jsonObject.put("password", password);
//			jsonObject.put("newPassword", password);
			// jsonObject.put("picture", "2");
			jsonObject.put("address", address_tv.getText());
			jsonObject.put("sign", sign_tv.getText());
			jsonObject.put("email", email_tv.getText());

			// jsonObject.put("email", email_tv.getText());
			// jsonObject.put("classmates", "classmates");
			return jsonObject;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			return null;
		}
	}

	public JSONArray arraytoJSon(String[] list) {
		try {
			JSONArray jsonarray = new JSONArray();

			for (int i = 0; i < list.length; i++) {
				jsonarray.put(i, list[i]);
			}
			return jsonarray;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			return null;
		}
	}

	/**
	 * ���±��ص��û���Ϣ
	 */
	public void updateLocalContactsInfo() {
		try {
			boolean flag = false;
			ClassmateDao classmateDao = new ClassmateDao(this);
			// ��ȡ�������ĵĵ�¼�û���Ϣ
			ContactsEntity contactsEntity = dataCenterManagerService
					.getUserSelfContactsEntity();

			//����֤�û������޸�����
			String auth = contactsEntity.getAuthenticated();
			if (auth == null || !auth.equals("1")) {
				String name = et_name.getText().toString();
//				if (name != null && !name.equals("")) {
					contactsEntity.setName(name);
//				}
			}
			
			String phoneNum = phoneNum_tv.getText().toString();
//			if (phoneNum != null && !phoneNum.equals("")
//					&& isMobileNO(phoneNum)) {
				contactsEntity.setPhoneNum(phoneNum);
//			}

			String adddress = address_tv.getText().toString();
//			if (adddress != null && !adddress.equals("")) {
				contactsEntity.setAddress(adddress);
//			}

			String sign = sign_tv.getText().toString();
			
//			if (sign != null && !sign.equals("")) {
				contactsEntity.setSign(sign);
				flag = true;
//			}

			String email = email_tv.getText().toString();
//			if (email != null && !email.equals("") && isEmailFromat(email)) {
				contactsEntity.setEmail(email);
//			}

			dataCenterManagerService.updateSelfContactsEntity(contactsEntity, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isMobileNO(String mobiles) {
		if (mobiles == null) {
			return false;
		}
		Pattern p = Pattern
				.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9])|(14[5,7])|(17[0-9]))\\d{8}$");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}

	/**
	 * �ַ������� ^ ��ƥ������Ŀ�ʼλ�á� \������һ���ַ����Ϊ�����ַ�������ֵ�� ��ƥ��ǰһ���ַ���λ򼸴Ρ� + ��ƥ��ǰһ���ַ�һ�λ��Ρ�
	 * (pattern) ��ģʽƥ�䲢��סƥ�䡣 x|y��ƥ�� x �� y�� [a-z] ����ʾĳ����Χ�ڵ��ַ�����ָ�������ڵ��κ��ַ�ƥ�䡣 \w
	 * �����κε����ַ�ƥ�䣬�����»��ߡ�
	 * 
	 * {n,m} ����ƥ�� n �������ƥ�� m �� $ ��ƥ������Ľ�β��
	 */

	public static boolean isEmailFromat(String email) {
		if (email == null) {
			return false;
		}
		Pattern p = Pattern
				.compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
		Matcher m = p.matcher(email);
		return m.matches();
	}
}