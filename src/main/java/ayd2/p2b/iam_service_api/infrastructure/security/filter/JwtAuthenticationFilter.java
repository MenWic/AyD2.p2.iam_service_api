package ayd2.p2b.iam_service_api.infrastructure.security.filter;

import ayd2.p2b.iam_service_api.application.port.security.ParsedToken;
import ayd2.p2b.iam_service_api.application.port.security.TokenParserPort;
import ayd2.p2b.iam_service_api.common.exception.ApiException;
import ayd2.p2b.iam_service_api.domain.model.auth.TokenType;
import ayd2.p2b.iam_service_api.domain.model.user.Role;
import ayd2.p2b.iam_service_api.infrastructure.security.principal.AuthenticatedUser;
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
            Set<Role> roles = parsed.roles().stream().map(Role::valueOf).collect(Collectors.toSet());
            AuthenticatedUser principal = new AuthenticatedUser(parsed.userId(), parsed.email(), roles);

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

