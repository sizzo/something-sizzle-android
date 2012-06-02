package spark.route.hello;

import static spark.Spark.get;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import spark.Request;
import spark.Response;
import spark.Route;

public class Hello {
	public static void routes() {
		get(new Route("/") {
			public InputStream handle(Request request, Response response) {
				return Hello.class.getResourceAsStream("/web/index.html");
			}
		});
		get(new Route("/web/*") {
			public InputStream handle(Request request, Response response) {
				String uri = request.raw().getUri();
				return Hello.class.getResourceAsStream(uri);
			}
		});
		get(new Route("/hello") {
			public InputStream handle(Request request, Response response) {
				try {
					return new ByteArrayInputStream("Hello World".getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		});
	}

}
