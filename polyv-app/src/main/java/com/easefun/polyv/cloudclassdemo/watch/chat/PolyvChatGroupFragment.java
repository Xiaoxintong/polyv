package com.easefun.polyv.cloudclassdemo.watch.chat;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.easefun.polyv.businesssdk.sub.gif.RelativeImageSpan;
import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.chat.PolyvConnectStatusListener;
import com.easefun.polyv.cloudclass.chat.PolyvLocalMessage;
import com.easefun.polyv.cloudclass.chat.event.PLVRewardEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvBanIpEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvChatImgEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvCloseRoomEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvCustomerMessageEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvEventHelper;
import com.easefun.polyv.cloudclass.chat.event.PolyvGongGaoEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvKickEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvLikesEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvLoginEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvLoginRefuseEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvReloginEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvRemoveContentEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvRemoveHistoryEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvSpeakEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvUnshieldEvent;
import com.easefun.polyv.cloudclass.chat.history.PolyvChatImgHistory;
import com.easefun.polyv.cloudclass.chat.history.PolyvHistoryConstant;
import com.easefun.polyv.cloudclass.chat.history.PolyvSpeakHistory;
import com.easefun.polyv.cloudclass.chat.send.img.PolyvSendChatImageListener;
import com.easefun.polyv.cloudclass.chat.send.img.PolyvSendLocalImgEvent;
import com.easefun.polyv.cloudclass.feature.point_reward.IPolyvPointRewardDataSource;
import com.easefun.polyv.cloudclass.feature.point_reward.PLVPointRewardDataSource;
import com.easefun.polyv.cloudclass.model.PolyvChatFunctionSwitchVO;
import com.easefun.polyv.cloudclass.model.point_reward.PolyvPointRewardSettingVO;
import com.easefun.polyv.cloudclass.net.PolyvApiManager;
import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.PolyvChatListAdapter;
import com.easefun.polyv.cloudclassdemo.watch.chat.menu.PolyvTuWenMenuFragment;
import com.easefun.polyv.cloudclassdemo.watch.chat.point_reward.dialog.PolyvPointRewardDialog;
import com.easefun.polyv.cloudclassdemo.watch.chat.point_reward.effect.IPolyvPointRewardEventProducer;
import com.easefun.polyv.cloudclassdemo.watch.chat.point_reward.effect.PolyvPointRewardEffectQueue;
import com.easefun.polyv.cloudclassdemo.watch.chat.point_reward.effect.PolyvPointRewardEffectWidget;
import com.easefun.polyv.commonui.R;
import com.easefun.polyv.commonui.base.PolyvBaseActivity;
import com.easefun.polyv.commonui.utils.PolyvSingleRelayBus;
import com.easefun.polyv.commonui.utils.PolyvTextImageLoader;
import com.easefun.polyv.commonui.utils.PolyvToast;
import com.easefun.polyv.commonui.utils.imageloader.PolyvImageLoader;
import com.easefun.polyv.commonui.widget.PolyvCornerBgTextView;
import com.easefun.polyv.commonui.widget.PolyvGreetingTextView;
import com.easefun.polyv.commonui.widget.PolyvLikeIconView;
import com.easefun.polyv.commonui.widget.PolyvMarqueeTextView;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBaseTransformer;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBus;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ToastUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.socket.client.Ack;
import okhttp3.ResponseBody;

/**
 * ??????
 */
public class PolyvChatGroupFragment extends PolyvChatBaseFragment {
    // <editor-fold defaultstate="collapsed" desc="????????????">

    private static final String TAG = "PolyvChatGroupFragment";

    //???????????????????????????????????????(??????????????????????????????????????????????????????????????????????????????????????????????????????????????????)
    private boolean isOnlyHostCanSendMessage = true;
    //????????????ui
    private PolyvCornerBgTextView bgStatus;
    //???????????????
    private FrameLayout flFlower;
    //???????????????????????????????????????????????????????????????????????????
    private ImageView onlyHostSwitch, flower, like;
    private PolyvLikeIconView liv_like;
    private PolyvMarqueeTextView tvGongGao;
    //????????????????????????
    private SwipeRefreshLayout chatPullLoad;
    //?????????
    private PolyvGreetingTextView greetingText;


    //********????????????
    //??????????????????
    private ImageView ivShowPointReward;
    //??????????????????
    private PolyvPointRewardDialog pointRewardWindow;
    //????????????????????????
    private IPolyvPointRewardDataSource pointRewardRepository;
    //????????????????????????
    private IPolyvPointRewardEventProducer pointRewardEventProducer;

    //    private PolyvGreetingView greetingView;
    //???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    private boolean isBanIp, isCloseRoom, isCloseRoomReconnect;
    // ???????????????????????????
    private int messageCount = 20;
    // ???????????????????????????
    private int count = 1;
    private Disposable gonggaoCdDisposable;
    private boolean isNormalLive = false;//??????????????????
    private boolean isShowLikeTips = false;//?????????????????????????????????
    private boolean isShowGreeting = true;//???????????????????????????????????????
    private boolean isPointRewardEnabled = false;//?????????????????? ????????????????????????
    private RelativeLayout plvRlGroupChatFragment;
    private Handler handler = new Handler(Looper.getMainLooper());
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????">
    @Override
    public int layoutId() {
        return R.layout.polyv_fragment_groupchat;
    }

    @Override
    public void loadDataAhead() {
        super.loadDataAhead();
        initCommonView();
        initMoreLayout();
        initView();
        //????????????????????????????????????????????????
        acceptConnectStatus();
        acceptEventMessage();
        listenSendChatImgStatus();
        initPointReward();
    }

    private void initView() {
        flFlower = findViewById(R.id.polyv_group_chat_fl_flower);
        plvRlGroupChatFragment = findViewById(R.id.plv_rl_group_chat_fragment);

        //????????????
        chatPullLoad = findViewById(R.id.chat_pull_load);
        chatPullLoad.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light,
                android.R.color.holo_orange_light, android.R.color.holo_green_light);
        chatPullLoad.setEnabled(false);
        delayLoadHistory(1666);
        //??????ui
        bgStatus = findViewById(R.id.tv_status);
        //????????????
        onlyHostSwitch = findViewById(R.id.iv_switch);
        onlyHostSwitch.setVisibility(View.VISIBLE);
        onlyHostSwitch.setSelected(false);
        onlyHostSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onlyHostSwitch.setSelected(!onlyHostSwitch.isSelected());
                toast.makeText(getContext(), onlyHostSwitch.isSelected() ? "??????????????????" : "???????????????", PolyvToast.LENGTH_LONG).show(true);
                chatListAdapter.setChatTypeItems(onlyHostSwitch.isSelected() ? teacherItems : chatTypeItems);
                chatListAdapter.notifyDataSetChanged();
                chatMessageList.scrollToPosition(chatListAdapter.getItemCount() - 1);

