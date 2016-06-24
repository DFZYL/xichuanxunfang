package com.weisen.xcxf.bean;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.weisen.xcxf.tool.CommonTool;


public class CaseWorker {

	private String id;
	private String name;

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

	public static List<CaseWorker> parseList(String res, String keyName) {
		List<CaseWorker> caseWorkerList = new ArrayList<CaseWorker>();
		try {
			JSONObject object = CommonTool.parseFromJson(res);
			JSONArray array = CommonTool.getJsonArry(object, keyName);
			if (array != null && array.length() > 0) {
				for (int i = 0; i < array.length(); i++) {
					CaseWorker caseWorker = new CaseWorker();
					JSONObject obj = array.getJSONObject(i);
					String id = CommonTool.getJsonString(obj, "id");
					String name = CommonTool.getJsonString(obj, "name");
					caseWorker.setId(id);
					caseWorker.setName(name);
					caseWorkerList.add(caseWorker);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return caseWorkerList;
	}

	public static String getStr(List<CaseWorker> caseWorkers) {
		String str = "";
		try {
			JSONObject obj = new JSONObject();
			JSONArray array = new JSONArray();
			for (CaseWorker caseWorker : caseWorkers) {
				JSONObject object = new JSONObject();
				object.put("id", caseWorker.getId());
				object.put("name", caseWorker.getName());
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
	
	public static CaseWorker getById(List<CaseWorker> caseWorkers, String id) {
		CaseWorker worker = null;
		for (CaseWorker caseWorker : caseWorkers) {
			if (caseWorker.getId().equals(id))
				worker = caseWorker;
		}
		return worker;
	}
}
