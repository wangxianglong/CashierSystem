package com.shengxun.cashiersystem;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.j256.ormlite.dao.Dao;
import com.shengxun.adapter.CashierGoodsListAdapter;
import com.shengxun.cashiersystem.app.ApplicationCS;
import com.shengxun.constant.C;
import com.shengxun.entity.ProductInfo;
import com.shengxun.externalhardware.cashbox.JBCashBoxInterface;
import com.shengxun.externalhardware.led.JBLEDInterface;
import com.shengxun.externalhardware.print.util.JBPrintInterface;
import com.shengxun.util.CheckVersionManager;
import com.zvezda.android.utils.AppManager;
import com.zvezda.android.utils.BaseUtils;
import com.zvezda.android.utils.LG;
import com.zvezda.android.utils.TimeConversion;

/**
 * 模块描述：收银系统主页 2015-4-20 下午3:29:28 Write by LILIN
 */
@SuppressLint("HandlerLeak")
public class MainActivity extends MyTimeLockBaseActivity {

	public static MainActivity instance = null;
	// 设置
	private ImageView cashier_system_machine_setting = null;
	// 退出
	private ImageView cashier_system_machine_exit = null;
	//锁屏
	private ImageView cashier_system_machine_lock = null;
	// 机器是否联网
	private TextView cashier_system_machine_status = null;
	// 确定输入的条形码
	private TextView cashier_system_btn_ok = null;
	// 收银员
	private TextView cashier_system_clerk = null;
	// 收款
	private TextView cashier_system_receive_payments = null;
	// 打开钱箱
	private TextView cashier_system_open_cashbox = null;
	// 提货
	private TextView cashier_system_get_good = null;
	// 退货
	private TextView cashier_system_return_good = null;
	// 搜索订单
	private TextView cashier_system_search_order = null;
	//上传小票
	private TextView cashier_system_upload_ticket = null;
	//中心
	private TextView cashier_system_center_name=null;

	// 当前时间
	private TextView cashier_system_machine_time = null;
	// 商品条码
	private EditText cashier_system_business = null;
	// 当前用户购物商品清单
	private ArrayList<ProductInfo> dataList = new ArrayList<ProductInfo>();
	private CashierGoodsListAdapter cashierGoodsListAdapter = null;
	private ListView cashier_listview = null;
	private Timer timerUpdate = null;
	/**
	 * 产品数据库
	 */
	private Dao<ProductInfo, Integer> productsDao = null;
	
//	ComponentName componentName;
//	DevicePolicyManager policyManager;
	
	public boolean isOpenPrint=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		instance = this;
		
