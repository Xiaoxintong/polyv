package com.easefun.polyv.cloudclassdemo.watch.player.live;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.easefun.polyv.businesssdk.model.video.PolyvBitrateVO;
import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.businesssdk.model.video.PolyvLiveLinesVO;
import com.easefun.polyv.businesssdk.model.video.PolyvMediaPlayMode;
import com.easefun.polyv.cloudclass.model.PolyvTeacherStatusInfo;
import com.easefun.polyv.cloudclass.video.PolyvCloudClassVideoView;
import com.easefun.polyv.cloudclass.video.api.IPolyvCloudClassController;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.danmu.PolyvDanmuFragment;
import com.easefun.polyv.cloudclassdemo.watch.player.live.widget.PolyvCloudClassMoreLayout;
import com.easefun.polyv.commonui.PolyvCommonMediacontroller;
import com.easefun.polyv.commonui.player.IPolyvBusinessMediaController;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBus;
import com.easefun.polyv.foundationsdk.rx.PolyvRxTimer;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;
import com.easefun.polyv.linkmic.PolyvLinkMicWrapper;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_HIDESUBVIEW;
import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_SHOWSUBVIEW;

/**
 * @author df
 * @create 2018/8/10
 * @Describe
 */
public class PolyvCloudClassMediaController extends PolyvCommonMediacontroller<PolyvCloudClassVideoView>
        implements IPolyvCloudClassController, IPolyvBusinessMediaController<PolyvCloudClassVideoView, PolyvCloudClassVideoHelper>, View.OnClickListener {

    private static final String TAG = "PolyvCloudClassMediaController";
    private static final int TOAST_SHOW_TIME = 5 * 1000;
    private static final int LINK_UP_TIMEOUT = 20 * 1000;

    private ImageView videoRefreshPort;
    private ImageView videoScreenSwitchPort;
    private ImageView videoDanmuPort;
    private ImageView videoPptChangeSwitchPort;
    private ImageView videoHandsUpPort;
    private ImageView videoBackPort;
    private ImageView ivVideoPausePortrait;
    private FrameLayout flGradientBarPort;

    private ImageView videoRefreshLand;
    private ImageView videoDanmuLand;
    private ImageView videoScreenSwitchLand;
    private ImageView videoHandsUpLand;
    private ImageView videoBackLand;
    private ImageView ivVideoPauseLand;
    private FrameLayout flGradientBarLand;

    //?????????????????????????????????????????????
    private TextView tvStartSendDanmuLand;

    //??????
    private PolyvCloudClassMoreLayout moreLayout;
    //?????????????????????
    private DanmuController danmuController;

    private PolyvCloudClassVideoHelper polyvCloudClassPlayerHelper;
    private PolyvDanmuFragment danmuFragment;

    private boolean showCamer;
    private boolean isPaused;

    // ??????????????????????????????
    private boolean showPPT;
    private PopupWindow bitRatePopupWindow;
    private Disposable popupWindowTimer, linkUpTimer;
    // ???????????????
    private AlertDialog alertDialog;

    //?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    private ImageView topBack;

    private OnClickOpenStartSendDanmuListener onClickOpenStartSendDanmuListener;


    public PolyvCloudClassMediaController(@NonNull Context context) {
        this(context, null);
    }

    public PolyvCloudClassMediaController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public PolyvCloudClassMediaController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // <editor-fold defaultstate="collapsed" desc="???????????????">

    protected void initialView() {
        context = (Activity) getContext();
        rootView = View.inflate(context, R.layout.polyv_cloudclass_controller, this);

        initialOtherView();

        addListener();
    }

    @Override
    public void initialConfig(ViewGroup view) {
        super.initialConfig(view);

        topBack = parentView.getRootView().findViewById(R.id.top_video_back_land);
        topBack.setOnClickListener(this);
    }

    private void initialOtherView() {
        //??????
        videoControllerPort = findViewById(R.id.video_controller_port);
        videoRefreshPort = findViewById(R.id.video_refresh_port);
        videoScreenSwitchPort = findViewById(R.id.video_screen_switch_port);
        videoDanmuPort = findViewById(R.id.video_danmu_port);
        videoPptChangeSwitchPort = findViewById(R.id.video_ppt_change_switch_port);
        videoHandsUpPort = findViewById(R.id.video_hands_up_port);
        videoBackPort = findViewById(R.id.iv_video_back_portrait);
        ivMorePortrait = findViewById(R.id.iv_more_portrait);
        ivVideoPausePortrait = findViewById(R.id.iv_video_pause_portrait);
        flGradientBarPort = findViewById(R.id.fl_gradient_bar_port);

        //??????
        videoControllerLand = findViewById(R.id.video_controller_land);
        videoRefreshLand = findViewById(R.id.video_refresh_land);
        videoDanmuLand = findViewById(R.id.video_danmu_land);
        videoScreenSwitchLand = findViewById(R.id.video_ppt_change_switch_land);
        videoHandsUpLand = findViewById(R.id.video_hands_up_land);
        videoBackLand = findViewById(R.id.iv_video_back_land);
        ivMoreLand = findViewById(R.id.iv_more_land);
        ivVideoPauseLand = findViewById(R.id.iv_video_pause_land);
        flGradientBarLand = findViewById(R.id.fl_gradient_bar_land);

        //??????
        moreLayout = new PolyvCloudClassMoreLayout(context, this);
        moreLayout.injectShowMediaControllerFunction(new PolyvCloudClassMoreLayout.ShowMediaControllerFunction() {
            @Override
            public void showMediaController() {
                PolyvCloudClassMediaController.this.show();
            }
        });
        moreLayout.injectShowGradientBarFunction(new PolyvCloudClassMoreLayout.ShowGradientBarFunction() {
            @Override
            public void showGradientBar(boolean show) {
                flGradientBarLand.setVisibility(show ? View.VISIBLE : View.GONE);
                flGradientBarPort.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
        moreLayout.setOnBitrateSelectedListener(new PolyvCloudClassMoreLayout.OnBitrateSelectedListener() {
            @Override
            public void onBitrateSelected(PolyvDefinitionVO definitionVO, int pos) {
                polyvVideoView.changeBitRate(pos);
            }
        });
        moreLayout.setOnLinesSelectedListener(new PolyvCloudClassMoreLayout.OnLinesSelectedListener() {
            @Override
            public void onLineSelected(PolyvLiveLinesVO line, int linePos) {
                polyvVideoView.changeLines(linePos);
            }
        });
        moreLayout.setOnOnlyAudioSwitchListener(new PolyvCloudClassMoreLayout.OnOnlyAudioSwitchListener() {
            @Override
            public boolean onOnlyAudioSelect(boolean onlyAudio) {
                if (polyvCloudClassPlayerHelper.isJoinLinkMick()) {
                    return false;
                } else {
                    if (onlyAudio) {
                        polyvVideoView.changeMediaPlayMode(PolyvMediaPlayMode.MODE_AUDIO);
                    } else {
                        polyvVideoView.changeMediaPlayMode(PolyvMediaPlayMode.MODE_VIDEO);
                    }
                    if (showPPT) {
                        PolyvCloudClassMediaController.this.showCameraView();
                    }
                    return true;
                }
            }
        });

        tvStartSendDanmuLand = findViewById(R.id.tv_start_send_danmu_land);

        videoControllerLand.setVisibility(View.GONE);

        danmuController = new DanmuController();
        danmuController.init();
    }


    private void addListener() {
        videoRefreshPort.setOnClickListener(this);
        videoScreenSwitchPort.setOnClickListener(this);
        videoDanmuPort.setOnClickListener(this);
        videoPptChangeSwitchPort.setOnClickListener(this);
        videoHandsUpPort.setOnClickListener(this);
        videoBackPort.setOnClickListener(this);
        ivMorePortrait.setOnClickListener(this);
        ivVideoPausePortrait.setOnClickListener(this);

        videoRefreshLand.setOnClickListener(this);
        videoDanmuLand.setOnClickListener(this);
        videoScreenSwitchLand.setOnClickListener(this);
        videoHandsUpLand.setOnClickListener(this);
        videoBackLand.setOnClickListener(this);
        ivMoreLand.setOnClickListener(this);
        ivVideoPauseLand.setOnClickListener(this);

        tvStartSendDanmuLand.setOnClickListener(this);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">

    @Override
    public void setViewBitRate(String vid, int bitRate) {

    }

    private void showBitrateChangeView() {
        if (polyvLiveBitrateVO == null || polyvLiveBitrateVO.getDefinitions() == null ||
                currentBitratePos == polyvLiveBitrateVO.getDefinitions().size() - 1) {
            return;
        }
        if (bitRatePopupWindow == null) {hide();
            creatBitrateChangeWindow();
        }
        //??????????????????????????????????????????????????????
        int[] location = new int[2];
        View showView = videoRefreshPort;
        if (videoRefreshLand.isShown()) {
            showView = videoRefreshLand;
        }
        showView.getLocationOnScreen(location);
        //?????????????????????
        View child = bitRatePopupWindow.getContentView();
        TextView definition = (TextView) child.findViewById(R.id.live_bitrate_popup_definition);

        PolyvDefinitionVO definitionVO = polyvLiveBitrateVO.getDefinitions().get(Math.max(0, currentBitratePos + 1));
        definition.setText(definitionVO.definition);

        definition.setOnClickListener(this);

        child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        int popupHeight = child.getMeasuredHeight();
        int popupWidth = child.getMeasuredWidth();
        bitRatePopupWindow.showAtLocation(showView, Gravity.NO_GRAVITY, (location[0] + 10), location[1] - popupHeight - 10);
//        handler.sendEmptyMessageDelayed(MESSAGE_HIDE_TOAST,TOAST_SHOW_TIME);
        popupWindowTimer = PolyvRxTimer.delay(TOAST_SHOW_TIME, new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                hideBitPopup();
            }
        });

    }


    @Override
    public void initialBitrate(PolyvBitrateVO bitrateVO) {
        super.initialBitrate(bitrateVO);
        moreLayout.initBitrate(bitrateVO);
    }

    @Override
    public void initialLines(List<PolyvLiveLinesVO> lines) {
        super.initialLines(lines);
        moreLayout.initLines(lines);
    }


    public void updateMoreLayout(int pos) {
        moreLayout.updateLinesStatus(pos);
    }

    private void hideBitPopup() {
        if (bitRatePopupWindow != null) {
            bitRatePopupWindow.dismiss();
        }
    }

    private void creatBitrateChangeWindow() {

        View child = View.inflate(getContext(), R.layout.polyv_live_bitrate_popu_layout, null);
        bitRatePopupWindow = new PopupWindow(child, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup
                .LayoutParams.WRAP_CONTENT, true);
        bitRatePopupWindow.setFocusable(true);//?????????????????????true?????????????????????????????????
        bitRatePopupWindow.setTouchable(true);//????????????PopupWindow???????????????????????????
        bitRatePopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        bitRatePopupWindow.setOutsideTouchable(true);
        bitRatePopupWindow.update();

        bitRatePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                bitRatePopupWindow = null;
                if (popupWindowTimer != null && !popupWindowTimer.isDisposed()) {
                    popupWindowTimer.dispose();
                }
//                handler.removeMessages(MESSAGE_HIDE_TOAST);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="???????????????">

    @Override
    public void changeToLandscape() {
        super.changeToLandscape();
//        danmuController.onLandscape();
        videoDanmuPort.post(new Runnable() {
            @Override
            public void run() {
                danmuController.refreshDanmuStatus();
            }
        });
    }

    @Override
    public void changeToPortrait() {
        super.changeToPortrait();
//        danmuController.onPortrait();
        videoDanmuPort.post(new Runnable() {
            @Override
            public void run() {
                danmuController.refreshDanmuStatus();
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????????????????">

    @Override
    public void release() {

    }

    @Override
    public void addHelper(PolyvCloudClassVideoHelper polyvCloudClassPlayerHelper) {
        this.polyvCloudClassPlayerHelper = polyvCloudClassPlayerHelper;
    }

    @Override
    public void destroy() {
        if (danmuFragment != null) {
            danmuFragment.release();
            danmuFragment = null;
        }
        if (popupWindowTimer != null) {
            popupWindowTimer.dispose();
            popupWindowTimer = null;
        }

        cancleLinkUpTimer();
    }


    @Override
    public void onPrepared(PolyvCloudClassVideoView mp) {

    }


    @Override
    public void onLongBuffering(String tip) {
        showBitrateChangeView();
    }

    @Override
    public void hide() {
        super.hide();
        moreLayout.hide();
        if (topBack != null) {
            topBack.setVisibility(GONE);
        }
    }

    @Override
    public void show() {
        super.show();
        if (polyvCloudClassPlayerHelper.isSupportRTC()
                && polyvCloudClassPlayerHelper.isJoinLinkMick()
                && PolyvScreenUtils.isLandscape(context)) {
            topBack.setVisibility(VISIBLE);
        }
    }

    protected void showTopController(boolean show) {
        topBack.setVisibility(show ? VISIBLE : GONE);
    }

    @Override
    public void setAnchorView(View view) {
    }

    @Deprecated
    @Override
    public void setMediaPlayer(MediaController.MediaPlayerControl player) {

    }

    @Override
    public void showOnce(View view) {
        setVisibility(VISIBLE);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????????????????????">

    @Override
    public void updatePPTShowStatus(boolean showPPT) {
        this.showPPT = showPPT;
        videoPptChangeSwitchPort.setVisibility(showPPT ? VISIBLE : GONE);
        videoScreenSwitchLand.setVisibility(showPPT ? VISIBLE : INVISIBLE);
    }

    public void showCameraView() {
        if (polyvCloudClassPlayerHelper != null && polyvCloudClassPlayerHelper.isJoinLinkMick()) {
            return;
        }
        PolyvTeacherStatusInfo detail = new PolyvTeacherStatusInfo();
        detail.setWatchStatus(LIVE_SHOWSUBVIEW);
        PolyvRxBus.get().post(detail);
        showCamer = false;
        //?????????????????????????????????????????????????????????
//        videoPptChangeSwitchPort.setImageResource(R.drawable.controller_exchange);
//        videoScreenSwitchLand.setImageResource(R.drawable.controller_exchange);
        polyvCloudClassPlayerHelper.showCameraView();
    }


    public void changePPTVideoLocation() {
        if (!showPPT) {//???????????????ppt  ??????????????????
            return;
        }
        changePPTVideoLocationUncheckPPT();
    }

    public void changePPTVideoLocationUncheckPPT() {
        if (polyvCloudClassPlayerHelper != null) {
            if (!polyvCloudClassPlayerHelper.changePPTViewToVideoView(showPPTSubView)) {
                return;
            }

            showPPTSubView = !showPPTSubView;
        }
    }

    //?????????????????? ???????????????
    public void updateControllerWithCloseSubView() {
        showCamer = true;
        if (showPPTSubView) {
            videoPptChangeSwitchPort.setImageResource(R.drawable.ppt);
            videoScreenSwitchLand.setImageResource(R.drawable.ppt);
        } else {
            videoPptChangeSwitchPort.setImageResource(R.drawable.camera);
            videoScreenSwitchLand.setImageResource(R.drawable.camera);

        }
    }

    @Override
    public boolean isPPTSubView() {
        return showPPTSubView;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">

    public void setCallMicView(ImageView callMicView) {
        videoHandsUpPort = callMicView;
    }

    public void handsUpAuto() {
        if (polyvCloudClassPlayerHelper.isParticipant() && !polyvCloudClassPlayerHelper.isJoinLinkMick()) {
            performClickLinkMic();
        }
    }

    //???ppt????????????????????? ?????????????????? ???????????????ppt?????????
    public void switchPPTToMainScreen() {
        if (!showPPTSubView) {//?????????????????????????????? ?????????????????????
            return;
        }
        if (polyvCloudClassPlayerHelper != null && (
                videoHandsUpLand.isSelected() || videoHandsUpPort.isSelected())) {
            polyvCloudClassPlayerHelper.changePPTViewToVideoView(true);
            showPPTSubView = false;
        }
    }

    //????????????
    public void handsUp(boolean joinSuccess) {
        View v = videoHandsUpLand;
        if (!videoHandsUpLand.isSelected()) {
            resetSelectedStatus();
            startHandsUpTimer();
            //??????????????????????????????channel
            polyvCloudClassPlayerHelper.sendJoinRequest();
        } else {
            showStopLinkDialog(joinSuccess, false);
        }

    }

    private void startHandsUpTimer() {
        cancleLinkUpTimer();
        linkUpTimer = PolyvRxTimer.delay(LINK_UP_TIMEOUT, new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                resetSelectedStatus();
            }
        });
    }

    public void cancleLinkUpTimer() {
        if (linkUpTimer != null) {
            PolyvCommonLog.d(TAG, "cancleLinkUpTimer");
            linkUpTimer.dispose();
            linkUpTimer = null;
        }
    }

    /**
     * ????????????????????????????????????/???????????????
     * <p>
     *
     * @param unLinkState true??????????????????????????????????????????false???????????????????????????????????????
     */
    public void updateLinkBtn2Ready(boolean unLinkState) {
        videoHandsUpPort.setSelected(unLinkState);
        videoHandsUpLand.setSelected(unLinkState);
    }

    /**
     * ????????????????????????
     *
     * @param enableForLinkMicOpen ??????????????????????????????true????????????????????????????????????false???
     */
    public void enableLinkBtn(boolean enableForLinkMicOpen) {
        videoHandsUpPort.setEnabled(enableForLinkMicOpen);
        videoHandsUpLand.setEnabled(enableForLinkMicOpen);
    }

    @Override
    public void showMicPhoneLine(int visiable) {

//        if (videoHandsUpPort != null) {
//            videoHandsUpPort.setVisibility(visiable);
//        }

        if (joinLinkMic) {
            return;
        }
        if (videoHandsUpLand != null) {
            videoHandsUpLand.setVisibility(visiable);
        }
    }

    //????????????
    public void onJoinLinkMic() {
        joinLinkMic = true;
        ivMoreLand.setVisibility(INVISIBLE);
        ivMorePortrait.setVisibility(INVISIBLE);

        ivVideoPauseLand.setVisibility(INVISIBLE);
        ivVideoPausePortrait.setVisibility(INVISIBLE);

        videoRefreshLand.setVisibility(INVISIBLE);
        videoRefreshPort.setVisibility(INVISIBLE);

        videoDanmuLand.setVisibility(INVISIBLE);
        videoDanmuPort.setVisibility(INVISIBLE);

        videoScreenSwitchLand.setVisibility(INVISIBLE);
        videoPptChangeSwitchPort.setVisibility(GONE);

        tvStartSendDanmuLand.setVisibility(INVISIBLE);
        videoHandsUpLand.setVisibility(INVISIBLE);

//
//        showTopController(true);
    }

    //????????????
    public void onLeaveLinkMic() {
        joinLinkMic = false;
        ivMoreLand.setVisibility(VISIBLE);
        ivMorePortrait.setVisibility(VISIBLE);

        ivVideoPauseLand.setVisibility(VISIBLE);
        ivVideoPausePortrait.setVisibility(VISIBLE);

        videoRefreshLand.setVisibility(VISIBLE);
        videoRefreshPort.setVisibility(VISIBLE);
        if (isPaused) {
            togglePauseBtn(true);
        }

        videoDanmuLand.setVisibility(VISIBLE);
        videoDanmuPort.setVisibility(VISIBLE);

        if (showPPT) {
            videoScreenSwitchLand.setVisibility(VISIBLE);
            videoPptChangeSwitchPort.setVisibility(VISIBLE);
        }

        tvStartSendDanmuLand.setVisibility(VISIBLE);
        videoHandsUpLand.setVisibility(VISIBLE);
//
//        showTopController(false);
    }

    //????????????????????????
    private void resetSelectedStatus() {
        videoHandsUpLand.setSelected(!videoHandsUpLand.isSelected());
        videoHandsUpPort.setSelected(!videoHandsUpPort.isSelected());
    }

    public void showStopLinkDialog(final boolean joinSuccess, final boolean isExit) {
        String message = joinSuccess ? String.format("???????????????????????????????????????%s???", isExit ? "?????????" : "") :
                "????????????????????????";
        String btnMsg = joinSuccess ? String.format("??????%s", isExit ? "?????????" : "") : "????????????";
        alertDialog = new AlertDialog.Builder(getContext()).setTitle(joinSuccess ? "????????????????????????\n" : "????????????????????????\n")
                .setNegativeButton(joinSuccess ? "????????????" : "????????????", null)
                .setPositiveButton(btnMsg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isExit) {
                            context.finish();
                            return;
                        }
                        videoHandsUpPort.setSelected(!videoHandsUpPort.isSelected());
                        videoHandsUpLand.setSelected(!videoHandsUpLand.isSelected());
                        if (joinSuccess) {
                            PolyvLinkMicWrapper.getInstance().leaveChannel();
                        } else {
                            polyvCloudClassPlayerHelper.leaveChannel();
                        }
                        startHandsUpTimer();
                    }
                })
                .create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.center_view_color_blue));
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.center_view_color_blue));
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">
    public void onServerDanmuOpen(boolean isServerDanmuOpen) {
        danmuController.onServerDanmuOpen(isServerDanmuOpen);
    }

    /**
     * ??????????????????????????????????????????
     */
    public void enableDanmuInPortrait() {
        danmuController.enableDanmuInPortrait();
    }

    public void setDanmuFragment(PolyvDanmuFragment danmuFragment) {
        this.danmuFragment = danmuFragment;
    }

    public void setOnClickOpenStartSendDanmuListener(OnClickOpenStartSendDanmuListener onClickOpenStartSendDanmuListener) {
        this.onClickOpenStartSendDanmuListener = onClickOpenStartSendDanmuListener;
    }

    /**
     * ???????????????
     * <p>
     * DanmuFragment????????????1.????????????????????????????????????????????????2.????????????Danmu????????????
     * Danmu?????????????????????????????????????????????
     */
    private class DanmuController {
        //???????????????????????????
        boolean isDanmuToggleOpen = false;

        //??????????????????
        boolean isEnableDanmuInPortrait = false;
        //?????????????????????
        boolean isServerDanmuOpen = false;


        void init() {
            videoDanmuPort.post(new Runnable() {
                @Override
                public void run() {
                    DanmuController.this.toggleDanmu();
                    DanmuController.this.refreshDanmuStatus();
                }
            });
        }

        // <editor-fold defaultstate="collapsed" desc="??????toggle">
        void toggleDanmu() {
            isDanmuToggleOpen = !isDanmuToggleOpen;
            videoDanmuPort.setSelected(isDanmuToggleOpen);
            videoDanmuLand.setSelected(isDanmuToggleOpen);
            if (isDanmuToggleOpen) {
                if (danmuFragment!=null){
                    danmuFragment.show();
                }
                tvStartSendDanmuLand.setVisibility(VISIBLE);
            } else {
                if (danmuFragment!=null){
                    danmuFragment.hide();
                }
                tvStartSendDanmuLand.setVisibility(GONE);
            }
        }
        // </editor-fold>

        void onServerDanmuOpen(boolean isServerDanmuOpen) {
            this.isServerDanmuOpen = isServerDanmuOpen;
            refreshDanmuStatus();
        }

        /**
         * ????????????????????????????????????????????????
         */
        void enableDanmuInPortrait() {
            isEnableDanmuInPortrait = true;
            refreshDanmuStatus();
        }

        void refreshDanmuStatus() {
            if (joinLinkMic) {//??????????????? ?????????????????????
                return;
            }
            if (danmuFragment == null) {
                return;
            }
            if (isServerDanmuOpen) {
                //??????????????????
                videoDanmuLand.setVisibility(VISIBLE);

                if (isEnableDanmuInPortrait) {
                    //??????????????????
                    videoDanmuPort.setVisibility(VISIBLE);
                    if (isDanmuToggleOpen) {
                        danmuFragment.show();
                        tvStartSendDanmuLand.setVisibility(VISIBLE);
                    } else {
                        danmuFragment.hide();
                        tvStartSendDanmuLand.setVisibility(INVISIBLE);
                    }
                } else {
                    //??????????????????
                    videoDanmuPort.setVisibility(INVISIBLE);
                    if (PolyvScreenUtils.isPortrait(getContext())) {
                        danmuFragment.hide();
                    } else {
                        if (isDanmuToggleOpen) {
                            danmuFragment.show();
                            tvStartSendDanmuLand.setVisibility(VISIBLE);
                        } else {
                            danmuFragment.hide();
                            tvStartSendDanmuLand.setVisibility(INVISIBLE);
                        }
                    }
                }
            } else {
                //??????????????????
                danmuFragment.hide();
                videoDanmuLand.setVisibility(GONE);
                videoDanmuPort.setVisibility(GONE);
                tvStartSendDanmuLand.setVisibility(GONE);
            }
        }
    }

    public interface OnClickOpenStartSendDanmuListener {
        void onStartSendDanmu();
    }
    // </editor-fold>


    // <editor-fold defaultstate="collapsed" desc="????????????????????????">
    private void refreshVideoView() {
        polyvCloudClassPlayerHelper.initVolume();
        polyvCloudClassPlayerHelper.restartPlay();
    }

    /**
     * @param justChangeUi ???????????????UI????????????false???????????????????????????????????????????????????
     */
    private void togglePauseBtn(boolean justChangeUi) {
        isPaused = !isPaused;
        boolean toPause = isPaused;
        if (!justChangeUi) {
            if (toPause) {
                polyvVideoView.pause();
            } else {
                refreshVideoView();
            }
        }
        ivVideoPauseLand.setSelected(toPause);
        ivVideoPausePortrait.setSelected(toPause);
    }


    public void changeAudioOrVideoMode(@PolyvMediaPlayMode.Mode int mediaPlayMode) {
        moreLayout.onChangeAudioOrVideoMode(mediaPlayMode);
    }


    public void onVideoViewPrepared() {
        if (isPaused) {
            togglePauseBtn(true);
        }
    }

    public void onLiveEnd() {
    }
    // </editor-fold>

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.video_danmu_land || id == R.id.video_danmu_port) {
            danmuController.toggleDanmu();
        } else if (id == R.id.video_hands_up_land || id == R.id.video_hands_up_port) {
            if (!polyvCloudClassPlayerHelper.requestPermission()) {
                return;
            }
        } else if (id == R.id.video_ppt_change_switch_port || id == R.id.video_ppt_change_switch_land) {
            v.setSelected(!v.isSelected());
            if (showCamer) {
                showCameraView();
            } else {
                hideSubView();
//                    changePPTVideoLocation();
            }
        } else if (id == R.id.video_refresh_land || id == R.id.video_refresh_port) {
            refreshVideoView();
            //            case R.id.video_screen_switch_land:
//                changeToPortrait();
//                polyvCloudClassPlayerHelper.resetFloatViewPort();
//                break;
        } else if (id == R.id.video_screen_switch_port) {
            PolyvScreenUtils.unlockOrientation();
            changeToLandscape();

        } else if (id == R.id.iv_video_back_portrait) {
            if (context != null) {
                context.finish();
            }

        } else if (id == R.id.top_video_back_land) {
            if (ScreenUtils.isLandscape()) {
                PolyvScreenUtils.unlockOrientation();
                changeToPortrait();
            } else {
                if (context != null) {
                    context.finish();
                }
            }

        } else if (id == R.id.iv_video_back_land) {
            if (ScreenUtils.isLandscape()) {
                PolyvScreenUtils.unlockOrientation();
                changeToPortrait();
            }

        } else if (id == R.id.iv_more_land) {
            moreLayout.showWhenLandscape();

        } else if (id == R.id.iv_more_portrait) {
            moreLayout.showWhenPortrait();

        } else if (id == R.id.iv_video_pause_land || id == R.id.iv_video_pause_portrait) {
            togglePauseBtn(false);

        } else if (id == R.id.tv_start_send_danmu_land) {
            onClickOpenStartSendDanmuListener.onStartSendDanmu();
        }
    }

    private void hideSubView() {
        PolyvTeacherStatusInfo detail = new PolyvTeacherStatusInfo();
        detail.setWatchStatus(LIVE_HIDESUBVIEW);
        PolyvRxBus.get().post(detail);

        showCamer = true;
        polyvCloudClassPlayerHelper.hideSubView(false);
    }

    // <editor-fold defaultstate="collapsed" desc="perform click ??????">

    public void performClickLinkMic() {
        videoHandsUpPort.performClick();
    }

    public void performClickCamera() {
        if (ScreenUtils.isPortrait()) {
            videoPptChangeSwitchPort.performClick();
            videoScreenSwitchLand.setSelected(!videoScreenSwitchLand.isSelected());
        } else {
            videoScreenSwitchLand.performClick();
            videoPptChangeSwitchPort.setSelected(!videoPptChangeSwitchPort.isSelected());
        }
    }
    // </editor-fold>

}
