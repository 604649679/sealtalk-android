package cn.rongcloud.im.ui.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.rongcloud.im.R;
import cn.rongcloud.im.server.SealAction;
import cn.rongcloud.im.server.network.async.AsyncTaskManager;
import cn.rongcloud.im.server.network.async.OnDataListener;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.pinyin.Friend;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.DialogWithYesOrNoUtils;
import cn.rongcloud.im.server.widget.LoadDialog;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;

/**
 * Created by AMing on 16/8/1.
 * Company RongCloud
 */
public class SinglePopWindow extends PopupWindow {
    private static final int ADDBLACKLIST = 167;
    private static final int REMOVEBLACKLIST = 168;
    private View conentView;
    private AsyncTaskManager asyncTaskManager;


    @SuppressLint("InflateParams")
    public SinglePopWindow(final Activity context, final Friend friend, final RongIMClient.BlacklistStatus blacklistStatus) {
        LayoutInflater inflater = (LayoutInflater) context
                                  .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        conentView = inflater.inflate(R.layout.popupwindow_more, null);
        // 设置SelectPicPopupWindow的View
        this.setContentView(conentView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        // 刷新状态
        this.update();
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        this.setBackgroundDrawable(dw);

        // 设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.AnimationPreview);
        asyncTaskManager = AsyncTaskManager.getInstance(context);
        RelativeLayout blacklistStatusRL = (RelativeLayout) conentView.findViewById(R.id.blacklist_status);
        final TextView blacklistText = (TextView) conentView.findViewById(R.id.blacklist_text_status);

        if (blacklistStatus == RongIMClient.BlacklistStatus.IN_BLACK_LIST) {
            blacklistText.setText("移除黑名单");
        } else {
            blacklistText.setText("加入黑名单");
        }


        blacklistStatusRL.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (blacklistStatus == RongIMClient.BlacklistStatus.IN_BLACK_LIST) {
                    RongIM.getInstance().removeFromBlacklist(friend.getUserId(), new RongIMClient.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            asyncTaskManager.request(ADDBLACKLIST, new OnDataListener() {
                                @Override
                                public Object doInBackground(int requsetCode, String parameter) throws HttpException {
                                    return new SealAction(context).removeFromBlackList(friend.getUserId());
                                }

                                @Override
                                public void onSuccess(int requestCode, Object result) {
                                    NToast.shortToast(context, "移除成功");
                                }

                                @Override
                                public void onFailure(int requestCode, int state, Object result) {

                                }
                            });
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {
                            NToast.shortToast(context, "移除失败");
                        }
                    });
                } else {
                    DialogWithYesOrNoUtils.getInstance().showDialog(context, "加入黑名单,你将不再受到对方的消息。", new DialogWithYesOrNoUtils.DialogCallBack() {
                        @Override
                        public void exectEvent() {
                            RongIM.getInstance().addToBlacklist(friend.getUserId(), new RongIMClient.OperationCallback() {
                                @Override
                                public void onSuccess() {

                                    asyncTaskManager.request(REMOVEBLACKLIST, new OnDataListener() {
                                        @Override
                                        public Object doInBackground(int requsetCode, String parameter) throws HttpException {
                                            return new SealAction(context).addToBlackList(friend.getUserId());
                                        }

                                        @Override
                                        public void onSuccess(int requestCode, Object result) {
                                            NToast.shortToast(context, "加入成功");
                                        }

                                        @Override
                                        public void onFailure(int requestCode, int state, Object result) {

                                        }
                                    });
                                }

                                @Override
                                public void onError(RongIMClient.ErrorCode errorCode) {
                                    NToast.shortToast(context, "加入失败");
                                }
                            });
                        }

                        @Override
                        public void exectEditEvent(String editText) {

                        }

                        @Override
                        public void updatePassword(String oldPassword, String newPassword) {

                        }
                    });

                }
                SinglePopWindow.this.dismiss();
            }

        });

    }

    /**
     * 显示popupWindow
     *
     * @param parent
     */
    public void showPopupWindow(View parent) {
        if (!this.isShowing()) {
            // 以下拉方式显示popupwindow
            this.showAsDropDown(parent, 0, 0);
        } else {
            this.dismiss();
        }
    }
}
