package com.weisen.xcxf.activity;

/**
 * Created by ZhangYunLong on 2016/6/23.
 */


        import java.util.ArrayList;
        import java.util.List;

        import android.app.AlertDialog;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.graphics.Color;
        import android.graphics.drawable.ColorDrawable;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.AdapterView.OnItemClickListener;
        import android.widget.ListView;
        import android.widget.TextView;

        import com.weisen.xcxf.Constant;
        import com.weisen.xcxf.R;
        import com.weisen.xcxf.adapter.MessageAdapter;
        import com.weisen.xcxf.bean.Notice;
        import com.weisen.xcxf.bean.NoticeDao;
        import com.weisen.xcxf.widget.PullToRefreshView;
        import com.weisen.xcxf.widget.PullToRefreshView.OnFooterRefreshListener;

public class MessageActivity extends BaseActivity {

    private ListView lv_message;
    private PullToRefreshView pl_refresh;
    private MessageAdapter messageAdapter;
    private List<Notice> noticeList, noticeList2;
    private UpdateMessageReceiver updateMessageReceiver;
    private NoticeDao noticeDao;
    private TextView tv_right;
    @Override
    protected void initView() {

        super.initView();
        setContentView(R.layout.activity_message);
        pl_refresh = (PullToRefreshView) findViewById(R.id.pl_refresh);
        pl_refresh.setEnablePullTorefresh(false);
        lv_message = (ListView) findViewById(R.id.lv_message);
        empty = findViewById(R.id.empty);
        lv_message.setEmptyView(empty);
        lv_message.setSelector(new ColorDrawable(Color.TRANSPARENT));
        initTitle();
        iv_left.setVisibility(View.GONE);
        // iv_left.setVisibility(View.VISIBLE);
        tv_right=(TextView)findViewById(R.id.tv_right);
        tv_right.setText("清空记录");
        tv_right.setVisibility(View.VISIBLE);
        tv_title.setText(getStringResource(R.string.title_message));
    }

    @Override
    protected void initData() {
        updateMessageReceiver = new UpdateMessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.BROADCAST_UPDATE_MESSAGE);
        registerReceiver(updateMessageReceiver, filter);

        pageNum = 1;
        pageSize = 10;
        noticeDao = new NoticeDao(getApplicationContext());
        noticeList = new ArrayList<Notice>();
        noticeList2 = noticeDao.getList(pageNum,pageSize);
        noticeList.addAll(noticeList2);
        messageAdapter = new MessageAdapter(this, noticeList);
        lv_message.setAdapter(messageAdapter);
        lv_message.setOnItemClickListener(new OnItemClickListener() {

                                              @Override
                                              public void onItemClick(AdapterView<?> arg0, View arg1,
                                                                      int position, long arg3) {

                                                  Intent intent = new Intent(MessageActivity.this,MessageDetailActivity.class);
                                                  intent.putExtra("id", noticeList.get(position).getId());
                                                  startActivity(intent);
                                              }

                                          }

        );
        lv_message.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                dialog1(position);

                return true;
            }

        });
        tv_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog2();

            }
        });


    }

    private void dialog2(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);  //先得到构造器
        builder.setTitle("提示"); //设置标题
        if(noticeList.size()<=0)
        {
            builder.setMessage("当前无记录！");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

        }
        else {
            builder.setMessage("是否确认删除全部记录" + "?"); //设置内容
//		builder.setIcon(R.mipmap.ic_launcher);//设置图标，图片id即可
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss(); //关闭dialog
                    noticeDao.deleteAllById();
                    noticeList2 = noticeDao.getList(pageNum, pageSize);
                    noticeList.clear();
                    noticeList.addAll(noticeList2);
                    messageAdapter.notifyDataSetChanged();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { //设置取消按钮
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                }
            });
        }
        //参数都设置完成了，创建并显示出来
        builder.create().show();

    }

    private void dialog1(final int position){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);  //先得到构造器
        builder.setTitle("提示"); //设置标题
        builder.setMessage("是否确认删除"+noticeList.get(position).getTitle()+"?"); //设置内容
//		builder.setIcon(R.mipmap.ic_launcher);//设置图标，图片id即可
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); //关闭dialog
                noticeDao.deleteById(noticeList.get(position).getId());
                noticeList2 = noticeDao.getList(pageNum,pageSize);
                noticeList.clear();
                noticeList.addAll(noticeList2);
                messageAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { //设置取消按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });


        //参数都设置完成了，创建并显示出来
        builder.create().show();

    }
    @Override
    protected void initEvent() {

        super.initEvent();
        pl_refresh.setOnFooterRefreshListener(new OnFooterRefreshListener() {

            @Override
            public void onFooterRefresh(PullToRefreshView view) {

                pageNum++;
                noticeList2 = noticeDao.getList(pageNum,pageSize);
                if(noticeList2.size() == 0) {
                    pl_refresh.setEnablePullLoadMoreDataStatus(false);
                    showShortToast("数据加载完毕！");
                }
                else
                    noticeList.addAll(noticeList2);
                pl_refresh.onFooterRefreshComplete();
            }
        });
    }

    class UpdateMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent intent) {

            if (intent.getAction().equals(Constant.BROADCAST_UPDATE_MESSAGE)) {
                pageNum = 1;
                noticeList2 = noticeDao.getList(pageNum,pageSize);
                noticeList.clear();
                noticeList.addAll(noticeList2);
                messageAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if(updateMessageReceiver != null)
            unregisterReceiver(updateMessageReceiver);
    }
}
