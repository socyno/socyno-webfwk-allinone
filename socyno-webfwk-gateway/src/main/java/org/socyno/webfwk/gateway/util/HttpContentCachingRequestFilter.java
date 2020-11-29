package org.socyno.webfwk.gateway.util;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter(filterName = "HttpContentCachingRequestFilter", urlPatterns = "/*")
public class HttpContentCachingRequestFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpContentCachingRequestWrapper requestWrapper = new HttpContentCachingRequestWrapper(
                (HttpServletRequest) request);
        chain.doFilter(requestWrapper, response);
    }
    
    @Override
    public void destroy() {
        
    }
}
