/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package spark.netty.http;

import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.DefaultHttpChunk;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.stream.ChunkedStream;
import org.jboss.netty.util.CharsetUtil;

import spark.route.RouteMatcherFactory;
import spark.route.books.Books;
import spark.route.hello.Hello;

/**
 * A simple handler that serves incoming HTTP requests to send their respective
 * HTTP responses. It also implements {@code 'If-Modified-Since'} header to take
 * advantage of browser cache, as described in <a
 * href="http://tools.ietf.org/html/rfc2616#section-14.25">RFC 2616</a>.
 * 
 * <h3>How Browser Caching Works</h3>
 * 
 * Web browser caching works with HTTP headers as illustrated by the following
 * sample:
 * <ol>
 * <li>Request #1 returns the content of <code>/file1.txt</code>.</li>
 * <li>Contents of <code>/file1.txt</code> is cached by the browser.</li>
 * <li>Request #2 for <code>/file1.txt</code> does return the contents of the
 * file again. Rather, a 304 Not Modified is returned. This tells the browser to
 * use the contents stored in its cache.</li>
 * <li>The server knows the file has not been modified because the
 * <code>If-Modified-Since</code> date is the same as the file's last modified
 * date.</li>
 * </ol>
 * 
 * <pre>
 * Request #1 Headers
 * ===================
 * GET /file1.txt HTTP/1.1
 * 
 * Response #1 Headers
 * ===================
 * HTTP/1.1 200 OK
 * Date:               Tue, 01 Mar 2011 22:44:26 GMT
 * Last-Modified:      Wed, 30 Jun 2010 21:36:48 GMT
 * Expires:            Tue, 01 Mar 2012 22:44:26 GMT
 * Cache-Control:      private, max-age=31536000
 * 
 * Request #2 Headers
 * ===================
 * GET /file1.txt HTTP/1.1
 * If-Modified-Since:  Wed, 30 Jun 2010 21:36:48 GMT
 * 
 * Response #2 Headers
 * ===================
 * HTTP/1.1 304 Not Modified
 * Date:               Tue, 01 Mar 2011 22:44:28 GMT
 * 
 * </pre>
 */
public class HttpServerHandler extends SimpleChannelUpstreamHandler {

	static {
		Map<String, String> mimeTypes = new HashMap<String, String>();
		mimeTypes.put("txt", "text/plain");
		mimeTypes.put("css", "text/css");
		mimeTypes.put("csv", "text/csv");
		mimeTypes.put("htm", "text/html");
		mimeTypes.put("html", "text/html");
		mimeTypes.put("js", "application/javascript");
		mimeTypes.put("xhtml", "application/xhtml+xml");
		mimeTypes.put("json", "application/json");
		mimeTypes.put("pdf", "application/pdf");
		mimeTypes.put("zip", "application/zip");
		mimeTypes.put("tar", "application/x-tar");
		mimeTypes.put("gif", "image/gif");
		mimeTypes.put("jpeg", "image/jpeg");
		mimeTypes.put("jpg", "image/jpg");
		mimeTypes.put("tiff", "image/tiff");
		mimeTypes.put("tif", "image/tif");
		mimeTypes.put("png", "image/png");
		mimeTypes.put("svg", "image/svg+xml");
		mimeTypes.put("ico", "image/vnd.microsoft.icon");
		DEFAULT_MIME_TYPES = Collections.unmodifiableMap(mimeTypes);
	}
	public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
	public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
	public static final int HTTP_CACHE_SECONDS = 60;
	private static Map<String, String> DEFAULT_MIME_TYPES;
	private MatcherFilter matcherFilter;

	public HttpServerHandler() {
		Hello.routes();
		Books.main(null);
		matcherFilter = new MatcherFilter(RouteMatcherFactory.get());

	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		HttpRequest httpRequest = (HttpRequest) e.getMessage();
		HttpResponse httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

		Object object = matcherFilter.doFilter(httpRequest, httpResponse);
		Channel channel = e.getChannel();
		httpResponse.setHeader(HttpHeaders.Names.CONTENT_TYPE, guessMimeType(httpRequest.getUri()) + "; charset=UTF-8");
		httpResponse.setChunked(true);
		httpResponse.setHeader(Names.TRANSFER_ENCODING, Values.CHUNKED);
		channel.write(httpResponse);
		HttpChunk chunk;
		// httpResponse.setHeader(HttpHeaders.Names.CONTENT_LENGTH, length);
		if (object instanceof InputStream) {
			InputStream is = (InputStream) object;
			ChunkedStream chunkStream = new ChunkedStream(is, 8192);
			while (chunkStream.hasNextChunk()) {
				ChannelBuffer chunkObject = (ChannelBuffer) chunkStream.nextChunk();
				chunk = new DefaultHttpChunk(chunkObject);
				channel.write(chunk);
			}
			channel.write(HttpChunk.LAST_CHUNK);
			chunkStream.close();
		} else {
			chunk = new DefaultHttpChunk(ChannelBuffers.wrappedBuffer(((String) object).getBytes(CharsetUtil.UTF_8)));
			channel.write(chunk);
		}
		// Decide whether to close the connection or not.
		if (!isKeepAlive(httpRequest)) {
			// Close the connection when the whole content is written out.
		} else {
			channel.close();
		}
	}

	private String guessMimeType(String path) {
		int lastDot = path.lastIndexOf('.');
		if (lastDot == -1) {
			return "text/html";
		}
		String extension = path.substring(lastDot + 1).toLowerCase();
		String mimeType = DEFAULT_MIME_TYPES.get(extension);
		if (mimeType == null) {
			return "text/html";
		}

		return mimeType;
	}

	/**
	 * Sets the Date header for the HTTP response
	 * 
	 * @param response
	 *            HTTP response
	 */
	private static void setDateHeader(HttpResponse response) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
		dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

		Calendar time = new GregorianCalendar();
		response.setHeader(HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));
	}

	/**
	 * Sets the Date and Cache headers for the HTTP Response
	 * 
	 * @param response
	 *            HTTP response
	 * @param fileToCache
	 *            file to extract content type
	 */
	private static void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
		dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

		// Date header
		Calendar time = new GregorianCalendar();
		response.setHeader(HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));

		// Add cache headers
		time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
		response.setHeader(HttpHeaders.Names.EXPIRES, dateFormatter.format(time.getTime()));
		response.setHeader(HttpHeaders.Names.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
		response.setHeader(HttpHeaders.Names.LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		Channel ch = e.getChannel();
		Throwable cause = e.getCause();
		if (cause instanceof TooLongFrameException) {
			sendError(ctx, HttpResponseStatus.BAD_REQUEST);
			return;
		}
		cause.printStackTrace();
		if (ch.isConnected()) {
			sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
		response.setHeader("Content-Type", "text/plain;charset=UTF-8");
		response.setContent(ChannelBuffers.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
		// close the connection as soon as the error message is sent.
		ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);

	}

}
