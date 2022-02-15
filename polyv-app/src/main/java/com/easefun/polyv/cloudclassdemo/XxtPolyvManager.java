package com.easefun.polyv.cloudclassdemo;
/**
 * ´´´´´´´´██´´´´´´´
 * ´´´´´´´████´´´´´´
 * ´´´´´████████´´´´
 * ´´`´███▒▒▒▒███´´´´´
 * ´´´███▒●▒▒●▒██´´´
 * ´´´███▒▒▒▒▒▒██´´´´´
 * ´´´███▒▒▒▒██´
 * ´´██████▒▒███´´´´´
 * ´██████▒▒▒▒███´´
 * ██████▒▒▒▒▒▒███´´´´
 * ´´▓▓▓▓▓▓▓▓▓▓▓▓▓▒´´
 * ´´▒▒▒▒▓▓▓▓▓▓▓▓▓▒´´´´´
 * ´.▒▒▒´´▓▓▓▓▓▓▓▓▒´´´´´
 * ´.▒▒´´´´▓▓▓▓▓▓▓▒
 * ..▒▒.´´´´▓▓▓▓▓▓▓▒
 * ´▒▒▒▒▒▒▒▒▒▒▒▒
 * ´´´´´´´´´███████´´´´´
 * ´´´´´´´´████████´´´´´´´
 * ´´´´´´´█████████´´´´´´
 * ´´´´´´██████████´´´´
 * ´´´´´´██████████´´´
 * ´´´´´´´█████████´´
 * ´´´´´´´█████████´´´
 * ´´´´´´´´████████´´´´´
 * ________▒▒▒▒▒
 * _________▒▒▒▒
 * _________▒▒▒▒
 * ________▒▒_▒▒
 * _______▒▒__▒▒
 * _____ ▒▒___▒▒
 * _____▒▒___▒▒
 * ____▒▒____▒▒
 * ___▒▒_____▒▒
 * ███____ ▒▒
 * ████____███
 * █ _███_ _█_███
 * ——————————————————————————女神保佑，代码无bug——————————————————————
 */

import static com.easefun.polyv.commonui.utils.MetaUtil.getMetaStringValue;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.easefun.polyv.businesssdk.PolyvChatDomainManager;
import com.easefun.polyv.businesssdk.model.chat.PolyvChatDomain;
import com.easefun.polyv.businesssdk.model.video.PolyvPlayBackVO;
import com.easefun.polyv.businesssdk.service.PolyvLoginManager;
import com.easefun.polyv.businesssdk.vodplayer.PolyvVodSDKClient;
import com.easefun.polyv.cloudclass.chat.PolyvChatApiRequestHelper;
import com.easefun.polyv.cloudclass.config.PolyvLiveChannelType;
import com.easefun.polyv.cloudclass.config.PolyvLiveSDKClient;
import com.easefun.polyv.cloudclass.config.PolyvVClassGlobalConfig;
import com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.cloudclass.model.PolyvLiveStatusVO;
import com.easefun.polyv.cloudclass.net.PolyvApiManager;
import com.easefun.polyv.cloudclass.playback.video.PolyvPlaybackListType;
import com.easefun.polyv.cloudclassdemo.watch.PolyvCloudClassHomeActivity;
import com.easefun.polyv.commonui.utils.StringUtil;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.net.PolyvResponseBean;
import com.easefun.polyv.foundationsdk.net.PolyvResponseExcutor;
import com.easefun.polyv.foundationsdk.net.PolyvrResponseCallback;
import com.easefun.polyv.linkmic.PolyvLinkMicClient;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.io.IOException;
import java.util.Random;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import retrofit2.adapter.rxjava2.HttpException;


/**
 * [类作用说明]
 *
 * @author lukem
 *
 * @date 2020/11/6
 */
public class XxtPolyvManager {

    private static XxtPolyvManager instance = null;

    private Disposable getTokenDisposable, verifyDispose, liveDetailDisposable;

    private Context context;

    private static String appSecret;

    private static String appId;

    private static String userId;

    private String viewerId;

    private String viewerName;

//    private String channelId;

    //是否是参与者
    private boolean isParticipant = false;

    private boolean hasInitPolyv = false;

    private Activity currentActivity;

    private XxtPolyvManager(Context context) {
        this.context = context;
    }

    public static synchronized XxtPolyvManager getInstance(Application application) {
        if (instance == null) {
            synchronized (XxtPolyvManager.class) {
                if (instance == null) {
                    instance = new XxtPolyvManager(application);
                    instance.init(application);
                }
            }
        }
        if (instance!=null && !instance.hasInitPolyv) {
            instance.init(application);
        }
        return instance;
    }

