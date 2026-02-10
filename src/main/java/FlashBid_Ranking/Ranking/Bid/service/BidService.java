package FlashBid_Ranking.Ranking.Bid.service;

import FlashBid_Ranking.Ranking.Bid.dto.BidEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidService {

  private final RedisTemplate<String, Object> redisTemplate;

  private static final String RANKING_KEY_PREFIX = "auction:ranking:";

  public void updateRanking(BidEventDto event) {
    String key = RANKING_KEY_PREFIX + event.getItemId();

    redisTemplate.opsForZSet().add(key, event.getWinnerNickname(), event.getBidPrice().doubleValue());

    log.info("Redis 랭킹 업데이트 완료 - 경매방: {}, 유저: {}, 입찰가: {}",
        event.getItemId(), event.getWinnerNickname(), event.getBidPrice());

    redisTemplate.opsForZSet().removeRange(key, 0, -101);
  }
}