package gr.aueb.cf.cookingfactory.authentication;

import gr.aueb.cf.cookingfactory.dto.AuthenticationRequestDTO;
import gr.aueb.cf.cookingfactory.dto.AuthenticationResponseDTO;
import gr.aueb.cf.cookingfactory.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO dto) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password()));

        User user = (User) authentication.getPrincipal();
        String access_token = jwtService.generateToken(authentication.getName(), user.getRole().name());
        String firstname = user.getInstructor() != null ? user.getInstructor().getFirstname() : null;
        String lastname = user.getInstructor() != null ? user.getInstructor().getLastname() : null;
        return new AuthenticationResponseDTO(access_token, user.getVat(), firstname, lastname, user.getRole().name());
    }
}