    private void init(Application application) {
        try {
            PolyvCommonLog.setDebug(true);
            PolyvLiveSDKClient liveSDKClient = PolyvLiveSDKClient.getInstance();

            liveSDKClient.initContext(application);
            liveSDKClient.enableHttpDns(true);
            PolyvVodSDKClient client = PolyvVodSDKClient.getInstance();

            String config = getMetaStringValue(context, "Polyv-Config");
            String aeskey = getMetaStringValue(context, "Polyv-AES-KEY");
            String iv = getMetaStringValue(context, "Polyv-AES-IV");

            appId = getMetaStringValue(context, "Polyv-App-Id");

            appSecret = getMetaStringValue(context, "Polyv-App-Secret");

            userId = getMetaStringValue(context, "Polyv-User-Id");

            //使用SDK加密串来配置
            client.setConfig(config, aeskey, iv);

            if (application==null) {
                // 这个为空，其实还是失败的，还是需要初始化
                hasInitPolyv = false;
            } else {
                hasInitPolyv = true;
            }

        } catch (Exception e) {
            hasInitPolyv = false;
        }
    }

    public XxtPolyvManager setAppConfig(String appId, String appSecret, String userId) {
        if (!StringUtil.isEmpty(appId)) {
            XxtPolyvManager.appId = appId;
        }
        if (!StringUtil.isEmpty(appSecret)) {
            XxtPolyvManager.appSecret = appSecret;
        }
        if (!StringUtil.isEmpty(userId)) {
            XxtPolyvManager.userId = userId;
        }
        return this;
    }

    public XxtPolyvManager setUserConfig(String viewerId, String viewerName) {
        if (!StringUtil.isEmpty(viewerId)) {
            this.viewerId = viewerId;
        }
        if (!StringUtil.isEmpty(viewerName)) {
            this.viewerName = viewerName;
        }
        return this;
    }

    public void gotoChannel(String channelId, int playType, String playbackVid, Activity currentActivity) {
        this.currentActivity = currentActivity;
        if (StringUtil.isEmpty(viewerId)) {
            // TODO 排查要传进来
//            int viewerIdTemp = Login.getInstance(context).getCurrentUserInfo().getWebId();
            int viewerIdTemp = 0;
            if (viewerIdTemp == 0) {
                Random random = new Random();
                int s = random.nextInt(900) % (900 - 100 + 1) + 100;
                int s1 = random.nextInt(90000) % (90000 - 10000 + 1) + 100000;
                viewerIdTemp = s1 + s;
            }
            viewerId = String.valueOf(viewerIdTemp);
        }

        if (StringUtil.isEmpty(viewerName)) {
//            viewerName = Login.getInstance(context).getCurrentUserInfo().getUserName();
//            if (StringUtil.isEmpty(viewerName)) {
//                viewerName = Login.getInstance(context).getCurrentUserInfo().getNickName();
//            }
//            if (StringUtil.isEmpty(viewerName)) {
                viewerName = "学员-" + viewerId;
//            }
        }

        checkToken(channelId, playType, playbackVid);
    }

    private void checkToken(final String channelId,  final int playType, final String playbackVid) {
        //请求token接口
        getTokenDisposable = PolyvLoginManager.checkLoginToken(userId, playType==1?null:appSecret, appId,
                channelId,  playType==1?playbackVid:null,
                new PolyvrResponseCallback<PolyvChatDomain>() {
                    @Override
                    public void onSuccess(PolyvChatDomain responseBean) {
                        if (playType ==1) {
                            //回放
                            PolyvLinkMicClient.getInstance().setAppIdSecret(appId, appSecret);
                            PolyvLiveSDKClient.getInstance().setAppIdSecret(appId, appSecret);
                            PolyvVodSDKClient.getInstance().initConfig(appId, appSecret);

                            requestPlayBackStatus(userId, playbackVid, channelId);
                        } else {
                            PolyvLinkMicClient.getInstance().setAppIdSecret(appId, appSecret);
                            PolyvLiveSDKClient.getInstance().setAppIdSecret(appId, appSecret);
                            PolyvVodSDKClient.getInstance().initConfig(appId, appSecret);

                            requestLiveStatus(userId, channelId);

                            PolyvChatDomainManager.getInstance().setChatDomain(responseBean);
                        }
                    }

                    @Override
                    public void onFailure(PolyvResponseBean<PolyvChatDomain> responseBean) {
                        super.onFailure(responseBean);
                        failedStatus(responseBean.getMessage());
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        errorStatus(e);
                    }
                });
    }

