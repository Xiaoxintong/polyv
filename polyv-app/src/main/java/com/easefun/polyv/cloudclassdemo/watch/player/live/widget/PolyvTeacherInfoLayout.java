package com.easefun.polyv.cloudclassdemo.watch.player.live.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.chat.event.PolyvTeacherInfo;
import com.easefun.polyv.cloudclass.model.PolyvTeacherStatusInfo;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.PolyvDemoClient;
import com.easefun.polyv.cloudclassdemo.watch.player.live.PolyvCloudClassVideoHelper;
import com.easefun.polyv.commonui.utils.imageloader.PolyvImageLoader;
import com.easefun.polyv.commonui.widget.PolyvTouchContainerView;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBus;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_CLOSECALLLINKMIC;
import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_END;
import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_HIDESUBVIEW;
import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_N0_PPT;
import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_NO_STREAM;
import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_OPENCALLLINKMIC;
import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_OPEN_PPT;
import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_SHOWSUBVIEW;
import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_START;
import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LOGIN_CHAT_ROOM;

/**
 * @author df
 * @create 2019/7/17
 * @Describe
 */
public class PolyvTeacherInfoLayout extends LinearLayout implements View.OnClickListener {
    private static final String TAG = "PolyvTeacherInfoLayout";

    private LinearLayout teacherInfo;
    private LinearLayout teacherInfoMiddleContainer;
    private FrameLayout teacherSubView;

    private ImageView teacherImg;
    private TextView teacherNameVertical;
    private TextView onlineNumber;
    private LinearLayout linkMicLayout, linkMicCallLayout;
    private ImageView linkMicBackTeacherInfo;
    private TextView linkMicStatus;
    private ImageView linkMicStatusImg;

    private boolean linkMicCallAbove;//??????????????????????????????

    //?????????????????????????????????
    private boolean isFirstTimeReceiveLinkMicOpen = true;
    //?????????????????????????????????
    private boolean isFirstTimeReceiveLinkMicClose = true;
    //?????????????????????????????????
    private boolean hasNotClickLinkMic = true;


    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private PolyvCloudClassVideoHelper cloudClassVideoHelper;

    public PolyvTeacherInfoLayout(Context context) {
        this(context, null);
    }

    public PolyvTeacherInfoLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvTeacherInfoLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView();
        addListener();
        regsiterBeanListener();

