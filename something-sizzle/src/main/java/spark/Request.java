/*
 * Copyright 2011- Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spark;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.handler.codec.http.HttpRequest;

import spark.route.HttpMethod;
import spark.route.RouteMatch;
import spark.utils.SparkUtils;

/**
 * Provides information about the HTTP request
 *
 * @author Per Wendel
 */
public class Request {

    private org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(getClass());
    
    private static final String USER_AGENT = "user-agent";
    
    private Map<String, String> params;
    
    private HttpMethod httpMethod;
    private HttpRequest servletRequest;

    /* Lazy loaded stuff */
    private String body = null;
    
    private Set<String> headers = null;
    
    //    request.body              # request body sent by the client (see below), DONE
    //    request.scheme            # "http"                                DONE
    //    request.path_info         # "/foo",                               DONE
    //    request.port              # 80                                    DONE
    //    request.request_method    # "GET",                                DONE
    //    request.query_string      # "",                                   DONE
    //    request.content_length    # length of request.body,               DONE
    //    request.media_type        # media type of request.body            DONE, content type?
    //    request.host              # "example.com"                         DONE
    //    request["SOME_HEADER"]    # value of SOME_HEADER header,          DONE
    //    request.user_agent        # user agent (used by :agent condition) DONE
    //    request.url               # "http://example.com/example/foo"      DONE
    //    request.ip                # client IP address                     DONE
    //    request.env               # raw env hash handed in by Rack,       DONE
    //    request.get?              # true (similar methods for other verbs)
    //    request.secure?           # false (would be true over ssl)
    //    request.forwarded?        # true (if running behind a reverse proxy)
    //    request.cookies           # hash of browser cookies
    //    request.xhr?              # is this an ajax request?
    //    request.script_name       # "/example"
    //    request.form_data?        # false
    //    request.referrer          # the referrer of the client or '/'
    
    protected Request() {
       // Used by wrapper
    }
    
    /**
     * Constructor
     */
    Request(RouteMatch match, HttpRequest request) {
        this.httpMethod = match.getHttpMethod();
        this.servletRequest = request;
        params = setParams(match);
    }
    
    /**
     * Returns the value of the provided route pattern parameter.
     * Example: parameter 'name' from the following pattern: (get '/hello/:name')
     * 
     * @return null if the given param is null or not found 
     */
    public String params(String param) {
        if (param == null) {
            return null;
        }

        if (param.startsWith(":")) {
            return params.get(param.toLowerCase());
        } else {
            return params.get(":" + param.toLowerCase());
        }
    }
    
    /**
     * Returns request method e.g. GET, POST, PUT, ...
     */
    public String requestMethod() {
        return httpMethod.name();
    }


    
    /**
     * Returns the host
     */
    public String host() {
        return servletRequest.getHeader("host");
    }

    /**
     * Returns the user-agent
     */
    public String userAgent() {
        return servletRequest.getHeader(USER_AGENT);
    }
    

    
    /**
     * Gets the raw HttpRequest object handed in by Jetty
     */
    public HttpRequest raw() {
        return servletRequest;
    }
    
    private final Map<String, String> setParams(RouteMatch match) {
        LOG.debug("set params for requestUri: "
                        + match.getRequestUri()
                        + ", matchUri: "
                        + match.getMatchUri());

        Map<String, String> params = new HashMap<String, String>();
        
        List<String> request = SparkUtils.convertRouteToList(match.getRequestUri());
        List<String> matched = SparkUtils.convertRouteToList(match.getMatchUri());

        for (int i = 0; (i < request.size()) && (i < matched.size()); i++) {
            String matchedPart = matched.get(i);
            if (SparkUtils.isParam(matchedPart)) {
                LOG.debug("matchedPart: "
                                + matchedPart
                                + " = "
                                + request.get(i));
                params.put(matchedPart, request.get(i));
            }
        }
        return Collections.unmodifiableMap(params);
    }
    
}
