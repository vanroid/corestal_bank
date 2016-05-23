package com.corestal.wpos.bank.main;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.DialogPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.corestal.wpos.bank.R;
import com.corestal.wpos.bank.biz.entity.FunctionMenu;
import com.corestal.wpos.bank.biz.entity.Order;
import com.corestal.wpos.bank.broadcast.HomeWatcherReceiver;
import com.corestal.wpos.bank.common.CSApplicationHolder;
import com.corestal.wpos.bank.service.MainService;
import com.corestal.wpos.bank.service.impl.MainServiceImpl;
import com.corestal.wpos.bank.view.adapter.OrderListAdapter;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.weipass.pos.sdk.Weipos;
import cn.weipass.pos.sdk.impl.WeiposImpl;

/**
 * 主Activity
 * create by cgz on 16-05-10
 */
public class MainActivity extends AppCompatActivity {
    /**
     * 取号按钮
     */
    @ViewInject(R.id.btn_take_no)
    private Button takeNoBtn;

    /**
     * 订单列表
     */
    @ViewInject(R.id.order_list_view)
    private ListView orderListView;

    /**
     * order List Adapter
     */
    private OrderListAdapter orderListAdapter;

    /**
     * 主要业务服务
     */
    private MainService mainService;

    /**
     * 订单数据
     */
    private List<Order> orderList;

    /**
     * Home事件监听器
     */
    private HomeWatcherReceiver homeWatcherReceiver;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Xutils view inject
        ViewUtils.inject(this);

        init();

        setListener();

        homeWatcherReceiver = new HomeWatcherReceiver();

        // 禁用home键
        registerReceiver(homeWatcherReceiver, new IntentFilter(
                Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

    }

    // TODO 此方法只会存在于开发阶段
    @Deprecated
    private void initTestData() {
        orderList = new ArrayList<Order>();
        Order order = null;
        for (int i = 0; i < 5; i++) {
            order = new Order();
            order.setOrderTime(new Date());
            order.setFunctionMenu(CSApplicationHolder.getFunctionMenu(3));
            order.setOrderNum("A"+i);
            orderList.add(order);
        }
    }

    private void init() {
        // 初始化数据
        mainService = new MainServiceImpl();
        mainService.initApplication();
        orderList = new ArrayList<Order>();
        // TODO 初始化时从数据库中取出数据
        orderListAdapter = new OrderListAdapter(this, orderList);
        orderListView.setAdapter(orderListAdapter);

        // 初始化wang pos
        WeiposImpl.as().init(this, new Weipos.OnInitListener() {
            @Override
            public void onInitOk() {
                WeiposImpl.as().speech("排队系统就绪");
            }

            @Override
            public void onError(String s) {
                Toast.makeText(MainActivity.this,"SDK初始化失败",Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 为View 设置事件监听
     */
    private void setListener() {
        orderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    /**
     * 取号按钮事件
     *
     * @param view
     */
    @OnClick(R.id.btn_take_no)
    public void takeNoBtnClick(View view) {
        showFunctionMenuDialog(view);
    }

    /**
     * 显示类型弹出层
     */
    private void showFunctionMenuDialog(View view) {
        final String[] fmNames = CSApplicationHolder.getFunctionMenuNames();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this).setTitle(R.string.function_menu_name).setItems(fmNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FunctionMenu  functionMenu = CSApplicationHolder.getFunctionMenuList().get(which);
                Order order = mainService.takeNo(functionMenu.getId());
                orderList.add(order);
                orderListAdapter.notifyDataSetChanged();
            }
        });
        Dialog dialog = dialogBuilder.create();
        // 半透明
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.alpha = 0.9f;
        dialog.getWindow().setAttributes(lp);
        dialog.show();
    }


}