                if (onlyHostSwitch.isSelected()) {
                    if (!isOnlyHostCanSendMessage) {
                        hideSoftInputAndEmoList();
                        resetOnlyHostRelateView(false);
                    }
                } else {
                    resetOnlyHostRelateView(true);
                }
            }
        });
        //??????(???????????????????????????)
        flower = findViewById(R.id.iv_flower);
        flower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //????????????
                int sendValue = chatManager.sendLikes(getSessionId());
                if (sendValue < 0) {
                    toast.makeText(getContext(), "???????????????" + sendValue, PolyvToast.LENGTH_SHORT).show(true);
                } else {
                    PolyvLikesEvent likesEvent = new PolyvLikesEvent();
                    likesEvent.setNick(chatManager.nickName);
                    likesEvent.setUserId(chatManager.userId);
                    likesEvent.setObjects(generateLikeSpan(likesEvent.getNick()));

                    PolyvChatListAdapter.ChatTypeItem chatTypeItem = new PolyvChatListAdapter.ChatTypeItem(likesEvent, PolyvChatListAdapter.ChatTypeItem.TYPE_TIPS, PolyvChatManager.SE_MESSAGE);
                    chatTypeItems.add(chatTypeItem);
                    teacherItems.add(chatTypeItem);
                    chatListAdapter.notifyItemInserted(chatListAdapter.getItemCount() - 1);
                    chatMessageList.scrollToPosition(chatListAdapter.getItemCount() - 1);
                }
            }
        });

        //??????
        liv_like = findViewById(R.id.liv_like);
        like = findViewById(R.id.like);
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int sendValue = chatManager.sendLikes(getSessionId());
                if (sendValue < 0) {
                    toast.makeText(getContext(), "???????????????" + sendValue, PolyvToast.LENGTH_SHORT).show(true);
                } else {
                    liv_like.addLoveIcon();

                    if (isShowLikeTips) {
                        PolyvLikesEvent likesEvent = new PolyvLikesEvent();
                        likesEvent.setNick(chatManager.nickName);
                        likesEvent.setUserId(chatManager.userId);
                        likesEvent.setObjects(chatManager.nickName + " ??????????????????????????????");

                        PolyvChatListAdapter.ChatTypeItem chatTypeItem = new PolyvChatListAdapter.ChatTypeItem(likesEvent, PolyvChatListAdapter.ChatTypeItem.TYPE_TIPS, PolyvChatManager.SE_MESSAGE);
                        chatTypeItems.add(chatTypeItem);
                        teacherItems.add(chatTypeItem);
                        chatListAdapter.notifyItemInserted(chatListAdapter.getItemCount() - 1);
                        chatMessageList.scrollToPosition(chatListAdapter.getItemCount() - 1);
                    }
                }
            }
        });

        if (isNormalLive) {
            like.setVisibility(View.VISIBLE);
        } else {
            flower.setVisibility(View.VISIBLE);
        }

        //??????
        tvGongGao = findViewById(R.id.tv_gonggao);

        //?????????
        greetingText = findViewById(R.id.greeting_text);

        //??????????????????
        ivShowPointReward = findViewById(R.id.plv_iv_show_point_reward);
        ivShowPointReward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pointRewardWindow.show();
            }
        });


