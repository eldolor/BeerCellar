package com.cm.beer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;

import com.cm.beer.config.AppConfig;

public class RestClient
{
	public static final int HTTP_OK = 200;

	// public static final String SERVER_URL = "BASE URL OF THE REST SERVER";

	public static String doGet(final String url) throws HttpException,
			IOException, URISyntaxException
	{

		final HttpClient httpClient = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(),
				AppConfig.HTTP_CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpClient.getParams(),
				AppConfig.HTTP_SOCKET_TIMEOUT);
		HttpGet httpget = new HttpGet(url);
		HttpResponse response = httpClient.execute(httpget);
		HttpEntity entity = response.getEntity();
		InputStream instream = entity.getContent();
		return read(instream);
	}

	public static String doPost(final String url, String contentType,
			final String POSTText) throws URISyntaxException, HttpException,
			IOException
	{

		final HttpClient httpClient = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(),
				AppConfig.HTTP_CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpClient.getParams(),
				AppConfig.HTTP_SOCKET_TIMEOUT);

		HttpPost httpPost = new HttpPost(url);
		StringEntity entity = new StringEntity(POSTText, "UTF-8");
		BasicHeader basicHeader = new BasicHeader(HTTP.CONTENT_TYPE,
				contentType);
		httpPost.getParams().setBooleanParameter(
				"http.protocol.expect-continue", false);
		entity.setContentType(basicHeader);
		httpPost.setEntity(entity);
		HttpResponse response = httpClient.execute(httpPost);
		InputStream instream = response.getEntity().getContent();
		return read(instream);
	}

	public static boolean doPut(final String url, String contentType,
			String acceptContentType, final String PUTText)
			throws URISyntaxException, HttpException, IOException
	{
		final HttpClient httpClient = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(),
				AppConfig.HTTP_CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpClient.getParams(),
				AppConfig.HTTP_SOCKET_TIMEOUT);

		HttpPut httpPut = new HttpPut(url);
		httpPut.addHeader("Accept", acceptContentType);
		httpPut.addHeader("Content-Type", contentType);
		StringEntity entity = new StringEntity(PUTText, "UTF-8");
		entity.setContentType(contentType);
		httpPut.setEntity(entity);
		HttpResponse response = httpClient.execute(httpPut);
		int statusCode = response.getStatusLine().getStatusCode();
		return statusCode == HTTP_OK ? true : false;
	}

	public static boolean doDelete(final String url, String acceptContentType)
			throws HttpException, IOException, URISyntaxException
	{
		final HttpClient httpClient = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(),
				AppConfig.HTTP_CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpClient.getParams(),
				AppConfig.HTTP_SOCKET_TIMEOUT);

		HttpDelete httpDelete = new HttpDelete(url);
		httpDelete.addHeader("Accept", acceptContentType);
		HttpResponse response = httpClient.execute(httpDelete);
		int statusCode = response.getStatusLine().getStatusCode();
		return statusCode == HTTP_OK ? true : false;
	}

	private static String read(InputStream in) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
		for (String line = r.readLine(); line != null; line = r.readLine())
		{
			sb.append(line);
		}
		in.close();
		return sb.toString();
	}
}
