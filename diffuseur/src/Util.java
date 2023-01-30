public class Util {
    public static String diese(String str, int n) {
		int len = n - str.length();
		str = str + "#".repeat(Math.max(0, len));
		return str;
	}
    public static String zero(String str, int n) {
		int len = n - str.length();
		StringBuilder strBuilder = new StringBuilder(str);
		for (int i = 0; i < len; i++)
			strBuilder.insert(0, '0');
		str = strBuilder.toString();
		return str;
	}
    public static String ip(String str) {
		int len = str.length();
		try {
			if (len > 15)
				throw new Exception("Format d'adresse ip invalide.");
			String[] splited;
			splited = str.split("\\.");
			if (splited.length != 4)
				throw new Exception("Format d'adresse ip invalide.");
			for (int i = 0; i < 4; i++)
				splited[i] = zero(splited[i], 3);
			return String.join(".", splited);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}
}
