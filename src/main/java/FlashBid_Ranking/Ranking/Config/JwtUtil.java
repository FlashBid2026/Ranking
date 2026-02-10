package FlashBid_Ranking.Ranking.Config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  @Value("${jwt.secret}")
  private String secretKeyString;

  private SecretKey secretKey;

  @PostConstruct
  public void init() {
    byte[] keyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
    this.secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
  }

  public Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public String extractNickname(String token) {
    return parseClaims(token).get("nickname", String.class);
  }

  public Long extractRoomId(String token) {
    return parseClaims(token).get("roomId", Long.class);
  }
}
