package bok.mitake.com.bokmbank.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import bok.mitake.com.bokmbank.BuildConfig;
import bok.mitake.com.bokmbank.FCM.FCMPushData;
import bok.mitake.com.bokmbank.FCM.MyFirebaseMessagingService;
import bok.mitake.com.bokmbank.R;
import bok.mitake.com.bokmbank.conn.BokUrl;
import bok.mitake.com.bokmbank.util.DialogHelper;
import bok.mitake.com.bokmbank.util.KikiUtil;
import bok.mitake.com.bokmbank.util.LifecycleCallbacks;
import bok.mitake.com.bokmbank.util.LogUtils;
import bok.mitake.com.bokmbank.util.SharedPreferencesUtil;
import bok.mitake.com.bokmbank.util.TamperDetectionUtils;
import bok.mitake.com.bokmbank.util.UtilHelper;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

import static bok.mitake.com.bokmbank.activity.Main.EXTRA_KEY_IS_PUSH_LOGIN;
import static bok.mitake.com.bokmbank.util.Constant.EXTRA_ERROR_CODE;
import static bok.mitake.com.bokmbank.util.Constant.EXTRA_ERROR_MSG;
import static bok.mitake.com.bokmbank.util.Constant.EXTRA_IS_ROOT;
import static bok.mitake.com.bokmbank.util.Constant.ROOT_TO_LOCALBROADCAST;

import com.google.gson.Gson;


/**
 * Splash
 * 高雄銀 程式 啟動 過場動畫
 * */
public class Splash extends BaseActivity {

	private final int STORAGE_PERMISSION = 5;
	/**
	 * 一般開啟
	 * @param context
	 * @return
	 */
	public static Intent getStartActivityIntent(Context context) {
		return new Intent(context, Splash.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtils.d("BOKTest","Splash oncreate");
		// 防截圖 客戶希望關閉
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
		setTheme(R.style.SplashTheme);

		setContentView(R.layout.activity_splash2);

		//if (BuildConfig.DEBUG)
		//	SharedPreferencesUtil.removeStringData(this,ID_NO_REMEMBER);

		// 鎖定 直立
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		if (!isRootDetectionRegistered) {
			LogUtils.d("5555", "reg rootDetectionReceiver");
			LocalBroadcastManager.getInstance(this).registerReceiver(rootDetectionReceiver, new IntentFilter(ROOT_TO_LOCALBROADCAST));
			isRootDetectionRegistered = true;
		}
		//移除基金單筆申購(11611)
		//SharedPreferencesUtil.removeItem(this,/*getString(R.string.buy_fund)*/"11611");
		//test root detect
		//if (BuildConfig.DEBUG) {
		//	BokUrl.Emulator_D1();
		//	return;
		//}
		LifecycleCallbacks.getInstance().setCurrAct(Splash.this);
		//fixme 正式版若未包arxan會無法進入首頁
		if (BuildConfig.DEBUG || BuildConfig.isSecurityTest)
			onActivityContinue();
		else {
			getFlag();
			//requestPermission();
		}
		//else
		//	willDetectRoot();
		//release

	}

	/** for axran
	 * willDetectRoot
	 * 				-> detect 		-> Root_D1			-> send receiver ->	check Extra isRoot -> alert
	 * 				-> non detect	-> nonTamperAction  -> send receiver -> check Extra !isRoot -> Splash onActivityContinue()
	 */
	//for axran
	private void willDetectRoot(){}
	//for axran nonTamperAction
	public static void nonTamperAction(){
		if (LifecycleCallbacks.getInstance().getCurrAct() != null) {
			Intent intent = new Intent(ROOT_TO_LOCALBROADCAST);
			//intent.putExtra(EXTRA_ERROR_MSG, "");
			intent.putExtra(EXTRA_IS_ROOT, false);
			intent.setPackage(LifecycleCallbacks.getInstance().getCurrAct().getPackageName());
			LocalBroadcastManager.getInstance(LifecycleCallbacks.getInstance().getCurrAct()).sendBroadcast(intent);
		}
	}
	private ISplash mListener = new ISplash() {
		@Override
		public void onCallback(Boolean isRoot, String rtnMsg, String rtnCode) {
			Intent intent = new Intent(ROOT_TO_LOCALBROADCAST);
			intent.putExtra(EXTRA_ERROR_MSG, rtnMsg);
			intent.putExtra(EXTRA_IS_ROOT, true);
			intent.putExtra(EXTRA_ERROR_CODE, rtnCode);
			intent.setPackage(getPackageName());
			LocalBroadcastManager.getInstance(Splash.this).sendBroadcast(intent);
		}
	};
	private void getFlag(){
		LogUtils.d("tryGetRootFlag",String.valueOf(BokUrl.getIsRoot()));
		if (BokUrl.getIsRoot() != null && BokUrl.getIsRoot()){
			BokUrl.Root_D1(mListener, this);
		}else{
			//等1秒沒得到root就當沒root
			TamperDetectionUtils.wait(1, new Function0<Unit>() {
				@Override
				public Unit invoke() {
					if (BokUrl.getIsRoot() != null && BokUrl.getIsRoot()){
						BokUrl.Root_D1(mListener, Splash.this);
					}else{
						mListener.onCallback(false,"","");
					}
					return null;
				}
			});
		}
	}
	@Override
	protected void onActivityContinue() {
		super.onActivityContinue();
		requestPermission();
		//openMainPage();
	}

	private void requestPermission(){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION);
		}else
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION);

	}
	/*private void requestPermission1(){
		//請求偵測螢幕截圖及裝置設備權限
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
			goInitSplash();
			//requestNotifyPermissions();
		} else {
			//ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE}, STORAGE_PERMISSION);
			//for api 33
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_PHONE_STATE}, STORAGE_PERMISSION);
			}else
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE}, STORAGE_PERMISSION);
		}
	}*/

	private final int REQUEST_CODE_NOTIFICATION_PERMISSIONS = 3001;
	private void requestNotifyPermissions(){
		//goInitSplash();
		//for api 33
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			int p = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS);
			if (p == PackageManager.PERMISSION_GRANTED) {
				goInitSplash();
			}else
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION_PERMISSIONS);
		}else
			goInitSplash();
	}
	private void goInitSplash() {
		//if (BuildConfig.DEBUG)
		//	openMainPage();
		//else
			playSplash();

		// 初始化加密元件
		KikiUtil.initKiki(this);
	}

	private void playSplash() {
		if (UtilHelper.isBackgroundRunning(this,Main.class)){
			videoOnCompletion();
			return;
		}
		String fileName = "android.resource://"+  getPackageName() + "/raw/splash";
		VideoView videoView = (VideoView) this.findViewById(R.id.videoView);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			videoView.setAudioFocusRequest(AudioManager.AUDIOFOCUS_NONE);
		}
		videoView.setVideoURI(Uri.parse(fileName));
		videoView.setMediaController(new MediaController(Splash.this));
		videoView.requestFocus();
		videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
