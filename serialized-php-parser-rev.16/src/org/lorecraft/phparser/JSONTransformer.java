package org.lorecraft.phparser;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class JSONTransformer {
	
	public static Object toJSON(Object o) {
		if (o instanceof Map) {
			return arrayToJSON((Map)o);
		} else if (o instanceof SerializedPhpParser.PhpObject) {
			return mapToJSON(((SerializedPhpParser.PhpObject)o).attributes);
		} else if (o == SerializedPhpParser.NULL) {
			return null;
		}
		return o;
	}

	private static JSONArray arrayToJSON(Map o) {
		JSONArray a = new JSONArray();
		for (Object obj : o.values()) {
			a.add(toJSON(obj));
		}
		return a;
	}

	private static JSONObject mapToJSON(Map o) {
		JSONObject obj = new JSONObject();
		Map map = (Map)o;
		Iterator<Map.Entry> i = map.entrySet().iterator();
		while (i.hasNext()) {
			Entry next = i.next();
			obj.put(next.getKey(), toJSON(next.getValue()));
		}
		return obj;
	}
}
