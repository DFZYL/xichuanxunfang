package com.weisen.xcxf.bean;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.weisen.xcxf.tool.CommonTool;

public class CaseType {

	private static final String MODE_MULTI_PROCESS = null;
	private String id;
	private String name;
	private String pid;
	private String isChooseCase = "0";
	

	public String getIsChooseCase() {
		return isChooseCase;
	}

	public void setIsChooseCase(String isChooseCase) {
		this.isChooseCase = isChooseCase;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public static List<CaseType> parseList(String res, String keyName) {
		List<CaseType> caseTypes = new ArrayList<CaseType>();
		String isSuccessCase1;
		try {
			JSONObject object = CommonTool.parseFromJson(res);
			JSONArray array = CommonTool.getJsonArry(object, keyName);
			if (array != null && array.length() > 0) {
				for (int i = 0; i < array.length(); i++) {
					CaseType caseType = new CaseType();
					JSONObject obj = array.getJSONObject(i);
					String pid = CommonTool.getJsonString(obj, "pid");
					String id = CommonTool.getJsonString(obj, "id");
					String name = CommonTool.getJsonString(obj, "name");
					if (CommonTool.getJsonString(obj, "isChooseCase").equals("")) {
						isSuccessCase1 = caseType.getIsChooseCase();
					}else {
						isSuccessCase1 = CommonTool.getJsonString(obj, "isChooseCase");
					}
					
					caseType.setId(id);
					caseType.setName(name);
					caseType.setPid(pid);
					caseType.setIsChooseCase(isSuccessCase1);
					caseTypes.add(caseType);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return caseTypes;
	}

	public static CaseType getById(List<CaseType> caseTypes, String id) {
		CaseType type = null;
		for (CaseType caseType : caseTypes) {
			if (caseType.getId().equals(id))
				type = caseType;
		}
		return type;
	}

	public static CaseType getParentById(List<CaseType> caseTypes, CaseType childType) {
		CaseType type = null;
		if(childType != null) {
			for (CaseType caseType : caseTypes) {
				if (caseType.getId().equals(childType.getPid()))
					type = caseType;
			}
		}
		return type;
	}

	
	public static String getStr(List<CaseType> caseTypes) {
		String str = "";
		try {
			JSONObject obj = new JSONObject();
			JSONArray array = new JSONArray();
			for (CaseType caseType : caseTypes) {
				JSONObject object = new JSONObject();
				object.put("id", caseType.getId());
				object.put("name", caseType.getName());
				object.put("pid", caseType.getPid());
				object.put("isChooseCase", caseType.getIsChooseCase());
				array.put(object);
			}
			obj.put("list", array);
			str = obj.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}

	public static List<CaseType> getChildList1(List<CaseType> caseTypes) {
		List<CaseType> childCaseTypes = new ArrayList<CaseType>();
		for (CaseType caseType : caseTypes) {
			if (caseType.getPid() == null || caseType.getPid().equals(""))
				childCaseTypes.add(caseType);
		}
		return childCaseTypes;
	}
	public static List<CaseType> getChildList2(List<CaseType> caseTypes,
			String pid) {
		List<CaseType> childCaseTypes = new ArrayList<CaseType>();
		for (CaseType caseType : caseTypes) {
			if (caseType.getPid().equals(pid))
				childCaseTypes.add(caseType);
		}
		return childCaseTypes;
	}
}