    private void requestLiveStatus(final String userId, final String channelId) {
        verifyDispose = PolyvResponseExcutor.excuteUndefinData(PolyvApiManager.getPolyvLiveStatusApi().geLiveStatusJson(channelId)
                , new PolyvrResponseCallback<PolyvLiveStatusVO>() {
                    @Override
                    public void onSuccess(PolyvLiveStatusVO statusVO) {

                        PolyvLiveChannelType channelType = null;
                        try {
                            channelType = PolyvLiveChannelType.mapFromServerString(statusVO.getChannelType());
                        } catch (PolyvLiveChannelType.UnknownChannelTypeException e) {
//                            XXTHud.dismiss();
                            ToastUtils.showShort("未知的频道类型");
                            e.printStackTrace();
                            return;
                        }
                        if (channelType != PolyvLiveChannelType.CLOUD_CLASS && channelType != PolyvLiveChannelType.NORMAL) {
//                            XXTHud.dismiss();
                            ToastUtils.showShort("只支持云课堂类型频道或普通直播类型频道");
                            return;
                        }
                        final boolean isAlone = channelType == PolyvLiveChannelType.NORMAL;//是否有ppt

                        requestLiveDetail(new Consumer<String>() {
                            @Override
                            public void accept(String rtcType) throws Exception {
//                                XXTHud.dismiss();
                                if (isParticipant) {
                                    if ("urtc".equals(rtcType) || TextUtils.isEmpty(rtcType)) {
                                        ToastUtils.showShort("暂不支持该频道观看");
                                        return;
                                    }
                                }
                                startActivityForLive(userId, isAlone, rtcType, channelId);
                            }
                        }, channelId);
                    }

                    @Override
                    public void onFailure(PolyvResponseBean<PolyvLiveStatusVO> responseBean) {
                        super.onFailure(responseBean);
                        failedStatus(responseBean.getMessage());
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        errorStatus(e);
                    }
                });
    }

    private void requestPlayBackStatus(final String userId, final String playbackVid, final String channelId) {
        if (TextUtils.isEmpty(playbackVid)) {
            return;
        }
        verifyDispose = PolyvLoginManager.getPlayBackType(playbackVid, new PolyvrResponseCallback<PolyvPlayBackVO>() {
            @Override
            public void onSuccess(PolyvPlayBackVO playBack) {
                boolean isLivePlayBack = playBack.getLiveType() == 0;
                startActivityForPlayback(userId, isLivePlayBack, playbackVid, channelId);
            }

            @Override
            public void onFailure(PolyvResponseBean<PolyvPlayBackVO> responseBean) {
                super.onFailure(responseBean);
                failedStatus(responseBean.getMessage());
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                errorStatus(e);
            }
        });
    }

    private void requestLiveDetail(final Consumer<String> onSuccess, String channelId) {
        if (liveDetailDisposable != null) {
            liveDetailDisposable.dispose();
        }
        liveDetailDisposable = PolyvResponseExcutor.excuteUndefinData(PolyvChatApiRequestHelper.getInstance()
                .requestLiveClassDetailApi(channelId), new PolyvrResponseCallback<PolyvLiveClassDetailVO>() {
            @Override
            public void onSuccess(PolyvLiveClassDetailVO polyvLiveClassDetailVO) {
                try {
                    onSuccess.accept(polyvLiveClassDetailVO.getData().getRtcType());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                errorStatus(e);
            }
        });
    }

    private void startActivityForLive(String userId, boolean isAlone, String rtcType, String channelId) {
        try {
            Integer.parseInt(viewerId);
        } catch (NumberFormatException e) {
            ToastUtils.showShort("参与者Id格式错误");
            return;
        }
        PolyvVClassGlobalConfig.username = viewerName;
        PolyvVClassGlobalConfig.viewerId = viewerId;

        if (currentActivity!=null) {
            PolyvCloudClassHomeActivity.startActivityForLiveWithParticipant(currentActivity,
                    channelId, userId, isAlone, isParticipant, rtcType);
        }
    }

    private void startActivityForPlayback(String userId, boolean isNormalLivePlayBack, String playbackVid, String channelId) {
        try {
            Integer.parseInt(viewerId);
        } catch (NumberFormatException e) {
            ToastUtils.showShort("参与者Id格式错误");
            return;
        }
        PolyvVClassGlobalConfig.username = viewerName;
        PolyvVClassGlobalConfig.viewerId = viewerId;
        if (this.currentActivity!=null) {
            PolyvCloudClassHomeActivity.startActivityForPlayBack(currentActivity,
                    StringUtil.trim(playbackVid), channelId, userId, isNormalLivePlayBack, PolyvPlaybackListType.PLAYBACK);
        }
    }

    public void failedStatus(String message) {
        ToastUtils.showLong(message);
//        XXTHud.dismiss();
    }

    public void errorStatus(Throwable e) {
        PolyvCommonLog.exception(e);
//        XXTHud.dismiss();
        if (e instanceof HttpException) {
            try {
                ToastUtils.showLong(((HttpException) e).response().errorBody().string());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } else {
            ToastUtils.showLong(e.getMessage());
        }
    }
}
