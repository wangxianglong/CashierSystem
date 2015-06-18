package com.shengxun.cashiersystem;

import java.util.ArrayList;

import net.tsz.afinal.http.AjaxCallBack;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.shengxun.cashiersystem.app.ApplicationCS;
import com.shengxun.constant.C;
import com.shengxun.customview.LDialog;
import com.shengxun.entity.ProductInfo;
import com.shengxun.externalhardware.led.JBLEDInterface;
import com.shengxun.util.CheckVersionManager;
import com.shengxun.util.ConnectManager;
import com.zvezda.android.utils.AppManager;
import com.zvezda.android.utils.BaseUtils;
import com.zvezda.android.utils.JSONParser;
import com.zvezda.android.utils.LG;
import com.zvezda.data.utils.DataSP;

/**
 * 设置界面
 * 
 * @author sw
 * @date 2015-5-15
 */
public class SettingActivity extends MyTimeLockBaseActivity {

	Button btn_back, btn_open, btn_close;

	Switch sw_open;

	private TextView check_new_app;

	private TextView click_to_update_all_product;

	private TextView click_to_update_lock_psd;

	String update_lock_psd_first_time = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cashier_setting_view);
		initWidget();
	}

	private void initWidget() {

		btn_back = (Button) findViewById(R.id.cashier_setting_back);
		btn_open = (Button) findViewById(R.id.cashier_setting_btn_open);
		btn_close = (Button) findViewById(R.id.cashier_setting_btn_close);

		check_new_app = (TextView) findViewById(R.id.check_new_app);
		click_to_update_all_product = (TextView) findViewById(R.id.click_to_update_all_product);
		click_to_update_lock_psd = (TextView) findViewById(R.id.click_to_update_lock_psd);
		btn_open.setOnClickListener(myclick);
		btn_close.setOnClickListener(myclick);
		btn_back.setOnClickListener(myclick);
		check_new_app.setOnClickListener(myclick);
		click_to_update_all_product.setOnClickListener(myclick);
		click_to_update_lock_psd.setOnClickListener(myclick);
	}

	OnClickListener myclick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.cashier_setting_back:
				AppManager.getAppManager().finishActivity(mActivity);
				break;
			// 打开客显
			case R.id.cashier_setting_btn_open:
				JBLEDInterface.openLed();
				break;
			// 关闭客显
			case R.id.cashier_setting_btn_close:
				JBLEDInterface.closeLed();
				break;
			// 检测软件更新
			case R.id.check_new_app: {
				CheckVersionManager.checkVersion(mActivity, true);
			}
				break;
			// 更新数据
			case R.id.click_to_update_all_product: {
				// 更新所有数据
				C.openProgressDialog(mActivity, null, "正在同步所有产品数据信息，请耐心等待...");
				ConnectManager.getInstance().getProductList("",
						productAjaxCallBack);
			}
				break;
			//更改锁屏密码
			case R.id.click_to_update_lock_psd: {
//				updateLockPsd(1);
				C.showDialogAlert("暂时不可用", mActivity);
			}
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 修改锁屏密码 index 代表执行的次数1：输入原密码，2：新密码，3：新密码确认
	 * 
	 * @auth shouwei
	 */
	private void updateLockPsd(int index) {
		final int fIndex = index;
		String hint = null;
		switch (index) {
		case 1:
			hint = "输入原密码";
			break;
		case 2:
			hint = "输入新密码";
			break;
		case 3:
			hint = "确认密码";
			break;
		default:
			break;
		}
		C.showDialogAlert("修改锁屏密码", true, hint, index != 3 ? "下一步" : "完成",
				mActivity, new UpdatePsdListener() {
					@Override
					public void callBack(String psd, AlertDialog dialog) {
						switch (fIndex) {
						case 1:
							if (psd.trim().equals(C.CURRENT_LOCK_PSD)) {
								updateLockPsd(2);
								dialog.dismiss();
							} else {
								C.showDialogAlert("密码不匹配", mActivity);
							}
							break;
						case 2:
							if (BaseUtils.IsNotEmpty(psd.trim())) {
								update_lock_psd_first_time = psd;
								dialog.dismiss();
								updateLockPsd(3);
							} else {
								C.showDialogAlert("密码不能为空", mActivity);
							}
							break;
						case 3:
							if (psd.equals(update_lock_psd_first_time)) {
								C.showLong("修改成功", getApplicationContext());
								C.CURRENT_LOCK_PSD = psd;
								DataSP sp = new DataSP(mActivity,
										C.SHARED_PREFENCE_NAME);
								sp.setValue(C.SHARED_LOCK_PSD, psd);
								dialog.dismiss();
							} else {
								C.showDialogAlert("两次输入不一致", mActivity);
							}
							break;
						default:
							break;
						}
					}
				});
	}

	private AjaxCallBack<String> productAjaxCallBack = new AjaxCallBack<String>() {

		@SuppressWarnings("unchecked")
		@Override
		public void onSuccess(String t) {
			super.onSuccess(t);
			LG.i(getClass(), "所有数据----->"+t);
			try {
				if (BaseUtils.IsNotEmpty(t)
						&& JSONParser.getStringFromJsonString("status", t)
								.equals("1")) {
					String data = JSONParser.getStringFromJsonString("data", t);
					String product_list = JSONParser.getStringFromJsonString(
							"product_list", data);
					String last_syn_time = JSONParser.getStringFromJsonString(
							"last_syn_time", data);
					ArrayList<ProductInfo> products = (ArrayList<ProductInfo>) JSONParser
							.JSON2Array(product_list, ProductInfo.class);
					if (products != null && products.size() > 0) {
						ormOpearationDao.deleteThisTable(ProductInfo.class);// 删除当前表
						Dao<ProductInfo, Integer> productsDao = ormOpearationDao
								.getDao(ProductInfo.class);
						LG.i(ApplicationCS.class, "收银系统手动产品信息数据全部更新");
						// productsDao.executeRawNoArgs("DELETE FROM productInfosTable");//删除所有数据
						for (ProductInfo entity : products) {
							productsDao.create(entity);
						}
						applicationCS.sp.setValue(ApplicationCS.LAST_SYN_TIME,
								last_syn_time);
						LDialog.openMessageDialog("产品信息数据全部更新成功!", false,
								mActivity);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			C.closeProgressDialog();
		}

		@Override
		public void onFailure(Throwable t, int errorNo, String strMsg) {
			super.onFailure(t, errorNo, strMsg);
			C.closeProgressDialog();
			LDialog.openMessageDialog("产品信息数据全部更新失败,请稍后再试!", false, mActivity);
		}

	};

	/**
	 * 修改密码回调监听
	 * 
	 * @author sw
	 * @date 2015-6-16
	 */
	public interface UpdatePsdListener {
		public void callBack(String psd, AlertDialog dialog);
	}
}
