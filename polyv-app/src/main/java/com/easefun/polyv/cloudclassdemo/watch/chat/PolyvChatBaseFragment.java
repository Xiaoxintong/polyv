package com.easefun.polyv.cloudclassdemo.watch.chat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easefun.polyv.businesssdk.api.common.player.microplayer.PolyvCommonVideoView;
import com.easefun.polyv.businesssdk.sub.gif.GifImageSpan;
import com.easefun.polyv.businesssdk.sub.gif.RelativeImageSpan;
import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.chat.event.PolyvChatImgEvent;
import com.easefun.polyv.cloudclass.chat.history.PolyvChatImgHistory;
import com.easefun.polyv.cloudclass.chat.playback.PolyvChatPlaybackImg;
import com.easefun.polyv.cloudclass.chat.send.img.PolyvSendLocalImgEvent;
import com.easefun.polyv.cloudclassdemo.watch.IPolyvHomeProtocol;
import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.PolyvChatListAdapter;
import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.PolyvEmoListAdapter;
import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.viewholder.PolyvSendMessageHolder;
import com.easefun.polyv.cloudclassdemo.watch.chat.imageScan.PolyvChatImageViewer;
import com.easefun.polyv.commonui.R;
import com.easefun.polyv.commonui.adapter.PolyvBaseRecyclerViewAdapter;
import com.easefun.polyv.commonui.adapter.viewholder.ClickableViewHolder;
import com.easefun.polyv.commonui.base.PolyvBaseFragment;
import com.easefun.polyv.commonui.utils.PolyvFaceManager;
import com.easefun.polyv.commonui.utils.PolyvPictureUtils;
import com.easefun.polyv.commonui.utils.PolyvToast;
import com.easefun.polyv.commonui.utils.PolyvUriPathHelper;
import com.easefun.polyv.commonui.widget.PolyvAnswerView;
import com.easefun.polyv.commonui.widget.PolyvChatRecyclerView;
import com.easefun.polyv.foundationsdk.permission.PolyvOnGrantedListener;
import com.easefun.polyv.foundationsdk.permission.PolyvPermissionManager;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBus;
import com.easefun.polyv.foundationsdk.utils.PolyvSDCardUtils;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.KeyboardUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;

public abstract class PolyvChatBaseFragment extends PolyvBaseFragment {
    // <editor-fold defaultstate="collapsed" desc="????????????">
    protected PolyvChatManager chatManager;
    //????????????
    protected PolyvChatRecyclerView chatMessageList;
    protected PolyvChatListAdapter chatListAdapter;
    //?????????item???????????????????????????????????????
    protected List<PolyvChatListAdapter.ChatTypeItem> chatTypeItems = new ArrayList<>();
    //??????????????????
    protected LinearLayout talkLayout;
    protected FrameLayout talkParentLayout;
    //???????????????
    protected EditText talk;
    //??????
    protected ImageView emoji;
    //??????
    protected RelativeLayout emoListLayout;
    protected RecyclerView emoList;
    protected PolyvEmoListAdapter emoListAdapter;
    //??????????????????
    protected ImageView ivMsgDelete;
    protected TextView tvSendMsg;
    // ?????????????????????
    private int emoLength;
    //??????????????????
    protected TextView unread;
    protected IPolyvHomeProtocol homePresnter;
    protected PolyvChatImageViewer imageViewer;
    protected boolean isKeyboardVisible;
    protected int keyboardHeight;
    protected List<View> popupBottomList = new ArrayList<>();
    protected List<ViewGroup> popupLayoutList = new ArrayList<>();
    protected boolean isShowPopupLayout;
    protected ViewGroup currentShowPopupLayout;
    protected int srcViewPagerHeight;

    //?????????????????????????????????????????????????????????
    protected ImageView more, selectPhotoButton, openCameraButton, openBulletinButton;
    //?????????????????????
    protected LinearLayout moreLayout, selectPhotoLayout, openCameraLayout, openBulletinLayout;

    protected File takePhotosFilePath;
    protected Uri takeCameraUri;
    protected static final int REQUEST_SELECT_PHOTO = 0x01;
    protected static final int REQUEST_OPEN_CAMERA = 0x02;

