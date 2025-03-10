package com.fino.configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fino.entity.FinoUserDetails;
import com.fino.service.UserService;

import com.fino.exception.*;
import com.fino.helpers.AppConstants;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {



	@Autowired
	private UserService userService;
	@Autowired
	private JwtHelpers jwtHelpers;
	private List<String> skipUrls = Arrays.asList("/fino/system/auth/**");
	
	
	@SuppressWarnings("null")
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		
		return this.skipUrls.stream().anyMatch(url->new AntPathRequestMatcher(url).matches(request)) ;
	}

	

	@SuppressWarnings("null")
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		final String requestTokenHeader = request.getHeader("Authorization");
		String mobileNumber = null;
		String jwtToken = null;
		log.info("token:- " + requestTokenHeader);
	    final ObjectMapper mapper = new ObjectMapper();	
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(HttpServletResponse.SC_OK);
		final Map<String, Object> errorMap = new HashMap<>();


		try {
			if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer")) {
				jwtToken = requestTokenHeader.substring(7);
				mobileNumber = this.jwtHelpers.getUsernameFromToken(jwtToken);
				if (mobileNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {
					FinoUserDetails finoUserDetails = this.userService.loadUserByUsername(mobileNumber);
					if (this.jwtHelpers.validateToken(jwtToken, finoUserDetails)) {
						UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
								finoUserDetails, null, finoUserDetails.getAuthorities());
						usernamePasswordAuthenticationToken
								.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
					} else {
						log.info("authentication failed");
					}
				}

				else {
					log.warn("JWT Token does not begin with Bearer String");
				}

			}

		}

		catch (Exception e) {
		CustomException authorisedExe = new CustomException(AppConstants.Unauthorized, AppConstants.Unauthorized_desc,
		LocalDateTime.now(), e.getMessage(),request.getServletPath());
		errorMap.put(AppConstants.statusCode, authorisedExe.getStatusCode());
		errorMap.put(AppConstants.status, authorisedExe.getStatus());
		errorMap.put(AppConstants.timeStamp, authorisedExe.getTimestamp().toString());
		errorMap.put(AppConstants.statusMessage, authorisedExe.getMessage());
		errorMap.put(AppConstants.description, request.getServletPath());
		mapper.writeValue(response.getOutputStream(), errorMap);

		}

		filterChain.doFilter(request, response);

	}

}
