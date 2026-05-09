package ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.filter;

import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.feature.auth.application.port.ParsedToken;
import ayd2.p2b.iam_service_api.feature.auth.application.port.TokenParserPort;
import ayd2.p2b.iam_service_api.feature.auth.domain.model.TokenType;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.principal.AuthenticatedUser;
import ayd2.p2b.iam_service_api.feature.user.domain.model.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenParserPort tokenParserPort;

    public JwtAuthenticationFilter(TokenParserPort tokenParserPort) {
        this.tokenParserPort = tokenParserPort;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            ParsedToken parsed = tokenParserPort.parseToken(token, TokenType.ACCESS);
            Set<Role> roles = parsed.getRoles().stream().map(Role::valueOf).collect(Collectors.toSet());
            AuthenticatedUser principal = new AuthenticatedUser(parsed.getUserId(), parsed.getEmail(), roles);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.name())).toList()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (ApiException ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}