		//检查版本更新
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				CheckVersionManager.checkVersion(mActivity, true);
			}
		}, 1000);
		initWidget();
		initExternalHardware();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if(isOpenPrint==false){
					initExternalHardware();
				}
			}
		}, 3000);
		
	}
	/**
	 * 初始化硬件设备
	 */
	private void initExternalHardware(){
		//开客显
		if(!JBLEDInterface.openLed()){
			JBLEDInterface.closeLed();
			JBLEDInterface.openLed();
		}
		//开打印机
		if(!JBPrintInterface.openPrinter()){
			JBPrintInterface.closePrinter();
			isOpenPrint=JBPrintInterface.openPrinter();
		}
	}
	private void initWidget() {
		cashier_system_machine_setting = (ImageView) this.findViewById(R.id.cashier_system_machine_setting);
		cashier_system_machine_exit = (ImageView) this.findViewById(R.id.cashier_system_machine_exit);
		cashier_system_machine_lock = (ImageView) this.findViewById(R.id.cashier_system_machine_lock);
		
		cashier_system_clerk = (TextView) this.findViewById(R.id.cashier_system_clerk);
		cashier_system_btn_ok = (TextView) this.findViewById(R.id.cashier_system_btn_ok);
		cashier_system_receive_payments = (TextView) this.findViewById(R.id.cashier_system_receive_payments);
		cashier_system_open_cashbox = (TextView) this.findViewById(R.id.cashier_system_open_cashbox);
		cashier_system_get_good = (TextView) this.findViewById(R.id.cashier_system_get_good);
		cashier_system_return_good = (TextView) this.findViewById(R.id.cashier_system_return_good);
		cashier_system_search_order = (TextView) this.findViewById(R.id.cashier_system_search_order);
		cashier_system_upload_ticket = (TextView) findViewById(R.id.cashier_system_upload_tichet);
		cashier_system_machine_status = (TextView) this.findViewById(R.id.cashier_system_machine_status);
		cashier_system_machine_time = (TextView) this.findViewById(R.id.cashier_system_machine_time);
		cashier_system_center_name = (TextView) this.findViewById(R.id.cashier_system_center_name);
		cashier_system_business = (EditText) this.findViewById(R.id.cashier_system_business);
		cashier_system_business.setOnEditorActionListener(new OnEditorActionListener() {

					@Override
					public boolean onEditorAction(TextView v, int actionId,KeyEvent event) {
						checkedThisGoodCode();
						LG.e(getClass(),"actionId---->"+actionId);
						return true;
					}
				});

		cashier_listview = (ListView) this.findViewById(R.id.cashier_listview);
		cashier_system_machine_setting.setOnClickListener(onClickListener);
		cashier_system_machine_exit.setOnClickListener(onClickListener);
		cashier_system_machine_lock.setOnClickListener(onClickListener);
		cashier_system_btn_ok.setOnClickListener(onClickListener);
		cashier_system_receive_payments.setOnClickListener(onClickListener);
		cashier_system_open_cashbox.setOnClickListener(onClickListener);
		cashier_system_get_good.setOnClickListener(onClickListener);
		cashier_system_return_good.setOnClickListener(onClickListener);
		cashier_system_search_order.setOnClickListener(onClickListener);
		cashier_system_upload_ticket.setOnClickListener(onClickListener);
		cashier_listview.setOnItemClickListener(myItemClick);
		initWidgetData();
	}

	private void checkedThisGoodCode() {
		String op_bar_code = cashier_system_business.getText().toString();
		if (BaseUtils.IsNotEmpty(op_bar_code)) {
			try {
				productsDao =ormOpearationDao.getDao(ProductInfo.class);
				//产品条码存在且状态正常为上架商品中的销售品
				ArrayList<ProductInfo> productInfos = (ArrayList<ProductInfo>) productsDao.queryBuilder().where().eq("op_bar_code", op_bar_code).and().eq("op_status", "1").and().eq("op_is_for_show", "0").query();
				// 查询到数据且唯一
				LG.i(getClass(), "productInfos size = >"+productInfos.size()+"");
				if (productInfos != null && productInfos.size() == 1) {
					LG.e(getClass(), "productInfos.get(0).qp_name"+ productInfos.get(0).qp_name);
					refreshData(productInfos.get(0));
				}else{
					//数据库没有的商家都为非合作商品,也可进行扫码付款2
					//C.showDialogAlert(resources.getString(R.string.cashier_system_alert_no_have_product), mActivity);
					ProductInfo mProduct = new ProductInfo();
					//条码
					String code = cashier_system_business.getText().toString().trim();
					mProduct.op_bar_code = code;
					mProduct.op_market_price=0.0;
					mProduct.op_promote_market_price = 0.0;
					mProduct.qp_name = code;
					mProduct.isProductInSystem = false;
					refreshData(mProduct);
					goActivity(GoodsDetailActivity.class, mProduct);
				}
				cashier_system_business.setText("");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void initWidgetData() {
		if (applicationCS.loginInfo != null) {
			if(applicationCS.loginInfo.user_info != null){
				String clerk_name="Hi,";
				if(BaseUtils.IsNotEmpty(applicationCS.loginInfo.user_info.me_realname)){
					clerk_name+=applicationCS.loginInfo.user_info.me_realname;
					
				}else{
					clerk_name+=applicationCS.loginInfo.user_info.me_username;
				}
				cashier_system_clerk.setText(clerk_name);
			}
			if(applicationCS.loginInfo.cashier_info != null){
				cashier_system_center_name.setText(""+applicationCS.loginInfo.cashier_info.rs_code_name+"("+"机号:" + applicationCS.mc_id+")");
			}
		}
		if (BaseUtils.isNetworkAvailable(mActivity)) {
			cashier_system_machine_status.setText(Html.fromHtml(resources
					.getString(R.string.cashier_system_machine_status)
					+ BaseUtils.getColorHtmlText("已联网", "#FF0000")));
		} else {
			cashier_system_machine_status.setText(Html.fromHtml(resources
					.getString(R.string.cashier_system_machine_status)
					+ BaseUtils.getColorHtmlText("无网络", "#5E5E5E")));
		}
		cashier_system_machine_time.setText(""
				+ TimeConversion.getSystemAppTimeCN());
		timerUpdate = new Timer();
		timerUpdate.schedule(myMachineTime, 1000, 1000);
		cashierGoodsListAdapter = new CashierGoodsListAdapter(mActivity,
				dataList);
		cashier_listview.setAdapter(cashierGoodsListAdapter);
	}

	private void refreshData(ProductInfo entity) {
		entity.buy_number += 1;
		boolean isHaved = false;
		for (int i = 0; i < dataList.size(); i++) {
			if (dataList.get(i).op_bar_code.equals(entity.op_bar_code)) {
				dataList.get(i).buy_number += 1;
				isHaved = true;
			}
		}
		if (!isHaved) {
			dataList.add(entity);
		}
		cashierGoodsListAdapter.notifyDataSetChanged();
		refreshNowTotal();
	}

	private void refreshNowTotal() {
		// 购买商品总价
		double totalPayment = 0;
		for (int i = 0; i < dataList.size(); i++) {
			// 每个商品的总价相加
			totalPayment += dataList.get(i).buy_number*dataList.get(i).op_market_price;
			
		}
		// 刷新付款总金额
		cashier_system_receive_payments.setText(resources.getString(R.string.cashier_system_receive_payments)
				+ "￥"
				+ BaseUtils.formatDouble(totalPayment) + "");
	}

	Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			cashier_system_machine_time.setText(""+ TimeConversion.getSystemAppTimeCN());
		}

	};
	TimerTask myMachineTime = new TimerTask() {
		@Override
		public void run() {
			myHandler.sendEmptyMessage(0);
		}
	};

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			// 收款
			case R.id.cashier_system_receive_payments: {
				if (dataList != null && dataList.size() > 0) {
					goActivity(GatheringActivity.class, dataList);
				} else {
					C.showDialogAlert(resources.getString(R.string.cashier_system_alert_no_product),mActivity);
				}

			}
				break;
			// 打开钱箱
			case R.id.cashier_system_open_cashbox: {
				JBCashBoxInterface.openCashBox();
			}
				break;
			// 提货
			case R.id.cashier_system_get_good: {
				goActivity(GoodsPickupActivity.class);
			}
				break;
			// 退货
			case R.id.cashier_system_return_good: {
				goActivity(GoodsReturnActivity.class);
			}
				break;
			// 查单
			case R.id.cashier_system_search_order: {
				goActivity(SearchOrderActivity.class);
			}
				break;
			//上传小票
			case R.id.cashier_system_upload_tichet:
				break;
			// 设置
			case R.id.cashier_system_machine_setting: {
				goActivity(SettingActivity.class);
			}
				break;
			// 退出
			case R.id.cashier_system_machine_exit: {
				LoginActivity.isLoadingData=false;
				goActivity(LoginActivity.class);
				LoginActivity.setIsLoginIn(false);
				applicationCS.loginInfo = null;
				AppManager.getAppManager().finishActivity(mActivity);
			}
				break;
			case R.id.cashier_system_machine_lock:{
//				LockScreenUtil.getInstance(mActivity).mylock();
				goLockActivity();
			}
				break;
			// 确定条形码
			case R.id.cashier_system_btn_ok: {
				checkedThisGoodCode();
			}
				break;
			}
		}

	};

	/**
	 * @param entity
	 * 删除该商品
	 */
	public void deleteGoods(ProductInfo entity) {
		for (int i = 0; i < dataList.size(); i++) {
			if (dataList.get(i).op_bar_code.equals(entity.op_bar_code)) {
				dataList.remove(i);
				cashierGoodsListAdapter.notifyDataSetChanged();
				refreshNowTotal();
				break;
			}
		}
	}

	/**
	 * @param entity
	 *            修改该商品数量
	 */
	public void updateGoods(ProductInfo entity) {
		for (int i = 0; i < dataList.size(); i++) {
			if (dataList.get(i).op_bar_code.equals(entity.op_bar_code)) {
				dataList.get(i).buy_number = entity.buy_number;
				dataList.get(i).op_market_price = entity.op_market_price;
				cashierGoodsListAdapter.notifyDataSetChanged();
				refreshNowTotal();
				break;
			}
		}
	}
	/**
	 *    清空已购商品
	 */
	public void clearGoods() {
		dataList.clear();
		cashierGoodsListAdapter.notifyDataSetChanged();
		refreshNowTotal();
	}
	
	/**
	 * item点击事件
	 */
	OnItemClickListener myItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int postion,
				long arg3) {
			Log.i("savion","buy number" +dataList.get(postion));
			goActivity(GoodsDetailActivity.class, dataList.get(postion));
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		JBLEDInterface.closeLed();
		JBPrintInterface.closePrinter();
		JBCashBoxInterface.closeCashBox();
		//关闭计时器
		mLock.closeTimer();
		ormOpearationDao.closeDataHelper();
	}
	
}
