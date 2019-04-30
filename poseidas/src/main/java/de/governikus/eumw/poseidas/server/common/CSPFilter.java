/*
 * Copyright (c) 2019 Governikus KG. Licensed under the EUPL, Version 1.2 or as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except
 * in compliance with the Licence. You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package de.governikus.eumw.poseidas.server.common;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;


public class CSPFilter implements Filter
{

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
    ServletException
  {
    HttpServletResponse response = (HttpServletResponse)res;
    response.setHeader("Content-Security-Policy", "default-src 'self'");
    chain.doFilter(req, response);
  }

  @Override
  public void init(FilterConfig filterConfig)
  {
    // nothing
  }

  @Override
  public void destroy()
  {
    // nothing
  }
}
