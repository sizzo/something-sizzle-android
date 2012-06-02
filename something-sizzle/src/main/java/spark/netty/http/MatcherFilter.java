package spark.netty.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.HaltException;
import spark.Request;
import spark.RequestResponseFactory;
import spark.Response;
import spark.Route;
import spark.route.HttpMethod;
import spark.route.RouteMatch;
import spark.route.RouteMatcher;

public class MatcherFilter implements IMatcherFilter {
	private RouteMatcher routeMatcher;
	private static final Logger LOG = LoggerFactory.getLogger(MatcherFilter.class);

	public MatcherFilter(RouteMatcher routeMatcher) {
		this.routeMatcher = routeMatcher;
	}

	public void init() {

	}

	public Object doFilter(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
		long t0 = System.currentTimeMillis();

		String httpMethodStr = httpRequest.getMethod().getName().toLowerCase();
		String uri = httpRequest.getUri().toLowerCase();

		Object bodyContent = null;

		LOG.debug("httpMethod:" + httpMethodStr + ", uri: " + uri);
		try{
			//---------BEFORE---------------
			List<RouteMatch> matchSet = routeMatcher.findTargetsForRequestedRoute(HttpMethod.before, uri);
			
			for(RouteMatch filterMatch:matchSet){
				Object filterTarget = filterMatch.getTarget();
				if(filterTarget!=null && filterTarget instanceof spark.Filter){
					Request request = RequestResponseFactory.create(filterMatch,httpRequest);
					Response response = RequestResponseFactory.create(httpResponse);
					
					spark.Filter filter = (spark.Filter)filterTarget;
					
					filter.handle(request, response);
					
					String bodyAfterFilter = response.body();
					if(bodyAfterFilter !=null){
						bodyContent = bodyAfterFilter;
					}
				}
			}
			//----------/BEFORE-----------
			HttpMethod httpMethod = HttpMethod.valueOf(httpMethodStr);
			
			RouteMatch match = null;
			match = routeMatcher.findTargetForRequestedRoute(HttpMethod.valueOf(httpMethodStr), uri);
			
			Object target = null;
			if(match!=null){
				target = match.getTarget();
			}
			else if(httpMethod == HttpMethod.head && bodyContent == null){
				//See if get is mapped to provide default head mapping
				bodyContent = routeMatcher.findTargetForRequestedRoute(HttpMethod.get, uri)!=null?"":null;
			}

			if(target!=null){
				try{
					Object result = null;
					if(target instanceof Route){
						Route route = ((Route)target);
						Request request = RequestResponseFactory.create(match, httpRequest);
						Response response = RequestResponseFactory.create(httpResponse);
						result = route.handle(request, response);
					}
					if(result !=null){
						bodyContent = result;
					}
					long t1 = System.currentTimeMillis() - t0;
					LOG.debug("Time for request:"+t1);
				}catch(HaltException hEx){
					throw hEx;
				}catch(Exception e){
					LOG.error(e);
					//httpResponse.setStatus(500
					bodyContent = INTERNAL_ERROR;
				}
			}
			
			//-----------AFTER filters-------------
			matchSet = routeMatcher.findTargetsForRequestedRoute(HttpMethod.after, uri);
			
			for(RouteMatch filterMatch:matchSet){
				Object filterTarget = filterMatch.getTarget();
				if(filterTarget!=null &&filterTarget instanceof spark.Filter){
					Request request = RequestResponseFactory.create(filterMatch, httpRequest);
					Response response = RequestResponseFactory.create(httpResponse);
					
					spark.Filter filter = (spark.Filter)filterTarget;
					filter.handle(request, response);
					
					String bodyAfterFilter = response.body();
					if(bodyAfterFilter!=null){
						bodyContent = bodyAfterFilter;
					}
					
				}
			}
			//----------/AFTER filters------------
		}catch (HaltException hEx){
			LOG.debug("halt performed");
			//httpResponse.setStatus(hEx.getStatusCode());
			if(hEx.getBody()!=null){
				bodyContent = hEx.getBody();
			}else{
				bodyContent = "";
			}
		}
		
		boolean consumed = bodyContent!=null?true:false;
		
		if(!consumed){
			//httpResponse.setStatus(404)
			bodyContent = NOT_FOUND;
			consumed = true;
		}
		return bodyContent;
	}
	
	private static final String NOT_FOUND = "<html><body><h2>404 Not Found</h2><The requested route has not been mapped in Spark</body></html>";
	private static final String INTERNAL_ERROR="<html><body><h2>500 Internal Error</h2></body></html>";
	
}
