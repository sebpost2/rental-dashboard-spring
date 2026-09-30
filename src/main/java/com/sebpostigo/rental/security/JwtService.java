package com.sebpostigo.rental.security;

import com.sebpostigo.rental.common.AppProperties;
import java.time.Clock;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	static final String USER_ID_CLAIM = "uid";

	private final JwtEncoder encoder;

	private final AppProperties props;

	private final Clock clock;

	public JwtService(JwtEncoder encoder, AppProperties props, Clock clock) {
		this.encoder = encoder;
		this.props = props;
		this.clock = clock;
	}

	public String issue(long userId, String email) {
		Instant now = clock.instant();
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.subject(email)
			.claim(USER_ID_CLAIM, userId)
			.issuedAt(now)
			.expiresAt(now.plus(props.jwt().expiry()))
			.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

}
