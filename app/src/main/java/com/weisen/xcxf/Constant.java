/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.weisen.xcxf;

public class Constant {
	public static final String KEY = "AF057413-61D1-492F-9C17-05D895BE618E";
	public static final String APP_SP = "xichuanxunfang";
    public static final String LIULIANG = "liuliang";
    public static final String ISWORK = "iswork";
    public static final String COMID = "comId";
    public static final String BSEIP = "baseip";
    public static final String ISFRIST = "isfrist";
	public static final String SP_USERNAME = "username";
	public static final String SP_PWD = "password";
	public static final String SP_USERID = "userId";
	public static final String SP_NAME = "name";
	public static final String SP_HEADPIC = "headPic";
	public static final String SP_APPNAME = "appName";
	public static final String SP_APPIMG = "appImg";
	public static final String SP_APPIMGPATH = "appImgPath";
	public static final String SP_IP = "ip";
	public static final String SP_PERIOD = "period";
	public static final String SP_PERIOD_NAME = "period_name";
	public static final String SP_BEGIN = "begin_time";
	public static final String SP_END = "end_time";
	public static final String SP_CASE_TYPE = "caseType";
	public static final String SP_CASE_TYPE1 = "caseType1";
	public static final String SP_CASE_CATEGORY = "caseCategory";
	public static final String SP_CASE_WORKERS = "caseWorkers";
	public static final String SP_USERINFO_NAME = "username";
	public static final String SP_USERINFO_SEX = "sex";
	public static final String SP_USERINFO_PHONE = "phone";
	public static final String SP_USERINFO_EMAIL = "email";
	public static final String SP_USERINFO_NATION = "nation";
	public static final String SP_USERINFO_SIGN = "sign";
	public static final String SP_USERINFO_IMG = "img";
	public static final String SP_REPLY_TXT = "replyTxt";
	public static final String SP_REPLY_TIME = "replyTime";
    public static final String SP_LATITUDE = "latitude";
    public static final String SP_LONGITUDE = "longitude";
	public static final int REQUEST_CODE = 10001;
	public static final int RESULT_CODE = 10002;
	public static final String REQUEST_GET_CITY_DATA = "REQUEST_GET_CITY_DATA";
    public static String DEFAULTDISTANCE = "defaultdistance";
	public static final String DEFAULT_TITLE = "淅川巡防";
	public static final long DEFAULT_PERIOD = 60 * 1000;
	public static final String DEFAULT_PERIOD_NAME = "1分钟";
	public static final String DEFAULT_BEGIN = "08:00";
	public static final String DEFAULT_END = "19:00";

	public static final String FILE_NAME = "xichuanxunfang";

	// 广播
	public static final String BROADCAST_UPDATE_MESSAGE = "updateMessageList";
	public static final String BROADCAST_UPDATE_REPORT = "updateReportList";
	public static final String BROADCAST_UPDATE_LOCATION = "updateLocationList";
	public static final String BROADCAST_UPDATE_TYPE = "updateTypeList";
    public static final String UPDATEUPDATELOCATIONTYPE = "updateupdateLocationtype";
    public static final String GETLOACTIONACTION = "getLoactionAction";
	// I

//    public static final String IP = "http://211.142.152.179:88/xcxf/";//实际地址

	public static final String IP = "http://117.158.67.11:82/xcxf/";//测试地址/



	public static final String WELCOME = "indexAction_initLoginInfo.action";

	// 登录
	public static final String LOGIN = "indexAction_login.action";
	// 轨迹上报
	public static final String UPLOAD_TRAIL = "baseInfo/trace/traceAction_uploadTraceUnderCur.action";
	public static final String  UPLOAD_TRAILPIC="baseInfo/user/userAction_updateUWStatus.action";
    //获取工作状态
    public static final String GETWORKSTATUS = "baseInfo/user/userAction_getUserWorkingStatus.action";

	// 时间上报类型
	public static final String CASE_TYPE = "baseInfo/event/eventTypeAction_getAllEventType.action";
	// 上级人员
	public static final String CASE_USER = "baseInfo/config/deptAction_getAllUpLevels.action";
	// 上报
	public static final String CASE_REPORT = "baseInfo/event/eventAction_uploadEventUnderCur.action";
	// 修改密码
	public static final String UPDATE_PWD = "baseInfo/user/userAction_editUserPwd.action";
	// 获取个人信息
	public static final String USER_INFO = "baseInfo/user/userAction_getUserInfoUnderCur.action";
	// 修改个人信息
	public static final String UPDATE_USER_INFO = "baseInfo/user/userAction_editUserInfo.action";
	// 修改个人头像
	public static final String UPDATE_USER_HEAD = "baseInfo/user/userAction_editUserIcon.action";
	// 修改已读
	public static final String DEAL_MSG = "baseInfo/message/messageUsersAction_updateMsgByCur.action";
	// 更新数据
	public static final String UPDATE_DATA = "baseInfo/user/userAction_updateSetting.action";
	// 退出
	public static final String EXIT = "indexAction_logOut.action";
	// 回复信息
	public static final String SEND_MESSAGE = "baseInfo/event/eventCommentAction_addMsgFeedback.action";
	// 找回密码
	public static final String RESET_PWD = "baseInfo/user/userAction_verifyUser4RePwd.action";
	// 找回密码
	public static final String RESET_NEW_PWD = "baseInfo/user/userAction_findPwd.action";
	// 上传流量
	public static final String SEND_DATAS = "baseInfo/other/netAction_updateUserNet.action";
	// 问题反馈
	public static final String SEND_QUESTION = "baseInfo/other/faqAction_saveFaq.action";
    //聊天 获取 token
    public static final String GET_TOKEN = "baseInfo/user/userAction_getRongyToken.action";
    //获取验证码
    public static final String GET_CODE = "indexAction_getIdentifyCode.action";
    //验证验证码
    public static final String CHECK_CODE = "indexAction_checkPwdCode.action";

    public static final String RESETPWD = "indexAction_modifyPwd.action";

    //http://ip:port/lyxf/baseInfo/trace/traceAction_uploadTracePatch.action
    //打包上传 轨迹
    public static final String UPLOAD_LOCATIONS = "baseInfo/trace/traceAction_uploadTracePatch.action";


}
