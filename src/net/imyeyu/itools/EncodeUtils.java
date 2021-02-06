package net.imyeyu.itools;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Pattern;

public final class EncodeUtils {
	
	public static String changeCharset(String data, String oldCharset, String newCharset) {
		try {
			if (data != null) {
				byte[] bs = data.getBytes(oldCharset);
				return new String(bs, newCharset);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String enUnicodeAll(String data) {
		StringBuilder sb = new StringBuilder();
		char[] c = data.toCharArray();
		for (int i = 0, l = c.length; i < l; i++) {
			sb.append("\\u" + Integer.toHexString(c[i]));
		}
		return sb.toString();
	}

	public static String enUnicode(String data) {
		StringBuilder sb = new StringBuilder();
		char[] c = data.toCharArray();
		for (int i = 0, l = c.length; i < l; i++) {
			if (!isHalfChar(c[i])) {
				sb.append("\\u" + Integer.toHexString(c[i]));
			} else {
				sb.append(c[i]);
			}
		}
		return sb.toString();
	}

	public static String deUnicode(String data) {
		StringBuilder sb = new StringBuilder();
		String[] hex = data.split("\\\\u");
		int index = -1;
		for (int i = 1, l = hex.length; i < l; i++) {
			if (4 < hex[i].length()) {
				index = Integer.parseInt(hex[i].substring(0, 4), 16);
				sb.append((char) index);
				sb.append(hex[i].substring(4, hex[i].length()));
			} else {
				index = Integer.parseInt(hex[i], 16);
				sb.append((char) index);
			}
		}
		return sb.toString();
	}

	public static String enBase64(String data) {
		String result;
		try {
			result = Base64.getEncoder().encodeToString(data.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}
	
	public static String deBase64(String data) {
		String result;
		try {
			result = new String(Base64.getDecoder().decode(data), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}

	public static String md5(String data) {
		String result = "";
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			result = new BigInteger(1, md.digest(data.getBytes())).toString(16);
			for (int i = 0; i < 32 - result.length(); i++) {
				result = "0" + result;
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean hasChinese(String data) {
		return Pattern.compile("[\u4e00-\u9fa5]").matcher(data).find();
	}
	
	public static boolean hasJapanese(String data) {
        try {
			return data.getBytes("shift-jis").length >= (2 * data.length());
		} catch (UnsupportedEncodingException e) {
			return false;
		}
	}

	public static boolean isNumber(String data) { 
        if (data == null || data.length() == 0) {
            return false;
        }
        final char[] chars = data.toCharArray();
        int sz = chars.length;
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        
        final int start = chars[0] == '-' || chars[0] == '+' ? 1 : 0;
        if (sz > start + 1 && chars[start] == '0' && data.indexOf('.') == -1) {
            if (chars[start + 1] == 'x' || chars[start + 1] == 'X') {
                int i = start + 2;
                if (i == sz) {
                    return false;
                }
                for (; i < chars.length; i++) {
                    if ((chars[i] < '0' || chars[i] > '9')
                        && (chars[i] < 'a' || chars[i] > 'f')
                        && (chars[i] < 'A' || chars[i] > 'F')) {
                        return false;
                    }
                }
                return true;
           } else if (Character.isDigit(chars[start + 1])) {
               int i = start + 1;
               for (; i < chars.length; i++) {
                   if (chars[i] < '0' || chars[i] > '7') {
                       return false;
                   }
               }
               return true;
           }
        }
        sz--;
        int i = start;
        while (i < sz || i < sz + 1 && allowSigns && !foundDigit) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                foundDigit = true;
                allowSigns = false;

            } else if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    return false;
                }
                hasDecPoint = true;
            } else if (chars[i] == 'e' || chars[i] == 'E') {
                if (hasExp) {
                    return false;
                }
                if (!foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
            } else if (chars[i] == '+' || chars[i] == '-') {
                if (!allowSigns) {
                    return false;
                }
                allowSigns = false;
                foundDigit = false;
            } else {
                return false;
            }
            i++;
        }
        if (i < chars.length) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                return true;
            }
            if (chars[i] == 'e' || chars[i] == 'E') {
                return false;
            }
            if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    return false;
                }
                return foundDigit;
            }
            if (!allowSigns  && (chars[i] == 'd' || chars[i] == 'D' || chars[i] == 'f' || chars[i] == 'F')) {
                return foundDigit;
            }
            if (chars[i] == 'l' || chars[i] == 'L') {
                return foundDigit && !hasExp && !hasDecPoint;
            }
            return false;
        }
        return !allowSigns && foundDigit;
	}

	public static boolean isHalfChar(char data) {
		return (int) data < 129;
	}

	public static String enURL(String url) {
        if (url == null || url.equals("")) throw new NullPointerException("空的 URL 地址");
        StringBuilder r = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        try {
        	if (url.indexOf("?") == -1) {
				return url;
			}
        	String[] urlSP = url.split("\\?");
        	r.append(urlSP[0]);
        	
        	String[] kvs = urlSP[1].split("&");
        	String[] kv;
        	for (int i = 0; i < kvs.length; i++) {
				sb.append("&");
        		kv = kvs[i].split("=");
        		sb.append(kv[0]);
        		sb.append("=");
				sb.append(URLEncoder.encode(kv[1], "UTF-8"));
			}
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return r.append("?").append(sb.substring(1)).toString();
	}

	public static String enURL(String url, Map<String, String> parameters) {
        if (url == null || url.equals("")) throw new NullPointerException("空的 URL 地址");
        StringBuilder r = new StringBuilder(url);
        StringBuilder sb = new StringBuilder();
        try {
        	for (Map.Entry<String, String> item : parameters.entrySet()) {
        		sb.append("&");
				sb.append(item.getKey());
				sb.append("=");
				sb.append(URLEncoder.encode(item.getValue(), "UTF-8"));
			}
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return r.append("?").append(sb.substring(1)).toString();
	}
	
	public static String deURL(String url) {
		if (url == null) return "";
		try {
			return URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return url;
	}

	public static String generateBase(String data) {
		String result = "";
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			result = Base64.getEncoder().encodeToString(md.digest(data.getBytes("UTF-8")));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
}
