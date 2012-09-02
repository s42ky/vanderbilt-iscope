package edu.vanderbilt.iscope.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import org.owasp.webscarab.model.HttpUrl;
import org.owasp.webscarab.model.Request;
import org.owasp.webscarab.util.Encoding;

import edu.vanderbilt.iscope.model.Variable;

public class RequestGenerator {
	
	private String project;
	private String baseUrl;
	private String http_version = "HTTP/1.1";
	private String host;
	
	private QueryParser _parser = new QueryParser();
	
	public RequestGenerator (String url, String p) {
		host = url;
		project = p;
		baseUrl = "http://" + host + "/" + project + "/";
	}
	
	public void setBaseUrl (String url) {
		baseUrl = url;
	}
	
	public Request constructBaseRequest() throws MalformedURLException {
		Request request = new Request();
		request.setMethod("GET");
		request.setVersion(http_version);
		request.setHeader("host", host);
		request.setURL(new HttpUrl(baseUrl));
		//request.setURL(new HttpUrl(baseUrl+"blog.php"));              // Customized for minibloggie.
		return request;
	}
	
	// construct redirection requests.
	public Request constructRedirectRequest(String header, String cookie) throws MalformedURLException {
		Request request = new Request();
		request.setMethod("GET");
		request.setVersion(http_version);
		request.setHeader("host", host);
		if (header != null) {
			if (header.contains("http://"+host+"/"+project+"/")) {
				request.setURL(new HttpUrl(header));
			} else {
				request.setURL(new HttpUrl("http://"+host+"/"+project+"/"+header));
			}
		}
		if (cookie != null) request.addHeader("Cookie", cookie);
		return request;
	}
	
	public Request constructRequest(String entry, String inputParas, String cookie) throws MalformedURLException {
		Request request = new Request();
		String method = entry.substring(0, entry.indexOf("-"));
		String script = entry.substring(entry.indexOf("-")+1);
		script = script.replaceAll(":", "/");
		request.setMethod(method);
		request.setVersion(http_version);
		request.setHeader("host", host);
		if (cookie != null) request.addHeader("Cookie", cookie);
		
		String url = baseUrl+script+".php";
		String query = null;
		ByteArrayOutputStream content = null;
		HashMap<String, String> parameters = _parser.parseInputParameters(inputParas);
		if (!parameters.isEmpty()) {
			if (method.equals("GET")) {
				for (String name: parameters.keySet()){
					String value = parameters.get(name);
					if (value.contains("NOT") && value.contains("{") && value.contains("}"))  value = genConcreteValue(value);  // TOKEN HERE.
					//String q = Encoding.urlEncode(name+"="+value);
					String q = name+"="+Encoding.urlEncode(value);                  // BUG HERE!!! FIXED for minibloggie
	                if (query == null) {
	                    query = q;
	                } else {
	                    query = query + "&" + q;
	                }
				}
			} else if (method.equals("POST")) {
				for (String name: parameters.keySet()){
					String value = parameters.get(name);
					if (value.contains("NOT") && value.contains("{") && value.contains("}"))  value = genConcreteValue(value);  // TOKEN HERE.
					String q = Encoding.urlEncode(name)+"="+Encoding.urlEncode(value);                  // BUG HERE!!! FIXED!!!!
					if (content == null) {
		                content = new ByteArrayOutputStream();
		                try { content.write(q.getBytes()); }
		                catch (IOException ioe) {}
		            } else {
		                try { content.write(("&"+q).getBytes()); }
		                catch (IOException ioe) {}
		            }
				}
			}
		}
		if (query != null) url = url + "?" + query;
		//System.out.println(url);
        if (request.getMethod().equals("POST")) {
        	request.setHeader("Content-Type", "application/x-www-form-urlencoded");
        }
        if (content != null) {
            request.setHeader("Content-Length", Integer.toString(content.size()));
            request.setContent(content.toByteArray());
        } else if (request.getMethod().equals("POST")) {
            request.setHeader("Content-Length", "0");
        }
        request.setURL(new HttpUrl(url));
		return request;
	}
	
	// Generate random value that matches the token.
	private String genConcreteValue(String token) {
		Variable var = new Variable("TempVar");
		HashSet<String> negateValues = new HashSet<String>();
		token = token.substring(4, token.length()-1);
		StringTokenizer st = new StringTokenizer(token, ",");
		while (st.hasMoreTokens()) {
			String value = st.nextToken();
			//System.out.println(value);
			if (!value.equals("null")) {    // BUG HERE:::
				negateValues.add(value);  
				var.addValue(value);
			}
		}
		var.testValueType();
		if (var.getValueType() == Variable.NUMBER) {			
			int num = 4;
			while (negateValues.contains(Integer.toString(num))) {
				num ++;
			}
			return Integer.toString(num);
		} else if (var.getValueType() == Variable.PHONE) {
			return "6159757285";
		} else if (var.getValueType() == Variable.EMAIL) {
			return "random@random.com";
		} else if (var.getValueType() == Variable.NAME) {
			return "random";
		} else {
			return "A Random String";
		}
	}
}