//        greetingView = findViewById(R.id.greeting_view);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????????">
    @Override
    protected void sendImage(PolyvSendLocalImgEvent sendLocalImgEvent, String sessionId) {
        chatManager.sendChatImage(sendLocalImgEvent, sessionId);
    }

    private void listenSendChatImgStatus() {
        chatManager.setSendChatImageListener(new PolyvSendChatImageListener() {
            @Override
            public void onUploadFail(PolyvSendLocalImgEvent localImgEvent, Throwable t) {
                onImgUploadFail(localImgEvent, t);
            }

            @Override
            public void onSendFail(PolyvSendLocalImgEvent localImgEvent, int sendValue) {
                onImgSendFail(localImgEvent, sendValue);
            }

            @Override
            public void onSuccess(PolyvSendLocalImgEvent localImgEvent, String uploadImgUrl, String imgId) {
                onImgSuccess(localImgEvent, uploadImgUrl, imgId);
            }

            @Override
            public void onProgress(PolyvSendLocalImgEvent localImgEvent, float progress) {
                onImgProgress(localImgEvent, progress);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????????????????">
    private void delayLoadHistory(long time) {
        disposables.add(
                Observable.timer(time, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong) throws Exception {
                                chatPullLoad.setEnabled(true);
                                chatPullLoad.setRefreshing(true);
                                loadHistory(true);
                                chatPullLoad.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                    @Override
                                    public void onRefresh() {
                                        loadHistory(false);
                                    }
                                });
                            }
                        })
        );
    }

    private void loadHistory(final boolean isFirstLoad) {
        disposables.add(
                PolyvApiManager.getPolyvApichatApi()
                        .getChatHistory(chatManager.roomId, (count - 1) * messageCount, count * messageCount, 1)
                        .map(new Function<ResponseBody, JSONArray>() {
                            @Override
                            public JSONArray apply(ResponseBody responseBody) throws Exception {
                                return new JSONArray(responseBody.string());
                            }
                        })
                        .compose(new PolyvRxBaseTransformer<JSONArray, JSONArray>())
                        .map(new Function<JSONArray, JSONArray>() {
                            @Override
                            public JSONArray apply(JSONArray jsonArray) throws Exception {
                                if (jsonArray.length() <= messageCount) {
                                    chatPullLoad.setEnabled(false);
                                    toast.makeText(getContext(), "????????????????????????????????????", PolyvToast.LENGTH_SHORT).show(true);
                                }
                                return jsonArray;
                            }
                        })
                        .observeOn(Schedulers.io())
                        .map(new Function<JSONArray, List<PolyvChatListAdapter.ChatTypeItem>[]>() {
                            @Override
                            public List<PolyvChatListAdapter.ChatTypeItem>[] apply(JSONArray jsonArray) throws Exception {
                                return acceptHistorySpeak(jsonArray, chatManager.userId);
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable) throws Exception {
                                //????????????????????????????????????????????????????????????????????????????????????
                                if (TextUtils.isEmpty(chatManager.roomId) || TextUtils.isEmpty(chatManager.userId))
                                    throw new IllegalArgumentException("roomId or userId is empty");
                            }
                        })
                        .doFinally(new Action() {
                            @Override
                            public void run() throws Exception {
                                chatPullLoad.setRefreshing(false);
                            }
                        })
                        .subscribe(new Consumer<List<PolyvChatListAdapter.ChatTypeItem>[]>() {
                            @Override
                            public void accept(List<PolyvChatListAdapter.ChatTypeItem>[] listArr) throws Exception {
                                //????????????????????????????????????
                                updateListData(listArr, isFirstLoad, true);
                                count++;
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                if (throwable instanceof IOException) {
                                    toast.makeText(getContext(), "???????????????????????????????????????", PolyvToast.LENGTH_LONG).show(true);
                                } else {
                                    toast.makeText(getContext(), "????????????????????????(" + throwable.getMessage() + ")", PolyvToast.LENGTH_LONG).show(true);
                                }
                            }
                        })
        );
    }

    private List<PolyvChatListAdapter.ChatTypeItem>[] acceptHistorySpeak(JSONArray jsonArray, String myChatUserId) {
        List<PolyvChatListAdapter.ChatTypeItem> tempChatItems = new ArrayList<>();
        List<PolyvChatListAdapter.ChatTypeItem> tempTeacherChatItems = new ArrayList<>();
        for (int i = 0; i < (jsonArray.length() <= messageCount ? jsonArray.length() : jsonArray.length() - 1); i++) {
            JSONObject jsonObject = jsonArray.optJSONObject(i);
            if (jsonObject != null) {
                String messageSource = jsonObject.optString("msgSource");
                if (!TextUtils.isEmpty(messageSource)) {
                    //???/?????????/???????????????????????????????????????
                    if (PolyvHistoryConstant.MSGSOURCE_CHATIMG.equals(messageSource)) {
                        PolyvChatImgHistory chatImgHistory = PolyvEventHelper.gson.fromJson(jsonObject.toString(), PolyvChatImgHistory.class);
                        int type;
                        //?????????????????????????????????
                        if (myChatUserId.equals(chatImgHistory.getUser().getUserId())) {
                            type = PolyvChatListAdapter.ChatTypeItem.TYPE_SEND;
                        } else {
                            type = PolyvChatListAdapter.ChatTypeItem.TYPE_RECEIVE;
                        }
                        PolyvChatListAdapter.ChatTypeItem chatTypeItem = new PolyvChatListAdapter.ChatTypeItem(chatImgHistory, type, PolyvChatManager.SE_MESSAGE);
                        tempChatItems.add(0, chatTypeItem);
                        if (isOnlyHostType(chatImgHistory.getUser().getUserType(), chatImgHistory.getUser().getUserId())) {
                            tempTeacherChatItems.add(0, chatTypeItem);
                        }
                        continue;
                    }
                }
                JSONObject jsonObject_user = jsonObject.optJSONObject("user");
                if (jsonObject_user != null) {
                    String uid = jsonObject_user.optString("uid");
                    JSONObject jsonObject_content = jsonObject.optJSONObject("content");

                    if (PolyvHistoryConstant.UID_REWARD.equals(uid)) {
                        //????????????
                        PLVRewardEvent.ContentBean rewardContentsBean = PolyvEventHelper.gson.fromJson(jsonObject_content.toString(), PLVRewardEvent.ContentBean.class);
                        PLVRewardEvent rewardEvent = new PLVRewardEvent();
                        if (rewardContentsBean != null) {
                            rewardEvent.setContent(rewardContentsBean);
                            rewardEvent.setEVENT(PolyvChatManager.EVENT_REWARD);
                            rewardEvent.setRoomId(jsonObject_user.optInt("roomId"));

                            String goodImage = rewardEvent.getContent().getGimg();
                            String nickName = rewardEvent.getContent().getUnick();
                            int goodNum = rewardEvent.getContent().getGoodNum();
                            Spannable rewardSpan = generateRewardSpan(nickName, goodImage, goodNum);
                            if (rewardSpan != null) {
                                rewardEvent.setObjects(rewardSpan);
                                int eventType = PolyvChatListAdapter.ChatTypeItem.TYPE_TIPS;
                                PolyvChatListAdapter.ChatTypeItem chatTypeItem = new PolyvChatListAdapter.ChatTypeItem(rewardEvent, eventType, PolyvChatManager.SE_MESSAGE);
                                tempChatItems.add(0, chatTypeItem);
                            }
                        }
                        continue;
                    }
                    if (PolyvHistoryConstant.UID_CUSTOMMSG.equals(uid)) {
                        //?????????????????????????????????
                        continue;
                    }
                    if (jsonObject_content != null) {
                        //content??????????????????????????????????????????
                        continue;
                    }
                    PolyvSpeakHistory speakHistory = PolyvEventHelper.gson.fromJson(jsonObject.toString(), PolyvSpeakHistory.class);
                    int type;
                    //?????????????????????????????????
                    if (myChatUserId.equals(speakHistory.getUser().getUserId())) {
                        type = PolyvChatListAdapter.ChatTypeItem.TYPE_SEND;
                    } else {
                        type = PolyvChatListAdapter.ChatTypeItem.TYPE_RECEIVE;
                    }

                    //???????????????????????????????????????
                    speakHistory.setObjects(PolyvTextImageLoader.messageToSpan(convertSpecialString(speakHistory.getContent()), ConvertUtils.dp2px(14), false, getContext()));
                    if(speakHistory.getQuote() != null && speakHistory.getQuote().getImage() == null){
                        speakHistory.getQuote().objects = PolyvTextImageLoader.messageToSpan(speakHistory.getQuote().getContent(), ConvertUtils.dp2px(12), false, getContext());
                    }

                    PolyvChatListAdapter.ChatTypeItem chatTypeItem = new PolyvChatListAdapter.ChatTypeItem(speakHistory, type, PolyvChatManager.SE_MESSAGE);
                    tempChatItems.add(0, chatTypeItem);
                    if (isOnlyHostType(speakHistory.getUser().getUserType(), speakHistory.getUser().getUserId())) {
                        tempTeacherChatItems.add(0, chatTypeItem);
                    }
                }
            }
        }
        return new List[]{tempChatItems, tempTeacherChatItems};
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????????????????">
    private void updateListData(List<PolyvChatListAdapter.ChatTypeItem>[] listArr, boolean isFirstLoad, boolean isHistory) {
        if (isHistory) {
            chatTypeItems.addAll(0, listArr[0]);
            teacherItems.addAll(0, listArr[1]);
            if (isOnlyWatchTeacher() && listArr[1].size() > 0) {
                chatListAdapter.notifyItemRangeInserted(0, listArr[1].size());
                chatMessageList.scrollToPosition(isFirstLoad ? chatListAdapter.getItemCount() - 1 : 0);

            } else if (!isOnlyWatchTeacher() && listArr[0].size() > 0) {
                chatListAdapter.notifyItemRangeInserted(0, listArr[0].size());
                chatMessageList.scrollToPosition(isFirstLoad ? chatListAdapter.getItemCount() - 1 : 0);
            }
        } else {
            int srcMaxPosition = chatListAdapter.getItemCount() - 1;
            chatTypeItems.addAll(listArr[0]);
            teacherItems.addAll(listArr[1]);
            if (isOnlyWatchTeacher() && listArr[1].size() > 0) {
                if (listArr[1].size() > 1)
                    chatListAdapter.notifyItemRangeInserted(srcMaxPosition + 1, chatListAdapter.getItemCount() - 1);
                else
                    chatListAdapter.notifyItemInserted(chatListAdapter.getItemCount() - 1);
                chatMessageList.scrollToBottomOrShowMore(listArr[1].size());

                //????????????????????????????????????????????????
                if (!isSelectedChat()) {
                    addUnreadChat(listArr[1].size());
                }
            } else if (!isOnlyWatchTeacher() && listArr[0].size() > 0) {
                if (listArr[0].size() > 1)
                    chatListAdapter.notifyItemRangeInserted(srcMaxPosition + 1, chatListAdapter.getItemCount() - 1);
                else
                    chatListAdapter.notifyItemInserted(chatListAdapter.getItemCount() - 1);
                chatMessageList.scrollToBottomOrShowMore(listArr[0].size());

                //????????????????????????????????????????????????
                if (!isSelectedChat()) {
                    addUnreadChat(listArr[0].size());
                }
            }
        }
    }


    private void removeItem(List<PolyvChatListAdapter.ChatTypeItem> lists, String chatMessageId, boolean isTeacherLists, boolean notifiyList) {
        for (int i = 0; i < lists.size(); i++) {
            PolyvChatListAdapter.ChatTypeItem chatTypeItem = lists.get(i);
            if (chatTypeItem.object instanceof PolyvSpeakEvent) {
                if (chatMessageId.equals(((PolyvSpeakEvent) chatTypeItem.object).getId())) {
                    lists.remove(chatTypeItem);
                    if (notifiyList &&
                            (!isOnlyWatchTeacher() && !isTeacherLists
                                    || (isOnlyWatchTeacher() && isTeacherLists))) {
                        chatListAdapter.notifyItemRemoved(i);
                    }
                    break;
                }
            } else if (chatTypeItem.object instanceof PolyvSpeakHistory) {
                if (chatMessageId.equals(((PolyvSpeakHistory) chatTypeItem.object).getId())) {
                    lists.remove(chatTypeItem);
                    if (notifiyList &&
                            (!isOnlyWatchTeacher() && !isTeacherLists
                                    || (isOnlyWatchTeacher() && isTeacherLists))) {
                        chatListAdapter.notifyItemRemoved(i);
                    }
                    break;
                }
            } else if (chatTypeItem.object instanceof PolyvLocalMessage){
                if (chatMessageId.equals(((PolyvLocalMessage) chatTypeItem.object).getId())) {
                    lists.remove(chatTypeItem);
                    if (notifiyList &&
                            (!isOnlyWatchTeacher() && !isTeacherLists
                                    || (isOnlyWatchTeacher() && isTeacherLists))) {
                        chatListAdapter.notifyItemRemoved(i);
                    }
                    break;
                }
            } else if (chatTypeItem.object instanceof PolyvSendLocalImgEvent){
                if (chatMessageId.equals(((PolyvSendLocalImgEvent) chatTypeItem.object).getId())) {
                    lists.remove(chatTypeItem);
                    if (notifiyList &&
                            (!isOnlyWatchTeacher() && !isTeacherLists
                                    || (isOnlyWatchTeacher() && isTeacherLists))) {
                        chatListAdapter.notifyItemRemoved(i);
                    }
                    break;
                }
            } else if(chatTypeItem.object instanceof PolyvChatImgEvent){
                if (chatMessageId.equals(((PolyvChatImgEvent) chatTypeItem.object).getId())) {
                    lists.remove(chatTypeItem);
                    if (notifiyList &&
                            (!isOnlyWatchTeacher() && !isTeacherLists
                                    || (isOnlyWatchTeacher() && isTeacherLists))) {
                        chatListAdapter.notifyItemRemoved(i);
                    }
                    break;
                }
            } else if(chatTypeItem.object instanceof PolyvChatImgHistory){
                if (chatMessageId.equals(((PolyvChatImgHistory) chatTypeItem.object).getId())) {
                    lists.remove(chatTypeItem);
                    if (notifiyList &&
                            (!isOnlyWatchTeacher() && !isTeacherLists
                                    || (isOnlyWatchTeacher() && isTeacherLists))) {
                        chatListAdapter.notifyItemRemoved(i);
                    }
                    break;
                }
            }
        }
    }

    private void removeItemWithMessageId(String chatMessageId) {
        if (TextUtils.isEmpty(chatMessageId))
            return;
        removeItem(chatTypeItems, chatMessageId, false, true);
        removeItem(teacherItems, chatMessageId, true, true);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????????????????">
    @Override
    public void sendMessage() {
        sendLocalMessage();
    }

    private void sendLocalMessage() {
        final String sendMessage = talk.getText().toString();
        if (sendMessage.trim().length() == 0) {
            toast.makeText(getContext(), "???????????????????????????", Toast.LENGTH_SHORT).show(true);
        } else {
            final PolyvLocalMessage localMessage = new PolyvLocalMessage(sendMessage);
            int sendValue = chatManager.sendChatMessage(localMessage, true, new Ack() {
                @Override
                public void call(final Object... args) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if ("".equals(args[0])) {
                                // ?????????????????????args[0]???""??????????????????????????????
                                Log.d(TAG, "??????????????????????????????");
                                return;
                            }
                            //???????????????????????????????????????
                            PolyvLocalMessage message = new PolyvLocalMessage(sendMessage);
                            message.setId(args[0].toString());
                            message.setObjects(PolyvTextImageLoader.messageToSpan(message.getSpeakMessage(), ConvertUtils.dp2px(14), false, getContext()));
                            addLocalMessageToAdapter(message);
                            //????????????
                            sendDanmu((CharSequence) message.getObjects()[0]);
                        }
                    });
                }
            });
            //?????????????????????????????????????????????????????????????????????????????????
            if (sendValue > 0 || sendValue == PolyvLocalMessage.SENDVALUE_BANIP) {
                talk.setText("");
                hideSoftInputAndEmoList();

                if(sendValue == PolyvLocalMessage.SENDVALUE_BANIP) {
//                //???????????????????????????????????????
                    localMessage.setObjects(PolyvTextImageLoader.messageToSpan(localMessage.getSpeakMessage(), ConvertUtils.dp2px(14), false, getContext()));
                    addLocalMessageToAdapter(localMessage);
                    //????????????
                    sendDanmu((CharSequence) localMessage.getObjects()[0]);
                }
            } else {
                toast.makeText(getContext(), "???????????????" + sendValue, PolyvToast.LENGTH_SHORT).show(true);
            }
        }
    }

    //?????????????????????????????????????????????
    public void sendChatMessageByDanmu(final String sendMessage) {
        if (talk == null) {
            return;
        }
        if (sendMessage.trim().length() == 0) {
            toast.makeText(getContext(), "???????????????????????????", Toast.LENGTH_SHORT).show(true);
        } else {
            PolyvLocalMessage localMessage = new PolyvLocalMessage(sendMessage);
            int sendValue = chatManager.sendChatMessage(localMessage, true, new Ack() {
                @Override
                public void call(final Object... args) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //???????????????????????????????????????
                            PolyvLocalMessage message = new PolyvLocalMessage(sendMessage);
                            if (args[0]!= null) {
                                message.setId(args[0].toString());
                            }
                            message.setObjects(PolyvTextImageLoader.messageToSpan(message.getSpeakMessage(), ConvertUtils.dp2px(14), false, getContext()));
                            addLocalMessageToAdapter(message);
                            //????????????
                            sendDanmu((CharSequence) message.getObjects()[0]);
                        }
                    });
                }
            });
            //??????????????????
            if (sendValue > 0 || sendValue == PolyvLocalMessage.SENDVALUE_BANIP) {//?????????????????????????????????????????????????????????
                talk.setText("");
                hideSoftInputAndEmoList();

                if(sendValue == PolyvLocalMessage.SENDVALUE_BANIP) {
//                //???????????????????????????????????????
                    localMessage.setObjects(PolyvTextImageLoader.messageToSpan(localMessage.getSpeakMessage(), ConvertUtils.dp2px(14), false, getContext()));
                    addLocalMessageToAdapter(localMessage);
                    //????????????
                    sendDanmu((CharSequence) localMessage.getObjects()[0]);
                }
            } else {
                toast.makeText(getContext(), "???????????????" + sendValue, PolyvToast.LENGTH_SHORT).show(true);
            }
        }
    }

    private void addLocalMessageToAdapter(PolyvLocalMessage message){
        PolyvChatListAdapter.ChatTypeItem chatTypeItem = new PolyvChatListAdapter.ChatTypeItem(message, PolyvChatListAdapter.ChatTypeItem.TYPE_SEND, PolyvChatManager.SE_MESSAGE);
        chatTypeItems.add(chatTypeItem);
        teacherItems.add(chatTypeItem);
        chatListAdapter.notifyItemInserted(chatListAdapter.getItemCount() - 1);
        chatMessageList.scrollToPosition(chatListAdapter.getItemCount() - 1);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="???????????????????????????????????????">
    private void acceptLoginSuccessEvent() {
        PolyvChatFunctionSwitchVO chatFunctionSwitchVO = chatManager.getChatFunctionSwitchVO();
        if (chatFunctionSwitchVO == null)
            return;
        List<PolyvChatFunctionSwitchVO.DataBean> dataBeanList = chatFunctionSwitchVO.getData();
        if (dataBeanList == null)
            return;
        for (PolyvChatFunctionSwitchVO.DataBean dataBean : dataBeanList) {
//            if (PolyvChatFunctionSwitchVO.ENABLE_Y.equals(dataBean.getEnabled())) {
//                //??????????????????????????????????????????????????????
//                if (PolyvChatFunctionSwitchVO.TYPE_VIEWER_SEND_IMG_ENABLED.equals(dataBean.getType())) {
//                    selectPhotoLayout.setVisibility(View.VISIBLE);
//                    openCameraLayout.setVisibility(View.VISIBLE);
//                } else if (PolyvChatFunctionSwitchVO.TYPE_WELCOME.equals(dataBean.getType())) {
//                    isShowGreeting = true;
//                }
//            } else {
//                if (PolyvChatFunctionSwitchVO.TYPE_WELCOME.equals(dataBean.getType())) {
//                    isShowGreeting = false;
//                }
//            }
            boolean isSwitchEnabled = dataBean.isEnabled();
            switch (dataBean.getType()) {
                //????????????????????????
                case PolyvChatFunctionSwitchVO.TYPE_VIEWER_SEND_IMG_ENABLED:
                    if (isSwitchEnabled) {
                        selectPhotoLayout.setVisibility(View.VISIBLE);
                        openCameraLayout.setVisibility(View.VISIBLE);
                    }
                    break;
                //???????????????
                case PolyvChatFunctionSwitchVO.TYPE_WELCOME:
                    isShowGreeting = isSwitchEnabled;
                    break;
                //????????????
                case PolyvChatFunctionSwitchVO.TYPE_SEND_FLOWERS_ENABLED:
                    flFlower.setVisibility(isSwitchEnabled ? View.VISIBLE : View.GONE);
                    break;
            }

        }
    }

    private void acceptLoginEvent(PolyvLoginEvent loginEvent) {
//        greetingView.acceptLoginEvent(loginEvent);
        greetingText.acceptLoginEvent(loginEvent);
    }

    private void acceptConnectStatus() {
        disposables.add(PolyvSingleRelayBus.get().toObservable(ConnectStatus.class).subscribe(new Consumer<ConnectStatus>() {
            @Override
            public void accept(ConnectStatus connectStatus) throws Exception {
                int status = connectStatus.status;
                Throwable t = connectStatus.t;

                switch (status) {
                    case PolyvConnectStatusListener.STATUS_DISCONNECT:
                        if (t != null) {
                            bgStatus.setText("????????????(" + t.getMessage() + ")");
                            bgStatus.show();
                        }//t???null?????????????????????????????????????????????
                        break;
                    case PolyvConnectStatusListener.STATUS_LOGINING:
                        bgStatus.setText("???????????????...");
                        bgStatus.show();
                        break;
                    case PolyvConnectStatusListener.STATUS_LOGINSUCCESS:
                        bgStatus.setText("????????????");
                        bgStatus.show(2000);
                        acceptLoginSuccessEvent();
                        break;
                    case PolyvConnectStatusListener.STATUS_RECONNECTING:
                        isCloseRoomReconnect = isCloseRoom;
                        bgStatus.setText("???????????????...");
                        bgStatus.show();
                        break;
                    case PolyvConnectStatusListener.STATUS_RECONNECTSUCCESS:
                        bgStatus.setText("????????????");
                        bgStatus.show(2000);
                        //???Bus???????????????TuWenView????????????
                        PolyvRxBus.get().post(new PolyvTuWenMenuFragment.BUS_EVENT(PolyvTuWenMenuFragment.BUS_EVENT.TYPE_REFRESH));
                        break;
                }
            }
        }));
    }

    private String convertSpecialString(String input) {
        String output;
        output = input.replace("&lt;", "<");
        output = output.replace("&lt", "<");
        output = output.replace("&gt;", ">");
        output = output.replace("&gt", ">");
        output = output.replace("&yen;", "??");
        output=output.replace("&yen","??");
        return output;
    }

    private void acceptEventMessage() {
        disposables.add(PolyvRxBus.get().toObservable(EventMessage.class).buffer(500, TimeUnit.MILLISECONDS).map(new Function<List<EventMessage>, List<PolyvChatListAdapter.ChatTypeItem>[]>() {
            @Override
            public List<PolyvChatListAdapter.ChatTypeItem>[] apply(List<EventMessage> eventMessages) throws Exception {
                final List<PolyvChatListAdapter.ChatTypeItem> tempChatItems = new ArrayList<>();
                final List<PolyvChatListAdapter.ChatTypeItem> tempTeacherChatItems = new ArrayList<>();
                for (EventMessage eventMessage : eventMessages) {
                    String event = eventMessage.event;
                    String message = eventMessage.message;
                    String socketListen = eventMessage.socketListen;
                    Object eventObject = null;
                    int eventType = -1;
                    if (PolyvChatManager.SE_CUSTOMMESSAGE.equals(socketListen)) {
                        //do nothing

                    } else {
                        switch (event) {
                            //??????????????????
                            case PolyvChatManager.EVENT_SPEAK:
                                final PolyvSpeakEvent speakEvent = PolyvEventHelper.getEventObject(PolyvSpeakEvent.class, message, event);
                                if (speakEvent != null) {
                                    eventObject = speakEvent;
                                    eventType = PolyvChatListAdapter.ChatTypeItem.TYPE_RECEIVE;
                                    //???????????????????????????????????????
                                    String speakMsg = speakEvent.getValues().get(0);
                                    speakMsg = convertSpecialString(speakMsg);
                                    speakEvent.setObjects(PolyvTextImageLoader.messageToSpan(speakMsg, ConvertUtils.dp2px(14), false, getContext()));
                                    if(speakEvent.getQuote() != null && speakEvent.getQuote().getImage() == null){
                                        speakEvent.getQuote().objects = PolyvTextImageLoader.messageToSpan(speakEvent.getQuote().getContent(), ConvertUtils.dp2px(12), false, getContext());
                                    }

                                    //????????????????????????????????????
                                    if (isOnlyHostType(speakEvent.getUser().getUserType(), speakEvent.getUser().getUserId())) {
                                        tempTeacherChatItems.add(new PolyvChatListAdapter.ChatTypeItem(eventObject, eventType, socketListen));
                                    }
                                    //??????????????????????????????
                                    if (PolyvChatManager.USERTYPE_MANAGER.equals(speakEvent.getUser().getUserType())) {
                                        startMarquee((CharSequence) speakEvent.getObjects()[0]);//?????????????????????
                                    }
                                    //????????????
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            sendDanmu((CharSequence) speakEvent.getObjects()[0]);
                                        }
                                    });
                                }
                                break;
                            //??????????????????
                            case PolyvChatManager.EVENT_CHAT_IMG:
                                PolyvChatImgEvent chatImgEvent = PolyvEventHelper.getEventObject(PolyvChatImgEvent.class, message, event);
                                if (chatImgEvent != null) {
                                    eventObject = chatImgEvent;
                                    eventType = PolyvChatListAdapter.ChatTypeItem.TYPE_RECEIVE;

                                    //????????????????????????????????????
                                    if (isOnlyHostType(chatImgEvent.getUser().getUserType(), chatImgEvent.getUser().getUserId())) {
                                        tempTeacherChatItems.add(new PolyvChatListAdapter.ChatTypeItem(eventObject, eventType, socketListen));
                                    }
                                }
                                break;
                            //??????(??????????????????)
                            case PolyvChatManager.EVENT_LIKES:
                                PolyvLikesEvent likesEvent = PolyvEventHelper.getEventObject(PolyvLikesEvent.class, message, event);
                                if (likesEvent != null) {
                                    if (!chatManager.userId.equals(likesEvent.getUserId())) {
                                        if (isNormalLive) {//??????
                                            liv_like.addLoveIcon();

                                            if (isShowLikeTips) {
                                                eventObject = likesEvent;
                                                eventType = PolyvChatListAdapter.ChatTypeItem.TYPE_TIPS;

                                                //?????????????????????????????????????????????
                                                likesEvent.setObjects(likesEvent.getNick() + " ??????????????????????????????");
                                            }
                                        } else {//??????
                                            eventObject = likesEvent;
                                            eventType = PolyvChatListAdapter.ChatTypeItem.TYPE_TIPS;

                                            //?????????????????????????????????????????????
                                            likesEvent.setObjects(generateLikeSpan(likesEvent.getNick()));
                                        }
                                    }
                                }
                                break;
                            //???????????????/?????????????????????????????????
                            case PolyvChatManager.EVENT_CLOSEROOM:
                                PolyvCloseRoomEvent closeRoomEvent = PolyvEventHelper.getEventObject(PolyvCloseRoomEvent.class, message, event);
                                if (closeRoomEvent != null) {
                                    eventObject = closeRoomEvent;
                                    eventType = PolyvChatListAdapter.ChatTypeItem.TYPE_TIPS;
                                    isCloseRoom = closeRoomEvent.getValue().isClosed();
                                    if (isCloseRoomReconnect) {
                                        isCloseRoomReconnect = !isCloseRoomReconnect;
                                    }
                                }
                                break;
                            //??????
                            case PolyvChatManager.EVENT_GONGGAO:
                                final PolyvGongGaoEvent gongGaoEvent = PolyvEventHelper.getEventObject(PolyvGongGaoEvent.class, message, event);
                                break;
                            //????????????????????????
                            case PolyvChatManager.EVENT_REMOVE_CONTENT:
                                final PolyvRemoveContentEvent removeContentEvent = PolyvEventHelper.getEventObject(PolyvRemoveContentEvent.class, message, event);
                                if (removeContentEvent != null) {
                                    removeItem(tempChatItems, removeContentEvent.getId(), false, false);
                                    removeItem(tempTeacherChatItems, removeContentEvent.getId(), true, false);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            String chatMessageId = removeContentEvent.getId();
                                            removeItemWithMessageId(chatMessageId);
                                        }
                                    });
                                }
                                break;
                            //??????????????????
                            case PolyvChatManager.EVENT_REMOVE_HISTORY:
                                final PolyvRemoveHistoryEvent removeHistoryEvent = PolyvEventHelper.getEventObject(PolyvRemoveHistoryEvent.class, message, event);
                                if (removeHistoryEvent != null) {
                                    tempChatItems.clear();
                                    tempTeacherChatItems.clear();
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            teacherItems.clear();
                                            chatTypeItems.clear();
                                            chatListAdapter.notifyDataSetChanged();
                                            toast.makeText(getContext(), "?????????????????????????????????", PolyvToast.LENGTH_LONG).show(true);
                                        }
                                    });
                                }
                                break;
                            //??????????????????????????????????????????????????????????????????????????????????????????
                            case PolyvChatManager.EVENT_CUSTOMER_MESSAGE:
                                PolyvCustomerMessageEvent customerMessageEvent = PolyvEventHelper.getEventObject(PolyvCustomerMessageEvent.class, message, event);
                                break;
                            //??????
                            case PolyvChatManager.EVENT_KICK:
                                final PolyvKickEvent kickEvent = PolyvEventHelper.getEventObject(PolyvKickEvent.class, message, event);
                                if (kickEvent != null) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (chatManager.userId.equals(kickEvent.getUser().getUserId())) {
                                                PolyvBaseActivity.showKickTips(getActivity(), "????????????????????????????????????");
                                            }
                                        }
                                    });
                                }
                                break;
                            //????????????????????????????????????(??????????????????????????????????????????????????????????????????????????????)
                            case PolyvChatManager.EVENT_LOGIN_REFUSE:
                                PolyvLoginRefuseEvent loginRefuseEvent = PolyvEventHelper.getEventObject(PolyvLoginRefuseEvent.class, message, event);
                                if (loginRefuseEvent != null) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            //?????????????????????????????????????????????????????????
                                            chatManager.disconnect();
                                            bgStatus.hide();
                                            PolyvBaseActivity.showKickTips(getActivity(), "????????????????????????????????????");
                                        }
                                    });
                                }
                                break;
                            //????????????????????????????????????????????????????????????loginEvent?????????
                            case PolyvChatManager.EVENT_LOGIN:
                                PolyvLoginEvent loginEvent = PolyvEventHelper.getEventObject(PolyvLoginEvent.class, message, event);
                                if (loginEvent != null) {
                                    if (isShowGreeting) {
                                        acceptLoginEvent(loginEvent);
                                    }
                                    if (chatManager.userId.equals(loginEvent.getUser().getUserId())) {
                                        //?????????????????????/????????????
//                                    if (isBanIp != loginEvent.getUser().isBanned()) {
//                                        tempChatItems.add(new PolyvChatListAdapter.ChatTypeItem(loginEvent.getUser().isBanned() ? new PolyvBanIpEvent() : new PolyvUnshieldEvent(), PolyvChatListAdapter.ChatTypeItem.TYPE_TIPS));
//                                    }
                                        //???????????????????????????????????????????????????
                                        if (isCloseRoomReconnect) {
                                            isCloseRoom = false;
                                            //????????????????????????????????????????????????
                                            PolyvCloseRoomEvent closeRoomEventS = new PolyvCloseRoomEvent();
                                            PolyvCloseRoomEvent.ValueBean valueBean = new PolyvCloseRoomEvent.ValueBean();
                                            valueBean.setClosed(isCloseRoom);
                                            closeRoomEventS.setValue(valueBean);
                                            tempChatItems.add(new PolyvChatListAdapter.ChatTypeItem(closeRoomEventS, PolyvChatListAdapter.ChatTypeItem.TYPE_TIPS, socketListen));
                                        }
                                    }
                                }
                                break;
                            //??????
                            case PolyvChatManager.EVENT_BANIP:
                                PolyvBanIpEvent banIpEvent = PolyvEventHelper.getEventObject(PolyvBanIpEvent.class, message, event);
                                break;
                            //????????????
                            case PolyvChatManager.EVENT_UNSHIELD:
                                PolyvUnshieldEvent unshieldEvent = PolyvEventHelper.getEventObject(PolyvUnshieldEvent.class, message, event);
                                break;
                            case PolyvChatManager.EVENT_RELOGIN:
                                final PolyvReloginEvent reloginEvent = PolyvEventHelper.getEventObject(PolyvReloginEvent.class, message, event);
                                if (reloginEvent != null) {
                                    disposables.add(AndroidSchedulers.mainThread().createWorker().schedule(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (chatManager.userId.equals(reloginEvent.getUser().getUserId())) {
                                                PolyvBaseActivity.showReloginTip(getActivity(), reloginEvent.getChannelId(), "????????????????????????????????????????????????????????????");
                                            }
                                        }
                                    }));
                                }
                                break;
                            //????????????
                            case PolyvChatManager.EVENT_REWARD:
                                PLVRewardEvent rewardEvent = PolyvEventHelper.getEventObject(PLVRewardEvent.class, message, event);
                                if (rewardEvent == null) {
                                    break;
                                }
                                if (pointRewardEventProducer != null) {
                                    if (ScreenUtils.isPortrait()) {
                                        //?????????????????????????????????
                                        pointRewardEventProducer.addEvent(rewardEvent);
                                    }
                                }
                                if (rewardEvent.getContent() != null) {
                                    String goodImage = rewardEvent.getContent().getGimg();
                                    String nickName = rewardEvent.getContent().getUnick();
                                    int goodNum = rewardEvent.getContent().getGoodNum();
                                    Spannable rewardSpan = generateRewardSpan(nickName, goodImage, goodNum);
                                    if (rewardSpan != null) {
                                        rewardEvent.setObjects(rewardSpan);
                                        eventType = PolyvChatListAdapter.ChatTypeItem.TYPE_TIPS;
                                        eventObject = rewardEvent;
                                    }
                                }
                                break;
                        }
                    }

                    //??????????????????eventObject????????????????????????
                    if (eventObject != null && eventType != -1) {
                        tempChatItems.add(new PolyvChatListAdapter.ChatTypeItem(eventObject, eventType, socketListen));
                    }
                }
                return new List[]{tempChatItems, tempTeacherChatItems};
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<List<PolyvChatListAdapter.ChatTypeItem>[]>() {
            @Override
            public void accept(List<PolyvChatListAdapter.ChatTypeItem>[] listArr) throws Exception {
                updateListData(listArr, false, false);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                toast.makeText(getContext(), "????????????????????????????????????(" + throwable.getMessage() + ")", PolyvToast.LENGTH_LONG).show(true);
            }
        }));
    }

    private Spannable generateLikeSpan(String sendNick) {
        SpannableStringBuilder span = new SpannableStringBuilder(sendNick + " ???????????????p");
        Drawable drawable = getContext().getResources().getDrawable(R.drawable.polyv_gift_flower);
        int textSize = ConvertUtils.dp2px(12);
        drawable.setBounds(0, 0, textSize * 2, textSize * 2);
        span.setSpan(new RelativeImageSpan(drawable, RelativeImageSpan.ALIGN_CENTER), span.length() - 1, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    private Spannable generateRewardSpan(String nickName, String goodImageUrl, int goodNum) {
        SpannableStringBuilder span = new SpannableStringBuilder(nickName + " ??????p");
        int drawableSpanStart = span.length() - 1;
        int drawableSpanEnd = span.length();
        if (goodNum != 1) {
            span.append(" x" + goodNum);
        }
        Drawable drawable = PolyvImageLoader.getInstance().getImageAsDrawable(getContext(), goodImageUrl);
        if (drawable == null) {
            return null;
        }
        int textSize = ConvertUtils.dp2px(12);
        drawable.setBounds(0, 0, textSize * 2, textSize * 2);
        span.setSpan(new RelativeImageSpan(drawable, RelativeImageSpan.ALIGN_CENTER), drawableSpanStart, drawableSpanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????????????????">
    private void resetOnlyHostRelateView(boolean enabled) {
        talk.setEnabled(enabled);
        emoji.setEnabled(enabled);
        flower.setEnabled(enabled);
        like.setEnabled(enabled);
        more.setEnabled(enabled);
    }

    private boolean isOnlyWatchTeacher() {
        return onlyHostSwitch.isSelected();
    }

    private boolean isOnlyHostType(String userType, String userId) {
        //????????????????????????????????????????????????????????????????????????
        return isTeacherType(userType) || chatManager.userId.equals(userId);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">
    private void initPointReward() {
        if (getActivity() == null) {
            return;
        }

        //******* ?????????????????? start
        PolyvPointRewardEffectWidget polyvPointRewardEffectWidget = new PolyvPointRewardEffectWidget(getActivity());
        RelativeLayout.LayoutParams lpOfEffectView = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpOfEffectView.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        plvRlGroupChatFragment.addView(polyvPointRewardEffectWidget, lpOfEffectView);
        //******* ?????????????????? end

        //****** ???????????????????????? start
        pointRewardEventProducer = new PolyvPointRewardEffectQueue();
        polyvPointRewardEffectWidget.setEventProducer(pointRewardEventProducer);
        //****** ???????????????????????? end

        //****** ?????????????????? start
        final String channelId = chatManager.roomId;
        final String viewerId = chatManager.userId;
        final String nickname = chatManager.nickName;
        final String avatarUrl = chatManager.imageUrl;

        pointRewardWindow = new PolyvPointRewardDialog((AppCompatActivity) getActivity(), new PolyvPointRewardDialog.OnMakePointRewardListener() {
            @Override
            public void onMakeReward(int goodNum, int goodId) {
                if (pointRewardRepository == null) {
                    return;
                }
                //????????????
                pointRewardRepository.makeReward(channelId, goodId, goodNum, viewerId, nickname, avatarUrl, new IPolyvPointRewardDataSource.IPointRewardListener<Integer>() {
                    @Override
                    public void onSuccess(Integer integer) {
                        pointRewardWindow.updateRemainingPoint(integer);
                    }

                    @Override
                    public void onFailed(Throwable throwable) {
                        ToastUtils.showShort(throwable.getMessage());
                    }
                });
            }
        }, new PolyvPointRewardDialog.OnShowListener() {
            @Override
            public void onShow() {
                //??????????????????
                pointRewardRepository.getRemainingRewardPoint(channelId, viewerId, nickname, new IPolyvPointRewardDataSource.IPointRewardListener<Integer>() {
                    @Override
                    public void onSuccess(Integer integer) {
                        pointRewardWindow.updateRemainingPoint(integer);
                    }

                    @Override
                    public void onFailed(Throwable throwable) {
                        ToastUtils.showShort(throwable.getMessage());
                    }
                });
            }
        });


        pointRewardRepository = new PLVPointRewardDataSource();
        pointRewardRepository.getPointRewardSetting(chatManager.roomId, new IPolyvPointRewardDataSource.IPointRewardListener<PolyvPointRewardSettingVO>() {
            @Override
            public void onSuccess(final PolyvPointRewardSettingVO polyvPointRewardSettingVO) {
                try {
                    // ????????????????????????????????????
//                LogUtils.d(PolyvGsonUtil.toJson(polyvPointRewardSettingVO));
                    if ("Y".equals(polyvPointRewardSettingVO.getDonatePointEnabled())
                            && "Y".equals(polyvPointRewardSettingVO.getChannelDonatePointEnabled())) {
                        //?????????????????????????????????????????????????????????????????????????????????????????????

                        isPointRewardEnabled = true;

                        //??????enable???????????????????????????????????????
                        List<PolyvPointRewardSettingVO.GoodsBean> enabledGoods = new ArrayList<>(polyvPointRewardSettingVO.getGoods().size());
                        for (PolyvPointRewardSettingVO.GoodsBean good : polyvPointRewardSettingVO.getGoods()) {
                            if ("Y".equals(good.getGoodEnabled())) {
                                enabledGoods.add(good);
                            }
                        }
                        polyvPointRewardSettingVO.setGoods(enabledGoods);

                        //???????????????????????????????????????Id??????1???????????????
                        for (int i = 0; i < polyvPointRewardSettingVO.getGoods().size(); i++) {
                            polyvPointRewardSettingVO.getGoods().get(i).setGoodId(i + 1);
                        }

                        pointRewardWindow.setPointRewardSettingVO(polyvPointRewardSettingVO);
                        ivShowPointReward.setVisibility(View.VISIBLE);


                    } else {
                        isPointRewardEnabled = false;
                        ivShowPointReward.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    isPointRewardEnabled = false;
                    ivShowPointReward.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailed(Throwable throwable) {
                //????????????????????????????????????????????????
                PolyvCommonLog.exception(throwable);
            }
        });
        //****** ?????????????????? end

//        //????????????
//        ViewGroup viewGroup = getActivity().findViewById(android.R.id.content);
//        Button btn = new Button(getActivity());
//        btn.setText("??????????????????");
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                pointRewardRepository.makeReward(channelId, 1, 1, viewerId, nickname, avatarUrl, new IPolyvPointRewardDataSource.IPointRewardListener<Integer>() {
//                    @Override
//                    public void onSuccess(Integer integer) {
//                        LogUtils.d(integer);
//                    }
//
//                    @Override
//                    public void onFailed(Throwable throwable) {
//                        LogUtils.e(throwable);
//                    }
//                });
//            }
//        });
//        viewGroup.addView(btn, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="is???has??????">
    public static boolean isTeacherType(String userType) {
        //???????????????????????????????????????????????????????????????????????????
        return PolyvChatManager.USERTYPE_MANAGER.equals(userType)
                || PolyvChatManager.USERTYPE_TEACHER.equals(userType)
                || PolyvChatManager.USERTYPE_GUEST.equals(userType)
                || PolyvChatManager.USERTYPE_ASSISTANT.equals(userType);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????????">
    private void startMarquee(final CharSequence msg) {
        disposableGonggaoCd();
        handler.post(new Runnable() {
            @Override
            public void run() {
                ((ViewGroup) tvGongGao.getParent()).setVisibility(View.VISIBLE);
                tvGongGao.setText(msg);
                tvGongGao.setOnGetRollDurationListener(new PolyvMarqueeTextView.OnGetRollDurationListener() {
                    @Override
                    public void onFirstGetRollDuration(int rollDuration) {
                        startCountDown(rollDuration * 3 + tvGongGao.getScrollFirstDelay());
                    }
                });
                tvGongGao.stopScroll();
                tvGongGao.startScroll();
            }
        });
    }

    private void startCountDown(long time) {
        gonggaoCdDisposable = Observable.timer(time, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        tvGongGao.setVisibility(View.INVISIBLE);
                        tvGongGao.stopScroll();
                        ((ViewGroup) tvGongGao.getParent()).setVisibility(View.GONE);
                        ((ViewGroup) tvGongGao.getParent()).clearAnimation();
                        ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 1f, 1f, 0f);
                        scaleAnimation.setDuration(555);
                        ((ViewGroup) tvGongGao.getParent()).startAnimation(scaleAnimation);
                    }
                });
    }

    private void disposableGonggaoCd() {
        if (gonggaoCdDisposable != null) {
            gonggaoCdDisposable.dispose();
            gonggaoCdDisposable = null;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="get???set??????">
    public void setNormalLive(boolean normalLive) {
        isNormalLive = normalLive;
    }

    public void setOnlyHostCanSendMessage(boolean onlyHostCanSendMessage) {
        isOnlyHostCanSendMessage = onlyHostCanSendMessage;
    }

    public void setShowLikeTips(boolean isShowLikeTips) {
        this.isShowLikeTips = isShowLikeTips;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fragment??????">
    @Override
    public void onDestroy() {
        super.onDestroy();
        disposableGonggaoCd();
        pointRewardRepository.destroy();
        pointRewardEventProducer.destroy();
    }
    // </editor-fold>
}