    //???????????????????????????????????????????????????????????????????????????
    protected List<PolyvChatListAdapter.ChatTypeItem> teacherItems = new ArrayList<>();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">
    protected void sendDanmu(CharSequence content) {
        if (homePresnter != null) {
            homePresnter.sendDanmu(content);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="get???set???add???is??????">
    protected PolyvCommonVideoView getVideoView() {
        if (homePresnter != null) {
            return homePresnter.getVideoView();
        }
        return null;
    }

    protected ViewGroup getImageViewerContainer() {
        if (homePresnter != null) {
            return homePresnter.getImageViewerContainer();
        }
        return null;
    }

    protected ViewGroup getChatEditContainer() {
        if (homePresnter != null) {
            return homePresnter.getChatEditContainer();
        }
        return null;
    }

    protected String getSessionId() {
        if (homePresnter != null) {
            return homePresnter.getSessionId();
        }
        return null;
    }

    protected void addUnreadQuiz(int unreadCount) {
        if (homePresnter != null) {
            homePresnter.addUnreadQuiz(unreadCount);
        }
    }

    protected boolean isSelectedQuiz() {
        if (homePresnter != null) {
            return homePresnter.isSelectedQuiz();
        }
        return false;
    }

    public int getChatListUnreadCount() {
        if (chatMessageList != null) {
            return chatMessageList.getUnreadCount();
        }
        return 0;
    }

    protected void addUnreadChat(int unreadCount) {
        if (homePresnter != null) {
            homePresnter.addUnreadChat(unreadCount);
        }
    }

    protected boolean isSelectedChat() {
        if (homePresnter != null) {
            return homePresnter.isSelectedChat();
        }
        return false;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????">
    @Override
    public void loadDataAhead() {
    }

    @Override
    public void loadDataDelay(boolean isFirst) {
        final ViewGroup chatEditLayout = getChatEditContainer();
        if (chatEditLayout != null) {
            chatEditLayout.setOnTouchListener(new View.OnTouchListener() {//??????fragment???????????????
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN && getUserVisibleHint()) {
                        if (unread.getVisibility() == View.VISIBLE) {//???????????????????????????????????????
                            float rx = event.getRawX();
                            float ry = event.getRawY();
                            int[] location = new int[2];
                            unread.getLocationOnScreen(location);
                            if (rx >= location[0] && rx <= location[0] + unread.getWidth() &&
                                    ry >= location[1] && ry <= location[1] + unread.getHeight()) {
                                unread.performClick();
                            } else {
                                hideSoftInputAndEmoList();
                            }
                        } else {
                            hideSoftInputAndEmoList();
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    protected void initCommonView() {
        //????????????
        chatMessageList = findViewById(R.id.chat_message_list);
        chatMessageList.initRecyclerView(chatMessageList, getContext());
        chatListAdapter = new PolyvChatListAdapter(chatMessageList, chatTypeItems);
        chatListAdapter.setOnChatImgViewClickListener(new PolyvChatListAdapter.OnChatImgViewClickListener() {
            @Override
            public void onClick(ImageView iv, int position) {
                ViewGroup vp = getImageViewerContainer();
                if (vp != null) {
                    if (imageViewer == null) {
                        imageViewer = new PolyvChatImageViewer(getContext());
                        imageViewer.setPermissionManager(permissionManager);
                    }
                    List<PolyvChatListAdapter.ChatTypeItem> imgItemList = new ArrayList<>();
                    for (PolyvChatListAdapter.ChatTypeItem chatTypeItem : chatListAdapter.getChatTypeItems()) {
                        if (chatTypeItem.object instanceof PolyvChatImgEvent
                                || chatTypeItem.object instanceof PolyvChatImgHistory
                                || chatTypeItem.object instanceof PolyvSendLocalImgEvent
                                || chatTypeItem.object instanceof PolyvChatPlaybackImg) {
                            imgItemList.add(chatTypeItem);
                        }
                    }
                    for (int i = 0; i < imgItemList.size(); i++) {
                        if (imgItemList.get(i) == chatListAdapter.getChatTypeItems().get(position)) {
                            position = i;
                            break;
                        }
                    }
                    imageViewer.setData(imgItemList, position);
                    imageViewer.attachRootView(vp);
                }
            }
        });
        chatMessageList.setAdapter(chatListAdapter);
        chatMessageList.addOnScrollListener();
        //????????????
        talkLayout = findViewById(R.id.ll_bottom);
        talkParentLayout = findViewById(R.id.fl_bottom);
        final ViewGroup chatEditLayout = getChatEditContainer();
        //??????????????????
        if (chatEditLayout != null) {
            chatEditLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (!getUserVisibleHint())
                        return;
                    if (bottom > 0 && oldBottom > 0 && right == oldRight) {
                        if ((keyboardHeight = Math.abs(bottom - oldBottom)) > PolyvScreenUtils.getNormalWH(getActivity())[1] * 0.3)
                            // ????????????
                            if (bottom > oldBottom) {
                                isKeyboardVisible = false;
                                acceptKeyboardCloseEvent(getCurrentShowPopupLayout(), chatEditLayout);
                            }// ????????????
                            else if (bottom < oldBottom) {
                                isKeyboardVisible = true;
                            }
                    } else if (right > 0 && oldRight > 0 && right != oldRight && isKeyboardVisible) {//???????????????????????????????????????
                        if (bottom != oldBottom) {//????????????
                            isKeyboardVisible = false;
                            acceptKeyboardCloseEvent(getCurrentShowPopupLayout(), chatEditLayout);
                        }
                    }
                }
            });
        }
        //?????????
        talk = findViewById(R.id.et_talk);
        talk.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() > 0) {
                    tvSendMsg.setEnabled(true);
                    tvSendMsg.setSelected(true);
                } else {
                    tvSendMsg.setSelected(false);
                    tvSendMsg.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        talk.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                    return true;
                }
                return false;
            }
        });
        talk.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    softInputWillShow(chatEditLayout);
                }
                return false;
            }
        });
        //????????????
        emoListLayout = findViewById(R.id.ic_chat_emo_list_layout);
        emoList = findViewById(R.id.rv_emo_list);
        emoList.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 6, GridLayoutManager.VERTICAL, false);
        emoList.setLayoutManager(gridLayoutManager);
        emoList.addItemDecoration(new PolyvEmoListAdapter.GridSpacingItemDecoration(6, ConvertUtils.dp2px(4), true));
        emoListAdapter = new PolyvEmoListAdapter(emoList);
        emoListAdapter.setOnItemClickListener(new PolyvBaseRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, ClickableViewHolder holder) {
                appendEmo(emoListAdapter.emoLists.get(position), false);
            }
        });
        emoList.setAdapter(emoListAdapter);
        //??????????????????
        emoji = findViewById(R.id.iv_emoji);
        emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePopupLayout(emoji, emoListLayout, chatEditLayout);
            }
        });
        addPopupBottom(emoji);
        addPopupLayout(emoListLayout);
        //??????????????????
        unread = findViewById(R.id.tv_unread);
        chatMessageList.setUnreadView(unread);
        //??????????????????
        ivMsgDelete = findViewById(R.id.iv_delete);
        ivMsgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteEmoText();
            }
        });
        tvSendMsg = findViewById(R.id.tv_send);
        tvSendMsg.setEnabled(false);
        tvSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="???????????????????????????">
    protected void initMoreLayout() {
        //????????????
        more = findViewById(R.id.add_more);
        more.setVisibility(View.VISIBLE);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePopupLayout(more, moreLayout, getChatEditContainer());
            }
        });
        moreLayout = findViewById(R.id.ic_chat_add_more_layout);
        addPopupBottom(more);
        addPopupLayout(moreLayout);
        selectPhotoLayout = findViewById(R.id.select_photo_layout);
        openCameraLayout = findViewById(R.id.open_camera_layout);
        selectPhotoButton = findViewById(R.id.select_photo_button);
        selectPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result = permissionManager.permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .opstrs(-1)
                        .meanings("????????????")
                        .setOnGrantedListener(new PolyvOnGrantedListener() {
                            @Override
                            public void afterPermissionsOnGranted() {
                                selectPhoto();
                            }
                        })
                        .request();
                if (!result) {
                    toast.makeText(getContext(), "???????????????????????????????????????", PolyvToast.LENGTH_SHORT).show(true);
                }
            }
        });
        openCameraButton = findViewById(R.id.open_camera_button);
        openCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result = permissionManager.permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                        .opstrs(-1, PolyvPermissionManager.OP_CAMERA)
                        .meanings("????????????", "????????????")
                        .setOnGrantedListener(new PolyvOnGrantedListener() {
                            @Override
                            public void afterPermissionsOnGranted() {
                                openCamera();
                            }
                        })
                        .request();
                if (!result) {
                    toast.makeText(getContext(), "??????????????????????????????????????????", PolyvToast.LENGTH_SHORT).show(true);
                }
            }
        });
        //???????????????????????????
        chatListAdapter.setOnResendMessageViewClickListener(new PolyvChatListAdapter.OnResendMessageViewClickListener() {
            @Override
            public void onClick(ImageView iv, int position) {
                PolyvChatListAdapter.ChatTypeItem chatTypeItem = chatListAdapter.getChatTypeItems().get(position);
                if (chatTypeItem.object instanceof PolyvSendLocalImgEvent) {
                    PolyvSendLocalImgEvent sendLocalImgEvent = (PolyvSendLocalImgEvent) chatTypeItem.object;
                    sendLocalImgEvent.initSendStatus();
                    PolyvSendMessageHolder sendMessageHolder = findVisiableSendMessageHolder(sendLocalImgEvent);
                    if (sendMessageHolder != null) {
                        sendMessageHolder.resendMessageButton.setVisibility(View.GONE);
                        sendMessageHolder.imgLoading.setVisibility(View.VISIBLE);
                        sendMessageHolder.imgLoading.setProgress(0);
                    }
                    //??????view????????????
                    sendImage(sendLocalImgEvent, getSessionId());
                }
            }
        });
        //??????
        openBulletinLayout = findViewById(R.id.open_bulletin_layout);
        openBulletinButton = findViewById(R.id.open_bulletin_button);
        openBulletinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //???Bus???????????????AnswerView????????????
                PolyvRxBus.get().post(new PolyvAnswerView.BUS_EVENT(PolyvAnswerView.BUS_EVENT.TYPE_SHOW_BULLETIN));
                PolyvChatBaseFragment.this.hidePopupLayout(more, moreLayout);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">
    protected void onImgUploadFail(PolyvSendLocalImgEvent localImgEvent, Throwable t) {
        localImgEvent.setSendFail(true);
        PolyvSendMessageHolder sendMessageHolder = findVisiableSendMessageHolder(localImgEvent);
        if (sendMessageHolder != null) {
            sendMessageHolder.imgLoading.setVisibility(View.GONE);
            sendMessageHolder.resendMessageButton.setVisibility(View.VISIBLE);
        }
        toast.makeText(getContext(), "?????????????????????" + t.getMessage(), PolyvToast.LENGTH_SHORT).show(true);
    }

    protected void onImgSendFail(PolyvSendLocalImgEvent localImgEvent, int sendValue) {
        localImgEvent.setSendFail(true);
        PolyvSendMessageHolder sendMessageHolder = findVisiableSendMessageHolder(localImgEvent);
        if (sendMessageHolder != null) {
            sendMessageHolder.imgLoading.setVisibility(View.GONE);
            sendMessageHolder.resendMessageButton.setVisibility(View.VISIBLE);
        }
        toast.makeText(getContext(), "?????????????????????" + sendValue, PolyvToast.LENGTH_SHORT).show(true);
    }

    protected void onImgSuccess(PolyvSendLocalImgEvent localImgEvent, String uploadImgUrl, String imgId) {
        localImgEvent.setSendSuccess(true);
        PolyvSendMessageHolder sendMessageHolder = findVisiableSendMessageHolder(localImgEvent);
        if (sendMessageHolder != null) {
            sendMessageHolder.imgLoading.setVisibility(View.GONE);
        }
    }

    protected void onImgProgress(PolyvSendLocalImgEvent localImgEvent, float progress) {
        localImgEvent.setSendProgress((int) (progress * 100));
        PolyvSendMessageHolder sendMessageHolder = findVisiableSendMessageHolder(localImgEvent);
        if (sendMessageHolder != null) {
            sendMessageHolder.imgLoading.setVisibility(View.VISIBLE);
            sendMessageHolder.imgLoading.setProgress((int) (progress * 100));
        }
    }

    protected void sendImage(PolyvSendLocalImgEvent sendLocalImgEvent, String sessionId) {
    }

    private PolyvSendMessageHolder findVisiableSendMessageHolder(PolyvSendLocalImgEvent sendLocalImgEvent) {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) chatMessageList.getLayoutManager();
        int firstPosition = linearLayoutManager.findFirstVisibleItemPosition();
        int lastPosition = linearLayoutManager.findLastVisibleItemPosition();
        if (firstPosition < 0 || lastPosition < 0)
            return null;
        for (int i = 0; i <= lastPosition - firstPosition; i++) {
            PolyvChatListAdapter.ChatTypeItem chatTypeItem = chatListAdapter.getChatTypeItems().get(i + firstPosition);
            if (chatTypeItem.object == sendLocalImgEvent) {
                RecyclerView.ViewHolder viewHolder = chatMessageList.getChildViewHolder(chatMessageList.getChildAt(i));
                if (viewHolder instanceof PolyvSendMessageHolder) {
                    return (PolyvSendMessageHolder) viewHolder;
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String picName = System.currentTimeMillis() + ".jpg";//???????????????
        String savePath = PolyvSDCardUtils.createPath(getContext(), "PolyvImg");
        takePhotosFilePath = new File(savePath, picName);
        Uri photoUri = createImageUri(picName);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_OPEN_CAMERA);
    }

    private Uri createImageUri(String fileName){
        String status = Environment.getExternalStorageState();
        if(Build.VERSION.SDK_INT >= 29){
            // ???????????????SD???,????????????SD?????????,?????????SD????????????????????????
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            takeCameraUri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            return takeCameraUri;
        } else {
            String savePath = PolyvSDCardUtils.createPath(getContext(), "PolyvImg");
            takePhotosFilePath = new File(savePath, fileName);
            Uri photoUri = FileProvider.getUriForFile(
                    getContext(),
                    getContext().getPackageName() + ".fileprovider",
                    takePhotosFilePath);
            return photoUri;
        }
    }

    private void selectPhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "????????????"), REQUEST_SELECT_PHOTO);
    }

    private void sendPicture(String picturePath) {
        PolyvSendLocalImgEvent sendLocalImgEvent = new PolyvSendLocalImgEvent();
        sendLocalImgEvent.setImageFilePath(picturePath);
        int[] pictureWh = PolyvPictureUtils.getPictureWh(picturePath);
        sendLocalImgEvent.setWidth(pictureWh[0]);
        sendLocalImgEvent.setHeight(pictureWh[1]);

        sendImage(sendLocalImgEvent, getSessionId());

        PolyvChatListAdapter.ChatTypeItem chatTypeItem = new PolyvChatListAdapter.ChatTypeItem(sendLocalImgEvent, PolyvChatListAdapter.ChatTypeItem.TYPE_SEND, PolyvChatManager.SE_MESSAGE);
        chatTypeItems.add(chatTypeItem);
        teacherItems.add(chatTypeItem);
        chatListAdapter.notifyItemInserted(chatListAdapter.getItemCount() - 1);
        chatMessageList.scrollToPosition(chatListAdapter.getItemCount() - 1);

        if (more.isSelected()) {
            hidePopupLayout(more, moreLayout);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????????????????????????????????????????">
    protected void togglePopupLayout(View view, ViewGroup popupLayout, ViewGroup chatEditLayout) {
        if (!view.isSelected()) {
            showPopupLayout(view, popupLayout, chatEditLayout);
        } else {
            hidePopupLayout(view, popupLayout);
        }
    }

    protected ViewGroup getCurrentShowPopupLayout() {
        return currentShowPopupLayout;
    }

    protected void showPopupLayout(View view, ViewGroup popupLayout, ViewGroup chatEditLayout) {
        isShowPopupLayout = true;
        currentShowPopupLayout = popupLayout;
        resetPopupBottomLayout();
        KeyboardUtils.hideSoftInput(talk);//??????????????????

        if (!isKeyboardVisible) {//????????????????????????
            replaceChatEditContainer(chatEditLayout);//???????????????????????????
            popupLayout.setVisibility(View.VISIBLE);//???????????????????????????
            changeViewLayoutParams(popupLayout);//???????????????????????????
        }
        view.setSelected(isShowPopupLayout);
        talk.requestFocus();
    }

    protected void hidePopupLayout(View view, ViewGroup popupLayout) {
        isShowPopupLayout = false;
        currentShowPopupLayout = null;
        KeyboardUtils.hideSoftInput(talk);//??????????????????

        resetChatEditContainer();
        popupLayout.setVisibility(View.GONE);
        resetViewLayoutParams();
        view.setSelected(isShowPopupLayout);
    }

    protected void softInputWillShow(ViewGroup chatEditLayout) {
        isShowPopupLayout = false;
        currentShowPopupLayout = null;
        resetViewLayoutParams();
        replaceChatEditContainer(chatEditLayout);
        KeyboardUtils.showSoftInput(talk);//?????????????????????????????????????????????????????????
        resetPopupBottomLayout();
    }

    private void resetPopupBottomLayout() {
        for (ViewGroup viewGroup : popupLayoutList) {
            viewGroup.setVisibility(View.GONE);
        }
        for (View popupBottom : popupBottomList) {
            popupBottom.setSelected(false);
        }
    }

    private boolean popupLayoutIsVisiable() {
        for (ViewGroup viewGroup : popupLayoutList) {
            if (viewGroup.getVisibility() == View.VISIBLE)
                return true;
        }
        return false;
    }

    protected void addPopupBottom(View view) {
        popupBottomList.add(view);
    }

    protected void addPopupLayout(ViewGroup popupLayout) {
        popupLayoutList.add(popupLayout);
    }

    private void resetViewLayoutParams() {
        if (view != null) {
            final ViewGroup viewParent = (ViewGroup) view.getParent();
            if (viewParent == null)
                return;
            ViewGroup.LayoutParams vlp = viewParent.getLayoutParams();//viewpager?????????view???????????????
            vlp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            viewParent.setLayoutParams(vlp);
        }
    }

    private void changeViewLayoutParams(final ViewGroup popupLayout) {
        if (popupLayout == null)
            return;
        if (popupLayout.getHeight() == 0) {
            popupLayout.post(new Runnable() {
                @Override
                public void run() {
                    setViewLayoutParams(popupLayout.getHeight());
                }
            });
        } else {
            setViewLayoutParams(popupLayout.getHeight());
        }
    }

    private void setViewLayoutParams(int height) {//???adjustResize??????
        final ViewGroup viewParent = (ViewGroup) view.getParent();
        if (viewParent == null)
            return;
        ViewGroup.LayoutParams vlp = viewParent.getLayoutParams();//viewpager?????????view???????????????
        if (srcViewPagerHeight == 0) {//???????????????????????????viewPager?????????????????????????????????????????????
            srcViewPagerHeight = viewParent.getHeight();
        }
        if (srcViewPagerHeight > height) {
            vlp.height = srcViewPagerHeight - height;
            viewParent.setLayoutParams(vlp);
        }
    }

    private void resetChatEditContainer() {
        if (talkLayout != null && talkLayout.getParent() != talkParentLayout) {
            if (talkLayout.getParent() != null) {
                ((ViewGroup) talkLayout.getParent()).setVisibility(View.GONE);
                ((ViewGroup) talkLayout.getParent()).removeView(talkLayout);
            }
            talkParentLayout.removeAllViews();
            FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            talkLayout.setLayoutParams(flp);
            talkParentLayout.addView(talkLayout);
        }
    }

    private void replaceChatEditContainer(ViewGroup chatEditLayout) {
        if (chatEditLayout != null && talkLayout.getParent() != chatEditLayout && chatEditLayout instanceof FrameLayout) {
            chatEditLayout.setVisibility(View.VISIBLE);
            if (talkLayout.getParent() != null) {
                ((ViewGroup) talkLayout.getParent()).removeView(talkLayout);
            }
            chatEditLayout.removeAllViews();
            FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            flp.gravity = Gravity.BOTTOM;
            talkLayout.setLayoutParams(flp);
            chatEditLayout.addView(talkLayout);
        }
    }

    private void acceptKeyboardCloseEvent(ViewGroup popupLayout, ViewGroup chatEditLayout) {
        if (!isShowPopupLayout) {
            resetChatEditContainer();
        } else {
            replaceChatEditContainer(chatEditLayout);//???????????????????????????????????????
            popupLayout.setVisibility(View.VISIBLE);//???????????????????????????????????????
            changeViewLayoutParams(popupLayout);//???????????????????????????????????????
        }
    }

    protected void hideSoftInputAndEmoList() {
        if (talk != null) {
            KeyboardUtils.hideSoftInput(talk);
        }
        resetPopupBottomLayout();
        if (isShowPopupLayout) {//isShowEmoji->???????????????
            resetChatEditContainer();//???????????????????????????????????????????????????????????????????????????
            resetViewLayoutParams();
        }
        isShowPopupLayout = false;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????????????????">
    // ????????????
    private void deleteEmoText() {
        int start = talk.getSelectionStart();
        int end = talk.getSelectionEnd();
        if (end > 0) {
            if (start != end) {
                talk.getText().delete(start, end);
            } else if (isEmo(end)) {
                talk.getText().delete(end - emoLength, end);
            } else {
                talk.getText().delete(end - 1, end);
            }
        }
    }

    //?????????????????????
    private boolean isEmo(int end) {
        String preMsg = talk.getText().subSequence(0, end).toString();
        int regEnd = preMsg.lastIndexOf("]");
        int regStart = preMsg.lastIndexOf("[");
        if (regEnd == end - 1 && regEnd - regStart >= 2) {
            String regex = preMsg.substring(regStart);
            emoLength = regex.length();
            if (PolyvFaceManager.getInstance().getFaceId(regex) != -1)
                return true;
        }
        return false;
    }

    //????????????
    private void appendEmo(String emoKey, boolean useGif) {
        SpannableStringBuilder span = new SpannableStringBuilder(emoKey);
        int textSize = (int) talk.getTextSize();
        Drawable drawable;
        ImageSpan imageSpan;
        try {
            if (useGif) {
                //??????????????????????????????????????????
                drawable = new GifDrawable(getResources(), PolyvFaceManager.getInstance().getFaceId(emoKey));
                imageSpan = new GifImageSpan(drawable, RelativeImageSpan.ALIGN_CENTER);
            } else {
                drawable = getResources().getDrawable(PolyvFaceManager.getInstance().getFaceId(emoKey));
                imageSpan = new RelativeImageSpan(drawable, RelativeImageSpan.ALIGN_CENTER);
            }
        } catch (Exception e) {
            try {
                drawable = getResources().getDrawable(PolyvFaceManager.getInstance().getFaceId(emoKey));
                imageSpan = new RelativeImageSpan(drawable, RelativeImageSpan.ALIGN_CENTER);
            } catch (Exception e1) {
                toast.makeText(getContext(), "?????????????????????", PolyvToast.LENGTH_SHORT).show(true);
                return;
            }
        }
        drawable.setBounds(0, 0, (int) (textSize * 1.5), (int) (textSize * 1.5));
        span.setSpan(imageSpan, 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        int selectionStart = talk.getSelectionStart();
        int selectionEnd = talk.getSelectionEnd();
        if (selectionStart != selectionEnd)
            talk.getText().replace(selectionStart, selectionEnd, span);
        else
            talk.getText().insert(selectionStart, span);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="???????????????">
    public boolean onBackPressed() {
        if (getImageViewerContainer() != null && getImageViewerContainer().getVisibility() == View.VISIBLE) {
            getImageViewerContainer().setVisibility(View.GONE);
            return true;
        } else if (popupLayoutIsVisiable()) {
            hideSoftInputAndEmoList();
            return true;
        }
        return false;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fragment??????">
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IPolyvHomeProtocol) {
            this.homePresnter = (IPolyvHomeProtocol) context;
            this.chatManager = homePresnter.getChatManager();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            hideSoftInputAndEmoList();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getUserVisibleHint()) {
            hideSoftInputAndEmoList();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_PHOTO && resultCode == Activity.RESULT_OK) {
            final Uri selectedUri = data.getData();
            if (selectedUri != null) {
                String picturePath = PolyvUriPathHelper.getPrivatePath(getContext(), selectedUri);
                sendPicture(picturePath);
            } else {
                toast.makeText(getContext(), "cannot retrieve selected image", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_OPEN_CAMERA && resultCode == Activity.RESULT_OK) {//data->null
            if(Build.VERSION.SDK_INT >= 29){
                String picturePath = PolyvUriPathHelper.getPrivatePath(getContext(), takeCameraUri);
                sendPicture(picturePath);
            } else {
                sendPicture(takePhotosFilePath.getAbsolutePath());
            }
        }
    }

    @Override
    public void onDestroy() {
        if(chatListAdapter != null){
            chatListAdapter.onDestory();
        }
        super.onDestroy();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">
    public abstract void sendMessage();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????">
    public static class ConnectStatus {
        public int status;
        public Throwable t;

        public ConnectStatus(int status, Throwable t) {
            this.status = status;
            this.t = t;
        }
    }

    public static class EventMessage {
        public String message;
        public String event;
        public String socketListen;

        public EventMessage(String message, String event, String socketListen) {
            this.message = message;
            this.event = event;
            this.socketListen = socketListen;
        }
    }
    // </editor-fold>
}