        initData();
    }

    private void initData() {
        fillTeacherInfo();
    }

    // <editor-fold defaultstate="collapsed" desc="?????????">
    @SuppressLint("ClickableViewAccessibility")
    private void addListener() {
        linkMicBackTeacherInfo.setOnClickListener(this);
        linkMicStatusImg.setOnClickListener(this);

        //????????????????????????onClickListener()???????????????????????????controller?????????????????????????????????OnTouchListener
        linkMicStatusImg.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    hasNotClickLinkMic = false;
                }
                return false;
            }
        });
    }

    private void initView() {
        View.inflate(getContext(), R.layout.polyv_teacher_info, this);

        teacherSubView = findViewById(R.id.subview_layout);
        teacherInfo = (LinearLayout) findViewById(R.id.teacher_info);
        teacherInfoMiddleContainer = (LinearLayout) findViewById(R.id.teacher_info_middle_container);
        teacherImg = (ImageView) findViewById(R.id.teacher_img);
        teacherNameVertical = (TextView) findViewById(R.id.teacher_name_vertical);
        onlineNumber = (TextView) findViewById(R.id.online_number);
        linkMicLayout = (LinearLayout) findViewById(R.id.link_mic_layout);
        linkMicCallLayout = (LinearLayout) findViewById(R.id.link_mic_call_layout);
        linkMicBackTeacherInfo = (ImageView) findViewById(R.id.link_mic_arrow);
        linkMicStatus = (TextView) findViewById(R.id.link_mic_status);
        linkMicStatusImg = (ImageView) findViewById(R.id.link_mic_status_img);

        linkMicStatusImg.setEnabled(false);

        moveLinkMicToRight();

    }

    private void regsiterBeanListener() {

        compositeDisposable.add(PolyvRxBus.get().toObservable(PolyvTeacherStatusInfo.class).
                subscribe(new Consumer<PolyvTeacherStatusInfo>() {
                    @Override
                    public void accept(final PolyvTeacherStatusInfo polyvLiveClassDetailVO) throws Exception {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                String watchStatus = polyvLiveClassDetailVO.getWatchStatus();
                                PolyvCommonLog.d(TAG, "teacher receive status:" + watchStatus);
                                if (LIVE_END.equals(watchStatus) || LIVE_NO_STREAM.equals(watchStatus)) {
                                    setVisibility(GONE);
                                } else if (LIVE_START.equals(watchStatus)) {
                                    setVisibility(VISIBLE);
                                } else if (LIVE_OPEN_PPT.equals(watchStatus)) {
                                    showWithPPTStatus(VISIBLE);
                                } else if (LIVE_N0_PPT.equals(watchStatus)) {
                                    showWithPPTStatus(GONE);
                                } else if (LIVE_HIDESUBVIEW.equals(watchStatus)) {
                                    teacherSubView.setVisibility(GONE);
                                    if (!linkMicCallAbove) {
                                        moveLinkMicToRight();
                                    }
                                } else if (LIVE_SHOWSUBVIEW.equals(watchStatus)) {
                                    teacherSubView.setVisibility(VISIBLE);
                                    if (!linkMicCallAbove) {
                                        moveLinkMicToRight();
                                    }
                                } else if (LIVE_OPENCALLLINKMIC.equals(watchStatus)) {
                                    linkMicStatusImg.setEnabled(true);
                                    linkMicStatus.setText("?????????????????????");

                                    handleAutoMoveLinkMicLayoutWhenReceiveBusEvent(true);
                                } else if (LIVE_CLOSECALLLINKMIC.equals(watchStatus)) {
                                    linkMicStatusImg.setEnabled(false);
                                    linkMicStatus.setText("?????????????????????");

                                    handleAutoMoveLinkMicLayoutWhenReceiveBusEvent(false);
                                } else if (LOGIN_CHAT_ROOM.equals(watchStatus)) {
                                    fillTeacherInfo();
                                }
                            }
                        });

                    }
                }));
    }

    protected void showWithPPTStatus(int visible) {
        if (cloudClassVideoHelper != null && !cloudClassVideoHelper.isJoinLinkMick()) {
            setVisibility(visible);
            teacherSubView.setVisibility(visible);
            if (!linkMicCallAbove) {
                moveLinkMicToRight();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">
    public void init(@NonNull PolyvCloudClassVideoHelper cloudClassVideoHelper,
                     @NonNull PolyvTouchContainerView videoPptContainer, boolean isParticipant, String rtcType) {
        this.cloudClassVideoHelper = cloudClassVideoHelper;
        cloudClassVideoHelper.bindCallMicView(linkMicStatusImg);

        if ("urtc".equals(rtcType) || TextUtils.isEmpty(rtcType)) {
            //???????????????????????????
            linkMicLayout.setVisibility(INVISIBLE);
        } else {
            //????????????????????????
            linkMicLayout.setVisibility(isParticipant ? INVISIBLE : VISIBLE);
        }

    }

    public void fillTeacherInfo() {
        onlineNumber.setText(PolyvChatManager.getInstance().getOnlineCount() + "?????????");
        PolyvTeacherInfo teacherInfo = PolyvDemoClient.getInstance().getTeacher();
        if (teacherInfo != null) {
            teacherNameVertical.setText(teacherInfo.getData().getNick());
            PolyvImageLoader.getInstance().loadImage(this.getContext(), teacherInfo.getData().getPic(), teacherImg);
        }
    }

    public void onDestroy() {
        if (compositeDisposable != null) {
            compositeDisposable.clear();
            compositeDisposable = null;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????????????????????????????????">

    /**
     * ???????????????????????????????????????
     *
     * @param show true????????????????????????????????????false????????????????????????????????????
     */
    private void showOrHideLinkMicLayout(final boolean show) {
        if (linkMicCallAbove == show) {
            return;
        }
        linkMicLayout.post(new Runnable() {
            @Override
            public void run() {
                int moveX = linkMicLayout.getMeasuredWidth() - PolyvScreenUtils.dip2px(getContext(), 36);
                PolyvCommonLog.d(TAG, "movex :" + moveX);
                ObjectAnimator animator =
                        ObjectAnimator.ofFloat(linkMicLayout,
                                "translationX", show ? moveX : 0, show ? 0 : moveX);
                animator.setDuration(1000);
                animator.start();
                linkMicCallAbove = show;
            }
        });

    }

    /**
     * ????????????????????????????????????
     */
    private void moveLinkMicToRight() {
        linkMicCallLayout.setVisibility(INVISIBLE);
        post(new Runnable() {
            @Override
            public void run() {
                int moveX = linkMicLayout.getMeasuredWidth() - PolyvScreenUtils.dip2px(getContext(), 36);
                ObjectAnimator animator =
                        ObjectAnimator.ofFloat(linkMicLayout,
                                "translationX", moveX, moveX);
                animator.setDuration(1);
                animator.start();
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        linkMicCallLayout.setVisibility(VISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }
        });

    }

    /**
     * ?????????????????????????????????????????????bus event??????????????????????????????????????????????????????
     * <p>
     * ??????????????????????????????????????????????????????socket??????????????????????????????????????????????????????
     * ???????????????????????????????????? {@link #isFirstTimeReceiveLinkMicClose}???{@link #isFirstTimeReceiveLinkMicOpen}
     * ?????????????????????????????????????????????
     *
     * @param isLinkMicOpen true:????????????;false:????????????
     */
    private void handleAutoMoveLinkMicLayoutWhenReceiveBusEvent(boolean isLinkMicOpen) {
        if (isLinkMicOpen) {
            if (isFirstTimeReceiveLinkMicOpen) {
                //reset flag
                isFirstTimeReceiveLinkMicOpen = false;
                isFirstTimeReceiveLinkMicClose = true;
                hasNotClickLinkMic = true;

                showOrHideLinkMicLayout(true);
                linkMicBackTeacherInfo.setSelected(true);
            }
        } else {
            if (isFirstTimeReceiveLinkMicClose) {
                //reset flag
                isFirstTimeReceiveLinkMicClose = false;
                isFirstTimeReceiveLinkMicOpen = true;

                //???????????????????????????????????????????????????????????????????????????
                if (hasNotClickLinkMic && linkMicCallAbove) {
                    showOrHideLinkMicLayout(false);
                    linkMicBackTeacherInfo.setSelected(false);
                }
            }
        }
    }
// </editor-fold>


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id==R.id.link_mic_arrow) {
            linkMicBackTeacherInfo.setSelected(!v.isSelected());
            showOrHideLinkMicLayout(v.isSelected());
        } else if (id==R.id.link_mic_status_img) {
            if (cloudClassVideoHelper != null) {
                cloudClassVideoHelper.requestPermission();
            }
        }
    }


    // <editor-fold defaultstate="collapsed" desc="override">
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (cloudClassVideoHelper != null) {
            cloudClassVideoHelper = null;
        }

    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (!linkMicCallAbove) {
                moveLinkMicToRight();
            }
        }
    }
    // </editor-fold>

}