//				dismissLoading();
				mp.start();

				new Thread() {

					public void run() {
						// for Arxan
						Splash_Thread();
					}
				}.start();
			}
		});
		videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
//				dismissLoading();
				return true;
			}
		});
		videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				videoOnCompletion();
			}
		});
		videoView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true; // Touch時不顯示toolbar
			}
		});
	}
	private void videoOnCompletion(){
		String permissionResult = SharedPreferencesUtil.getStringData(Splash.this, SharedPreferencesUtil.APP_PERMISSION, "N");
		if ("N".equals(permissionResult)) {
			openPermissionDialog();
		} else {
			openMainPage();
		}
	}
	private void openPermissionDialog() {
		AlertDialog.Builder b =  DialogHelper.createAlertDialogBuilder(Splash.this,
				getString(R.string.dialog_permission_title),
				getString(R.string.dialog_permission_content2),
				getString(R.string.dialog_permission_agree),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedPreferencesUtil.saveStringData(Splash.this, SharedPreferencesUtil.APP_PERMISSION, "Y");
						openMainPage();
					}
				});
		if (b != null)
			b.show();
		else{
			SharedPreferencesUtil.saveStringData(Splash.this, SharedPreferencesUtil.APP_PERMISSION, "Y");
			openMainPage();
		}
	}

	private void openMainPage() {
		//要判斷是否有開啟無障礙功能 會導向不同的頁面
		AccessibilityManager accessibiltyManager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
		//boolean isAccessibilityEnabled = accessibiltyManager.isEnabled(); // 單純語音開啟，但Talkback可能是關閉的，所以不能用這個
		boolean isExploreByTouchEnabled = accessibiltyManager.isTouchExplorationEnabled();

		Intent intent = null;
		if (isExploreByTouchEnabled) {
			intent = WebActivity.getStartActivityIntent(Splash.this, BuildConfig.accessibilityUrl);
		} else {
			/*if (BuildConfig.DEBUG) {
				intent = Main2022Activity.Companion.getStartActivityIntent(Splash.this, false);
			}else*/
			Bundle bundle = getIntent().getExtras();
			if (bundle != null && bundle.getString(MyFirebaseMessagingService.KEY_MESSAGE_ID) != null) {
				FCMPushData pushData = new FCMPushData();
				pushData.setMessageId(bundle.getString(MyFirebaseMessagingService.KEY_MESSAGE_ID));
				pushData.setLogin(bundle.getString(MyFirebaseMessagingService.KEY_IS_LOGIN));
				pushData.setParam(bundle.getString(MyFirebaseMessagingService.KEY_PARAM));
				pushData.setWfid(bundle.getString(MyFirebaseMessagingService.PARAM_KEY_WFID));

				Log.d("FCM","Splash checkFCMData"+new Gson().toJson(pushData));
				Intent i = getIntent();
				i.putExtra(EXTRA_KEY_IS_PUSH_LOGIN, false);
				i.putExtra(EXTRA_KEY_FCM_PUSH_DATA, pushData);
				if (checkFCMData(this,i)){
					finish();
					return;
				}
			} else {

				Log.d("FCM","Main");

				intent = Main.getStartActivityIntent(Splash.this, false);
			}
		}
		startActivity(intent);

		finish();
	}

	// OnKeyDown ---> 禁用 [Back] 鍵
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		// 禁用 [Back] 鍵
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			return false;
		}

		return super.onKeyDown(keyCode, event);
	}

	private static void Splash_Thread() {
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

		switch (requestCode) {
			case REQUEST_CODE_NOTIFICATION_PERMISSIONS:
				goInitSplash();
				break;
			case STORAGE_PERMISSION:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					//若無儲存空間權限仍允許使用App
					goInitSplash();
					//requestNotifyPermissions();
				} else if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
					DialogHelper.createAlertDialogBuilderAndShow(this, getString(R.string.dialog_title), getString(R.string.dialog_phone_state_permission_msg),
							"確定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									finish();
								}
							});
				}

		}
	}
	public interface ISplash{
		void onCallback(Boolean isRoot, String rtnMsg, String rtnCode);
	}
}
